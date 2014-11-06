package org.gusdb.wdk.model.filter;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.question.Question;
import org.json.JSONArray;
import org.json.JSONObject;

public class FilterOptionList {

  private final Map<String, FilterOption> options = new LinkedHashMap<>();

  private final Question question;

  public FilterOptionList(Question question) {
    this.question = question;
  }

  public FilterOptionList(Question question, JSONArray jsOptions) throws WdkModelException {
    this(question);
    for (int i = 0; i < jsOptions.length(); i++) {
      JSONObject jsFilterOption = jsOptions.getJSONObject(i);
      FilterOption option = new FilterOption(question, jsFilterOption);
      options.put(option.getKey(), option);
    }
  }

  public void addFilterOption(String filterName, JSONObject filterValue) throws WdkModelException {
    Filter filter = question.getFilter(filterName);
    FilterOption option = new FilterOption(filter, filterValue);
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
