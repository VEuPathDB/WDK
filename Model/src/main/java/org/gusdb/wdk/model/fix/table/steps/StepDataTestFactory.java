package org.gusdb.wdk.model.fix.table.steps;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Function;
import org.gusdb.fgputil.functional.Functions;

/**
 * Overrides StepDataFactory to not actually write data back to the Steps table, but instead
 * insert records into a test table
 * 
 * @author rdoherty
 *
 */
public class StepDataTestFactory extends StepDataFactory {

  private static final String TEST_TABLE_NAME = "gus4_steps_test";
  
  private static final String INSERT_COLS_TEXT = SELECT_COLS_TEXT;

  private static final String INSERT_WILDCARDS = FormatUtil.join(Functions.mapToList(SQLTYPES.keySet(),
      new Function<String, String>() { @Override public String apply(String obj) { return "?"; }}).toArray(), ",");

  private static final Integer[] INSERT_PARAMETER_TYPES = SQLTYPES.values().toArray(new Integer[SQLTYPES.size()]);
  
  public StepDataTestFactory(boolean includeGuestUserSteps) {
    super(includeGuestUserSteps);
  }

  @Override
  public String getRecordsSql(String schema, String projectId) {
    String basicConditions = "is_deleted = 0 and project_id = '" + projectId + "'";
    return "select " + SELECT_COLS_TEXT + " from " + schema + "steps" +
        (_includeGuestUserSteps ? " where " + basicConditions :
          " s, " + schema + "users u where " + basicConditions + " and u.user_id = s.user_id and u.is_guest = 1");
  }

  @Override
  public String getUpdateRecordSql(String schema) {
    return "insert into " + schema + TEST_TABLE_NAME + " (" +
        INSERT_COLS_TEXT + ") values (" + INSERT_WILDCARDS + ")";
  }

  @Override
  public Integer[] getUpdateParameterTypes() {
    return INSERT_PARAMETER_TYPES;
  }

  @Override
  public Object[] toUpdateVals(StepData row) {
    return new Object[] {
        row.getStepId(),
        row.getLeftChildId(),
        row.getRightChildId(),
        row.getLegacyAnswerFilter(),
        row.getProjectId(),
        row.getQuestionName(),
        row.getParamFilters().toString()
    };
  }
}
