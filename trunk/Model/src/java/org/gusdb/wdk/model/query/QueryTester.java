package org.gusdb.gus.wdk.model.query;

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
    SqlResultSetManager resultMgr;

    public QueryTester(QuerySet querySet, DataSource dataSource) {
	this.querySet = querySet;
	this.resultMgr = new SqlResultSetManager(dataSource);
    }

    //////////////////////////////////////////////////////////////////////
    /////////////   public methods   /////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    public ResultSet executeQuery(String queryName, Hashtable paramHash) throws SQLException, QueryParamsException {
	Query query = querySet.getQuery(queryName);
	QueryInstance instance = query.makeInstance();
       	instance.setValues(paramHash);
	return resultMgr.getResult((SqlQueryInstance)instance);
    }

    //////////////////////////////////////////////////////////////////////
    /////////////   protected methods   //////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    void displayQuery(String queryName) throws SQLException {
	Query query = querySet.getQuery(queryName);

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
	    resultMgr.closeResultSet(rs);
	}
    }

    void closeResultSet(ResultSet rs) throws SQLException {
	resultMgr.closeResultSet(rs);
    }

    String formatParamPrompt(Param param) throws SQLException {

	String newline = System.getProperty( "line.separator" );

	String prompt = "  " + param.getPrompt();

	if (param instanceof SqlEnumParam) {
	    SqlEnumParam enumParam = (SqlEnumParam)param;
	    prompt += " (chose one";
	    if (enumParam.getMultiPick().booleanValue()) prompt += " or more"; 
	    prompt += "):";
	    Hashtable hash = enumParam.getKeysAndValues(resultMgr);
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

    public static void main(String[] args) {
	
	String connectionUrl = args[0];
	String login = args[1];
	String password = args[2];
	File querySetFile = new File(args[3]);
	String queryName = args[4];
	
	try {
	    DataSource dataSource = 
		setupDataSource(connectionUrl,login, password);
	    QuerySet querySet = QuerySetParser.parseXmlFile(querySetFile);
	    QueryTester tester = new QueryTester(querySet, dataSource);

	    // if no params supplied, show the query prompts
	    if (args.length == 5) {
		tester.displayQuery(queryName);
	    } 

	    // else, run the query with the supplied params
	    else {
		Hashtable paramHash = tester.parseParamArgs(args, 5);
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
    
