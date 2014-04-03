package org.gusdb.wdk.model.user.analysis;

public enum ExecutionStatus {
  CREATED     (true),  // newly created
  INVALID     (false), // made for a step it does not support; should never be run
  PENDING     (false), // run requested but not yet started
  RUNNING     (false), // currently running (or interrupted but not yet 'fixed')
  COMPLETE    (false), // completed successfully
  INTERRUPTED (true),  // server shutdown or other interruption
  ERROR       (true),  // no longer running due to runtime error
  OUT_OF_DATE (true),  // has been run but results cache purged
  UNKNOWN     (false); // for internal use only
  
  private boolean _requiresRerun;

  private ExecutionStatus(boolean requiresRerun) {
    _requiresRerun = requiresRerun;
  }

  public boolean requiresRerun() {
    return _requiresRerun;
  }
}
