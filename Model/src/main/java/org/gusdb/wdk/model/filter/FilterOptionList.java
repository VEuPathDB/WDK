package org.gusdb.wdk.model.filter;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.question.Question;
import org.json.JSONArray;
import org.json.JSONObject;

public class FilterOptionList implements Iterable<FilterOption>{

  private final Question _question;
  private final Map<String, FilterOption> _options = new LinkedHashMap<>();

  public FilterOptionList(Question question) {
    _question = question;
  }
  
  public FilterOptionList(Question question, JSONArray jsOptions) throws WdkModelException {
    _question = question;
    for (int i = 0; i < jsOptions.length(); i++) {
      JSONObject jsFilterOption = jsOptions.getJSONObject(i);
      FilterOption option = new FilterOption(question, jsFilterOption);
      _options.put(option.getKey(), option);
    }
  }

  public int getSize() {
    return _options.size();
  }

  public boolean isFiltered() {
    return (_options.size() > 0);
  }

  public void addFilterOption(FilterOption filterOption) throws WdkModelException {
    // make sure this option is valid for this list's question
    String filterName = filterOption.getKey();
    _question.getFilter(filterName);
    _options.put(filterName, filterOption);
  }

  public void addFilterOption(String filterName, JSONObject filterValue) throws WdkModelException {
    Filter filter = _question.getFilter(filterName);
    FilterOption option = new FilterOption(_question, filter, filterValue);
    _options.put(filterName, option);
  }

  public void removeFilterOption(String filterName) {
    _options.remove(filterName);
  }

  public Map<String, FilterOption> getFilterOptions() {
    return new LinkedHashMap<>(_options);
  }

  public JSONArray getJSON() {
    JSONArray jsOptions = new JSONArray();
    for (FilterOption option : _options.values()) {
      jsOptions.put(option.getJSON());
    }
    return jsOptions;
  }

  /**
   * Returns the filter option for the passed name, or null if no option
   * exists for that name in this list
   * 
   * @param filterName filter name
   * @return filter option for the name or null if none is found
   */
  public FilterOption getFilterOption(String filterName) {
    return _options.get(filterName);
  }

  @Override
  public Iterator<FilterOption> iterator() {
    return _options.values().iterator();
  }
}
