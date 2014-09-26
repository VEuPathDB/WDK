package org.gusdb.wdk.model.filter;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeField;

public abstract class ColumnFilter extends AbstractFilter {

  protected final ColumnAttributeField attribute;

  public ColumnFilter(String name, ColumnAttributeField attribute) {
    super(name + "-" + attribute.getName());
    this.attribute = attribute;
  }

  protected String getAttributeSql(AnswerValue answer, String idSql) throws WdkModelException,
      WdkUserException {
    String queryName = attribute.getColumn().getQuery().toString();
    WdkModel wdkModel = attribute.getWdkModel();
    Query query = (Query) wdkModel.resolveReference(queryName);
    return answer.getAttributeSql(query);
  }
}
