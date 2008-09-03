/**
 * 
 */
package org.gusdb.wdk.model.dbms;

import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbcp.DelegatingConnection;
import org.gusdb.wdk.model.WdkModel;

import oracle.jdbc.driver.OracleDriver;
import oracle.sql.CLOB;

/**
 * @author Jerric Gao
 * 
 */
public class Oracle extends DBPlatform {

    public Oracle() throws ClassNotFoundException, SQLException {
        // register the driver
        Class.forName("oracle.jdbc.driver.OracleDriver");

        DriverManager.registerDriver(new OracleDriver());
        System.setProperty("jdbc.drivers", "oracle.jdbc.driver.OracleDriver");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#createSequence(java.lang.String,
     *      int, int)
     */
    @Override
    public void createSequence(String sequence, int start, int increment)
            throws SQLException {
        StringBuffer sql = new StringBuffer("CREATE SEQUENCE ");
        sql.append(sequence);
        sql.append(" START WITH ").append(start);
        sql.append(" INCREMENT BY ").append(increment);
        SqlUtils.executeUpdate(dataSource, sql.toString());
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
     *      java.lang.String)
     */
    @Override
    public int getNextId(String schema, String table) throws SQLException {
        schema = normalizeSchema(schema);

        StringBuffer sql = new StringBuffer("SELECT ");
        sql.append(schema).append(table).append(ID_SEQUENCE_SUFFIX);
        sql.append(".nextval FROM dual");
        BigDecimal id = (BigDecimal) SqlUtils.executeScalar(dataSource, sql.toString());
        return id.intValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#getClobData(java.sql.ResultSet,
     *      java.lang.String)
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
     * @see org.gusdb.wdk.model.dbms.DBPlatform#updateClobData(java.sql.PreparedStatement,
     *      int, java.lang.String, boolean)
     */
    @Override
    public int updateClobData(PreparedStatement ps, int columnIndex,
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
     *      int, int)
     */
    @Override
    public String getPagedSql(String sql, int startIndex, int endIndex) {
        StringBuffer buffer = new StringBuffer();
        // construct the outer query
        buffer.append("SELECT n.* FROM (");
        // construct the inner nested query
        buffer.append("SELECT b.*, rownum AS row_index FROM (");
        buffer.append(sql);
        buffer.append(") b WHERE rownum <= ").append(endIndex);
        buffer.append(") n WHERE row_index >= ").append(startIndex);
        return buffer.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#isTableExist(java.lang.String)
     */
    @Override
    public boolean checkTableExists(String schema, String tableName)
            throws SQLException {
        StringBuffer sql = new StringBuffer("SELECT count(*) FROM ALL_TABLES ");
        sql.append("WHERE table_name = '");
        sql.append(tableName.toUpperCase()).append("'");
        
        if (schema == null) schema = defaultSchema;
        if (schema.charAt(schema.length() - 1) == '.')
            schema = schema.substring(0, schema.length() - 1);
        sql.append(" AND owner = '").append(schema.toUpperCase()).append("'");

        BigDecimal count = (BigDecimal) SqlUtils.executeScalar(dataSource,
                sql.toString());
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
}
