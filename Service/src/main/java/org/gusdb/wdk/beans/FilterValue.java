package org.gusdb.wdk.beans;

import org.gusdb.wdk.model.answer.AnswerFilterInstance;

public class FilterValue {

  private final AnswerFilterInstance _filter;
  private final Object _value;

  public FilterValue(AnswerFilterInstance filter, Object value) {
    _filter = filter;
    _value = value;
  }

  public String getName() {
    return _filter.getName();
  }

  public Object getValue() {
    return _value;
  }

}
