package org.gusdb.wdk.model;

import java.lang.Thread.State;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.Tuples.ThreeTuple;
import org.gusdb.fgputil.logging.MDCUtil;
import org.gusdb.fgputil.runtime.ThreadUtil;
import org.gusdb.wdk.model.config.ModelConfig;

/**
 * This monitor reports on the state of the threads in the pool.  A "blocked" thread is just a thread
 * that is in a synchronized block, waiting for another thread.  It may be doing so fleetingly, and normally, or
 * it might be stuck.  This report does not distinguish those cases.  It takes the simple
 * approach that, if the number of threads that the JVM tells us are blocked is over a threshold, and 
 * was also over the threshold a while ago when we last checked (SLEEP_INTERVAL * MIN_BLOCKED CYLES), then
 * this could signal that threads are locked (stuck) in the blocked state.
 * 
 * the JVM does provide enough information to create a genuine "stuck threads" report.  
 * if a thread is blocked and its blocked count (ie, how many times in its lifetime it has blocked) has not changed 
 * since we last looked, then it is likely stuck.  a future iteration of this report could do so
 *
 */
public class ThreadMonitor implements Runnable {

  private static final Logger LOG = Logger.getLogger(ThreadMonitor.class);

  private static final long REPORT_INTERVAL = 3600 * 1000;
  private static final long SLEEP_INTERVAL = 10 * 1000;
  private static final long MIN_BLOCKED_CYCLES = 10;

  private static Thread _monitor;

  public static synchronized void start(ThreadMonitorConfig config) {
    // ignore secondary calls if thread is alive; should ensure only one instance
    if (_monitor == null || !_monitor.isAlive()) {
      _monitor = new Thread(new ThreadMonitor(config));
      _monitor.start();
    }
  }

  public static synchronized void shutDown() {
    if (_monitor == null) {
      LOG.warn("Attempt made to shut down Thread Monitor which was never started.");
    }
    else if (!_monitor.isAlive()) {
      LOG.info("Thread Monitor already shut down.");
    }
    else {
      LOG.info("Shutting down Thread Monitor.  Will wait for completion.");
      _monitor.interrupt();
      // wait until thread shut down
      waitForShutDown(_monitor);
      LOG.info("Thread Monitor successfully shut down.");
    }
  }

  private final ThreadMonitorConfig _config;

  private ThreadMonitor(ThreadMonitorConfig config) {
    _config = config;
  }

  @Override
  public void run() {
    MDCUtil.setNonRequestThreadVars("thrm");
    if (!_config.isActive()) {
      LOG.info("Thread monitor not configured to run.  Monitor returning.");
      return;
    }
    ThreadMXBean thbean = ManagementFactory.getThreadMXBean();
    if (!thbean.isThreadContentionMonitoringSupported()) {
      LOG.warn("Thread monitoring is not supported by this JVM.  Monitor returning.");
      return;
    }
    monitorThreads(thbean);
  }

  public void monitorThreads(ThreadMXBean thbean) {

    String siteInfo = _config.getAppName() + ", " + ManagementFactory.getRuntimeMXBean().getName();
    LOG.info("Thread Monitor started on thread #" + Thread.currentThread().getId() + " - " + siteInfo);
    thbean.setThreadContentionMonitoringEnabled(true);

    int blockedCycles = 0; // keeps track of amount of time since last report
    long lastReportTime = 0; // set to ensure first report happens immediately

    while (!Thread.interrupted()) {
      ThreadState threadState = getThreadState(thbean);
      String stateText = threadState.getSummary(blockedCycles);
      LOG.debug(stateText);

      if (threadState.getBlockedThreads().size() > _config.getMaxBlockedThreshold()) {
        blockedCycles++;
        if (blockedCycles >= MIN_BLOCKED_CYCLES) {
          // enough cycles reached where blocked thread count exceeds maximum
          if (System.currentTimeMillis() - lastReportTime > REPORT_INTERVAL) {
            report(thbean, siteInfo, stateText, threadState.getThird());
            lastReportTime = System.currentTimeMillis();
          }
        }
      }
      else {
        blockedCycles = 0; // reset the cycle
      }

      // sleep for a while, and break if interrupted
      if (ThreadUtil.sleep(SLEEP_INTERVAL)) {
        LOG.info("Thread Monitor interrupted during operation.  Exiting...");
        break;
      }
    }
    LOG.info("Thread monitor stopped on Thread " + Thread.currentThread().getId() + " - " + siteInfo);
  }

  private static ThreadState getThreadState(ThreadMXBean thbean) {
    Thread[] threads = getAllThreads(thbean);
    Map<State, Integer> states = new HashMap<State, Integer>();
    List<Thread> blockedThreads = new ArrayList<>();
    for (Thread thread : threads) {
      State state = thread.getState();
      int count = states.containsKey(state) ? states.get(state) : 0;
      states.put(state, count + 1);
      if (state == State.BLOCKED) {
        blockedThreads.add(thread);
      }
    }
    return new ThreadState(threads.length, states, blockedThreads);
  }

