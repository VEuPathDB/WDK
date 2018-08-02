package org.gusdb.wdk.model.filter;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.factory.AnswerValue;
import org.gusdb.wdk.model.answer.spec.SimpleAnswerSpec;
import org.json.JSONObject;

public abstract class AbstractFilter implements Filter {

  private String _display;
  private String _description;
  private String _view;
  protected JSONObject _defaultValue;
  private FilterType _filterType = FilterType.STANDARD;
  private boolean _isAlwaysApplied = false;

  @Override
  public String getDisplay() {
    return _display;
  }

  @Override
  public void setDisplay(String display) {
    _display = display;
  }

  @Override
  public String getDescription() {
    return _description;
  }

  @Override
  public void setDescription(String description) {
    _description = description;
  }

  @Override
  public String getView() {
    return _view;
  }

  @Override
  public void setView(String view) {
    _view = view;
  }

  @Override
  public JSONObject getDefaultValue(SimpleAnswerSpec answerSpec) {
    return _defaultValue;
  }

  @Override
  public void setDefaultValue(JSONObject defaultValue) {
    _defaultValue = defaultValue;
  }

  @Override
  public FilterType getFilterType() {
    return _filterType;
  }

  @Override
  public void setIsViewOnly(boolean isViewOnly) {
    _filterType = isViewOnly ? FilterType.VIEW_ONLY : FilterType.STANDARD;
  }

  @Override
  public boolean getIsAlwaysApplied() {
    return _isAlwaysApplied;
  }

  @Override
  public void setIsAlwaysApplied(boolean isAlwaysApplied) {
    _isAlwaysApplied = isAlwaysApplied;
  }

  @Override
  public JSONObject getSummaryJson(AnswerValue answer, String idSql)
      throws WdkModelException {
    return null;
  }

}
