package org.gusdb.wdk.model.dbms;

import java.util.Optional;
import java.util.function.BiFunction;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dbms.ResultFactory.CacheTableCreator;

public class TemporaryTable implements AutoCloseable {

  private static final Logger LOG = Logger.getLogger(TemporaryTable.class);

  private final WdkModel _wdkModel;
  private final String _cacheSchema;
  private final InstanceInfo _instance;

  public TemporaryTable(WdkModel wdkModel, BiFunction<String,String,String> createTableSql) throws WdkModelException {

    _wdkModel = wdkModel;
    _cacheSchema = wdkModel.getModelConfig().getAppDB().getCacheSchema();

    ResultFactory resultFactory = new ResultFactory(wdkModel);
    String checksum = Utilities.randomAlphaNumericString(40);

    _instance = resultFactory.cacheResults(checksum, new CacheTableCreator() {

      @Override
      public Optional<String> createCacheTableAndInsertResult(DatabaseInstance appDb, String cacheSchema, String tableName, long instanceId) throws WdkModelException {
        try {
          new SQLRunner(appDb.getDataSource(), createTableSql.apply(cacheSchema, tableName), "create-custom-temp-table").executeStatement();
          return Optional.empty();
        }
        catch (SQLRunnerException e) {
          throw new WdkModelException("Unable to create temporary table", e.getCause());
        }
      }

      @Override
      public String[] getCacheTableIndexColumns() {
        return new String[0];
      }

      @Override
      public String getQueryName() {
        return "WdkTemporaryTable_" + checksum.substring(0, 10);
      }
    }, false);

    LOG.info("Created temporary table " + _instance.getTableName());
  }

  public String getTableNameWithSchema() {
    return _cacheSchema + _instance.getTableName();
  }

  @Override
  public void close() throws Exception {
    LOG.info("Dropping temporary table " + _instance.getTableName());
    new CacheFactory(_wdkModel).dropCache(_instance.getInstanceId(), true);
  }
}
