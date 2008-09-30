/**
 * 
 */
package org.gusdb.wdk.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.gusdb.wdk.model.implementation.SqlUtils;

/**
 * @author: xingao
 * @created: Feb 20, 2007
 * @updated: Feb 20, 2007 The cache table provides a data access layer to each
 *           cache tables associated with an answer.
 */
public class CacheTable {

    // private Logger logger = Logger.getLogger(CacheTable.class);

    private RDBMSPlatformI platform;
    private String schema;
    private int queryInstanceId;
    private String cacheTableFullName;

    private String projectIdColumn;
    private String primaryKeyColumn;

    public CacheTable(RDBMSPlatformI platform, String schema,
            int queryInstanceId, String projectIdColumn, String primaryKeyColumn) {
        this.platform = platform;
        this.schema = schema;
        this.queryInstanceId = queryInstanceId;
        this.cacheTableFullName = platform.getTableFullName(schema,
                ResultFactory.CACHE_TABLE_PREFIX + queryInstanceId);
        if (projectIdColumn != null && projectIdColumn.length() == 0) this.projectIdColumn = null;
        else this.projectIdColumn = projectIdColumn;
        this.primaryKeyColumn = primaryKeyColumn;
    }

    public String getCacheTableFullName() {
        return cacheTableFullName;
    }

    /**
     * @return the primaryKeyColumn
     */
    public String getPrimaryKeyColumn() {
        return primaryKeyColumn;
    }

    /**
     * @return the projectIdColumn
     */
    public String getProjectIdColumn() {
        return projectIdColumn;
    }

    /**
     * @param sortingColumnMap
     *            a map of <[TableFullName, ColumnName], Ascending>
     * @return
     * @throws WdkModelException
     */
    public int getSortingIndex(Set<SortingColumn> sortingColumns)
            throws WdkModelException {
        // determine sorting level
        StringBuffer sbColumns = new StringBuffer();

        // if there is no sorting column, use the default index id, 0
        if (sortingColumns.size() == 0) return 0;

        for (SortingColumn column : sortingColumns) {
            if (sbColumns.length() > 0) sbColumns.append(", ");
            String columnName = column.getColumnName();
            boolean ascending = column.isAscending();

            sbColumns.append(columnName);
            sbColumns.append(ascending ? " ASC" : " DESC");
        }

        // check if the index id exists
        String sql = "SELECT " + ResultFactory.COLUMN_SORTING_INDEX + " FROM "
                + schema + "." + ResultFactory.TABLE_SORTING_INDEX + " WHERE "
                + ResultFactory.COLUMN_QUERY_INSTANCE_ID + " = ? AND "
                + ResultFactory.COLUMN_SORTING_COLUMNS + " = ?";
        ResultSet rsIndex = null;
        int sortingIndex = 0;
        try {
            PreparedStatement psIndex = SqlUtils.getPreparedStatement(
                    platform.getDataSource(), sql);
            psIndex.setInt(1, queryInstanceId);
            psIndex.setString(2, sbColumns.toString());
            rsIndex = psIndex.executeQuery();
            if (rsIndex.next()) { // index exist
                sortingIndex = rsIndex.getInt(ResultFactory.COLUMN_SORTING_INDEX);
            } else { // index doesn't exist, create it along with sorting
                // cache
                sortingIndex = createSortingIndex(sortingColumns,
                        sbColumns.toString());
            }
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        } finally {
            try {
                SqlUtils.closeResultSet(rsIndex);
            } catch (SQLException ex) {
                throw new WdkModelException(ex);
            }
        }
        return sortingIndex;
    }

