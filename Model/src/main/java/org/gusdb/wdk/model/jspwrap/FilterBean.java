package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.answer.AnswerFilterInstance;

public class FilterBean {

  private final AnswerFilterInstance _filter;
  private final Object _value;

  public FilterBean(AnswerFilterInstance filter, Object value) {
    _filter = filter;
    _value = value;
  }

  public String getName() {
    return _filter.getName();
  }

}
