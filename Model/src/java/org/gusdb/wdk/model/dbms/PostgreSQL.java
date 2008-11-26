/**
 * 
 */
package org.gusdb.wdk.model.dbms;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.gusdb.wdk.model.WdkModelException;

/**
 * @author Jerric Gao
 * 
 */
public class PostgreSQL extends DBPlatform {

    public PostgreSQL() throws SQLException, ClassNotFoundException {
        // register the driver
        Class.forName("org.postgresql.Driver");

        DriverManager.registerDriver(new org.postgresql.Driver());
        System.setProperty("jdbc.drivers", "org.postgresql.Driver");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#createSequence(java.lang.String,
     * int, int)
     */
    @Override
    public void createSequence(String sequence, int start, int increment)
            throws SQLException {
        StringBuffer sql = new StringBuffer("CREATE SEQUENCE ");
        sql.append(sequence);
        sql.append(" START ");
        sql.append(start);
        sql.append(" INCREMENT ");
        sql.append(increment);
        SqlUtils.executeUpdate(dataSource, sql.toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#getBooleanDataType()
     */
    @Override
    public String getBooleanDataType() {
        return "boolean";
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
        return rs.getString(columnName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#getClobDataType()
     */
    @Override
    public String getClobDataType() {
        return "text";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#getMinusOperator()
     */
    public String getMinusOperator() {
        return "EXCEPT";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#getNextId(java.lang.String,
     * java.lang.String)
     */
    public int getNextId(String schema, String table) throws SQLException,
            WdkModelException {
        schema = normalizeSchema(schema);

        StringBuffer sql = new StringBuffer("SELECT nextval('");
        sql.append(schema).append(table).append(ID_SEQUENCE_SUFFIX);
        sql.append("')");
        long id = (Long) SqlUtils.executeScalar(dataSource, sql.toString());
        return (int) id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#getNumberDataType(int)
     */
    public String getNumberDataType(int size) {
        return "NUMERIC(" + size + ")";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#getPagedSql(java.lang.String,
     * int, int)
     */
    public String getPagedSql(String sql, int startIndex, int endIndex) {
        StringBuffer buffer = new StringBuffer("SELECT f.* FROM ");
        buffer.append("(").append(sql).append(") f ");
        buffer.append(" LIMIT ").append(endIndex - startIndex + 1);
        buffer.append(" OFFSET ").append(startIndex - 1);
        return buffer.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#getStringDataType(int)
     */
    public String getStringDataType(int size) {
        return "VARCHAR(" + size + ")";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.dbms.DBPlatform#updateClobData(java.sql.PreparedStatement
     * , int, java.lang.String, boolean)
     */
    public int setClobData(PreparedStatement ps, int columnIndex,
            String content, boolean commit) throws SQLException {
        ps.setString(columnIndex, content);
        return commit ? ps.executeUpdate() : 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#isTableExisted(java.lang.String)
     */
    @Override
    public boolean checkTableExists(String schema, String tableName)
            throws SQLException, WdkModelException {
        if (schema == null || schema.length() == 0) schema = defaultSchema;
        if (schema.endsWith("."))
            schema = schema.substring(0, schema.length() - 1);
        tableName = tableName.toLowerCase();

        StringBuffer sql = new StringBuffer("SELECT count(*) FROM pg_tables ");
        sql.append("WHERE tablename = '").append(tableName).append("'");
        sql.append(" AND schemaname = '").append(schema).append("'");
        long count = (Long) SqlUtils.executeScalar(dataSource, sql.toString());
        return (count > 0);
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
        return value ? "TRUE" : "FALSE";
    }
}