    private int createSortingIndex(Set<SortingColumn> sortingColumns,
            String columns) throws SQLException, WdkModelException {
        DataSource dataSource = platform.getDataSource();

        // get next sorting index
        int sortingIndex = Integer.parseInt(platform.getNextId(schema,
                ResultFactory.TABLE_SORTING_INDEX));
        String indexTableFullName = platform.getTableFullName(schema,
                ResultFactory.TABLE_SORTING_INDEX);

        // construct SQL for inserting the sorting index record
        StringBuffer sbIndex = new StringBuffer();
        sbIndex.append("INSERT INTO " + indexTableFullName + "(");
        sbIndex.append(ResultFactory.COLUMN_SORTING_INDEX + ", ");
        sbIndex.append(ResultFactory.COLUMN_QUERY_INSTANCE_ID + ", ");
        sbIndex.append(ResultFactory.COLUMN_SORTING_COLUMNS);
        sbIndex.append(") VALUES (?, ?, ?)");

        // construct SQl for getting the sorted results
        // get the sorted result
        StringBuffer sbSorting = new StringBuffer("SELECT ");
        if (projectIdColumn != null)
            sbSorting.append(cacheTableFullName + "." + projectIdColumn + ", ");
        sbSorting.append(cacheTableFullName + "." + primaryKeyColumn);
        sbSorting.append(" FROM " + cacheTableFullName);

        Map<String, String> tableMap = new LinkedHashMap<String, String>();
        tableMap.put(cacheTableFullName, cacheTableFullName);

        // use default sorting (=0) as template
        StringBuffer sbWhere = new StringBuffer(" WHERE ");
        sbWhere.append(cacheTableFullName + "."
                + ResultFactory.COLUMN_SORTING_INDEX + " = 0 ");
        StringBuffer sbOrder = new StringBuffer();
        int columnCount = 0;
        for (SortingColumn column : sortingColumns) {
            String tableName = column.getTableName();
            String columnName = column.getColumnName();
            boolean ascending = column.isAscending();
            boolean lowerCase = column.isLowerCase();

            String newTableName;
            boolean skipTable = false;
            if (tableMap.containsKey(tableName)) {
                newTableName = tableMap.get(tableName);
                skipTable = true;
            } else {
                newTableName = "sort_" + columnCount;
                tableMap.put(tableName, newTableName);
            }
            if (!skipTable) {
                // add to from clause
                sbSorting.append(", " + tableName + " " + newTableName);

                // add to where clause
                if (projectIdColumn != null) {
                    sbWhere.append(" AND " + cacheTableFullName + "."
                            + projectIdColumn + " = " + newTableName + "."
                            + projectIdColumn);
                }
                sbWhere.append(" AND " + cacheTableFullName + "."
                        + primaryKeyColumn + " = " + newTableName + "."
                        + primaryKeyColumn);
            }

            // add to order by clause
            sbOrder.append((sbOrder.length() == 0) ? " ORDER BY " : ", ");
            String fullColumnName = newTableName + "." + columnName;
            if (lowerCase) {
                sbOrder.append("LOWER(" + fullColumnName + ")");
            } else sbOrder.append(fullColumnName);
            sbOrder.append(ascending ? " ASC" : " DESC");

            columnCount++;
        }
        sbSorting.append(sbWhere);
        sbSorting.append(sbOrder);

        // construct SQL for inserting sorted result into cache table
        StringBuffer sbInsertCache = new StringBuffer("INSERT INTO ");
        sbInsertCache.append(cacheTableFullName + "(");
        sbInsertCache.append(ResultFactory.RESULT_TABLE_I + ", ");
        sbInsertCache.append(primaryKeyColumn + ", ");
        if (projectIdColumn != null)
            sbInsertCache.append(projectIdColumn + ", ");
        sbInsertCache.append(ResultFactory.COLUMN_SORTING_INDEX);
        if (projectIdColumn != null) sbInsertCache.append(") VALUES (?, ?, ?, ?)");
        else sbInsertCache.append(") VALUES (?, ?, ?)");

        // start executing queries
        PreparedStatement psIndex = null;
        ResultSet rsCache = null;
        PreparedStatement psCache = null;
        Connection connection = dataSource.getConnection();
        synchronized (connection) {
            // start a transaction for it, and since the attribute of the
            // connection is set, need to make sure this connection is used
            // by only one thread to avoid undesired result
            connection.setAutoCommit(false);

            try {
                // insert a sorting index record
                psIndex = connection.prepareStatement(sbIndex.toString());
                psIndex.setInt(1, sortingIndex);
                psIndex.setInt(2, queryInstanceId);
                psIndex.setString(3, columns);
                psIndex.execute();

                // TEST
                // logger.info("get sorting result: " + sbSorting.toString());

                // get sorted result
                Statement stmt = connection.createStatement();
                rsCache = stmt.executeQuery(sbSorting.toString());

                // insert sorted result into cache table
                psCache = connection.prepareStatement(sbInsertCache.toString());
                int resultCount = 1;
                while (rsCache.next()) {
                    psCache.setInt(1, resultCount);
                    psCache.setString(2, rsCache.getString(primaryKeyColumn));
                    if (projectIdColumn != null) {
                        psCache.setString(3, rsCache.getString(projectIdColumn));
                        psCache.setInt(4, sortingIndex);
                    } else {
                        psCache.setInt(3, sortingIndex);
                    }
                    psCache.addBatch();
                    resultCount++;
                    if (resultCount % 1000 == 0) psCache.executeBatch();
                }
                psCache.executeBatch();

                // verify the insertion of the sorted cache
                // get the size of the result
                int cacheSize = SqlUtils.runIntegerQuery(dataSource, "SELECT "
                        + "count(*) FROM " + cacheTableFullName + " WHERE "
                        + ResultFactory.COLUMN_SORTING_INDEX + " = 0");
                if (cacheSize != (resultCount - 1)) {
                    StringBuffer error = new StringBuffer();
                    error.append("Not all cached records are sorted. Cached: ");
                    error.append(cacheSize);
                    error.append(", sorted: ");
                    error.append(resultCount - 1);
                    error.append(System.getProperty("line.separator"));
                    error.append(sbSorting);
                    throw new WdkModelException(error.toString());
                }
                connection.commit();
            } finally {
                connection.setAutoCommit(true);

                SqlUtils.closeStatement(psIndex);
                SqlUtils.closeResultSet(rsCache);
                // no need to close the statement, since the connection has been
                // closed
                // SqlUtils.closeStatement(psCache);
            }
        }
        return sortingIndex;
    }
}
