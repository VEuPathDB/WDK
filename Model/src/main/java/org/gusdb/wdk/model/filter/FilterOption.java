package org.gusdb.wdk.model.filter;

import org.json.JSONObject;

public class FilterOption {

  public static final String KEY_NAME = "name";
  public static final String KEY_VALUE = "value";

  private final String name;
  private final JSONObject value;

  public FilterOption(JSONObject jsFilterOption) {
    name = jsFilterOption.getString(KEY_NAME);
    value = jsFilterOption.getJSONObject(KEY_VALUE);
  }

  public FilterOption(String name, JSONObject value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public JSONObject getValue() {
    return value;
  }

  public JSONObject getJSON() {
    JSONObject jsFilterOption = new JSONObject();
    jsFilterOption.put(KEY_NAME, name);
    jsFilterOption.put(KEY_VALUE, value);
    return jsFilterOption;
  }
}
