package org.gusdb.wdk.model.filter;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.json.JSONObject;
import org.gusdb.wdk.model.user.Step;

/**
 * Filter is an interface for step-based or column-based result filter.
 * 
 * @author Jerric Gao
 *
 */
public interface Filter {

  /**
   * @return the unique name of a filter. The name can only contain: [a-zA-Z0-9\.\-_].
   */
  String getKey();

  /**
   * @return the display name of the filter. The display name will be shown on the filter list.
   */
  String getDisplay();

  void setDisplay(String display);

  /**
   * @return the description, or help information, of the filter.
   */
  String getDescription();

  void setDescription(String description);

  /**
   * @return the path to the summary view jsp file of the filter.
   */
  String getView();

  void setView(String view);

  /**
   * @returns false if this filter should affect the actual result; true if it only affects the results view
   */
  boolean getIsViewOnly();

  void setIsViewOnly(boolean isViewOnly);

  /**
   * get the display value of the filter. The value will be displayed on the applied filter list.
   * 
   * @param answer
   *          the answerValue on which the filter has been applied.
   * @param jsValue
   *          the actual value that has been applied to the current filter.
   * @return
   * @throws WdkModelException
   * @throws WdkUserException
   */
  void setDefaultValue(JSONObject defaultValue);
  
  JSONObject getDefaultValue(Step step);
  
  String getDisplayValue(AnswerValue answer, JSONObject jsValue) throws WdkModelException, WdkUserException;

  /**
   * Get the summary model for the filter display. The summary model contains information that will be
   * rendered on the filter interface.
   * 
   * @param answer
   *          the AnswerValue that the filter will be applied on.
   * @param idSql
   *          the ID SQL from the answerValue, with all the not-view-only filters, except the current one, applied.
   * @return
   * @throws WdkModelException
   * @throws WdkUserException
   */
  FilterSummary getSummary(AnswerValue answer, String idSql) throws WdkModelException, WdkUserException;

  /**
   * Get the wrapped ID SQL from the filter, with the filter value applied to the SQL as where clauses.
   * 
   * @param answer
   * @param idSql
   *          the ID SQL from answerValue, with the previous filters (in the order of the filters being
   *          applied) already wrapped.
   * @param jsValue
   *          the actual filter value.
   * @return
   * @throws WdkModelException
   * @throws WdkUserException
   */
  String getSql(AnswerValue answer, String idSql, JSONObject jsValue) throws WdkModelException,
      WdkUserException;
  
  /**
   * return true if supplied value equals default value.  return false if no default value.
   * @param value
   * @return
   */
  boolean defaultValueEquals(JSONObject value) throws WdkModelException;
}
