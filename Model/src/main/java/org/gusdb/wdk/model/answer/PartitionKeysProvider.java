package org.gusdb.wdk.model.answer;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.SqlQuery;

public interface PartitionKeysProvider {

  public static PartitionKeysProvider PLACEHOLDER_PROVIDER = new PartitionKeysProvider() {
    @Override
    public String substitutePartitionKeys(String sql, String queryName) throws WdkModelException {
      return sql;
    }

    @Override
    public String getPartitionKeysStringForPostCacheUpdate(String cacheSchema,
                                                           String tableName, String queryName) throws WdkModelException {
      return SqlQuery.PARTITION_KEYS_PLACEHOLDER;
    }
  };

  public String substitutePartitionKeys(String sql, String queryName) throws WdkModelException;

  public String getPartitionKeysStringForPostCacheUpdate(String cacheSchema, String tableName, String queryName) throws WdkModelException;

  }
