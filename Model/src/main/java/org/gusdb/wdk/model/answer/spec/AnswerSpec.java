package org.gusdb.wdk.model.answer.spec;

import java.util.HashMap;
import java.util.Map;

import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.filter.FilterOptionList;
import org.gusdb.wdk.model.question.Question;

public class AnswerSpec {

  private final Question _question;
  private Map<String, ParamValue> _params = new HashMap<>();
  private AnswerFilterInstance _legacyFilter;
  private FilterOptionList _filters;
  private FilterOptionList _viewFilters;
  private int _weight = 0;

  public AnswerSpec(Question question) {
    _question = question;
  }

  public Question getQuestion() {
    return _question;
  }

  public Map<String, ParamValue> getParamValues() {
    return _params;
  }
  public void setParamValues(Map<String, ParamValue> params) {
    _params = params;
  }

  public AnswerFilterInstance getLegacyFilter() {
    return _legacyFilter;
  }
  public void setLegacyFilter(AnswerFilterInstance legacyFilter) {
    _legacyFilter = legacyFilter;
  }

  public FilterOptionList getFilterValues() {
    return _filters;
  }
  public void setFilterValues(FilterOptionList filters) {
    _filters = filters;
  }

  public FilterOptionList getViewFilterValues() {
    return _viewFilters;
  }
  public void setViewFilterValues(FilterOptionList viewFilters) {
    _viewFilters = viewFilters;
  }

  public int getWeight() {
    return _weight;
  }
  public void setWeight(int weight) {
    _weight = weight;
  }
}
