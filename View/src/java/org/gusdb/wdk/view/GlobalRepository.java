package org.gusdb.gus.wdk.view;

import java.io.File;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.gusdb.gus.wdk.model.ModelConfig;
import org.gusdb.gus.wdk.model.ModelConfigParser;
import org.gusdb.gus.wdk.model.QueryParamsException;
import org.gusdb.gus.wdk.model.RDBMSPlatformI;
import org.gusdb.gus.wdk.model.ResultFactory;
import org.gusdb.gus.wdk.model.SimpleQuerySet;
import org.gusdb.gus.wdk.model.WdkModel;
import org.gusdb.gus.wdk.model.implementation.ModelXmlParser;
import org.gusdb.gus.wdk.model.implementation.SqlResultFactory;



public class GlobalRepository {

    private static final GlobalRepository INSTANCE = new GlobalRepository();
    private SimpleQuerySet simpleQuerySet;
    private ResultFactory resultFactory;
    private DataSource dataSource;
    
    static public GlobalRepository getInstance() {
	return INSTANCE;
    }


    private GlobalRepository() {
	File modelXmlFile = new File("/nfs/team81/art/gus/gus_home/lib/xml/sampleQuerySet.xml");
	File modelConfigXmlFile = new File("/nfs/team81/art/gus/gus_home/lib/xml/modelConfig.xml");
	String querySetName = "RNASimpleQueries";

	try {
	    // read config info
	    ModelConfig modelConfig = 
		ModelConfigParser.parseXmlFile(modelConfigXmlFile);
	    String connectionUrl = modelConfig.getConnectionUrl();
	    String login = modelConfig.getLogin();
	    String password = modelConfig.getPassword();
	    String instanceTable = modelConfig.getQueryInstanceTable();
	    String platformClass = modelConfig.getPlatformClass();
	    
	    this.dataSource = 
		setupDataSource(connectionUrl,login, password);
	
	    RDBMSPlatformI platform = 
		(RDBMSPlatformI)Class.forName(platformClass).newInstance();
	    platform.setDataSource(dataSource);
       
	    WdkModel wdkModel = ModelXmlParser.parseXmlFile(modelXmlFile);
	    SimpleQuerySet simpleQuerySet = wdkModel.getSimpleQuerySet(querySetName);
//	    QuerySetContainer querySetContainer = 
//		QuerySetParser.parseXmlFile(querySetFile);
	    ResultFactory resultFactory = wdkModel.getResultFactory();
	    SqlResultFactory sqlResultFactory = 
		new SqlResultFactory(dataSource, platform, 
				     login, instanceTable);
	    resultFactory.setSqlResultFactory(sqlResultFactory);
	    this.simpleQuerySet = simpleQuerySet;
	    this.resultFactory = resultFactory;

	} catch (QueryParamsException e) {
	    System.err.println(e.formatErrors());
	} catch (Exception e) {
	    e.printStackTrace();
        } 

    }


    public SimpleQuerySet getSimpleQuerySet() {
	return simpleQuerySet;
    }


    //////////////////////////////////////////////////////////////////////
    /////////////   static methods   /////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    static DataSource setupDataSource(String connectURI, String login, 
				      String password)  {

	//	DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

        ObjectPool connectionPool = new GenericObjectPool(null);
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI, login, password);
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);
        PoolingDataSource dataSource = new PoolingDataSource(connectionPool);
        return dataSource;
    }
	/**
	 * @return Returns the resultFactory.
	 */
	public ResultFactory getResultFactory() {
		return resultFactory;
	}
	/**
	 * @return Returns the dataSource.
	 */
	public DataSource getDataSource() {
		return dataSource;
	}
}
    
