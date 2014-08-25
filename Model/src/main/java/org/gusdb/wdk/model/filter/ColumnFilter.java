package org.gusdb.wdk.model.filter;

import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;

public abstract class ColumnFilter implements Filter {

  private final AttributeField field;
  
  public ColumnFilter(AttributeField field) {
    this.field = field;
  }

  @Override
  public FilterSummary getSummary(AnswerValue answer) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getSql(AnswerValue answer, String idSql, String options) {
    // TODO Auto-generated method stub
    return null;
  }

}
