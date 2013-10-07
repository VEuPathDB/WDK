package org.gusdb.wdk.model.user;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.gusdb.fgputil.db.QueryLogger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

public class QueryFactory {

    private static final String TABLE_CLOB_VALUES = "clob_values";

    private static final String COLUMN_CLOB_CHECKSUM = "clob_checksum";
    private static final String COLUMN_CLOB_VALUE = "clob_value";

    // private static final Logger logger = Logger.getLogger( QueryFactory.class
    // );

    private WdkModel wdkModel;
    private String wdkSchema;
    private DatabaseInstance userDb;
    private DataSource dataSource;

    public QueryFactory(WdkModel wdkModel) {
        this.wdkModel = wdkModel;
        this.userDb = this.wdkModel.getUserDb();
        this.dataSource = userDb.getDataSource();
        this.wdkSchema = wdkModel.getModelConfig().getUserDB().getWdkEngineSchema();
    }

    public String makeSummaryChecksum(String[] summaryAttributes)
            throws WdkModelException {
        Set<String> usedAttributes = new HashSet<String>();
        // create checksum for config columns
        StringBuffer sb = new StringBuffer();
        for (String attribute : summaryAttributes) {
            // skip redundant attributes
            if (usedAttributes.contains(attribute)) continue;

            if (sb.length() > 0) sb.append(", ");
            sb.append(attribute);
            usedAttributes.add(attribute);
        }
        String summaryContent = sb.toString();
        // System.err.println("saving summary: " + summaryContent);
        String checksum = Utilities.encrypt(summaryContent);

        // check if the configuration exists
        PreparedStatement psInsert = null;
        try {
            if (null != getSummaryAttributes(checksum)) return checksum;

            // configuration not exists, add one
            String sql = "INSERT INTO" + " " + wdkSchema + TABLE_CLOB_VALUES
                    + " (" + COLUMN_CLOB_CHECKSUM + ", " + COLUMN_CLOB_VALUE
                    + ") VALUES (?, ?)";
            long start = System.currentTimeMillis();
            psInsert = SqlUtils.getPreparedStatement(dataSource, sql);
            psInsert.setString(1, checksum);
            psInsert.setString(2, summaryContent);
            psInsert.execute();
            QueryLogger.logEndStatementExecution(sql, "wdk-query-factory-insert-clob",
                    start);
            return checksum;
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        } finally {
            SqlUtils.closeStatement(psInsert);
        }
    }

    public String[] getSummaryAttributes(String summaryChecksum)
            throws WdkModelException {
        ResultSet rsSelect = null;
        try {
            String sql = "SELECT " + COLUMN_CLOB_VALUE + " FROM " + wdkSchema
                    + TABLE_CLOB_VALUES + " WHERE " + COLUMN_CLOB_CHECKSUM
                    + " = ?";
            long start = System.currentTimeMillis();
            PreparedStatement psSelect = SqlUtils.getPreparedStatement(
                    dataSource, sql);
            psSelect.setString(1, summaryChecksum);
            rsSelect = psSelect.executeQuery();
            QueryLogger.logEndStatementExecution(sql, "wdk-query-factory-select-clob",
                    start);
            if (!rsSelect.next()) return null;

            // get the configuration
            String summaryContent = rsSelect.getString(COLUMN_CLOB_VALUE);
            // System.err.println("getting summary: " + summaryContent);

            String[] attributes = summaryContent.split(",");
            for (int i = 0; i < attributes.length; i++) {
                attributes[i] = attributes[i].trim();
            }
            return attributes;
        } catch (SQLException ex) {
            throw new WdkModelException("Unable to get summary attributes for checksum " + summaryChecksum, ex);
        } finally {
            SqlUtils.closeResultSetAndStatement(rsSelect);
        }
    }

