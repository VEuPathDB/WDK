package org.gusdb.wdk.model.fix;

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
import org.json.JSONObject;

/**
 * Encapsulates the following data from the users table:
 * 
 * "STEP_ID" NUMBER(12,0)
 * "LEFT_CHILD_ID" NUMBER(12,0)
 * "RIGHT_CHILD_ID" NUMBER(12,0)
 * "ANSWER_FILTER" VARCHAR2(100 BYTE)
 * "PROJECT_ID" VARCHAR2(50 BYTE)
 * "QUESTION_NAME" VARCHAR2(200 BYTE)
 * "DISPLAY_PARAMS" CLOB
 * 
 * @author rdoherty
 */
public class StepData {

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

  public static final Integer[] UPDATE_PARAMETER_TYPES =
      mapToList(new ListBuilder<String>().addAll(UPDATE_COLS).add(STEP_ID).toList(),
          toMapFunction(SQLTYPES)).toArray(new Integer[COLS.length]);

  private Long _stepId;
  private Long _leftChildId;
  private Long _rightChildId;
  private String _legacyAnswerFilter;
  private String _projectId;
  private String _questionName;
  private JSONObject _paramFilters;

  // loads next row into StepData object
  public StepData(ResultSet rs, DBPlatform platform) throws SQLException {
    _stepId = rs.getLong(STEP_ID);
    _leftChildId = rs.getLong(LEFT_CHILD_ID);
    _rightChildId = rs.getLong(RIGHT_CHILD_ID);
    _legacyAnswerFilter = rs.getString(ANSWER_FILTER);
    _projectId = rs.getString(ANSWER_FILTER);
    _questionName = rs.getString(ANSWER_FILTER);
    _paramFilters = new JSONObject(platform.getClobData(rs, DISPLAY_PARAMS));
  }

  public static String getAllStepsSql(String schema, boolean includeGuestUserSteps) {
    return "select " + SELECT_COLS_TEXT + " from " + schema + "steps" +
        (includeGuestUserSteps ? " where is_deleted = 0" :
          " s, " + schema + "users u where s.is_deleted = 0 and u.user_id = s.user_id and u.is_guest = 0");
  }

  public static String getUpdateStepSql(String schema) {
    return "update " + schema + "steps set " + UPDATE_COLS_TEXT + " where " + STEP_ID + " = ?";
  }

  public Object[] toUpdateVals() {
    return new Object[]{ _leftChildId, _rightChildId, _legacyAnswerFilter,
        _projectId, _questionName, _paramFilters.toString(), _stepId };
  }

  /*%%%%%%%%%%%%%%%% Getters and Setters %%%%%%%%%%%%%%%%*/
  
  public Long getStepId() {
    return _stepId;
  }

  public Long getLeftChildId() {
    return _leftChildId;
  }

  public void setLeftChildId(Long leftChildId) {
    _leftChildId = leftChildId;
  }

  public Long getRightChildId() {
    return _rightChildId;
  }

  public void setRightChildId(Long rightChildId) {
    _rightChildId = rightChildId;
  }

  public String getLegacyAnswerFilter() {
    return _legacyAnswerFilter;
  }

  public void setLegacyAnswerFilter(String legacyAnswerFilter) {
    _legacyAnswerFilter = legacyAnswerFilter;
  }

  public String getProjectId() {
    return _projectId;
  }

  public void setProjectId(String projectId) {
    _projectId = projectId;
  }

  public String getQuestionName() {
    return _questionName;
  }

  public void setQuestionName(String questionName) {
    _questionName = questionName;
  }

  public JSONObject getParamFilters() {
    return _paramFilters;
  }

  public void setParamFilters(JSONObject paramFilters) {
    _paramFilters = paramFilters;
  }
}
