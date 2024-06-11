package org.gusdb.wdk.model.dbms;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.wdk.model.WdkModelException;

/**
 * Wrapper around a JDBC ResultSet that implements the ResultList interface and
 * handles a few JDBC "oddities":
 *
 * 1. if a column is null, always returns null, not JDBC default values e.g. 0 for ints
 * 2. if a column is a CLOB, reads the entire CLOB value into a String and returns it
 *     NOTE: this is a -little- dangerous for memory but usually fine in places we're using ResultLists
 *
 * By default this class is responsible for closing the underlying ResultSet and its Statement and Connection.
 * This responsibility can be turned off with a call to setResponsibleForClosingResultSet().
 *
 * @author Jerric Gao
 */
public class SqlResultList implements ResultList {

  // result set wrapped by this result list
  private final ResultSet _resultSet;

  // cache of columns this result list serves
  private Set<String> _columns;

  // whether this result list will close the underlying result set
  private boolean _responibleForClosingResultSet = true;

  // whether the underlying result set has already been closed by this result list
  private boolean _resultSetClosed = false;

  public SqlResultList(ResultSet resultSet) {
    _resultSet = resultSet;
  }

  public SqlResultList setResponsibleForClosingResultSet(boolean responibleForClosingResultSet) {
    _responibleForClosingResultSet = responibleForClosingResultSet;
    return this;
  }

  @Override
  public void close() throws WdkModelException {
    if (!_resultSetClosed && _responibleForClosingResultSet) {
      SqlUtils.closeResultSetAndStatement(_resultSet, null);
      _resultSetClosed = true;
    }
  }

  @Override
  public boolean contains(String columnName) throws WdkModelException {
    try {
      if (_columns == null)
        _columns = SqlUtils.getColumnNames(_resultSet);
      return _columns.contains(columnName);
    }
    catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
  }

  @Override
  public Object get(String columnName) throws WdkModelException {
    try {
      Object value = _resultSet.getObject(columnName);
      if (_resultSet.wasNull() || value == null) {
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
      boolean hasNext = _resultSet.next();
      if (!hasNext)
        close();
      return hasNext;
    }
    catch (SQLException ex) {
      throw new WdkModelException(ex);
    }
  }
}
