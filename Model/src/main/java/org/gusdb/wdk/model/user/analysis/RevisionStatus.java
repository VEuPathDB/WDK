package org.gusdb.wdk.model.user.analysis;

public enum RevisionStatus {
  STEP_CLEAN (0), // instance has been successfully run at least once
  NEW        (1), // instance has not been run since creation
  STEP_DIRTY (2); // instance's step has been explicitly revised since last run

  private int _dbValue;

  private RevisionStatus(int dbValue) {
    _dbValue = dbValue;
  }

  public int getDbValue() {
    return _dbValue;
  }

  public static RevisionStatus valueOf(int dbValue) {
    for (RevisionStatus state : values()) {
      if (state._dbValue == dbValue) {
        return state;
      }
    }
    throw new IllegalArgumentException("No " + RevisionStatus.class.getSimpleName() +
        " corresponds to DB value " + dbValue);
  }
}
