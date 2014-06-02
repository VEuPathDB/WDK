package org.gusdb.wdk.model.user.analysis;

public enum ExecutionStatus {
  CREATED     (true, false),  // newly created
  INVALID     (false, false), // made for a step it does not support; should never be run
  PENDING     (false, false), // run requested but not yet started
  RUNNING     (false, false), // currently running (or interrupted but not yet 'fixed')
  COMPLETE    (false, true), // completed successfully
  INTERRUPTED (true, true),  // server shutdown or other interruption
  ERROR       (true, true),  // no longer running due to runtime error
  OUT_OF_DATE (true, false),  // has been run but results cache purged
  UNKNOWN     (false, false); // for internal use only
  
  private boolean _requiresRerun;
  private boolean _isTerminal;

  private ExecutionStatus(boolean requiresRerun, boolean isTerminal) {
    _requiresRerun = requiresRerun;
    _isTerminal = isTerminal;
  }

  /**
   * @return whether this status indicates that a re-run of an analysis is necessary
   */
  public boolean requiresRerun() {
    return _requiresRerun;
  }

  /**
   * @return true if this status is a valid return code for an analyzer plugin
   * to return after it has completed, even if that completion was not successful
   */
  public boolean isTerminal() {
    return _isTerminal;
  }
}
