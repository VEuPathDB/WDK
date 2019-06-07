package org.gusdb.wdk.model.dbms;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.wdk.model.WdkModelException;

/**
 * @author Jerric Gao
 * 
 */
public class SqlResultList implements ResultList {

  private Set<String> columns;
  private ResultSet resultSet;
  private boolean resultSetClosed = false;

  public SqlResultList(ResultSet resultSet) {
    this.resultSet = resultSet;
    resultSetClosed = false;
  }

  @Override
  public void close() throws WdkModelException {
    if (!resultSetClosed) {
      SqlUtils.closeResultSetAndStatement(resultSet, null);
      resultSetClosed = true;
    }
  }

  @Override
  public boolean contains(String columnName) throws WdkModelException {
    try {
      if (columns == null)
        columns = SqlUtils.getColumnNames(resultSet);
      return columns.contains(columnName);
    }
    catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
  }

  @Override
  public Object get(String columnName) throws WdkModelException {
    try {
      Object value = resultSet.getObject(columnName);
      if (resultSet.wasNull() || value == null) {
        return null;
      }
      if (value instanceof Clob) {
        Clob clob = (Clob) value;
        return clob.getSubString(1, (int) clob.length());
      }
      return value;
    }
    catch (SQLException ex) {
      throw new WdkModelException("Cannot get value for column '" + columnName + "'", ex);
    }
  }

  @Override
  public boolean next() throws WdkModelException {
    try {
      boolean hasNext = resultSet.next();
      if (!hasNext)
        close();
      return hasNext;
    }
    catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
  }
}