    public String makeSortingChecksum(Map<String, Boolean> columns) throws WdkModelException {
        // create checksum for sorting columns
        StringBuffer sb = new StringBuffer();
        int i = 0;
        for (String colName : columns.keySet()) {
            // only use a limit number of attribtues in sorting
            if (i >= Utilities.SORTING_LEVEL) break;

            boolean ascend = columns.get(colName);
            if (sb.length() > 0) sb.append(", ");
            sb.append(colName);
            sb.append(ascend ? " ASC" : " DESC");
            i++;
        }
        String columnsContent = sb.toString();
        String checksum = Utilities.encrypt(columnsContent);

        // check if the sorting exists
        PreparedStatement psInsert = null;
        try {
            if (null != getSortingAttributes(checksum)) return checksum;

            // sorting not exists, add one
            String sql = "INSERT INTO" + " " + wdkSchema + TABLE_CLOB_VALUES
                    + " (" + COLUMN_CLOB_CHECKSUM + ", " + COLUMN_CLOB_VALUE
                    + ") VALUES (?, ?)";
            long start = System.currentTimeMillis();
            psInsert = SqlUtils.getPreparedStatement(dataSource, sql);
            psInsert.setString(1, checksum);
            psInsert.setString(2, columnsContent);
            psInsert.execute();
            QueryLogger.logEndStatementExecution(sql, "wdk-query-factory-insert-clob",
                    start);
            return checksum;
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        } finally {
            SqlUtils.closeStatement(psInsert);
        }
    }

    public Map<String, Boolean> getSortingAttributes(String sortingChecksum)
            throws WdkModelException {
        ResultSet rsSelect = null;
        try {
            String sql = "SELECT " + COLUMN_CLOB_VALUE + " FROM " + wdkSchema
                    + TABLE_CLOB_VALUES + " WHERE " + COLUMN_CLOB_CHECKSUM
                    + " = ?";
            long start = System.currentTimeMillis();
            PreparedStatement psSelect = SqlUtils.getPreparedStatement(
                    dataSource, sql);
            psSelect.setString(1, sortingChecksum);
            rsSelect = psSelect.executeQuery();
            QueryLogger.logEndStatementExecution(sql, "wdk-query-factory-select-clob",
                    start);

            if (!rsSelect.next()) return null;

            // get the sorting attributes
            String sortingContent = rsSelect.getString(COLUMN_CLOB_VALUE);
            String[] sortingPairs = sortingContent.split(",");
            Map<String, Boolean> attributes = new LinkedHashMap<String, Boolean>();
            for (String sortingPair : sortingPairs) {
                String[] pair = sortingPair.trim().split("\\s+");

                // validate the format of the pair
                if (pair.length != 2
                        || (!"ASC".equalsIgnoreCase(pair[1]) && !"DESC".equalsIgnoreCase(pair[1])))
                    throw new WdkModelException(
                            "Invalid sorting attribute format: '" + sortingPair
                                    + "'");
                String attribute = pair[0];
                boolean ascend = pair[1].equalsIgnoreCase("asc");
                attributes.put(attribute, ascend);
            }
            return attributes;
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        } finally {
            SqlUtils.closeResultSetAndStatement(rsSelect);
        }
    }

    public String makeClobChecksum(String paramValue) throws WdkModelException {
        // make the checksum
        String checksum = Utilities.encrypt(paramValue);

        PreparedStatement psInsert = null;
        try {
            // get the clob with the new checksum
            if (null != getClobValue(checksum)) return checksum;

            // clob value does not exist, add one
            String sql = "INSERT INTO" + " " + wdkSchema + TABLE_CLOB_VALUES
                    + " (" + COLUMN_CLOB_CHECKSUM + ", " + COLUMN_CLOB_VALUE
                    + ") VALUES (?, ?)";
            long start = System.currentTimeMillis();
            psInsert = SqlUtils.getPreparedStatement(dataSource, sql);
            psInsert.setString(1, checksum);
            psInsert.setString(2, paramValue);
            psInsert.execute();
            QueryLogger.logEndStatementExecution(sql, "wdk-query-factory-insert-clob",
                    start);

            return checksum;
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        } finally {
            SqlUtils.closeStatement(psInsert);
        }
    }

    public String getClobValue(String paramChecksum) throws WdkModelException {
        ResultSet rs = null;
        try {
            long start = System.currentTimeMillis();
            String sql = "SELECT " + COLUMN_CLOB_VALUE + " FROM " + wdkSchema
                    + TABLE_CLOB_VALUES + " WHERE " + COLUMN_CLOB_CHECKSUM
                    + " = ?";
            PreparedStatement ps = SqlUtils.getPreparedStatement(dataSource,
                    sql);
            ps.setString(1, paramChecksum);
            rs = ps.executeQuery();
            QueryLogger.logEndStatementExecution(sql, "wdk-query-factory-select-clob",
                    start);
            if (!rs.next()) return null;

            String clobValue = userDb.getPlatform().getClobData(rs, COLUMN_CLOB_VALUE);
            return clobValue;
        } catch (SQLException ex) {
            throw new WdkModelException(ex);
        } finally {
            SqlUtils.closeResultSetAndStatement(rs);
        }

    }
}
