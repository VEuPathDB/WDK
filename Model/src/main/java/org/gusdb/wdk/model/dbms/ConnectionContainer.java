package org.gusdb.wdk.model.dbms;

import java.sql.Connection;
import java.sql.SQLException;

import org.gusdb.wdk.model.WdkModelException;

public interface ConnectionContainer {

  Connection getConnection(String key) throws WdkModelException, SQLException;
  
}
