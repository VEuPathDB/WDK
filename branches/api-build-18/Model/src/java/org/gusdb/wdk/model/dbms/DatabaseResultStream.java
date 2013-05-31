package org.gusdb.wdk.model.dbms;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Wrapper;


public class DatabaseResultStream extends InputStream implements Wrapper {

  private ResultSet _resultSet;
  private InputStream _dataStream;
  
  public DatabaseResultStream(ResultSet resultSet, String dataFieldName)
      throws SQLException {
    _resultSet = resultSet;
    _dataStream = _resultSet.getBinaryStream(dataFieldName);
  }
  
  @Override
  public int read() throws IOException {
    return _dataStream.read();
  }

  @Override
  public void close() throws IOException {
    try {
      _dataStream.close();
    }
    catch (Exception e) {
      // do nothing; hopefully will be fixed when we close result set
    }
    try {
      Statement stmt = _resultSet.getStatement();
      Connection conn = stmt.getConnection();
      SqlUtils.closeQuietly(_resultSet, stmt, conn);
    }
    catch (SQLException e) {
      throw new IOException("Unable to retrieve statement or connection for closing.", e);
    }
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new UnsupportedOperationException("This class does not wrap an instance of " + iface.getName());
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return false;
  }
}
