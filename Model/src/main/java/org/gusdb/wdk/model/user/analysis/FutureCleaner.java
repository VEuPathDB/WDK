package org.gusdb.wdk.model.user.analysis;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.logging.ThreadLocalLoggingVars;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModelException;

public class FutureCleaner implements Callable<Boolean> {

  private static final Logger LOG = Logger.getLogger(FutureCleaner.class);

  private static final int FUTURE_CLEANUP_INTERVAL_SECS = 20;
  private static final int FUTURE_CLEANUP_RETRY_SECS = 60 * 60; // 1-hour delay
  private static final int FUTURE_CLEANER_SLEEP_SECS = 2;

  public static class RunningAnalysis {
    public RunnableObj<StepAnalysisInstance> instance;
    public Future<ExecutionStatus> future;
    public RunningAnalysis(RunnableObj<StepAnalysisInstance> instance, Future<ExecutionStatus> future) {
      this.instance = instance;
      this.future = future;
    }
  }

  private volatile StepAnalysisFactory _analysisFactory;
  private volatile StepAnalysisDataStore _dataStore;
  private volatile ConcurrentLinkedDeque<RunningAnalysis> _threadResults;

  public FutureCleaner(
      StepAnalysisFactory analysisFactory,
      StepAnalysisDataStore dataStore,
      ConcurrentLinkedDeque<RunningAnalysis> threadResults) {
    _analysisFactory = analysisFactory;
    _dataStore = dataStore;
    _threadResults = threadResults;
  }

  @Override
  public Boolean call() throws Exception {
    try {
      ThreadLocalLoggingVars.setNonRequestThreadVars("safc"); // safc = step analysis future cleaner
      LOG.info("Step Analysis Thread Monitor initialized and running.");
      int waitedSecs = 0;
      boolean mostRecentAttemptSucceeded = true;
      while (true) {
        int timeToWait = (mostRecentAttemptSucceeded ?
            FUTURE_CLEANUP_INTERVAL_SECS : FUTURE_CLEANUP_RETRY_SECS);
        if (waitedSecs >= timeToWait) {
          try {
            // do the business of this thread
            expireLongRunningExecutions(); // writes to appDb
            removeCompletedThreads();      // cleans up memory
            removeExpiredThreads();        // cleans up memory
            mostRecentAttemptSucceeded = true;
          }
          catch (Exception e) {
            // don't hide  interrupted exceptions
            if (e instanceof InterruptedException) {
              throw e;
            }
            if (mostRecentAttemptSucceeded) {
              // First loop with exception after recovery, log details
              // Probably DB blink so set wait time to higher interval and retry later
              LOG.error("Could not clean up expired step analysis threads.  Will retry in " +
                  FUTURE_CLEANUP_RETRY_SECS + " seconds.", e);
            }
            else {
              LOG.error("Expired step analysis thread cleaner still failing (" +
                  e.getClass().getSimpleName() + ")");
            }
            mostRecentAttemptSucceeded = false;
          }
          // reset waitSecs for next run
          waitedSecs = 0;
        }
        // only sleep for a little while; wake up to check if interrupted
        Thread.sleep(FUTURE_CLEANER_SLEEP_SECS * 1000);
        if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
        waitedSecs += FUTURE_CLEANER_SLEEP_SECS;
      }
    }
    catch (InterruptedException e) {
      LOG.info("Step Analysis future cleaner thread was interrupted and will return cleanly.");
      return true;
    }
  }

  // go through all running/pending analysis runs in DB, and mark expired if expired
  private void expireLongRunningExecutions() throws WdkModelException {
    long currentTime = System.currentTimeMillis();
    List<ExecutionInfo> execList = _dataStore.getAllRunningExecutions();
    for (ExecutionInfo exec : execList) {
      if (isRunExpired(exec.getTimeoutMins(), currentTime, exec.getStartDate())) {
        _dataStore.updateExecution(exec.getContextHash(), ExecutionStatus.EXPIRED, new Date(), null, null);
      }
    }
  }

  private static boolean isRunExpired(long timeoutMinutes, long currentTime, Date startTime) {
    long expirationDuration = timeoutMinutes * 60 * 1000;
    long currentDuration = currentTime - startTime.getTime();
    return (currentDuration > expirationDuration);
  }

  private void removeExpiredThreads() throws WdkModelException {
    long currentTime = System.currentTimeMillis();
    List<RunningAnalysis> futuresToRemove = new ArrayList<>();
    for (RunningAnalysis run : _threadResults) {
      // see if this thread has been running too long; if so, cancel the job
      ExecutionInfo info = _analysisFactory.getExecutionInfo(run.instance).orElse(null);
      if (info != null &&
          (info.getStatus().equals(ExecutionStatus.RUNNING) ||
           info.getStatus().equals(ExecutionStatus.PENDING))) {
        // check to see if it's been running too long
        if (isRunExpired(info.getTimeoutMins(), currentTime, info.getStartDate())) {
          run.future.cancel(true);
          futuresToRemove.add(run);
        }
      }
      else {
        // any other status means Future should be cleaned up
        LOG.warn("Step Analysis Future found referencing discontinued analysis " +
            "with status: " + (info == null ? "unknown" : info.getStatus()) + ".  Cancelling thread.");
        run.future.cancel(true);
        futuresToRemove.add(run);
      }
    }
    // remove futures after collecting them so as not to interfere with iterator above
    for (RunningAnalysis run : futuresToRemove) {
      _threadResults.remove(run);
    }
  }

  private void removeCompletedThreads() {
    List<RunningAnalysis> futuresToRemove = new ArrayList<>();
    for (RunningAnalysis run : _threadResults) {
      Future<ExecutionStatus> future = run.future;
      if (future.isDone() || future.isCancelled()) {
        try {
          LOG.info("Step Analysis run for step analysis with ID " +
              run.instance.get().getAnalysisId() + " completed with status: " + future.get());
        }
        catch (ExecutionException | CancellationException | InterruptedException e) {
          LOG.error("Exception thrown while retrieving step analysis status (on completion)", e);
        }
        futuresToRemove.add(run);
      }
    }
    // remove futures after collecting them so as not to interfere with iterator above
    for (RunningAnalysis run : futuresToRemove) {
      _threadResults.remove(run);
    }
  }
}
