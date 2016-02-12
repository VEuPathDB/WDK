package org.gusdb.wdk.model.filter;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeField;
import org.json.JSONObject;

public abstract class ColumnFilter extends AbstractFilter {

  protected final ColumnAttributeField attribute;

  public ColumnFilter(String name, ColumnAttributeField attribute) {
    super(name + "-" + attribute.getName());
    this.attribute = attribute;
  }

  @Override
  public String getDisplay() {
    String display = super.getDisplay();
    if (display == null || display.length() == 0)
      display = attribute.getDisplayName();
    return display;
  }

  protected String getAttributeSql(AnswerValue answer, String idSql) throws WdkModelException,
      WdkUserException {
    String queryName = attribute.getColumn().getQuery().getFullName();
    WdkModel wdkModel = attribute.getWdkModel();
    Query query = (Query) wdkModel.resolveReference(queryName);
    String attributeSql = answer.getAttributeSql(query);
    String[] pkColumns = answer.getQuestion().getRecordClass().getPrimaryKeyAttributeField().getColumnRefs();
    StringBuilder sql = new StringBuilder("SELECT aq.* ");
    sql.append(" FROM (" + idSql + ") idq, (" + attributeSql + ") aq ");
    for (int i = 0; i < pkColumns.length; i++) {
      sql.append((i == 0) ? " WHERE " : " AND ");
      sql.append(" idq." + pkColumns[i] + " = aq." + pkColumns[i]);
    }
    return sql.toString();
  }
  
  @Override
  public void setDefaultValue(JSONObject defaultValue) {
	  throw new UnsupportedOperationException("Not supported until the defaultValueEquals() method is fully implemented");
  }
  
  @Override
  /**
   * Not fully implemented yet.
   */
  public boolean defaultValueEquals(JSONObject value)  throws WdkModelException {
	  return false;
  }
}
