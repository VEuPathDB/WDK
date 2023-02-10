package org.gusdb.wdk.model.query;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dbms.CacheFactory;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.dbms.SqlResultList;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

/**
 * The query instance for SqlQuery, this instance will substitute param values into the SQL template defined
 * in the SqlQuery, and execute the sql in DBMS. It will wrap the result into a ResultList.
 *
 * @author Jerric Gao
 */
public class SqlQueryInstance extends QueryInstance<SqlQuery> {

  private static final Logger LOG = Logger.getLogger(SqlQueryInstance.class);

  SqlQueryInstance(RunnableObj<QueryInstanceSpec> spec) {
    super(spec);
  }

  @Override
  protected void appendJSONContent(JSONObject jsInstance) {
    // nothing to add
  }

  @Override
  protected ResultList getResults(boolean performSorting) throws WdkModelException {
    return _query.isCacheable()
      ? getCachedResults(performSorting)
      : getUncachedResults();
  }

  @Override
  public String getSql() throws WdkModelException {
    return getSql(true);
  }

  @Override
  public String getSqlUnsorted() throws WdkModelException {
    return getSql(false);
  }

  private String getSql(boolean performSorting) throws WdkModelException {
    return _query.isCacheable()
      ? getCachedSql(performSorting)
      : getUncachedSql();
  }

  private ResultList getUncachedResults() throws WdkModelException {
    try {
      var sql = getUncachedSql();
      LOG.debug("Performing the following SQL: " + sql);
      return new SqlResultList(SqlUtils.executeQuery(
        _wdkModel.getAppDb().getDataSource(),
        sql,
        _query.getFullName() + "__select-uncached",
        0,
        _query.isUseDBLink()
      ));
    }
    catch (SQLException e) {
      throw new WdkModelException("Could not get uncached results from DB.", e);
    }
  }

  @Override
  public Optional<String> createCacheTableAndInsertResult(DatabaseInstance appDb, String tableName, long instanceId)
      throws WdkModelException {
    return new QueryMetric(_wdkModel.getProjectId(), _query.getFullName())
        .observeCacheInsertion(appDb, tableName, () -> {
      LOG.debug("Creating cache table for query " + _query.getFullName());
      // get the sql with param values applied.
      var sql = getUncachedSql();
      LOG.debug("Uncached SQL for query " + _query.getFullName() + ": " + sql);
      var rowNumber = appDb.getPlatform().getRowNumberColumn();
      var insertSql = new StringBuilder("CREATE TABLE " + tableName)
        .append(" AS SELECT ")
        .append(instanceId + " AS " + CacheFactory.COLUMN_INSTANCE_ID + ", ")
        .append(rowNumber + " AS " + CacheFactory.COLUMN_ROW_ID + ", ")
        .append(" f.* FROM (").append(sql).append(") f")
        .toString();

      var dataSource = appDb.getDataSource();
      try {
        SqlUtils.executeUpdate(dataSource, insertSql, _query.getFullName() + "__create-cache-table",
            _query.isUseDBLink());
      }
      catch (SQLException e) {
        LOG.error("Failed to run sql:\n" + insertSql);
        throw new WdkModelException("Unable to create cache.", e);
      }

      executePostCacheUpdateSql(tableName, instanceId);
      LOG.debug("created!!  cache table for query " + _query.getFullName());
      return Optional.empty();
    });
  }

  public String getUncachedSql() throws WdkModelException {
    return getUncachedSql(getParamInternalValues());
  }

  /**
   * @param internal
   *   param internal values
   */
  public String getUncachedSql(Map<String, String> internal) {
    var params = _query.getParamMap();
    var sql = _query.getSql();

    for (var paramName : params.keySet()) {
      var param = params.get(paramName);
      var value = internal.get(paramName);
      if (value == null) {
        LOG.warn("value doesn't exist for param " + param.getFullName()
          + " in query " + _query.getFullName());
        value = "";
      }
      sql = param.replaceSql(sql, _spec.get().get(paramName), value);
    }

    var buffer = new StringBuilder("SELECT o.* ");
    if (_query.isHasWeight()) {
      // add weight to the last column if it doesn't exist, it has to be
      // the last column.
      var columns = _query.getColumnMap();
      if (!columns.containsKey(Utilities.COLUMN_WEIGHT)) {
        buffer.append(", ")
          .append(_spec.get().getAssignedWeight())
          .append(" AS " + Utilities.COLUMN_WEIGHT);
      }
    }

    buffer.append(" FROM (")
      .append(sql)
      .append(") o");

    // append sorting columns to the sql
    var sortingMap = _query.getSortingMap();
    var firstSortingColumn = true;
    for (var column : sortingMap.keySet()) {
      if (firstSortingColumn) {
        buffer.append(" ORDER BY ");
        firstSortingColumn = false;
      } else {
        buffer.append(", ");
      }
      var order = sortingMap.get(column) ? " ASC " : " DESC ";
      buffer.append(column).append(order);
    }

    return buffer.toString();
  }
}
