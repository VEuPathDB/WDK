/**
 * 
 */
package org.gusdb.wdk.model.dbms;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkException;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.json.JSONException;

/**
 * @author Jerric Gao
 * 
 */
public class ResultFactory {

    private Logger logger = Logger.getLogger(ResultFactory.class);

    private DBPlatform platform;
    private CacheFactory cacheFactory;
    private WdkModel wdkModel;

    public ResultFactory(WdkModel wdkModel) throws SQLException {
        this.platform = wdkModel.getQueryPlatform();
        this.cacheFactory = new CacheFactory(wdkModel, platform);
        this.wdkModel = platform.getWdkModel();
    }

    public CacheFactory getCacheFactory() {
        return cacheFactory;
    }

    public ResultList getCachedResults(QueryInstance instance)
            throws WdkModelException {

        // get the cached sql
        Query query = instance.getQuery();
        QueryInfo queryInfo = cacheFactory.getQueryInfo(query);
        StringBuffer sql = new StringBuffer("SELECT ");
        boolean firstColumn = true;
        for (Column column : query.getColumns()) {
            if (firstColumn) firstColumn = false;
            else sql.append(", ");
            sql.append(column.getName());
        }
        sql.append(" FROM ").append(queryInfo.getCacheTable());
        sql.append(" WHERE ").append(CacheFactory.COLUMN_INSTANCE_ID);
        sql.append(" = ").append(instance.getInstanceId());

        // get the resultList
        try {
        	DataSource dataSource = platform.getDataSource();
        	ResultSet resultSet = SqlUtils.executeQuery(wdkModel, dataSource,
        			sql.toString(), query.getFullName() + "__select-cache");
        	return new SqlResultList(resultSet);
        }
        catch (SQLException e) {
        	throw new WdkModelException("Unable to retrieve cached results.", e);
        }
    }

    public int getInstanceId(QueryInstance instance, String[] indexColumns)
            throws WdkModelException {
        Query query = instance.getQuery();
        QueryInfo queryInfo = cacheFactory.getQueryInfo(query);
        // get the query instance id; null if not exist
        Integer instanceId = checkInstanceId(instance, queryInfo);
        if (instanceId == null) // instance cache not exist, create it
            instanceId = createCache(instance, queryInfo, indexColumns);
        instance.setInstanceId(instanceId);
        return instanceId;
    }

    public String getCacheTable(QueryInstance instance, String[] indexColumn)
            throws WdkModelException, WdkUserException {
        // make sure the instance is cached
        getInstanceId(instance, indexColumn);
        Query query = instance.getQuery();
        QueryInfo queryInfo = cacheFactory.getQueryInfo(query);
        return queryInfo.getCacheTable();
    }

    /**
     * look up the cache index table for the query instance; if the instance
     * doesn't exist, return null;
     * 
     * @param instance
     * @return
     * @throws SQLException
     * @throws WdkUserException
     * @throws JSONException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     * @throws SQLException
     * @throws JSONException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     * @throws WdkUserException
     */
    private Integer checkInstanceId(QueryInstance instance, QueryInfo queryInfo)
    		throws WdkModelException {
        String checksum = instance.getChecksum();
        StringBuffer sql = new StringBuffer("SELECT ");
        sql.append(CacheFactory.COLUMN_INSTANCE_ID).append(", ");
        sql.append(CacheFactory.COLUMN_RESULT_MESSAGE);
        sql.append(" FROM ").append(CacheFactory.TABLE_INSTANCE);
        sql.append(" WHERE ").append(CacheFactory.COLUMN_QUERY_ID);
        sql.append(" = ").append(queryInfo.getQueryId());
        sql.append(" AND ").append(CacheFactory.COLUMN_INSTANCE_CHECKSUM);
        sql.append(" = '").append(checksum).append("'");

        DataSource dataSource = platform.getDataSource();
        ResultSet resultSet = null;
        try {
            resultSet = SqlUtils.executeQuery(wdkModel, dataSource,
                    sql.toString(), "wdk-check-instance-exist");
            if (!resultSet.next()) return null;

            int instanceId = resultSet.getInt(CacheFactory.COLUMN_INSTANCE_ID);
            String message = platform.getClobData(resultSet,
                    CacheFactory.COLUMN_RESULT_MESSAGE);
            instance.setResultMessage(message);
            instance.setInstanceId(instanceId);
            return instanceId;
        }
        catch (SQLException e) {
        	throw new WdkModelException("Unable to check instance ID.", e);
        }
        finally {
            SqlUtils.closeResultSetAndStatement(resultSet);
        }
    }

