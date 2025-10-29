package org.gusdb.wdk.model.dbms;

import java.util.Date;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.cache.ValueFactory;
import org.gusdb.fgputil.cache.ValueProductionException;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.wdk.model.WdkModelException;

public class InstanceInfoFetcher implements ValueFactory<String, Optional<InstanceInfo>> {

  private static final Logger LOG = Logger.getLogger(InstanceInfoFetcher.class);

  private static final long EXPIRATION_SECS = 8;

  public static String getKey(String checksum) {
    return checksum;
  }

  private final DatabaseInstance _appDb;
  private final String _cacheSchema;

  public InstanceInfoFetcher(DatabaseInstance appDb, String cacheSchema) {
    _appDb = appDb;
    _cacheSchema = cacheSchema;
  }

  @Override
  public Optional<InstanceInfo> getNewValue(String id) throws ValueProductionException {
    try {
      LOG.debug("Fetching instance info item with ID: " + id);
      return getInstanceInfo(_appDb, _cacheSchema, id);
    }
    catch(WdkModelException e) {
      throw new ValueProductionException(e);
    }
  }

  @Override
  public Optional<InstanceInfo> getUpdatedValue(String id, Optional<InstanceInfo> previousVersion) throws ValueProductionException {
    return getNewValue(id);
  }

  @Override
  public boolean valueNeedsUpdating(Optional<InstanceInfo> item) {
    return !item.isPresent() ||
        (new Date().getTime() - item.get().getCreationDate()) >= (EXPIRATION_SECS * 1000);
  }

  static Optional<InstanceInfo> getInstanceInfo(DatabaseInstance appDb, String cacheSchema, String checksum) throws WdkModelException {

    String sql = new StringBuilder("SELECT ")
        .append(CacheFactory.COLUMN_INSTANCE_ID).append(", ")
        .append(CacheFactory.COLUMN_TABLE_NAME).append(", ")
        .append(CacheFactory.COLUMN_QUERY_NAME).append(", ")
        .append(CacheFactory.COLUMN_RESULT_MESSAGE)
        .append(" FROM ").append(cacheSchema).append(CacheFactory.TABLE_INSTANCE)
        .append(" WHERE ").append(CacheFactory.COLUMN_INSTANCE_CHECKSUM)
        .append(" = '").append(checksum).append("'")
        .append(" ORDER BY " + CacheFactory.COLUMN_INSTANCE_ID)
        .toString();

    try {
      return new SQLRunner(appDb.getDataSource(), sql, "wdk-check-instance-exist")
        .executeQuery(resultSet ->
          resultSet.next() ?
          Optional.of(new InstanceInfo(
            resultSet.getLong(CacheFactory.COLUMN_INSTANCE_ID),
            resultSet.getString(CacheFactory.COLUMN_TABLE_NAME),
            resultSet.getString(CacheFactory.COLUMN_QUERY_NAME),
            checksum,
            Optional.ofNullable(appDb.getPlatform()
                .getClobData(resultSet, CacheFactory.COLUMN_RESULT_MESSAGE)))) :
          Optional.empty()
        );
    }
    catch(Exception e) {
      throw WdkModelException.translateFrom(e, "Error while trying to find WDK cache instance info.  Erroring SQL: \n" + sql);
    }
  }
}
