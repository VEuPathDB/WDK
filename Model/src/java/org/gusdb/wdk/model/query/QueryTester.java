package org.gusdb.gus.wdk.model.query;

import org.gusdb.gus.wdk.model.ModelConfig;
import org.gusdb.gus.wdk.model.ModelConfigParser;

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;
import javax.sql.DataSource;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;


public class QueryTester {

    QuerySet querySet;
    SqlResultFactory resultFactory;

    public QueryTester(QuerySet querySet, SqlResultFactory resultFactory) {
	this.querySet = querySet;
	this.resultFactory = resultFactory;
    }

    //////////////////////////////////////////////////////////////////////
    /////////////   public methods   /////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    public ResultSet executeQuery(String queryName, Hashtable paramHash) throws SQLException, QueryParamsException {
	Query query = querySet.getQuery(queryName);
	QueryInstance instance = query.makeInstance();
       	instance.setValues(paramHash);
	return resultFactory.getResult((SqlQueryInstance)instance);
    }

    //////////////////////////////////////////////////////////////////////
    /////////////   protected methods   //////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    void displayQuery(String queryName) throws SQLException {
	Query query = querySet.getQuery(queryName);
	if (query == null) 
	    throw new IllegalArgumentException("Query set '" + querySet.getName() +
					       "' does not include a query named '" +
					       queryName + "'");

	String newline = System.getProperty( "line.separator" );
	System.out.println(newline + "Query: " + 
			   query.getDisplayName() + newline);

	System.out.println("Parameters");

	Param[] params = query.getParams();

	for (int i=0; i<params.length; i++) {
	    System.out.println(formatParamPrompt(params[i]));
	}
	System.out.println("");
    }

    Hashtable parseParamArgs(String[] args, int startIndex) {
	Hashtable h = new Hashtable();
	for (int i=startIndex; i<args.length; i+=2) {
	    h.put(args[i], args[i+1]);
	}
	return h;
    }

    void printResultSet(ResultSet rs) throws SQLException{
	try {
	    int colCount = rs.getMetaData().getColumnCount();
	    int count = 0;
	    while (rs.next() && count++ <= 100) {
		for (int i=1; i<=colCount; i++) {
		    System.out.print(rs.getString(i) + "\t");
		}
		System.out.println("");
	    }
	} catch (SQLException e) {
	    throw e;
	} finally {
	    SqlUtils.closeResultSet(rs);
	}
    }

    String formatParamPrompt(Param param) throws SQLException {

	String newline = System.getProperty( "line.separator" );

	String prompt = "  " + param.getPrompt();

	if (param instanceof SqlEnumParam) {
	    SqlEnumParam enumParam = (SqlEnumParam)param;
	    prompt += " (chose one";
	    if (enumParam.getMultiPick().booleanValue()) prompt += " or more"; 
	    prompt += "):";
	    Hashtable hash = enumParam.getKeysAndValues(resultFactory);
	    Enumeration keys = hash.keys();
	    while (keys.hasMoreElements()) {
		String key = (String)keys.nextElement();
		prompt += newline + "    " + key + " = " + hash.get(key);
	    }
	} 

	else if (param instanceof StringParam) {
	    StringParam stringParam = (StringParam)param;
	    if (stringParam.getSample() != null)
		prompt += " (" + stringParam.getSample() + ")";
	    prompt += ":";
	} 

	else {
	    prompt = param.getPrompt() + ":";
	}

	return prompt;
    }

    //////////////////////////////////////////////////////////////////////
    /////////////   static methods   /////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    static int STARTINDEX = 3; // index of first optional arg

    public static void main(String[] args) {
	
	File modelConfigXmlFile = new File(args[0]);
	File querySetFile = new File(args[1]);
	String queryName = args[2];

	try {
	    // read config info
	    ModelConfig modelConfig = 
		ModelConfigParser.parseXmlFile(modelConfigXmlFile);
	    String connectionUrl = modelConfig.getConnectionUrl();
	    String login = modelConfig.getLogin();
	    String password = modelConfig.getPassword();
	    String cacheTable = modelConfig.getQueryCacheTable();
	    String platformClass = modelConfig.getPlatformClass();
	    
	    DataSource dataSource = 
		setupDataSource(connectionUrl,login, password);
	
	    RDBMSPlatformI platform = 
		(RDBMSPlatformI)Class.forName(platformClass).newInstance();
	    platform.setDataSource(dataSource);
       
	    QuerySet querySet = QuerySetParser.parseXmlFile(querySetFile);
	    SqlResultFactory resultFactory = new SqlResultFactory(dataSource, platform, login, cacheTable);
	    QueryTester tester = new QueryTester(querySet, resultFactory);

	    // if no params supplied, show the query prompts
	    if (args.length == STARTINDEX) {
		tester.displayQuery(queryName);
	    } 

	    // else, run the query with the supplied params
	    else {
		Hashtable paramHash = tester.parseParamArgs(args, STARTINDEX);
		ResultSet rs = tester.executeQuery(queryName, paramHash);
		tester.printResultSet(rs);
	    }
	} catch (QueryParamsException e) {
	    System.err.println(e.formatErrors());
	    System.exit(1);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
        } 
    }

    static DataSource setupDataSource(String connectURI, String login, 
				      String password)  {

	//	DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

        //
        // First, we'll need a ObjectPool that serves as the
        // actual pool of connections.
        //
        // We'll use a GenericObjectPool instance, although
        // any ObjectPool implementation will suffice.
        //
        ObjectPool connectionPool = new GenericObjectPool(null);

        //
        // Next, we'll create a ConnectionFactory that the
        // pool will use to create Connections.
        // We'll use the DriverManagerConnectionFactory,
        // using the connect string passed in the command line
        // arguments.
        //
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI, login, password);

        //
        // Now we'll create the PoolableConnectionFactory, which wraps
        // the "real" Connections created by the ConnectionFactory with
        // the classes that implement the pooling functionality.
        //
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);

        //
        // Finally, we create the PoolingDriver itself,
        // passing in the object pool we created.
        //
        PoolingDataSource dataSource = new PoolingDataSource(connectionPool);

        return dataSource;
    }
}
    
