package org.gusdb.wdk.model.fix.table.steps;

import org.gusdb.wdk.model.fix.table.TableRowInterfaces.TableRow;
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
public class StepData implements TableRow {

  private Long _stepId;
  private Long _leftChildId;
  private Long _rightChildId;
  private String _legacyAnswerFilter;
  private String _projectId;
  private String _questionName;
  private String _origParamFiltersString;
  private JSONObject _paramFilters;

  public StepData() {}

  public StepData(StepData orig) {
    _stepId = orig._stepId;
    _leftChildId = orig._leftChildId;
    _rightChildId = orig._rightChildId;
    _legacyAnswerFilter = orig._legacyAnswerFilter;
    _projectId = orig._projectId;
    _projectId = orig._questionName;
    _origParamFiltersString = orig._origParamFiltersString;
    _paramFilters = orig._paramFilters;
  }

  @Override
  public String getDisplayId() {
    return _stepId.toString();
  }

  /*%%%%%%%%%%%%%%%% Getters and Setters %%%%%%%%%%%%%%%%*/

  public Long getStepId() {
    return _stepId;
  }

  public void setStepId(Long stepId) {
    _stepId = stepId;
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

  public String getOrigParamFiltersString() {
    return _origParamFiltersString;
  }

  public void setOrigParamFiltersString(String origParamFiltersString) {
    _origParamFiltersString = origParamFiltersString;
  }

  public JSONObject getParamFilters() {
    return _paramFilters;
  }

  public void setParamFilters(JSONObject paramFilters) {
    _paramFilters = paramFilters;
  }
}
