/**
 * 
 */
package org.gusdb.wdk.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.implementation.SqlUtils;

/**
 * @author: xingao
 * @created: Feb 20, 2007
 * @updated: Feb 20, 2007
 * 
 * The cache table provides a data access layer to each cache tables associated
 * with an answer.
 */
public class CacheTable {

    public static final int SORTING_LEVEL = 3;

    private Logger logger = Logger.getLogger(CacheTable.class);

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
     * @param sortingColumnMap a map of <[TableFullName, ColumnName], Ascending>
     * @return
     * @throws WdkModelException
     */
    public int getSortingIndex(Map<String[], Boolean> sortingColumnMap)
            throws WdkModelException {
        // determine sorting level
        StringBuffer sbColumns = new StringBuffer();

        // if there is now sorting column, use the default index id, 0
        if (sortingColumnMap.size() == 0) return 0;

        int columnCount = 0;
        for (String[] fullColumn : sortingColumnMap.keySet()) {
            if (sbColumns.length() > 0) sbColumns.append(", ");
            String column = fullColumn[1];
            boolean ascending = sortingColumnMap.get(fullColumn);

            sbColumns.append(column);
            sbColumns.append(ascending ? " ASC" : " DESC");

            // only perform on predefined level
            columnCount++;
            if (columnCount >= SORTING_LEVEL) break;
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
                sortingIndex = createSortingIndex(sortingColumnMap,
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

    private int createSortingIndex(Map<String[], Boolean> sortingColumnMap,
            String columns) throws SQLException {
        // get next sorting index
        int sortingIndex = Integer.parseInt(platform.getNextId(schema,
                ResultFactory.TABLE_SORTING_INDEX));
        String indexTableFullName = platform.getTableFullName(schema,
                ResultFactory.TABLE_SORTING_INDEX);
        DataSource dataSource = platform.getDataSource();

        PreparedStatement psIndex = null;
        ResultSet rsCache = null;
        PreparedStatement psCache = null;

        // insert the index record
        StringBuffer sbIndex = new StringBuffer();
        sbIndex.append("INSERT INTO " + indexTableFullName + "(");
        sbIndex.append(ResultFactory.COLUMN_SORTING_INDEX + ", ");
        sbIndex.append(ResultFactory.COLUMN_QUERY_INSTANCE_ID + ", ");
        sbIndex.append(ResultFactory.COLUMN_SORTING_COLUMNS);
        sbIndex.append(") VALUES (?, ?, ?)");
        try {
            psIndex = SqlUtils.getPreparedStatement(dataSource,
                    sbIndex.toString());
            psIndex.setInt(1, sortingIndex);
            psIndex.setInt(2, queryInstanceId);
            psIndex.setString(3, columns);
            psIndex.execute();

            // get the sorted result
            StringBuffer sbSorting = new StringBuffer("SELECT ");
            if (projectIdColumn != null)
                sbSorting.append(cacheTableFullName + "." + projectIdColumn
                        + ", ");
            sbSorting.append(cacheTableFullName + "." + primaryKeyColumn);
            sbSorting.append(" FROM " + cacheTableFullName);

            // use default sorting (=0) as template
            StringBuffer sbWhere = new StringBuffer(" WHERE ");
            sbWhere.append(ResultFactory.COLUMN_SORTING_INDEX + " = 0 ");
            StringBuffer sbOrder = new StringBuffer();
            Map<String, String> tableMap = new LinkedHashMap<String, String>();
            int columnCount = 0;
            for (String[] fullColumn : sortingColumnMap.keySet()) {
                String tableName = fullColumn[0];
                String columnName = fullColumn[1];
                boolean ascending = sortingColumnMap.get(fullColumn);

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
                sbOrder.append((sbOrder.length() == 0)?" ORDER BY " : ", ");
                sbOrder.append(newTableName + "." + columnName);
                sbOrder.append(ascending ? " ASC" : " DESC");

                columnCount++;
                // only perform on predefined level
                if (columnCount >= SORTING_LEVEL) break;
            }
            sbSorting.append(sbWhere);
            sbSorting.append(sbOrder);

            // TEST
            logger.info("get sorting result: " + sbSorting.toString());

            rsCache = SqlUtils.getResultSet(dataSource, sbSorting.toString());

            // insert into the cache with new sorted order
            StringBuffer sbInsertCache = new StringBuffer("INSERT INTO ");
            sbInsertCache.append(cacheTableFullName + "(");
            sbInsertCache.append(ResultFactory.RESULT_TABLE_I + ", ");
            sbInsertCache.append(primaryKeyColumn + ", ");
            if (projectIdColumn != null)
                sbInsertCache.append(projectIdColumn + ", ");
            sbInsertCache.append(ResultFactory.COLUMN_SORTING_INDEX);
            if (projectIdColumn != null) sbInsertCache.append(") VALUES (?, ?, ?, ?)");
            else sbInsertCache.append(") VALUES (?, ?, ?)");
            psCache = SqlUtils.getPreparedStatement(dataSource,
                    sbInsertCache.toString());
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
            }
            psCache.executeBatch();
        } finally {
            SqlUtils.closeStatement(psIndex);
            SqlUtils.closeResultSet(rsCache);
            SqlUtils.closeStatement(psCache);
        }
        return sortingIndex;
    }
}
