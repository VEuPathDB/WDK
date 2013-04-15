/**
 * 
 */
package org.gusdb.wdk.model.dbms;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import oracle.jdbc.driver.OracleDriver;
import oracle.sql.CLOB;

import org.apache.commons.dbcp.DelegatingConnection;
import org.gusdb.wdk.model.WdkModelException;

/**
 * @author Jerric Gao
 * 
 */
public class Oracle extends DBPlatform {

    public Oracle() throws ClassNotFoundException, SQLException {
        super("SELECT 'ok' FROM dual");
        // register the driver
        Class.forName("oracle.jdbc.driver.OracleDriver");

        DriverManager.registerDriver(new OracleDriver());
        System.setProperty("jdbc.drivers", "oracle.jdbc.driver.OracleDriver");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#createSequence(java.lang.String,
     * int, int)
     */
    @Override
    public void createSequence(String sequence, int start, int increment)
            throws WdkModelException {
        StringBuffer sql = new StringBuffer("CREATE SEQUENCE ");
        sql.append(sequence);
        sql.append(" START WITH ").append(start);
        sql.append(" INCREMENT BY ").append(increment);
        SqlUtils.executeUpdate(wdkModel, dataSource, sql.toString(),
                "wdk-create-sequence");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#getBooleanDataType()
     */
    @Override
    public String getBooleanDataType() {
        return "NUMBER(1)";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#getNumberDataType(int)
     */
    @Override
    public String getNumberDataType(int size) {
        return "NUMBER(" + size + ")";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#getStringDataType(int)
     */
    @Override
    public String getStringDataType(int size) {
        return "VARCHAR(" + size + ")";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#getClobDataType()
     */
    @Override
    public String getClobDataType() {
        return "CLOB";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#getMinusOperator()
     */
    @Override
    public String getMinusOperator() {
        return "MINUS";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#getNextId(java.lang.String,
     * java.lang.String)
     */
    @Override
    public int getNextId(String schema, String table) throws WdkModelException {
		schema = normalizeSchema(schema);

		StringBuffer sql = new StringBuffer("SELECT ");
		sql.append(schema).append(table).append(ID_SEQUENCE_SUFFIX);
		sql.append(".nextval FROM dual");
		BigDecimal id = (BigDecimal) SqlUtils.executeScalar(wdkModel,
            dataSource, sql.toString(), "wdk-select-next-id");
		return id.intValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.dbms.DBPlatform#getNextIdSqlExpression(java.lang.
     * String, java.lang.String)
     */
    @Override
    public String getNextIdSqlExpression(String schema, String table) {
        schema = normalizeSchema(schema);

        StringBuffer sql = new StringBuffer("");
        sql.append(schema).append(table).append(ID_SEQUENCE_SUFFIX);
        sql.append(".nextval");
        return sql.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#getClobData(java.sql.ResultSet,
     * java.lang.String)
     */
    @Override
    public String getClobData(ResultSet rs, String columnName)
            throws SQLException {
        Clob messageClob = rs.getClob(columnName);
        if (messageClob == null) return null;
        return messageClob.getSubString(1, (int) messageClob.length());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.dbms.DBPlatform#updateClobData(java.sql.PreparedStatement
     * , int, java.lang.String, boolean)
     */
    @Override
    public int setClobData(PreparedStatement ps, int columnIndex,
            String content, boolean commit) throws SQLException {
        Connection connection = ((DelegatingConnection) ps.getConnection()).getInnermostDelegate();
        CLOB clob = CLOB.createTemporary(connection, false,
                CLOB.DURATION_SESSION);
        clob.setString(1, content);
        ps.setClob(columnIndex, clob);
        return commit ? ps.executeUpdate() : 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#getPagedSql(java.lang.String,
     * int, int)
     */
    @Override
    public String getPagedSql(String sql, int startIndex, int endIndex) {
        StringBuffer buffer = new StringBuffer();
        // construct the outer query
        buffer.append("SELECT lb.* FROM (");
        // construct the inner nested query
        buffer.append("SELECT ub.*, rownum AS row_index FROM (");
        buffer.append(sql);
        buffer.append(") ub WHERE rownum <= ").append(endIndex);
        buffer.append(") lb WHERE lb.row_index >= ").append(startIndex);
        return buffer.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#isTableExist(java.lang.String)
     */
    @Override
    public boolean checkTableExists(String schema, String tableName)
            throws WdkModelException {
        StringBuffer sql = new StringBuffer("SELECT count(*) FROM ALL_TABLES ");
        sql.append("WHERE table_name = '");
        sql.append(tableName.toUpperCase()).append("'");

        if (schema == null) schema = defaultSchema;
        if (schema.charAt(schema.length() - 1) == '.')
            schema = schema.substring(0, schema.length() - 1);
        sql.append(" AND owner = '").append(schema.toUpperCase()).append("'");

        BigDecimal count = (BigDecimal) SqlUtils.executeScalar(wdkModel,
                dataSource, sql.toString(), "wdk-check-table-exist");
        return (count.longValue() > 0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#getDateDataType()
     */
    @Override
    public String getDateDataType() {
        return "TIMESTAMP";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#getFloatDataType(int)
     */
    @Override
    public String getFloatDataType(int size) {
        return "FLOAT(" + size + ")";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#convertBoolean(boolean)
     */
    @Override
    public String convertBoolean(boolean value) {
        return value ? "1" : "0";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#dropTable(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void dropTable(String schema, String table, boolean purge)
            throws WdkModelException {
        String name = "wdk-drop-table-" + table;
        String sql = "DROP TABLE ";
        if (schema != null) sql = schema;
        sql += table;
        if (purge) {
            sql += " PURGE";
            name += "_purge";
        }
        SqlUtils.executeUpdate(wdkModel, dataSource, sql, name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.dbms.DBPlatform#disableStatistics(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void disableStatistics(Connection connection, String schema,
            String tableName) throws WdkModelException {
        schema = schema.toUpperCase();
        tableName = tableName.toUpperCase();
        CallableStatement stUnlock = null, stDelete = null, stLock = null;
        try {
            stUnlock = connection.prepareCall(tableName);
            stUnlock.executeUpdate("{call DBMS_STATS.unlock_table_stats('"
                    + schema + "', '" + tableName + "') }");
            stUnlock.executeUpdate();

            stDelete = connection.prepareCall(tableName);
            stDelete.executeUpdate("{call DBMS_STATS.DELETE_TABLE_STATS('"
                    + schema + "', '" + tableName + "') }");
            stDelete.executeUpdate();

            stLock = connection.prepareCall(tableName);
            stLock.executeUpdate("{call DBMS_STATS.LOCK_TABLE_STATS('" + schema
                    + "', '" + tableName + "') }");
            stLock.executeUpdate();
        }
        catch (SQLException e) {
        	throw new WdkModelException("Unable to disable statistics on table " + schema + "." + tableName, e);
        }
        finally {
        	SqlUtils.closeQuietly(stUnlock, stDelete, stLock);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#getTables(java.lang.String,
     * java.lang.String)
     */
    @Override
    public String[] queryTableNames(String schema, String pattern)
            throws WdkModelException {
        String sql = "SELECT table_name FROM all_tables WHERE owner = '"
                + schema.toUpperCase() + "' AND table_name LIKE '"
                + pattern.toUpperCase() + "'";
        ResultSet resultSet = null;
        try {
            resultSet = SqlUtils.executeQuery(wdkModel, dataSource, sql,
                    "wdk-oracle-select-table-names");
            List<String> tables = new ArrayList<String>();
            while (resultSet.next()) {
                tables.add(resultSet.getString("table_name"));
            }
            String[] array = new String[tables.size()];
            tables.toArray(array);
            return array;
        } catch (SQLException e) {
        	throw new WdkModelException("Could not query table names from schema [ " + schema + " ]", e);
        } finally {
            SqlUtils.closeResultSetAndStatement(resultSet);
        }
    }

    @Override
    public String getDummyTable() {
        return " FROM dual";
    }

}
