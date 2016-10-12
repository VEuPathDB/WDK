package org.gusdb.wdk.model.filter;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.Step;
import org.json.JSONArray;
import org.json.JSONObject;

public class FilterOptionList implements Iterable<FilterOption>{

  private WdkModel _wdkModel;
  private String _questionName;
  private Map<String, FilterOption> _options;

  public FilterOptionList(WdkModel wdkModel, String questionName) {
    init(wdkModel, questionName, new LinkedHashMap<String, FilterOption>());
  }

  public FilterOptionList(WdkModel wdkModel, String questionName, JSONArray jsOptions) {
    Map<String, FilterOption> options = new LinkedHashMap<>();
    for (int i = 0; i < jsOptions.length(); i++) {
      JSONObject jsFilterOption = jsOptions.getJSONObject(i);
      FilterOption option = new FilterOption(wdkModel, questionName, jsFilterOption);
      options.put(option.getKey(), option);
    }
    init(wdkModel, questionName, options);
  }

  public FilterOptionList(FilterOptionList source) {
    init(source._wdkModel, source._questionName, new LinkedHashMap<String, FilterOption>(source._options));
  }

  private void init(WdkModel wdkModel, String questionName, Map<String, FilterOption> options) {
    _wdkModel = wdkModel;
    _questionName = questionName;
    _options = options;
  }

  public boolean isFiltered(Step step) throws WdkModelException {
    for (FilterOption option : _options.values()) {
      if (!option.isDisabled() && !option.isSetToDefaultValue(step))
        return true;
    }
    return false;
  }

  public int getSize() {
    return _options.size();
  }

  public FilterOptionList addFilterOption(String filterName, JSONObject filterValue) throws WdkModelException {
    addFilterOption(filterName, filterValue, false);
    return this;
  }

  // we need to pass the disabled property
  public FilterOptionList addFilterOption(String filterName, JSONObject filterValue, boolean isDisabled) throws WdkModelException {
    addFilterOption(new FilterOption(_wdkModel, _questionName, filterName, filterValue, isDisabled));
    return this;
  }

  public FilterOptionList addFilterOption(FilterOption filterOption) throws WdkModelException {
    // make sure this option is valid for this list's question
    _wdkModel.getQuestion(_questionName).getFilter(filterOption.getKey());
    // no exception thrown; add filter to map
    _options.put(filterOption.getKey(), filterOption);
    return this;
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

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof FilterOptionList) {
      FilterOptionList other = (FilterOptionList)obj;
      if (!_questionName.equals(other._questionName)) {
        return false;
      }
      // FIXME: this is not the most efficient way, and may even be
      //    incorrect (due to property ordering), but it is the easiest code.
      return getJSON().toString().equals(other.getJSON().toString());
    }
    return false;
  }

  // overriding since we are overriding equals()
  @Override
  public int hashCode() {
    return (_questionName + " " + getJSON().toString()).hashCode();
  }
}
