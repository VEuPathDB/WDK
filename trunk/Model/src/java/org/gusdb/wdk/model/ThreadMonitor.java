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

public class ThreadMonitor implements Runnable {

  private static final long REPORT_INTERVAL = 3600 * 1000;
  private static final long SLEEP_INTERVAL = 10 * 1000;

  private static final Logger logger = Logger.getLogger(ThreadMonitor.class);

  private static ThreadMonitor monitor;

  public synchronized static void setup(WdkModel wdkModel) {
    if (monitor != null)
      return;
    if (!wdkModel.getModelConfig().isMonitorBlockedThreads())
      return;

    monitor = new ThreadMonitor(wdkModel);
    new Thread(monitor).start();
  }

  public static void shutdown() {
    if (monitor == null)
      return;
    monitor.stop();
    while (!monitor.isStopped()) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException ex) {}
    }
    monitor = null;
  }

  private final WdkModel wdkModel;
  private boolean running;
  private boolean stopped;
  private String siteInfo;

  private ThreadMonitor(WdkModel wdkModel) {
    this.wdkModel = wdkModel;
    // get process & host name
    String host = ManagementFactory.getRuntimeMXBean().getName();
    siteInfo = wdkModel.getProjectId() + " v" + wdkModel.getVersion() + ", "
        + host;
  }

  public boolean isStopped() {
    return stopped;
  }

  @Override
  public void run() {
    logger.info("Thread monitor started at Thread#"
        + Thread.currentThread().getId() + " - " + siteInfo);
    running = true;
    stopped = false;
    int threshold = wdkModel.getModelConfig().getBlockedThreshold();
    long lastReport = 0;
    ThreadMXBean thbean = ManagementFactory.getThreadMXBean();
    while (running) {
      // get all threads
      Thread[] threads = getAllThreads(thbean);

      // summarize the states
      Map<State, Integer> states = new HashMap<State, Integer>();
      List<Thread> blockedThreads = new ArrayList<>();
      for (Thread thread : threads) {
        State state = thread.getState();
        int count = states.containsKey(state) ? states.get(state) : 0;
        states.put(state, count + 1);
        if (state == State.BLOCKED)
          blockedThreads.add(thread);
      }
      String stateText = printStates(states, threads.length);
      logger.debug(stateText);

      if (blockedThreads.size() >= threshold) {
        // enough blocked threads reached
        if (System.currentTimeMillis() - lastReport > REPORT_INTERVAL) {
          report(thbean, stateText, blockedThreads);
          lastReport = System.currentTimeMillis();
        }
      }

      // sleep for a while
      try {
        Thread.sleep(SLEEP_INTERVAL);
      } catch (InterruptedException ex) {}
    }
    logger.info("Thread monitor stopped on Thread#"
        + Thread.currentThread().getId() + " - " + siteInfo);
    stopped = true;
  }

  private void stop() {
    running = false;
  }

  private Thread[] getAllThreads(ThreadMXBean thbean) {
    // get root thread group
    ThreadGroup group = Thread.currentThread().getThreadGroup();
    ThreadGroup parent;
    while ((parent = group.getParent()) != null)
      group = parent;

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

  private String printStates(Map<State, Integer> states, int total) {
    StringBuilder buffer = new StringBuilder("Current Threads - ");
    buffer.append("Total: " + total);
    State[] keys = states.keySet().toArray(new State[0]);
    Arrays.sort(keys);
    for (State state : keys) {
      buffer.append(", " + state);
      buffer.append(": " + states.get(state));
    }
    return buffer.toString();
  }

  private void report(ThreadMXBean thbean, String stateText,
      List<Thread> blockedThreads) {
    // get title
    String subject = "[" + siteInfo + "] WARNING - Too many blocked threads: "
        + blockedThreads.size();

    // get thread infos
    long[] ids = new long[blockedThreads.size()];
    for (int i = 0; i < ids.length; i++) {
      ids[i] = blockedThreads.get(i).getId();
    }
    ThreadInfo[] infos = thbean.getThreadInfo(ids);

    // get content
    StringBuilder buffer = new StringBuilder();
    buffer.append("<p>" + siteInfo + "</p>");
    buffer.append("<p>" + stateText + "</p><br/>\n");
    buffer.append("<p>Too many blocked threads detected.<p>\n");
    for (int i = 0; i < ids.length; i++) {
      Thread thread = blockedThreads.get(i);
      ThreadInfo info = infos[i];
      if (info == null)
        continue;

      buffer.append("<div><div>Thread id=" + thread.getId() + ", name='"
          + thread.getName() + "'</div>\n");
      buffer.append("<div>\tblocked count=" + info.getBlockedCount()
          + ", blocked time=" + info.getBlockedTime() + "</div>\n");
      buffer.append("<ol>");
      for (StackTraceElement element : thread.getStackTrace()) {
        buffer.append("\t<li>" + element.toString() + "</li>\n");
      }
      buffer.append("</ol></div><br/>\n\n");
    }
    String content = buffer.toString();
    logger.warn(subject + "\n" + content.replaceAll("<[^<>]+>", " "));

    try {
      // get admin email
      String email = wdkModel.getModelConfig().getAdminEmail();
      if (email != null)
        Utilities.sendEmail(wdkModel, email, email, subject, content);

    } catch (WdkModelException ex) {
      ex.printStackTrace();
      // ignore the exception here, it might be caused by an unconfigured admin
      // email.
    }
  }
}