    private int createCache(QueryInstance instance, QueryInfo queryInfo,
            String[] indexColumns) throws WdkModelException {
    	try {
	        DataSource dataSource = platform.getDataSource();
	
	        // start transaction
	        Connection connection = null;
	        try {
	        	connection = dataSource.getConnection();
	        	connection.setAutoCommit(false);
	            // add instance into cache index table, and get instanceId back
	            // get a new id for the instance
	            int instanceId = platform.getNextId(null,
	                    CacheFactory.TABLE_INSTANCE);
	            instance.setInstanceId(instanceId);
	
	            // check whether need to create the cache;
	            String cacheTable = queryInfo.getCacheTable();
	            if (!platform.checkTableExists(null, cacheTable)) {
	                // create the cache using the result of the first query
	                instance.createCache(connection, cacheTable, instanceId,
	                        indexColumns);
	                // disable the stats on the new cache table
	                String schema = platform.getWdkModel().getModelConfig().getAppDB().getLogin();
	                platform.disableStatistics(connection, schema, cacheTable);
	            } else {// insert result into existing cache table
	                instance.insertToCache(connection, cacheTable, instanceId);
	            }
	
	            // instance record is added after the cache is created to make sure
	            // if there is something wrong with the query, nothing was cached.
	            // check the id again, if it has been created, discard the newly
	            // inserted data and use the old one.
	            Integer newId = checkInstanceId(instance, queryInfo);
	            if (newId == null) {
	                String checksum = instance.getChecksum();
	                try {
	                    addCacheInstance(connection, queryInfo, instance,
	                            instanceId, checksum);
	                } catch (WdkModelException ex) {
	                    // the row must be inserted by other process at the moment.
	                    // If so, retrieve it; otherwise, throw error.
	                    newId = checkInstanceId(instance, queryInfo);
	                    if (newId == null) throw ex;
	                }
	            } else {
	                instanceId = newId;
	            }
	
	            connection.commit();
	            return instanceId;
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	            logger.error("Failed to create cache for query ["
	                    + instance.getQuery().getFullName() + "]");
	            connection.rollback();
	            throw ex;
	        } catch (WdkModelException ex) {
	            connection.rollback();
	            throw ex;
	        } finally {
	            connection.setAutoCommit(true);
	            SqlUtils.closeQuietly(connection);
	        }
    	}
    	catch (SQLException e) {
    		throw new WdkModelException(e);
    	}
    }

    public void createCacheTableIndex(Connection connection, String cacheTable,
            String[] indexColumns) throws WdkModelException {
        // resize the index columns first
        resizeIndexColumns(connection, cacheTable, indexColumns);
      
        // create index on query instance id
        StringBuffer sqlId = new StringBuffer("CREATE INDEX ");
        sqlId.append(cacheTable).append("_idx01 ON ").append(cacheTable);
        sqlId.append(" (").append(CacheFactory.COLUMN_INSTANCE_ID).append(")");

        // create index on other columns
        StringBuffer sqlOther = null;
        if (indexColumns != null) {
            sqlOther = new StringBuffer("CREATE INDEX ");
            sqlOther.append(cacheTable).append("_idx02 ON ").append(cacheTable);
            sqlOther.append(" (");
            boolean firstColumn = true;
            for (String column : indexColumns) {
                if (firstColumn) firstColumn = false;
                else sqlOther.append(", ");
                sqlOther.append(column);
            }
            sqlOther.append(")");
        }

        Statement stmt = null;
        try {
            long start = System.currentTimeMillis();
            stmt = connection.createStatement();

            stmt.execute(sqlId.toString());
            SqlUtils.verifyTime(wdkModel, sqlId.toString(), "wdk-create-cache-index01", start);
            if (indexColumns != null) {
                start = System.currentTimeMillis();
                stmt.execute(sqlOther.toString());
                SqlUtils.verifyTime(wdkModel, sqlOther.toString(), "wdk-create-cache-index02", start);
            }
        }
        catch (SQLException e) {
        	throw new WdkModelException(e);
        }
        finally {
            SqlUtils.closeQuietly(stmt);
        }
    }
    
