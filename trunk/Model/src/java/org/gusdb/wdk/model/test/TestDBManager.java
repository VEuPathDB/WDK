package org.gusdb.gus.wdk.model.test;

import org.gusdb.gus.wdk.model.ModelConfig;
import org.gusdb.gus.wdk.model.ModelConfigParser;
import org.gusdb.gus.wdk.model.RDBMSPlatformI;
import org.gusdb.gus.wdk.model.implementation.SqlUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import javax.sql.DataSource;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;


public class TestDBManager {

    public static void main(String[] args){

	String cmdName = System.getProperties().getProperty("cmdName");
	
	// process args
	Options options = declareOptions();
	CommandLine cmdLine = parseOptions(cmdName, options, args);
	
	File modelConfigXmlFile = 
	    new File(cmdLine.getOptionValue("configFile"));

	try {
	    
	    ModelConfig modelConfig = 
		ModelConfigParser.parseXmlFile(modelConfigXmlFile);
	    String connectionUrl = modelConfig.getConnectionUrl();
	    String login = modelConfig.getLogin();
	    String password = modelConfig.getPassword();
	    String instanceTable = modelConfig.getQueryInstanceTable();
	    String platformClass = modelConfig.getPlatformClass();

	    RDBMSPlatformI platform = 
		(RDBMSPlatformI)Class.forName(platformClass).newInstance();
	    platform.init(connectionUrl, login, password);

	    boolean drop = cmdLine.hasOption("dropDatabase");
	    boolean create = cmdLine.hasOption("createDatabase");
	    
	    String[] tables = cmdLine.getOptionValues("tables");
	    
	    if (drop == false && create == false){ //valid option
		System.err.println("Test Database Manager:  user has not specified any database management operations");
		System.exit(0);
	    }
	    for (int t = 0; t < tables.length; t++){
		File nextTable = new File(tables[t]);
        if ("CVS".equals(nextTable.getName())) {
            continue;
        }
		
		BufferedReader reader = new BufferedReader(new FileReader(nextTable));
		String firstLine = reader.readLine();
		String tableName = platform.getTableFullName(login, nextTable.getName());
		
		if (drop){
		    if (platform.checkTableExists(tableName) == true){
			System.err.println("Dropping existing table " + tableName);
			dropTable(tableName, platform.getDataSource());
		    }
		}
		if (create){
		    if (platform.checkTableExists(tableName) == false){
			createTable(tableName, firstLine, platform.getDataSource());
			
			String noReturn = "select * from " + tableName;
			
			//HACK -- query empty table to get MetaData for use later (we will need column types, etc.)
			ResultSet empty = SqlUtils.getResultSet(platform.getDataSource(), noReturn);
			ResultSetMetaData rsmd = empty.getMetaData();
			PreparedStatement prepStmt = makePreparedStatement(tableName, platform.getDataSource(), rsmd);
			int columnCount = rsmd.getColumnCount();
			String nextLine = reader.readLine();
			System.err.println("Loading table " + tableName + " to database from file\n");
			while (nextLine != null){
			    
			    String[] parts = nextLine.split("\t", columnCount);
			    for (int i = 0; i < parts.length; i++){
				
				String nextValue = parts[i];
				
				if (nextValue.trim().equals("")){
				    int origSqlType = rsmd.getColumnType(i+1);
				    prepStmt.setNull(i+1, origSqlType);

			    }
				else {
				    prepStmt.setObject(i + 1, nextValue);
				}
			    }
			    SqlUtils.getResultSet(platform.getDataSource(), prepStmt);
			    nextLine = reader.readLine();
			}
			SqlUtils.closeStatement(prepStmt);
		    }
		}
	    }
	}
	catch(Exception e){
	    System.err.println(e.getMessage());
	    e.printStackTrace();
	}
    }
    
    private static void createTable(String tableName, String firstLine, DataSource dataSource)throws Exception{

	String createTable = "create table " + tableName + 
	    " (" + firstLine + ")";
	//	System.err.println("creating table with sql " + createTable);
	
	SqlUtils.execute(dataSource, createTable);
    }

    private static void dropTable(String tableName, DataSource dataSource) throws Exception{
	
	String dropTable = "drop table " + tableName;
	SqlUtils.execute(dataSource, dropTable);
    }

    private static PreparedStatement makePreparedStatement(String tableName, DataSource dataSource,
							   ResultSetMetaData rsmd) throws Exception{

	int columnCount = rsmd.getColumnCount();
	StringBuffer preparedInsert = new StringBuffer();
	preparedInsert.append("insert into " + tableName + " (");
	for (int i = 0; i < columnCount - 1; i++){
	    preparedInsert.append(rsmd.getColumnName(i+1) + ", ");
	}
	preparedInsert.append(rsmd.getColumnName(columnCount) + ") values (");
	for (int i = 0; i < columnCount-1; i++){
	    preparedInsert.append("?, ");
	}
	preparedInsert.append("?)");
	PreparedStatement prepStmt = SqlUtils.getPreparedStatement(dataSource, preparedInsert.toString());
	if (prepStmt == null){
	    System.err.println("could not get prepared Statement!");
	}

	return prepStmt;
    }


    
    private static void addOption(Options options, String argName, String desc) {
	
	Option option = new Option(argName, true, desc);
	option.setRequired(true);
	option.setArgName(argName);
	
	options.addOption(option);
    }
    
    
    static Options declareOptions() {
	Options options = new Options();

	// config file
	addOption(options, "configFile", "An .xml file that specifies a ModelConfig object.");
	// tables
	Option tables = new Option("tables", "a list of files to be parsed and created as tables in the database");
	tables.setArgs(Option.UNLIMITED_VALUES);
	tables.setRequired(true);
	options.addOption(tables);
	
	Option dropDb = new Option("dropDatabase", false, "Drop existing test database");
	options.addOption(dropDb);

	Option createDb = new Option("createDatabase", false, "Create new test database");
	options.addOption(createDb);

	//params
	Option params = new Option("params", true, "space delimited list of param_name param_value ....");
	params.setArgName("params");
	params.setArgs(Option.UNLIMITED_VALUES);
	options.addOption(params);
	return options;
    }


    static CommandLine parseOptions(String cmdName, Options options, 
				    String[] args) {

	CommandLineParser parser = new BasicParser();
	CommandLine cmdLine = null;
	try {
	    // parse the command line arguments
	    cmdLine = parser.parse( options, args );
	}
	catch( ParseException exp ) {
	    // oops, something went wrong
	    System.err.println("");
	    System.err.println( "Parsing failed.  Reason: " + exp.getMessage() ); 
	    System.err.println("");
	    System.err.println("put usage back in!");
	    //usage(cmdName, options);
	}

	return cmdLine;
    }

}
