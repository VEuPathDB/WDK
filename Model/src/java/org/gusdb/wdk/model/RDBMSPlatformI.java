package org.gusdb.wdk.model;

import java.sql.SQLException;

import javax.sql.DataSource;

/**
 *
 * @author Steve Fischer
 * @version $Revision$ $Date$ $Author$
 */
public interface RDBMSPlatformI {
    

    public void init(String url, String user, String password, Integer minIdle,
		     Integer maxIdle, Integer maxWait, Integer maxActive, 
		     Integer initialSize) throws SQLException;

    public DataSource getDataSource();

    public String getTableFullName(String schemaName, String tableName);

    public String getNextId(String schemaName, String tableName) throws SQLException;

    public String cleanStringValue(String val);

    public String getCurrentDateFunction();

    public boolean checkTableExists(String tableName) throws SQLException;

    public int dropTable(String schemaName, String tableName) throws SQLException;
    public void createSequence(String sequenceName, int start, int increment) throws SQLException;

    public void dropSequence(String sequenceName) throws SQLException;

    public void createTableFromQuerySql(DataSource dataSource,
					String tableName, 
					String sql) throws SQLException ;

    public void close() throws WdkModelException;
}