    /**
     * Resize the index columns to the max size defined in the model config 
     * file. Please note, only varchar columns are resized.
     *  
     * @param connection
     * @param cacheTable
     * @param indexColumns
     * @throws SQLException 
     */
  private void resizeIndexColumns(Connection connection, String cacheTable,
      String[] indexColumns) throws WdkModelException {
    // get the type of the columns
    Statement statement = null;
    try {
      statement = connection.createStatement();
      ResultSet resultSet = statement.executeQuery("SELECT * FROM "
          + cacheTable);
      ResultSetMetaData metaData = resultSet.getMetaData();
      Map<String, Integer> columnSizes = new LinkedHashMap<>();
      for (int i = 1; i <= metaData.getColumnCount(); i++) {
        String column = metaData.getColumnName(i).toLowerCase();
        String type = metaData.getColumnTypeName(i).toLowerCase();
        if (type.contains("char"))
          columnSizes.put(column, metaData.getColumnDisplaySize(i));
      }
      metaData = null;
      resultSet.close();

      // resize columns
      int maxSize = wdkModel.getModelConfig().getAppDB().getMaxPkColumnWidth();
      for (String column : indexColumns) {
        column = column.toLowerCase();
        if (!columnSizes.containsKey(column)) continue;
//          throw new WdkModelException("The required index column '" + column
//              + "' doesn't exist in the cache table: " + cacheTable
//              + ". Please look up the query in 'Query' table that generates " +
//              "this cache table, and make sure it returns the index column.");

        
        String sql = platform.getResizeColumnSql(cacheTable, column, maxSize);
        statement.executeUpdate(sql);
      }
    } catch (SQLException ex) {
      throw new WdkModelException("Failed to alter the sizes of index columns"
          + " '" + Utilities.fromArray(indexColumns) + "' on cache table "
          + cacheTable + ". Please look up the query in 'Query' table that "
          + "creates this cache, and make sure the maxPkColumnWidth property "
          + "defined in the model-config.xml is the same or bigger than the "
          + "size of primary key columns from that query.", ex);
    } finally {
      try {
        if (statement != null) statement.close();
      } catch (SQLException ex) {
        throw new WdkModelException(ex);
      }
    }
  }
    
    private void addCacheInstance(Connection connection, QueryInfo queryInfo,
            QueryInstance instance, int instanceId, String checksum)
    		throws WdkModelException {
        StringBuffer sql = new StringBuffer("INSERT INTO ");
        sql.append(CacheFactory.TABLE_INSTANCE).append(" (");
        sql.append(CacheFactory.COLUMN_INSTANCE_ID).append(", ");
        sql.append(CacheFactory.COLUMN_QUERY_ID).append(", ");
        sql.append(CacheFactory.COLUMN_INSTANCE_CHECKSUM).append(", ");
        sql.append(CacheFactory.COLUMN_RESULT_MESSAGE);
        sql.append(") VALUES (?, ?, ?, ?)");

        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql.toString());
            ps.setInt(1, instanceId);
            ps.setInt(2, queryInfo.getQueryId());
            ps.setString(3, checksum);
            platform.setClobData(ps, 4, instance.getResultMessage(), false);
            ps.executeUpdate();
        }
        catch (SQLException e) {
        	throw new WdkModelException("Unable to add cache instance.", e);
        }
        finally {
            // close the statement manually, since we cannot close the
            // connection; it's not committed yet.
        	SqlUtils.closeQuietly(ps);
        }
    }
}
