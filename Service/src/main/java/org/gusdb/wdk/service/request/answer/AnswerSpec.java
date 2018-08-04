package org.gusdb.wdk.service.request.answer;

import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.filter.FilterOptionList;
import org.gusdb.wdk.model.query.param.values.StableValues;
import org.gusdb.wdk.model.question.Question;

public class AnswerSpec {

  private final Question _question;
  private StableValues _params;
  private AnswerFilterInstance _legacyFilter;
  private FilterOptionList _filters;
  private FilterOptionList _viewFilters;
  private int _weight = 0;

  AnswerSpec(Question question) {
    _question = question;
  }

  public Question getQuestion() {
    return _question;
  }

  public StableValues getParamValues() {
    return _params;
  }
  void setParamValues(StableValues params) {
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
