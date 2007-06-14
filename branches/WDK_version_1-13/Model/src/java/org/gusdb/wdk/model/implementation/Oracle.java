package org.gusdb.wdk.model.implementation;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import java.sql.Clob;
import oracle.sql.CLOB;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DelegatingConnection;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.ResultFactory;
import org.gusdb.wdk.model.WdkLogManager;
import org.gusdb.wdk.model.WdkModelException;

/**
 * An implementation of RDBMSPlatformI for Oracle 8i.
 * 
 * @author Steve Fischer
 * @version $Revision$ $Date: 2006-04-21 10:19:13 -0400 (Fri, 21 Apr
 *          2006) $ $Author$
 */
public class Oracle implements RDBMSPlatformI, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1097981172046682268L;

    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.model.implementation.Oracle");

    private DataSource dataSource;
    private GenericObjectPool connectionPool;

    public Oracle() {}

    public DataSource getDataSource() {

        return dataSource;
    }

    public String getTableFullName(String schemaName, String tableName) {
        return schemaName + "." + tableName;
    }

    public String getTableAliasAs() {
        return "";
    }

    public String getNextId(String schemaName, String tableName)
            throws SQLException {
        // remove the ending dot, if necessary
        int len = schemaName.length() - 1;
        if (schemaName.charAt(len) == '.')
            schemaName = schemaName.substring(0, len);

        String sql = "select " + schemaName + "." + tableName
                + "_pkseq.nextval from dual";
        String nextId = SqlUtils.runStringQuery(dataSource, sql);
        logger.finest("getNextId is: " + nextId + " after running " + sql);
        return nextId;
    }

    public String cleanStringValue(String val) {
        return val.replaceAll("'", "''");
    }

    public String getCurrentDateFunction() {
        return "sysdate";
    }

    public String getNumberDataType() {
        return "number";
    }

    public String getClobDataType() {
        return "clob";
    }

    public boolean checkTableExists(String tableName) throws SQLException {

        String[] parts = tableName.split("\\.");
        String owner = parts[0];
        String realTableName = parts[1];

        String sql = "select owner, table_name from all_tables where owner='"
                + owner.toUpperCase() + "' and table_name='"
                + realTableName.toUpperCase() + "'";

        String result = SqlUtils.runStringQuery(dataSource, sql);

        boolean tableExists = result == null ? false : true;
        return tableExists;
    }

    public void createSequence(String sequenceName, int start, int increment)
            throws SQLException {

        String sql = "create sequence " + sequenceName + " start with " + start
                + " increment by " + increment;
        SqlUtils.execute(dataSource, sql);
    }

    public void dropSequence(String sequenceName) throws SQLException {

        String sql = "drop sequence " + sequenceName;
        SqlUtils.execute(dataSource, sql);
    }

    /**
     * @return count of removed rows
     */
    public int dropTable(String fullTableName) throws SQLException {
        // String sql = "truncate table " + fullTableName;
        // SqlUtils.executeUpdate(dataSource, sql);

        // String sql = "drop table " + fullTableName + " purge";
        String sql = "drop table " + fullTableName;

        return SqlUtils.executeUpdate(dataSource, sql);
    }

    /**
     * Write the output of a query into a table, to which will be added a column
     * "i" numbering the rows.
     */
    public void createResultTable(DataSource dataSource, String tableName,
            String sql) throws SQLException {

        // Initialize the table with the results of <code>sql</code>
        String newSql = "create table " + tableName + " as " + sql;

        SqlUtils.execute(dataSource, newSql);

        addIndexColumn(dataSource, tableName);

    }

    public void addIndexColumn(DataSource dataSource, String tableName)
            throws SQLException {
        // Add "i" to the table and initialize each row in that column to be
        // rownum
        String alterSql = "alter table " + tableName + " add ("
                + ResultFactory.RESULT_TABLE_I + " number(12), "
                + ResultFactory.COLUMN_SORTING_INDEX + " NUMBER(12))";

        SqlUtils.execute(dataSource, alterSql);

        // initialize sorting index as 0
        String rownumSql = "update " + tableName + " set "
                + ResultFactory.RESULT_TABLE_I + " = rownum, "
                + ResultFactory.COLUMN_SORTING_INDEX + " = 0"; 
        SqlUtils.execute(dataSource, rownumSql);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.RDBMSPlatformI#createDataSource(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void init(String url, String user, String password, Integer minIdle,
            Integer maxIdle, Integer maxWait, Integer maxActive,
            Integer initialSize, String fileName) throws WdkModelException {

        try {
            // this is required for oci driver to work under tomcat
            Class.forName("oracle.jdbc.driver.OracleDriver");
            // DriverManager.registerDriver(new
            // oracle.jdbc.driver.OracleDriver());
            // System.setProperty("jdbc.drivers","oracle.jdbc.driver.OracleDriver");
            this.connectionPool = new GenericObjectPool(null);

            ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
                    url, user, password);

            String validationQuery = "select * from dual";
            
            // create abandoned configuration
            new PoolableConnectionFactory(connectionFactory, connectionPool,
                    null, validationQuery, false, true);

            PoolingDataSource dataSource = new PoolingDataSource(connectionPool);
            dataSource.setAccessToUnderlyingConnectionAllowed(true);
            this.dataSource = dataSource;
            Connection connection = dataSource.getConnection();
            connection.close();
        } catch (SQLException sqle) {
            throw new WdkModelException(
                    "\n\n*************ERROR***********\nCould not connect to database.\nIt is possible that you are using an incorrect url for connecting to the database or that your login or password is incorrect.\nPlease check "
                            + fileName
                            + " and make sure all information provided there is valid.\n(This is the most likely cause of the error; note it could be something else)\n\n",
                    sqle);

        } catch (ClassNotFoundException cnfe) {
            throw new WdkModelException(
                    "not able to find class for oracle.jdbc.driver.OracleDriver",
                    cnfe);
        }
        connectionPool.setMaxWait(maxWait.intValue());
        connectionPool.setMaxIdle(maxIdle.intValue());
        connectionPool.setMinIdle(minIdle.intValue());
        connectionPool.setMaxActive(maxActive.intValue());
        // no initial size yet

        // configure validationQuery tests
        connectionPool.setTestOnBorrow(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.RDBMSPlatformI#close()
     */
    public void close() throws WdkModelException {
        try {
            connectionPool.close();
        } catch (Exception exp) {
            throw new WdkModelException(exp);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.RDBMSPlatformI#getMinus()
     */
    public String getMinus() {
        // TODO Auto-generated method stub
        return "MINUS";
    }

    public int getActiveCount() {
        return connectionPool.getNumActive();
    }

    public int getIdleCount() {
        return connectionPool.getNumIdle();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.RDBMSPlatformI#getTableCount(java.lang.String)
     */
    public int getTableCount(String tableNamePattern) throws SQLException {
        String sql = "SELECT count(*) FROM user_tables WHERE table_name like '"
                + tableNamePattern + "'";
        return SqlUtils.runIntegerQuery(dataSource, sql);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.RDBMSPlatformI#forceDropTables(java.lang.String)
     */
    public int forceDropTables(String tableNamePattern) throws SQLException {
        // select table names matched with the pattern
        ResultSet rs = SqlUtils.getResultSet(dataSource, "SELECT table_name "
                + "from user_tables WHERE table_name like '" + tableNamePattern
                + "'");
        // drop each table of the matched name
        int count = 0;
        while (rs.next()) {
            String tableName = rs.getString("table_name");
            dropTable(tableName);
            count++;
        }
        return count;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.RDBMSPlatformI#updateClobData(java.sql.PreparedStatement,
     *      java.lang.String)
     */
    public int updateClobData(PreparedStatement ps, int columnIndex,
            String content, boolean commit) throws SQLException {
        Connection conn = ((DelegatingConnection) ps.getConnection()).getInnermostDelegate();
        CLOB clob = CLOB.createTemporary(conn, false, CLOB.DURATION_SESSION);
        clob.setString(1, content);
        ps.setClob(columnIndex, clob);
        return commit ? ps.executeUpdate() : 0;
    }

    public String getClobData(ResultSet rs, String columnName) throws SQLException {
	Clob messageClob = rs.getClob(columnName);
        if (messageClob == null) return null;
	return messageClob.getSubString(1, (int) messageClob.length());

    }
}
