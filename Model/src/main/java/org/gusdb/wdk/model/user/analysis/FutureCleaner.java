package org.gusdb.wdk.model.user.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

public class FutureCleaner implements Callable<Boolean> {

  private static final Logger LOG = Logger.getLogger(FutureCleaner.class);

  private static final int FUTURE_CLEANUP_INTERVAL_SECS = 20;
  private static final int FUTURE_CLEANUP_RETRY_SECS = 60 * 60; // 1-hour delay
  private static final int FUTURE_CLEANER_SLEEP_SECS = 2;

  public static class RunningAnalysis {
    public int analysisId;
    public Future<ExecutionStatus> future;
    public RunningAnalysis(int analysisId, Future<ExecutionStatus> future) {
      this.analysisId = analysisId;
      this.future = future;
    }
  }

  private volatile StepAnalysisFactory _analysisMgr;
  private volatile ConcurrentLinkedDeque<RunningAnalysis> _threadResults;

  public FutureCleaner(StepAnalysisFactory analysisMgr,
      ConcurrentLinkedDeque<RunningAnalysis> threadResults) {
    _analysisMgr = analysisMgr;
    _threadResults = threadResults;
  }

  @Override
  public Boolean call() throws Exception {
    try {
      LOG.info("Step Analysis Thread Monitor initialized and running.");
      int waitedSecs = 0;
      boolean mostRecentAttemptSucceeded = true;
      while (true) {
        int timeToWait = (mostRecentAttemptSucceeded ?
            FUTURE_CLEANUP_INTERVAL_SECS : FUTURE_CLEANUP_RETRY_SECS);
        if (waitedSecs >= timeToWait) {
          try {
            // do the business of this thread
            _analysisMgr.expireLongRunningExecutions();
            removeCompletedThreads();
            removeExpiredThreads();
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

  private void removeExpiredThreads() throws WdkUserException, WdkModelException {
    long currentTime = System.currentTimeMillis();
    List<RunningAnalysis> futuresToRemove = new ArrayList<>();
    for (RunningAnalysis run : _threadResults) {
      // see if this thread has been running too long; if so, cancel the job
      int analysisId = run.analysisId;
      StepAnalysisContext context = _analysisMgr.getSavedContext(analysisId);
      if (context.getStatus().equals(ExecutionStatus.RUNNING) ||
          context.getStatus().equals(ExecutionStatus.PENDING)) {
        // check to see if it's been running too long
        AnalysisResult result = _analysisMgr.getAnalysisResult(context);
        if (StepAnalysisFactoryImpl.isRunExpired(context.getStepAnalysis(),
            currentTime, result.getStartDate())) {
          run.future.cancel(true);
          futuresToRemove.add(run);
        }
      }
      else {
        // any other status means Future should be cleaned up
        LOG.warn("Step Analysis Future found referencing discontinued analysis " +
            "with status: " + context.getStatus() + ".  Cancelling thread.");
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
              run.analysisId + " completed with status: " + future.get());
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
