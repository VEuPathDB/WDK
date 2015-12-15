package org.gusdb.wdk.service.request.answer;

import java.util.HashMap;
import java.util.Map;

import org.gusdb.wdk.beans.ParamValue;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.filter.FilterOptionList;
import org.gusdb.wdk.model.question.Question;

public class AnswerRequest {

  private final Question _question;
  private Map<String, ParamValue> _params = new HashMap<>();
  private AnswerFilterInstance _legacyFilter;
  private FilterOptionList _filters;
  private FilterOptionList _viewFilters;
  private int _weight = 0;

  AnswerRequest(Question question) {
    _question = question;
  }

  public Question getQuestion() {
    return _question;
  }

  public Map<String, ParamValue> getParamValues() {
    return _params;
  }
  void setParamValues(Map<String, ParamValue> params) {
    _params = params;
  }

  public AnswerFilterInstance getLegacyFilter() {
    return _legacyFilter;
  }
  void setLegacyFilter(AnswerFilterInstance legacyFilter) {
    _legacyFilter = legacyFilter;
  }

  public FilterOptionList getFilterValues() {
    return _filters;
  }
  void setFilterValues(FilterOptionList filters) {
    _filters = filters;
  }

  public FilterOptionList getViewFilterValues() {
    return _viewFilters;
  }
  void setViewFilterValues(FilterOptionList viewFilters) {
    _viewFilters = viewFilters;
  }

  public int getWeight() {
    return _weight;
  }
  void setWeight(int weight) {
    _weight = weight;
  }
}
