package org.gusdb.wdk.model.fix.table.tables;

import static org.gusdb.fgputil.FormatUtil.join;
import static org.gusdb.fgputil.functional.FunctionalInterfaces.notEqualTo;
import static org.gusdb.fgputil.functional.Functions.filter;
import static org.gusdb.fgputil.functional.Functions.mapToList;
import static org.gusdb.fgputil.functional.Functions.toMapFunction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.ListBuilder;
import org.gusdb.fgputil.MapBuilder;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Function;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces.TableRowFactory;
import org.json.JSONObject;

public class StepDataFactory implements TableRowFactory<StepData> {

  // constants for column names
  private static final String STEP_ID = "STEP_ID";               // NUMBER(12,0)
  private static final String LEFT_CHILD_ID = "LEFT_CHILD_ID";   // NUMBER(12,0)
  private static final String RIGHT_CHILD_ID = "RIGHT_CHILD_ID"; // NUMBER(12,0)
  private static final String ANSWER_FILTER = "ANSWER_FILTER";   // VARCHAR2(100 BYTE)
  private static final String PROJECT_ID = "PROJECT_ID";         // VARCHAR2(50 BYTE)
  private static final String QUESTION_NAME = "QUESTION_NAME";   // VARCHAR2(200 BYTE)
  private static final String DISPLAY_PARAMS = "DISPLAY_PARAMS"; // CLOB

  // SQL type map
  private static final Map<String, Integer> SQLTYPES = 
      new MapBuilder<String, Integer>(new LinkedHashMap<String, Integer>())
      .put(STEP_ID, Types.INTEGER)
      .put(LEFT_CHILD_ID, Types.INTEGER)
      .put(RIGHT_CHILD_ID, Types.INTEGER)
      .put(ANSWER_FILTER, Types.VARCHAR)
      .put(PROJECT_ID, Types.VARCHAR)
      .put(QUESTION_NAME, Types.VARCHAR)
      .put(DISPLAY_PARAMS, Types.CLOB)
      .toMap();

  private static final String[] COLS = SQLTYPES.keySet().toArray(new String[SQLTYPES.size()]);
  
  private static final String SELECT_COLS_TEXT = join(COLS, ",");

  private static final List<String> UPDATE_COLS = filter(Arrays.asList(COLS), notEqualTo(STEP_ID));

  private static final String UPDATE_COLS_TEXT = join(mapToList(UPDATE_COLS, new Function<String, String>() {
        @Override public String apply(String col) { return col + " = ?"; }
      }).toArray(), ", ");

  private static final Integer[] UPDATE_PARAMETER_TYPES =
      mapToList(new ListBuilder<String>().addAll(UPDATE_COLS).add(STEP_ID).toList(),
          toMapFunction(SQLTYPES)).toArray(new Integer[COLS.length]);

  private final boolean _includeGuestUserSteps;

  public StepDataFactory(boolean includeGuestUserSteps) {
    _includeGuestUserSteps = includeGuestUserSteps;
  }

  // loads next row into StepData object
  @Override
  public StepData newTableRow(ResultSet rs, DBPlatform platform) throws SQLException {
    StepData newRow = new StepData();
    newRow.setStepId(rs.getLong(STEP_ID));
    newRow.setLeftChildId(rs.getLong(LEFT_CHILD_ID));
    newRow.setRightChildId(rs.getLong(RIGHT_CHILD_ID));
    newRow.setLegacyAnswerFilter(rs.getString(ANSWER_FILTER));
    newRow.setProjectId(rs.getString(PROJECT_ID));
    newRow.setQuestionName(rs.getString(QUESTION_NAME));
    newRow.setParamFilters(new JSONObject(platform.getClobData(rs, DISPLAY_PARAMS)));
    return newRow;
  }

  @Override
  public String getRecordsSql(String schema, String projectId) {
    String basicConditions = "is_deleted = 0 and project_id = '" + projectId + "'";
    return "select " + SELECT_COLS_TEXT + " from " + schema + "steps" +
        (_includeGuestUserSteps ? " where " + basicConditions :
          " s, " + schema + "users u where " + basicConditions + " and u.user_id = s.user_id and u.is_guest = 0");
  }

  @Override
  public String getUpdateRecordSql(String schema) {
    return "update " + schema + "steps set " + UPDATE_COLS_TEXT + " where " + STEP_ID + " = ?";
  }

  @Override
  public Integer[] getUpdateParameterTypes() {
    return UPDATE_PARAMETER_TYPES;
  }

  @Override
  public Object[] toUpdateVals(StepData row) {
    return new Object[] {
        row.getLeftChildId(),
        row.getRightChildId(),
        row.getLegacyAnswerFilter(),
        row.getProjectId(),
        row.getQuestionName(),
        row.getParamFilters().toString(),
        row.getStepId()
    };
  }

}
