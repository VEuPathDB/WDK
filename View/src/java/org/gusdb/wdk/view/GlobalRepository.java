package org.gusdb.gus.wdk.view;

import oracle.jdbc.pool.OracleConnectionCacheImpl;

import org.gusdb.gus.wdk.model.ModelConfig;
import org.gusdb.gus.wdk.model.ModelConfigParser;
import org.gusdb.gus.wdk.model.QueryParamsException;
import org.gusdb.gus.wdk.model.QuerySet;
import org.gusdb.gus.wdk.model.RDBMSPlatformI;
import org.gusdb.gus.wdk.model.RecordSet;
import org.gusdb.gus.wdk.model.ResultFactory;
import org.gusdb.gus.wdk.model.WdkModel;
import org.gusdb.gus.wdk.model.implementation.ModelXmlParser;
import org.gusdb.gus.wdk.model.implementation.SqlResultFactory;

import java.io.File;
import java.sql.SQLException;

import javax.sql.DataSource;



/**
 * A hack for making global resources available. Should be phased out
 */
public class GlobalRepository {

    private static GlobalRepository INSTANCE;
    private SimpleQuerySet simpleQuerySet;
    private WdkModel wdkRecordModel;
    private WdkModel wdkQueryModel;
    private ResultFactory recordResultFactory;
    private ResultFactory queryResultFactory;
    private DataSource dataSource;
    
    static public GlobalRepository getInstance() {
        if (INSTANCE == null) {
            throw new RuntimeException("GlobalRepository instance hasn't been created");
        }
        return INSTANCE;
    }


    private GlobalRepository() {
        // Deliberately empty
    }
    
    public static void createInstance(String loginConfigLocation, String queryConfigLocation, String recordConfigLocation) {
        INSTANCE = new GlobalRepository();
        
        File querySetFile = new File(queryConfigLocation);
        File recordSetFile = new File(recordConfigLocation);
        File modelConfigXmlFile = new File(loginConfigLocation);
        
        try {
            // read config info
            ModelConfig modelConfig = 
                ModelConfigParser.parseXmlFile(modelConfigXmlFile);
            String connectionUrl = modelConfig.getConnectionUrl();
            String login = modelConfig.getLogin();
            String password = modelConfig.getPassword();
            String instanceTable = modelConfig.getQueryInstanceTable();
            String platformClass = modelConfig.getPlatformClass();
            
            DataSource dataSource = setupDataSource(connectionUrl,login, password);
            
            RDBMSPlatformI platform = 
                (RDBMSPlatformI)Class.forName(platformClass).newInstance();
            platform.setDataSource(dataSource);
            
            INSTANCE.wdkQueryModel = ModelXmlParser.parseXmlFile(querySetFile);
            INSTANCE.wdkRecordModel = ModelXmlParser.parseXmlFile(recordSetFile);
            INSTANCE.queryResultFactory = INSTANCE.wdkQueryModel.getResultFactory();
            INSTANCE.recordResultFactory = INSTANCE.wdkRecordModel.getResultFactory();
            SqlResultFactory sqlResultFactory = 
                new SqlResultFactory(dataSource, platform, 
                        login, instanceTable);
            INSTANCE.recordResultFactory.setSqlResultFactory(sqlResultFactory);

            INSTANCE.queryResultFactory.setSqlResultFactory(sqlResultFactory);
            INSTANCE.dataSource = dataSource;
            
        } catch (QueryParamsException e) {
            System.err.println(e.formatErrors());
        } catch (Exception e) {
            e.printStackTrace();
        } 
        
    }
    
    
    public QuerySet getQuerySet(String querySetName) {
        if (wdkQueryModel == null) {
            System.err.println("wdkQueryModel is null!");
            return null;
        }
        return wdkQueryModel.getQuerySet(querySetName);
    }


    //////////////////////////////////////////////////////////////////////
    /////////////   static methods   /////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    static DataSource setupDataSource(String connectURI, String login, 
				      String password)  {

        try {
            OracleConnectionCacheImpl ds = new oracle.jdbc.pool.OracleConnectionCacheImpl();
            ds.setURL(connectURI);

            ds.setPassword(password);
            ds.setUser(login);
//            ds.setURL("jdbc:oracle:thin:@ocs3:1532:tpat");
//
//            ds.setPassword("GUSrw");
//            ds.setUser("GUSrw");
            return (DataSource) ds;
        }
        catch (SQLException exp) {
            exp.printStackTrace();
        }
        
        return null;
    }
    
	/**
	 * @return Returns the resultFactory.
	 */
	public ResultFactory getRecordResultFactory() {
		return recordResultFactory;
	}
    
    public ResultFactory getQueryResultFactory() {
        return queryResultFactory;
    }
    
    /**
     * @return Returns the recordSet.
     */
    public RecordSet getRecordSet(String recordSetName) {
        return wdkRecordModel.getRecordSet(recordSetName);
    }
	/**
	 * @return Returns the dataSource.
	 */
	public DataSource getDataSource() {
		return dataSource;
	}
}
    
