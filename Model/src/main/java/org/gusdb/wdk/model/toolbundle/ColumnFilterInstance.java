package org.gusdb.wdk.model.toolbundle;

public interface ColumnFilterInstance extends ColumnToolInstance {

  /**
   * @return an SQL {@code WHERE} clause.
   */
  String buildSqlWhere();

}
