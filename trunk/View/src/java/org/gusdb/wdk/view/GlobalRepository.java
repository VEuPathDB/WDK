package org.gusdb.gus.wdk.view;

import oracle.jdbc.pool.OracleConnectionCacheImpl;

import java.io.File;
import java.sql.SQLException;

import javax.sql.DataSource;

//import org.apache.commons.dbcp.ConnectionFactory;
//import org.apache.commons.dbcp.DriverManagerConnectionFactory;
//import org.apache.commons.dbcp.PoolableConnectionFactory;
//import org.apache.commons.dbcp.PoolingDataSource;
//import org.apache.commons.pool.ObjectPool;
//import org.apache.commons.pool.impl.GenericObjectPool;
import org.gusdb.gus.wdk.model.ModelConfig;
import org.gusdb.gus.wdk.model.ModelConfigParser;
import org.gusdb.gus.wdk.model.QueryParamsException;
import org.gusdb.gus.wdk.model.RDBMSPlatformI;
import org.gusdb.gus.wdk.model.RecordSet;
import org.gusdb.gus.wdk.model.ResultFactory;
import org.gusdb.gus.wdk.model.SimpleQuerySet;
import org.gusdb.gus.wdk.model.WdkModel;
import org.gusdb.gus.wdk.model.implementation.ModelXmlParser;
import org.gusdb.gus.wdk.model.implementation.SqlResultFactory;



public class GlobalRepository {

    private static final GlobalRepository INSTANCE = new GlobalRepository();
    private SimpleQuerySet simpleQuerySet;
    private WdkModel wdkRecordModel;
    private WdkModel wdkQueryModel;
    private ResultFactory recordResultFactory;
    private ResultFactory queryResultFactory;
    
    static public GlobalRepository getInstance() {
        return INSTANCE;
    }


    private GlobalRepository() {
        File querySetFile = new File("/nfs/team81/art/gus/gus_home/lib/xml/PSUSampleQuerySet.xml");
        File recordSetFile = new File("/nfs/team81/art/gus/gus_home/lib/xml/PSUSampleRecordSet.xml");
        File modelConfigXmlFile = new File("/nfs/team81/art/gus/gus_home/lib/xml/modelConfig.xml");
        
        try {
            // read config info
            ModelConfig modelConfig = 
                ModelConfigParser.parseXmlFile(modelConfigXmlFile);
            String connectionUrl = modelConfig.getConnectionUrl();
            String login = modelConfig.getLogin();
            String password = modelConfig.getPassword();
            String instanceTable = modelConfig.getQueryInstanceTable();
            String platformClass = modelConfig.getPlatformClass();
            
            DataSource dataSource = 
                setupDataSource(connectionUrl,login, password);
            
            RDBMSPlatformI platform = 
                (RDBMSPlatformI)Class.forName(platformClass).newInstance();
            platform.setDataSource(dataSource);
            
            wdkQueryModel = ModelXmlParser.parseXmlFile(querySetFile);
            wdkRecordModel = ModelXmlParser.parseXmlFile(recordSetFile);
            queryResultFactory = wdkQueryModel.getResultFactory();
            recordResultFactory = wdkRecordModel.getResultFactory();
            SqlResultFactory sqlResultFactory = 
                new SqlResultFactory(dataSource, platform, 
                        login, instanceTable);
            recordResultFactory.setSqlResultFactory(sqlResultFactory);

            queryResultFactory.setSqlResultFactory(sqlResultFactory);
            
        } catch (QueryParamsException e) {
            System.err.println(e.formatErrors());
        } catch (Exception e) {
            e.printStackTrace();
        } 
        
    }
    
    
    public SimpleQuerySet getSimpleQuerySet(String querySetName) {
        if (wdkQueryModel == null) {
            System.err.println("wdkQueryModel is null!");
            return null;
        }
        return wdkQueryModel.getSimpleQuerySet(querySetName);
    }


    //////////////////////////////////////////////////////////////////////
    /////////////   static methods   /////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    static DataSource setupDataSource(String connectURI, String login, 
				      String password)  {

	//	DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

        try {
        OracleConnectionCacheImpl ds = new oracle.jdbc.pool.OracleConnectionCacheImpl();
        ds.setURL("jdbc:oracle:thin:@ocs3:1532:tpat");
//        
        ds.setPassword("GUSrw");
        ds.setUser("GUSrw");
        return (DataSource) ds;
        }
        catch (SQLException exp) {
            exp.printStackTrace();
        }
        
//        <data-sources>
//        <data-source type="oracle.jdbc.pool.OracleConnectionCacheImpl">
//          <set-property property="autocommit" value="false" />
//          <set-property property="readOnly" value="true" />
//          <set-property property="description" value="Gusdev Oracle data source" />
//        </data-source>
//      </data-sources>
      
        
        
//        ObjectPool connectionPool = new GenericObjectPool(null);
//        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI, login, password);
//        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);
//        PoolingDataSource dataSource = new PoolingDataSource(connectionPool);
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
}
    
