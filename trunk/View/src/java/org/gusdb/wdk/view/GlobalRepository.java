package org.gusdb.gus.wdk.view;

import org.gusdb.gus.wdk.model.ModelConfig;
import org.gusdb.gus.wdk.model.ModelConfigParser;
import org.gusdb.gus.wdk.model.query.ResultFactory;
import org.gusdb.gus.wdk.model.query.QueryParamsException;
import org.gusdb.gus.wdk.model.query.QuerySetContainer;
import org.gusdb.gus.wdk.model.query.RDBMSPlatformI;
import org.gusdb.gus.wdk.model.query.implementation.QuerySetParser;
import org.gusdb.gus.wdk.model.query.implementation.SqlResultFactory;

import java.io.File;
import javax.sql.DataSource;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;



public class GlobalRepository {

    private static final GlobalRepository INSTANCE = new GlobalRepository();
    private QuerySetContainer querySetContainer;
    private ResultFactory resultFactory;

    
    static public GlobalRepository getInstance() {
	return INSTANCE;
    }


    private GlobalRepository() {
	File querySetFile = new File("/nfs/team81/art/gus/gus_home/lib/xml/sampleQuerySet.xml");
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
	    
	    DataSource dataSource = 
		setupDataSource(connectionUrl,login, password);
	
	    RDBMSPlatformI platform = 
		(RDBMSPlatformI)Class.forName(platformClass).newInstance();
	    platform.setDataSource(dataSource);
       
	    QuerySetContainer querySetContainer = 
		QuerySetParser.parseXmlFile(querySetFile);
	    ResultFactory resultFactory = querySetContainer.getResultFactory();
	    SqlResultFactory sqlResultFactory = 
		new SqlResultFactory(dataSource, platform, 
				     login, instanceTable);
	    resultFactory.setSqlResultFactory(sqlResultFactory);
	    this.querySetContainer = querySetContainer;
	    this.resultFactory = resultFactory;

	} catch (QueryParamsException e) {
	    System.err.println(e.formatErrors());
	} catch (Exception e) {
	    e.printStackTrace();
        } 

    }


    public QuerySetContainer getQuerySetContainer() {
	return querySetContainer;
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
}
    
