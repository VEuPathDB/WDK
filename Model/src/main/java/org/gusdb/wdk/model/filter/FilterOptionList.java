package org.gusdb.wdk.model.filter;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.question.Question;
import org.json.JSONArray;
import org.json.JSONObject;

public class FilterOptionList implements Iterable<FilterOption>{

  private static final Logger logger = Logger.getLogger(FilterOptionList.class);

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
      logger.debug("filter option LIST: 1 (question -- jsoptions array): ADDING FILTER OPTIONS to step with question: " + question.getFullName());
      _options.put(option.getKey(), option);
    }
  }

  public boolean isFiltered() throws WdkModelException {
    for (FilterOption option : _options.values()) {
  if (!option.isDisabled() && !option.isSetToDefaultValue()) return true;
    }
    return false;
  }

  public int getSize() {
    return _options.size();
  }

  public void addFilterOption(FilterOption filterOption) throws WdkModelException {
    // make sure this option is valid for this list's question
    String filterName = filterOption.getKey();
    _question.getFilter(filterName);
      logger.debug("filter option LIST: 2 (filteroption, copy): ADDING FILTER OPTION to step with question: " + _question.getFullName());
    _options.put(filterName, filterOption);
  }

  // we need to add/pass the disabled property
  public void addFilterOption(String filterName, JSONObject filterValue) throws WdkModelException {
    Filter filter = _question.getFilter(filterName);
    FilterOption option = new FilterOption(_question, filter, filterValue);
    logger.debug("filter option LIST: 3 (filter name -- js value jsobject):ADDING FILTER OPTION to step with question: " + _question.getFullName());
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
