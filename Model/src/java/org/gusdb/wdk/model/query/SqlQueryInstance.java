/**
 * 
 */
package org.gusdb.wdk.model.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.CacheFactory;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.dbms.SqlResultList;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.user.User;
import org.json.JSONObject;

/**
 * The query instance for SqlQuery, this instance will substitute param values
 * into the SQL template defined in the SqlQuery, and execute the sql in DBMS.
 * It will wrap the result into a ResultList.
 * 
 * @author Jerric Gao
 * 
 */
public class SqlQueryInstance extends QueryInstance {

  private static Logger logger = Logger.getLogger(SqlQueryInstance.class);

  private SqlQuery query;

  /**
   * @param query
   * @param values
   * @throws WdkUserException
   */
  protected SqlQueryInstance(User user, SqlQuery query,
      Map<String, String> values, boolean validate, int assignedWeight,
      Map<String, String> context) throws WdkModelException {
    super(user, query, values, validate, assignedWeight, context);
    this.query = query;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gusdb.wdk.model.query.QueryInstance#appendSJONContent(org.json.JSONObject
   * )
   */
  @Override
  protected void appendSJONContent(JSONObject jsInstance) {
    // nothing to add to;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.QueryInstance#getUncachedResults(java.lang.
   * Integer, java.lang.Integer)
   */
  @Override
  protected ResultList getUncachedResults() throws WdkModelException {
    try {
      String sql = getUncachedSql();
      DatabaseInstance platform = query.getWdkModel().getAppDb();
      DataSource dataSource = platform.getDataSource();
      ResultSet resultSet = SqlUtils.executeQuery(dataSource, sql,
          query.getFullName() + "__select-uncached");
      return new SqlResultList(resultSet);
    } catch (SQLException e) {
      throw new WdkModelException("Could not get uncached results from DB.", e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gusdb.wdk.model.query.QueryInstance#insertToCache(java.sql.Connection ,
   * java.lang.String)
   */
  @Override
  public void insertToCache(String tableName, int instanceId)
      throws WdkModelException {
    String idColumn = CacheFactory.COLUMN_INSTANCE_ID;
    String rowIdColumn = CacheFactory.COLUMN_ROW_ID;

    Map<String, Column> columns = query.getColumnMap();
    StringBuffer columnList = new StringBuffer();
    for (Column column : columns.values()) {
      columnList.append(", " + column.getName());
    }
    if (query.isHasWeight()) {
      String weightColumn = Utilities.COLUMN_WEIGHT;
      if (!columns.containsKey(weightColumn))
        columnList.append(", " + weightColumn);
    }

    DBPlatform platform = wdkModel.getAppDb().getPlatform();
    String rowNumber = platform.getRowNumberColumn();

    // get the sql with param values applied. The last column has to be the
    // weight.
    String sql = getUncachedSql();

    StringBuffer buffer = new StringBuffer("INSERT INTO " + tableName);
    buffer.append(" (" + idColumn + ", " + rowIdColumn + columnList + ") ");
    buffer.append("SELECT ");
    buffer.append(instanceId + " AS " + idColumn + ", ");
    buffer.append(rowNumber + " AS " + rowIdColumn);
    buffer.append(columnList + " FROM (" + sql + ") f");

    try {
      DataSource dataSource = wdkModel.getAppDb().getDataSource();
      SqlUtils.executeUpdate(dataSource, buffer.toString(), query.getFullName()
          + "__insert-cache");
    } catch (SQLException e) {
      throw new WdkModelException("Unable to insert record into cache.", e);
    }
  }

  public String getUncachedSql() throws WdkModelException {
    Map<String, String> internalValues = getParamInternalValues();
    Map<String, Param> params = query.getParamMap();
    String sql = query.getSql();
    for (String paramName : params.keySet()) {
      Param param = params.get(paramName);
      String value = internalValues.get(paramName);
      sql = param.replaceSql(sql, value);
    }
    StringBuilder buffer = new StringBuilder("SELECT o.* ");
    if (query.isHasWeight()) {
      // add weight to the last column if it doesn't exist, it has to be
      // the last column.
      Map<String, Column> columns = query.getColumnMap();
      if (!columns.containsKey(Utilities.COLUMN_WEIGHT)) {
        buffer.append(", " + assignedWeight + " AS " + Utilities.COLUMN_WEIGHT);
      }
    }
    buffer.append(" FROM (" + sql + ") o");

    // append sorting columns to the sql
    Map<String, Boolean> sortingMap = query.getSortingMap();
    boolean firstSortingColumn = true;
    for (String column : sortingMap.keySet()) {
      if (firstSortingColumn) {
        buffer.append(" ORDER BY ");
        firstSortingColumn = false;
      } else {
        buffer.append(", ");
      }
      String order = sortingMap.get(column) ? " ASC " : " DESC ";
      buffer.append(column).append(order);
    }

    return buffer.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.query.QueryInstance#getSql()
   */
  @Override
  public String getSql() throws WdkModelException {
    if (isCached()) return getCachedSql();
    else return getUncachedSql();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gusdb.wdk.model.query.QueryInstance#createCache(java.sql.Connection,
   * java.lang.String, int)
   */
  @Override
  public void createCache(String tableName, int instanceId)
      throws WdkModelException {
    logger.debug("creating cache table for query " + query.getFullName());
    // get the sql with param values applied.
    String sql = getUncachedSql();

    DBPlatform platform = wdkModel.getAppDb().getPlatform();
    String rowNumber = platform.getRowNumberColumn();

    StringBuffer buffer = new StringBuffer("CREATE TABLE " + tableName);
    buffer.append(" AS SELECT ");
    buffer.append(instanceId + " AS " + CacheFactory.COLUMN_INSTANCE_ID + ", ");
    buffer.append(rowNumber + " AS " + CacheFactory.COLUMN_ROW_ID + ", ");
    buffer.append(" f.* FROM (").append(sql).append(") f");

    try {
      DataSource dataSource = wdkModel.getAppDb().getDataSource();
      SqlUtils.executeUpdate(dataSource, buffer.toString(), query.getFullName()
          + "__create-cache");
    } catch (SQLException e) {
      throw new WdkModelException("Unable to create cache.", e);
    }
  }
}
