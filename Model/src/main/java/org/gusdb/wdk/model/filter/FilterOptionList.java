package org.gusdb.wdk.model.filter;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class FilterOptionList {

  private final Map<String, FilterOption> options = new LinkedHashMap<>();

  public FilterOptionList() {}

  public FilterOptionList(JSONArray jsOptions) {
    for (int i = 0; i < jsOptions.length(); i++) {
      JSONObject jsFilterOption = jsOptions.getJSONObject(i);
      FilterOption option = new FilterOption(jsFilterOption);
      options.put(option.getName(), option);
    }
  }

  public void addFilterOption(String filterName, JSONObject filterValue) {
    FilterOption option = new FilterOption(filterName, filterValue);
    options.put(filterName, option);
  }
  
  public void removeFilterOption(String filterName) {
    options.remove(filterName);
  }

  public Map<String, FilterOption> getFilterOptions() {
    return new LinkedHashMap<>(options);
  }

  public JSONArray getJSON() {
    JSONArray jsOptions = new JSONArray();
    for (FilterOption option : options.values()) {
      jsOptions.put(option.getJSON());
    }
    return jsOptions;
  }
  
  public FilterOption getFilterOption(String filterName) {
    return options.get(filterName);
  }
}
