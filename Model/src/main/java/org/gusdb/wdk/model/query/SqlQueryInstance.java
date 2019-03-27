package org.gusdb.wdk.model.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dbms.CacheFactory;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.dbms.SqlResultList;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.json.JSONObject;

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
    return _query.getIsCacheable() ? getCachedResults(performSorting) : getUncachedResults();
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
    return _query.getIsCacheable() ? getCachedSql(performSorting) : getUncachedSql();
  }

  private ResultList getUncachedResults() throws WdkModelException {
    try {
      String sql = getUncachedSql();
      DatabaseInstance platform = _query.getWdkModel().getAppDb();
      DataSource dataSource = platform.getDataSource();
      LOG.debug("Performing the following SQL: " + sql);
      ResultSet resultSet = SqlUtils.executeQuery(dataSource, sql, _query.getFullName() + "__select-uncached",
          SqlUtils.DEFAULT_FETCH_SIZE, _query.isUseDBLink());
      return new SqlResultList(resultSet);
    }
    catch (SQLException e) {
      throw new WdkModelException("Could not get uncached results from DB.", e);
    }
  }

  @Override
  public Optional<String> createCacheTableAndInsertResult(DatabaseInstance appDb, String tableName, long instanceId)
      throws WdkModelException {
    LOG.debug("creating cache table for query " + _query.getFullName());
    // get the sql with param values applied.
    String sql = getUncachedSql();
    String rowNumber = appDb.getPlatform().getRowNumberColumn();

    StringBuffer buffer = new StringBuffer("CREATE TABLE " + tableName);
    buffer.append(" AS SELECT ");
    buffer.append(instanceId + " AS " + CacheFactory.COLUMN_INSTANCE_ID + ", ");
    buffer.append(rowNumber + " AS " + CacheFactory.COLUMN_ROW_ID + ", ");  
    
    buffer.append(" f.* FROM (").append(sql).append(") f");

    DataSource dataSource = appDb.getDataSource();
    try {
      SqlUtils.executeUpdate(dataSource, buffer.toString(), _query.getFullName() + "__create-cache-table",
          _query.isUseDBLink());
    }
    catch (SQLException e) {
      LOG.error("Failed to run sql:\n" + buffer);
      throw new WdkModelException("Unable to create cache.", e);
    }
        
    executePostCacheUpdateSql(tableName, instanceId);
    LOG.debug("created!!  cache table for query " + _query.getFullName());
    return Optional.empty();
  }

  public String getUncachedSql() throws WdkModelException {
    Map<String, String> internalValues = getParamInternalValues();
    Map<String, Param> params = _query.getParamMap();
    String sql = _query.getSql();
    for (String paramName : params.keySet()) {
      Param param = params.get(paramName);
      String value = internalValues.get(paramName);
      if (value == null) {
        LOG.warn("value doesn't exist for param " + param.getFullName() + " in query " +
            _query.getFullName());
        value = "";
      }
      sql = param.replaceSql(sql, value);
    }
    StringBuilder buffer = new StringBuilder("SELECT o.* ");
    if (_query.isHasWeight()) {
      // add weight to the last column if it doesn't exist, it has to be
      // the last column.
      Map<String, Column> columns = _query.getColumnMap();
      if (!columns.containsKey(Utilities.COLUMN_WEIGHT)) {
        buffer.append(", " + _spec.get().getAssignedWeight() + " AS " + Utilities.COLUMN_WEIGHT);
      }
    }
    buffer.append(" FROM (" + sql + ") o");

    // append sorting columns to the sql
    Map<String, Boolean> sortingMap = _query.getSortingMap();
    boolean firstSortingColumn = true;
    for (String column : sortingMap.keySet()) {
      if (firstSortingColumn) {
        buffer.append(" ORDER BY ");
        firstSortingColumn = false;
      }
      else {
        buffer.append(", ");
      }
      String order = sortingMap.get(column) ? " ASC " : " DESC ";
      buffer.append(column).append(order);
    }

    return buffer.toString();
  }
}
