package org.gusdb.wdk.model.filter;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.user.Step;
import org.json.JSONObject;

/**
 * Holds the user's choice of values for a filter.
 * The value is a weakly typed JSONObject because it is passed to a plugged in filter.
 *
 */
public class FilterOption {

  private static final Logger LOG = Logger.getLogger(FilterOption.class);

  private static final boolean DEFAULT_IS_DISABLED = false;
  
  public static final String KEY_NAME = "name";
  public static final String KEY_VALUE = "value";
  public static final String KEY_DISABLED = "disabled";

  private WdkModel _wdkModel;
  private String _questionName;
  private String _filterName;
  private JSONObject _value;
  private boolean _isDisabled = false;

  public FilterOption(WdkModel wdkModel, String questionName, JSONObject jsFilterOption) {
    String filterName = jsFilterOption.getString(KEY_NAME);
    LOG.debug("FilterOption created (read from database?) for filter: " + filterName +  " on step with question: " +  questionName );
    JSONObject value = jsFilterOption.has(KEY_VALUE) ? jsFilterOption.getJSONObject(KEY_VALUE) : null;
    boolean isDisabled = (jsFilterOption.has(KEY_DISABLED) ? jsFilterOption.getBoolean(KEY_DISABLED) : DEFAULT_IS_DISABLED);
    init(wdkModel, questionName, filterName, value, isDisabled);
  }

  // we need to add/pass the disabled property
  public FilterOption(WdkModel wdkModel, String questionName, String filterName, JSONObject value) {
    init(wdkModel, questionName, filterName, value, DEFAULT_IS_DISABLED);
  }

  // we need to add/pass the disabled property
  public FilterOption(WdkModel wdkModel, String questionName, String filterName, JSONObject value, boolean isDisabled) {
    init(wdkModel, questionName, filterName, value, isDisabled);
  }

  private void init(WdkModel wdkModel, String questionName, String filterName, JSONObject value, boolean isDisabled) {
    _wdkModel = wdkModel;
    _questionName = questionName;
    _filterName = filterName;
    _value = value;
    _isDisabled = isDisabled;
    LOG.debug("FilterOption created for filter: " + _filterName +  " on step with question: " +  _questionName  + ", isDisabled? " + isDisabled );
  }

 public Filter getFilter() throws WdkModelException {
   return _wdkModel.getQuestion(_questionName).getFilter(_filterName);
 }

  public String getKey() {
    return _filterName;
  }

  public JSONObject getValue() {
    return _value;
  }

  public String getDisplayValue(AnswerValue answerValue) throws WdkModelException, WdkUserException {
    return getFilter().getDisplayValue(answerValue, _value);
  }

  public boolean isDisabled() {
    return _isDisabled;
  }

  public void setDisabled(boolean isDisabled) {
    _isDisabled = isDisabled;
  }
  
  public JSONObject getJSON() {
    JSONObject jsFilterOption = new JSONObject();
    jsFilterOption.put(KEY_NAME, _filterName);
    jsFilterOption.put(KEY_VALUE, _value);
    jsFilterOption.put(KEY_DISABLED, _isDisabled);
    return jsFilterOption;
  }

  public boolean isSetToDefaultValue(Step step) throws WdkModelException {
    return getFilter().defaultValueEquals(step, getValue());
  }

  // FIXME: this is a total hack to support the JSP calling
  //   getDisplayValue(AnswerValue) with an argument.  It should be removed
  //   once we move filter displays from JSP to the new service architecture.
  public Map<AnswerValueBean, String> getDisplayValueMap() {
    return new HashMap<AnswerValueBean, String>() {
      @Override
      public String get(Object answerValue) {
        if (answerValue instanceof AnswerValueBean) {
          try {
            return getDisplayValue(((AnswerValueBean)answerValue).getAnswerValue());
          }
          catch (WdkModelException | WdkUserException e) {
            throw new WdkRuntimeException(e);
          }
        }
        throw new IllegalArgumentException("Argument must be a AnswerValueBean.");
      }
    };
  }
}
