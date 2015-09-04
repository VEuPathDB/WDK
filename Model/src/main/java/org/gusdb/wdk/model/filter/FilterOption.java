package org.gusdb.wdk.model.filter;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.question.Question;
import org.json.JSONObject;

public class FilterOption {

  public static final String KEY_NAME = "name";
  public static final String KEY_VALUE = "value";
  public static final String KEY_DISABLED = "disabled";

  private final Filter _filter;
  private final JSONObject _value;
  private boolean _disabled = false;

  public FilterOption(Question question, JSONObject jsFilterOption) throws WdkModelException {
    String name = jsFilterOption.getString(KEY_NAME);
    this._value = jsFilterOption.getJSONObject(KEY_VALUE);
    this._filter = question.getFilter(name);
    if (jsFilterOption.has(KEY_DISABLED))
      this._disabled = jsFilterOption.getBoolean(KEY_DISABLED);
  }

  public FilterOption(Question question, Filter filter, JSONObject value) {
    this._filter = filter;
    this._value = value;
    this._disabled = false;
  }

  public String getKey() {
    return _filter.getKey();
  }

  public Filter getFilter() {
    return _filter;
  }

  public JSONObject getValue() {
    return _value;
  }

  public String getDisplayValue(AnswerValue answerValue) throws WdkModelException, WdkUserException {
    return _filter.getDisplayValue(answerValue, _value);
  }

  public boolean isDisabled() {
    return _disabled;
  }

  public void setDisabled(boolean disabled) {
    this._disabled = disabled;
  }
  
  public JSONObject getJSON() {
    JSONObject jsFilterOption = new JSONObject();
    jsFilterOption.put(KEY_NAME, _filter.getKey());
    jsFilterOption.put(KEY_VALUE, _value);
    jsFilterOption.put(KEY_DISABLED, _disabled);
    return jsFilterOption;
  }

    public boolean isSetToDefaultValue() throws WdkModelException {
	return getFilter().defaultValueEquals(getValue());
    }
}
