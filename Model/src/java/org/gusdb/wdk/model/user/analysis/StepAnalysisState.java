package org.gusdb.wdk.model.user.analysis;

public enum StepAnalysisState {
  SHOW_RESULTS    (0), // tab valid and results expected
  NO_RESULTS      (1), // do not try to show results; new analysis or submitted with errors
  INVALID_RESULTS (2); // invalid due to revise; tell user to run again

  private int _dbValue;

  private StepAnalysisState(int dbValue) {
    _dbValue = dbValue;
  }

  public int getDbValue() {
    return _dbValue;
  }

  public static StepAnalysisState valueOf(int dbValue) {
    for (StepAnalysisState state : values()) {
      if (state._dbValue == dbValue) {
        return state;
      }
    }
    throw new IllegalArgumentException("No " + StepAnalysisState.class.getSimpleName() +
        " corresponds to DB value " + dbValue);
  }
}
package org.gusdb.wdk.model.user.analysis;

public enum StepAnalysisState {
  SHOW_RESULTS    (0), // tab valid and results expected
  NO_RESULTS      (1), // do not try to show results; new analysis or submitted with errors
  INVALID_RESULTS (2); // invalid due to revise; tell user to run again

  private int _dbValue;

  private StepAnalysisState(int dbValue) {
    _dbValue = dbValue;
  }

  public int getDbValue() {
    return _dbValue;
  }

  public static StepAnalysisState valueOf(int dbValue) {
    for (StepAnalysisState state : values()) {
      if (state._dbValue == dbValue) {
        return state;
      }
    }
    throw new IllegalArgumentException("No " + StepAnalysisState.class.getSimpleName() +
        " corresponds to DB value " + dbValue);
  }
}
