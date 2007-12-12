package org.gusdb.wdk.model;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

import javax.sql.DataSource;

/**
 * 
 * @author Steve Fischer
 * @version $Revision$ $Date: 2006-09-07 10:55:59 -0400 (Thu, 07 Sep
 *          2006) $ $Author$
 */
public interface RDBMSPlatformI {

    public void init(String url, String user, String password, Integer minIdle,
            Integer maxIdle, Integer maxWait, Integer maxActive,
            Integer initialSize, String fileName) throws WdkModelException;

    public DataSource getDataSource();

    public String getTableFullName(String schemaName, String tableName);

    public String getNextId(String schemaName, String tableName)
            throws SQLException;

    public String cleanStringValue(String val);

    public String getCurrentDateFunction();

    public String getNumberDataType();

    public String getClobDataType();

    public boolean checkTableExists(String tableName) throws SQLException;

    public int dropTable(String fullTableName) throws SQLException;

    public void createSequence(String sequenceName, int start, int increment)
            throws SQLException;

    public void dropSequence(String sequenceName) throws SQLException;

    public void createResultTable(DataSource dataSource, String tableName,
            String sql) throws SQLException;

    public void close() throws WdkModelException;

    public String getTableAliasAs();

    public String getMinus();

    public void addIndexColumn(DataSource dataSource, String tableName)
            throws SQLException;

    public int getActiveCount();

    public int getIdleCount();

    public int getTableCount(String tableNamePattern) throws SQLException;

    public int forceDropTables(String tableNamePattern) throws SQLException;

    public int updateClobData(PreparedStatement ps, int columnIndex,
            String content, boolean commit) throws SQLException;

    public String getClobData(ResultSet rs, String columnName) throws SQLException;
}
