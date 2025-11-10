package org.gusdb.wdk.model.answer;

import org.gusdb.wdk.model.WdkModelException;

public interface PartitionKeysProvider {

  public static PartitionKeysProvider PLACEHOLDER_PROVIDER = new PartitionKeysProvider() {
    @Override
    public String substitutePartitionKeys(String sql, String queryName) throws WdkModelException {
      return sql;
    }
  };

  public String substitutePartitionKeys(String sql, String queryName) throws WdkModelException;

}
