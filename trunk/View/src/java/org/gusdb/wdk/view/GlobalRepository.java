package org.gusdb.gus.wdk.view;

import oracle.jdbc.pool.OracleConnectionCacheImpl;

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

import java.io.File;
import java.sql.SQLException;

import javax.sql.DataSource;



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

        try {
            OracleConnectionCacheImpl ds = new oracle.jdbc.pool.OracleConnectionCacheImpl();
            ds.setURL("jdbc:oracle:thin:@ocs3:1532:tpat");

            ds.setPassword("GUSrw");
            ds.setUser("GUSrw");
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
}
    
