package org.gusdb.wdk.model.filter;

import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.spec.SimpleAnswerSpec;
import org.gusdb.wdk.model.question.Question;
import org.json.JSONObject;

/**
 * Filter is an interface for step-based or column-based result filter.
 *
 * @author Jerric Gao
 *
 */
public interface Filter {

  enum FilterType {
    STANDARD,  // can be used as a regular or view filter
    VIEW_ONLY; // can only be used as a view filter (typically uses >1 rows to do filtering)

    public boolean isViewOnly() { return equals(VIEW_ONLY); }

    public boolean containerSupports(FilterType filterType) {
      switch(this) {
        case STANDARD: return filterType.equals(STANDARD);
        case VIEW_ONLY: return true;
      }
      throw new IllegalStateException("This method must have cases for all values.");
    }
  }

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
   * @return false if this filter should affect the actual result; true if it only affects the results view
   */
  FilterType getFilterType();

  void setIsViewOnly(boolean isViewOnly);

  /**
   * @return true if this filter will always be applied to steps whose questions include it, false if it
   * can be removed from those steps
   */
  boolean getIsAlwaysApplied();

  void setIsAlwaysApplied(boolean isAlwaysApplied);

  void setDefaultValue(JSONObject defaultValue);

  /**
   * Returns the default value of this filter given the question and params in simpleSpec
   *
   * @param simpleSpec the question and parameters of a step to be filtered
   * @return default value of this filter, or null if by default this filter should not be applied
   */
  JSONObject getDefaultValue(SimpleAnswerSpec simpleSpec);

  /**
   * get the display value of the filter. The value will be displayed on the applied filter list.
   *
   * @param answer
   *          the answerValue on which the filter has been applied.
   * @param jsValue
   *          the actual value that has been applied to the current filter.
   * @return
   */
  String getDisplayValue(AnswerValue answer, JSONObject jsValue) throws WdkModelException;

  /**
   * Get a JSON formatted version of the summary model for the filter display.
   * @param answer
   * @param idSql
   * @return
   * @throws WdkModelException
   */
  JSONObject getSummaryJson(AnswerValue answer, String idSql) throws WdkModelException;

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
   */
  String getSql(AnswerValue answer, String idSql, JSONObject jsValue) throws WdkModelException;

  /**
   * return true if supplied value equals default value for the given step.  return false if no default value.
   * @param value
   * @return
   */
  boolean defaultValueEquals(SimpleAnswerSpec simpleAnswerSpec,
      JSONObject value) throws WdkModelException;

  /**
   * Validates the passed value the best it can without access to the AnswerValue it will be applied to
   *
   * @param question question of the answer spec this filter will be applied to
   * @param value potential JSON value for this filter
   * @param validationLevel level of validation to perform
   * @return bundle of information describing validity of the passed value for this filter
   */
  ValidationBundle validate(Question question, JSONObject value,
      ValidationLevel validationLevel);
}
