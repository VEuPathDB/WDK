package org.gusdb.wdk.model.filter;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.Step;
import org.json.JSONObject;

public class FilterOption {

  public static final String KEY_NAME = "name";
  public static final String KEY_VALUE = "value";
  public static final String KEY_DISABLED = "disabled";

  private final Step _step;
  private final Filter _filter;
  private final JSONObject _value;
  private boolean _disabled = false;

  public FilterOption(Step step, JSONObject jsFilterOption) throws WdkModelException {
    String name = jsFilterOption.getString(KEY_NAME);
    this._step = step;
    this._value = jsFilterOption.getJSONObject(KEY_VALUE);
    this._filter = step.getQuestion().getFilter(name);
    if (jsFilterOption.has(KEY_DISABLED))
      this._disabled = jsFilterOption.getBoolean(KEY_DISABLED);
  }

  public FilterOption(Step step, Filter filter, JSONObject value) {
    this._step = step;
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

  public String getDisplayValue() throws WdkModelException, WdkUserException {
    return _filter.getDisplayValue(_step.getAnswerValue(), _value);
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
}
