package org.gusdb.wdk.model.dbms;

import org.gusdb.wdk.model.WdkModelException;

/**
 * @author Jerric Gao
 */
public interface ResultList extends AutoCloseable {

  boolean next() throws WdkModelException;

  Object get(String columnName) throws WdkModelException;

  boolean contains(String columnName) throws WdkModelException;

  @Override
  void close() throws WdkModelException;
}
