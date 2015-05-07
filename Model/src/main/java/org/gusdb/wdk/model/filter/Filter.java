package org.gusdb.wdk.model.filter;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.json.JSONObject;

public interface Filter {
  
  /**
   * @return the unique name of a filter. The name can only contain: [a-zA-Z0-9\.\-_].
   */
  String getKey();
  
  String getDisplay();
  
  void setDisplay(String display);
  
  String getDescription();
  
  void setDescription(String description);
  
  String getView();
  
  void setView(String view);
  
  void setDefaultValue(JSONObject defaultValue);
  
  JSONObject getDefaultValue();
  
  String getDisplayValue(AnswerValue answer, JSONObject jsValue) throws WdkModelException, WdkUserException;

  FilterSummary getSummary(AnswerValue answer, String idSql) throws WdkModelException, WdkUserException;
  
  String getSql(AnswerValue answer, String idSql, JSONObject jsValue) throws WdkModelException, WdkUserException;
  
  /**
   * return true if supplied value equals default value.  return false if no default value.
   * @param value
   * @return
   */
  boolean defaultValueEquals(JSONObject value) throws WdkModelException;
}
