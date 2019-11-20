package org.gusdb.wdk.model.fix.table.steps;

import static org.gusdb.fgputil.FormatUtil.join;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.fgputil.MapBuilder;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces.TableRowFactory;
import org.json.JSONObject;

public class StepDataFactory implements TableRowFactory<StepData> {

  // constants for column names
  public static final String STEP_ID = "STEP_ID";               // NUMBER(12,0)
  public static final String LEFT_CHILD_ID = "LEFT_CHILD_ID";   // NUMBER(12,0)
  public static final String RIGHT_CHILD_ID = "RIGHT_CHILD_ID"; // NUMBER(12,0)
  public static final String ANSWER_FILTER = "ANSWER_FILTER";   // VARCHAR2(100 BYTE)
  public static final String PROJECT_ID = "PROJECT_ID";         // VARCHAR2(50 BYTE)
  public static final String QUESTION_NAME = "QUESTION_NAME";   // VARCHAR2(200 BYTE)
  public static final String DISPLAY_PARAMS = "DISPLAY_PARAMS"; // CLOB

  // SQL type map
  public static final Map<String, Integer> SQLTYPES =
      new MapBuilder<String, Integer>(new LinkedHashMap<>())
      .put(STEP_ID, Types.INTEGER)
      .put(LEFT_CHILD_ID, Types.INTEGER)
      .put(RIGHT_CHILD_ID, Types.INTEGER)
      .put(ANSWER_FILTER, Types.VARCHAR)
      .put(PROJECT_ID, Types.VARCHAR)
      .put(QUESTION_NAME, Types.VARCHAR)
      .put(DISPLAY_PARAMS, Types.CLOB)
      .toMap();

  public static final String[] COLS = SQLTYPES.keySet().toArray(new String[0]);

  protected static final String SELECT_COLS_TEXT = join(COLS, ",");

  protected final boolean _includeGuestUserSteps;

  public StepDataFactory(boolean includeGuestUserSteps) {
    _includeGuestUserSteps = includeGuestUserSteps;
  }

  // loads next row into StepData object
  @Override
  public StepData newTableRow(ResultSet rs, DBPlatform platform) throws SQLException {
    StepData newRow = new StepData();
    newRow.setStepId(rs.getLong(STEP_ID));
    newRow.setLeftChildId(rs.getLong(LEFT_CHILD_ID));
    if (rs.wasNull()) newRow.setLeftChildId(null);
    newRow.setRightChildId(rs.getLong(RIGHT_CHILD_ID));
    if (rs.wasNull()) newRow.setRightChildId(null);
    newRow.setLegacyAnswerFilter(rs.getString(ANSWER_FILTER));
    if (rs.wasNull()) newRow.setLegacyAnswerFilter(null);
    newRow.setProjectId(rs.getString(PROJECT_ID));
    newRow.setQuestionName(rs.getString(QUESTION_NAME));
    newRow.setOrigParamFiltersString(platform.getClobData(rs, DISPLAY_PARAMS));
    newRow.setParamFilters(new JSONObject(newRow.getOrigParamFiltersString()));
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
  public void setUp(WdkModel wdkModel) throws Exception {
    // nothing to do here
  }

  @Override
  public void tearDown(WdkModel wdkModel) throws Exception {
    // nothing to do here
  }

}
