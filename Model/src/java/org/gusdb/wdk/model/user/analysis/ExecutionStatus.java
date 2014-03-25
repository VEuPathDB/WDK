package org.gusdb.wdk.model.user.analysis;

public enum ExecutionStatus {
  CREATED,      // newly created
  PENDING,      // run requested but not yet started
  RUNNING,      // currently running (or interrupted but not yet 'fixed')
  COMPLETE,     // completed successfully
  INTERRUPTED,  // server shutdown or other interruption
  ERROR,        // no longer running due to runtime error
  OUT_OF_DATE,  // has been run but results cache purged
  UNKNOWN;      // for internal use only
  
  /**
   * Resolves ambiguous execution status.  When status is unavailable (because
   * result record is not present), status differs depending on whether the
   * analysis has ever been run.  If it has, this method returns OUT_OF_DATE; if
   * not, it returns CREATED.
   * 
   * @param isNew pass true if analysis has never been run, else false
   * @param status a status value if available, else null
   * @return a resolved status value
   */
  public static ExecutionStatus resolveStatus(boolean isNew, ExecutionStatus status) {
    if (isNew) return ExecutionStatus.CREATED;
    if (status == null) return ExecutionStatus.OUT_OF_DATE;
    return status;
  }
}
