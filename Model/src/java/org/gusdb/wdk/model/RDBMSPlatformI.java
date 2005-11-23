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
		     Integer initialSize, String fileName) throws WdkModelException;

    public DataSource getDataSource();

    public String getTableFullName(String schemaName, String tableName);

    public String getNextId(String schemaName, String tableName) throws SQLException;

    public String cleanStringValue(String val);

    public String getCurrentDateFunction();

    public String getNumberDataType(); 

    public String getClobDataType(); 
    
    public boolean checkTableExists(String tableName) throws SQLException;

    public int dropTable(String fullTableName) throws SQLException;
    public void createSequence(String sequenceName, int start, int increment) throws SQLException;

    public void dropSequence(String sequenceName) throws SQLException;

    public void createResultTable(DataSource dataSource,
				  String tableName, 
				  String sql) throws SQLException ;

    public void close() throws WdkModelException;

    public String getTableAliasAs();
    
    public String getMinus();

    public String addIndexColumn(DataSource dataSource, String tableName) throws SQLException;

}