  private static Thread[] getAllThreads(ThreadMXBean thbean) {
    // get root thread group
    ThreadGroup group = Thread.currentThread().getThreadGroup();
    ThreadGroup parent;
    while ((parent = group.getParent()) != null) {
      group = parent;
    }
    // get all threads
    int count = thbean.getThreadCount();
    int n = 0;
    Thread[] threads;
    do {
      count *= 2;
      threads = new Thread[count];
      n = group.enumerate(threads, true);
    } while (n == count);
    return Arrays.copyOf(threads, n);
  }

  private void report(ThreadMXBean thbean, String siteInfo, String stateText, List<Thread> blockedThreads) {
    // get title
    String subject = "[" + siteInfo + "] WARNING - Too many blocked threads: " + blockedThreads.size();

    // get content and log
    String content = getEmailBody(thbean, siteInfo, stateText, blockedThreads);
    LOG.warn(subject + "\n" + content.replaceAll("<[^<>]+>", " "));

    try {
      // get admin email
      List<String> emails = _config.getAdminEmails();
      String smtpServer = _config.getSmtpServer();
      if (!emails.isEmpty())
        Utilities.sendEmail(smtpServer, FormatUtil.join(emails.toArray(), ","), emails.get(0), subject, content);
    }
    catch (WdkModelException e) {
      // simply log the exception here; it might be caused by a unconfigured admin email.
      LOG.error("Unable to send 'Too Many Blocked Threads' email to admins", e);
    }
  }

  private String getEmailBody(ThreadMXBean thbean, String siteInfo, String stateText, List<Thread> blockedThreads) {
    // get thread infos
    long[] ids = new long[blockedThreads.size()];
    for (int i = 0; i < ids.length; i++) {
      ids[i] = blockedThreads.get(i).getId();
    }
    ThreadInfo[] infos = thbean.getThreadInfo(ids);

    // build body text
    StringBuilder buffer = new StringBuilder()
        .append("<p>").append(siteInfo).append("</p>")
        .append("<p>").append(stateText).append("</p><br/>\n")
        .append("<p>Too many blocked threads detected.<p>\n");
    for (int i = 0; i < ids.length; i++) {
      Thread thread = blockedThreads.get(i);
      ThreadInfo info = infos[i];
      if (info != null) {
        buffer.append("<div><div>Thread id=").append(thread.getId())
              .append(", name='").append(thread.getName()).append("'</div>\n")
              .append("<div>\tblocked count=").append(info.getBlockedCount())
              .append(", blocked time=").append(info.getBlockedTime() + "</div>\n")
              .append("<ol>");
        for (StackTraceElement element : thread.getStackTrace()) {
          buffer.append("\t<li>").append(element.toString()).append("</li>\n");
        }
        buffer.append("</ol></div><br/>\n\n");
      }
    }
    return buffer.toString();
  }

  private static class ThreadState extends ThreeTuple<Integer, Map<State, Integer>, List<Thread>> {

    public ThreadState(Integer numTotalThreads, Map<State, Integer> threadStateCounts, List<Thread> blockedThreads) {
      super(numTotalThreads, threadStateCounts, blockedThreads);
    }

    public List<Thread> getBlockedThreads() {
      return getThird();
    }

    public String getSummary(int blockedCycles) {
      StringBuilder buffer = new StringBuilder("Current Threads - ");
      buffer.append("Total: " + getFirst());
      State[] keys = getSecond().keySet().toArray(new State[0]);
      Arrays.sort(keys);
      for (State state : keys) {
        buffer.append(", " + state);
        buffer.append(": " + getSecond().get(state));
      }
      buffer.append(", blocked cycle: " + blockedCycles);
      return buffer.toString();
    }
  }

  private static void waitForShutDown(Thread monitor) {
    while (monitor.isAlive()) {
      if (ThreadUtil.sleep(10)) {
        LOG.warn("Thread Monitor shut down interrupted.  " +
            "Do not know if spawned thread died successfully.");
        return;
      }
    }
  }

  public static interface ThreadMonitorConfig {
    public boolean isActive();
    public String getAppName();
    public int getMaxBlockedThreshold();
    public String getSmtpServer();
    public List<String> getAdminEmails();
  }

  public static ThreadMonitorConfig getThreadMonitorConfig(final WdkModel wdkModel) {
    final ModelConfig modelConfig = wdkModel.getModelConfig();
    return new ThreadMonitorConfig() {
      @Override public boolean isActive() { return modelConfig.isMonitorBlockedThreads(); }
      @Override public String getAppName() { return wdkModel.getProjectId() + " v" + wdkModel.getVersion(); }
      @Override public int getMaxBlockedThreshold() { return modelConfig.getBlockedThreshold(); }
      @Override public String getSmtpServer() { return modelConfig.getSmtpServer(); }
      @Override public List<String> getAdminEmails() { return modelConfig.getAdminEmails(); }
    };
  }
}
