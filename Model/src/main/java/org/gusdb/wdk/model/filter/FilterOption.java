package org.gusdb.wdk.model.filter;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.question.Question;
import org.json.JSONObject;

public class FilterOption {

  public static final String KEY_NAME = "name";
  public static final String KEY_VALUE = "value";

  private final Filter filter;
  private final JSONObject value;

  public FilterOption(Question question, JSONObject jsFilterOption) throws WdkModelException {
    String name = jsFilterOption.getString(KEY_NAME);
    this.value = jsFilterOption.getJSONObject(KEY_VALUE);
    this.filter = question.getFilter(name);
  }

  public FilterOption(Filter filter, JSONObject value) {
    this.filter = filter;
    this.value = value;
  }

  public String getKey() {
    return filter.getKey();
  }
  
  public Filter getFilter() {
    return filter;
  }

  public JSONObject getValue() {
    return value;
  }

  public JSONObject getJSON() {
    JSONObject jsFilterOption = new JSONObject();
    jsFilterOption.put(KEY_NAME, filter.getKey());
    jsFilterOption.put(KEY_VALUE, value);
    return jsFilterOption;
  }
}
