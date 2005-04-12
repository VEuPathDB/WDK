package org.gusdb.wdk.model.test;

import org.gusdb.wdk.model.ModelConfig;
import org.gusdb.wdk.model.ModelConfigParser;
import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.implementation.SqlUtils;

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
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public class TestDBManager {

    public static void main(String[] args){

	String cmdName = System.getProperties().getProperty("cmdName");
        File configDir = 
	    new File(System.getProperties().getProperty("configDir"));
	
	// process args
	Options options = declareOptions();
	CommandLine cmdLine = parseOptions(cmdName, options, args);
	
	String modelName = cmdLine.getOptionValue("model");

        File modelConfigXmlFile = new File(configDir, modelName+"-config.xml");

	try {
	    
	    ModelConfig modelConfig = 
		ModelConfigParser.parseXmlFile(modelConfigXmlFile);
	    String connectionUrl = modelConfig.getConnectionUrl();
	    String login = modelConfig.getLogin();
	    String password = modelConfig.getPassword();
	    String instanceTable = modelConfig.getQueryInstanceTable();
	    String platformClass = modelConfig.getPlatformClass();

	    Integer maxIdle = modelConfig.getMaxIdle();
	    Integer minIdle = modelConfig.getMinIdle();
	    Integer maxWait = modelConfig.getMaxWait();
	    Integer maxActive = modelConfig.getMaxActive();
	    Integer initialSize = modelConfig.getInitialSize();
	    
	    RDBMSPlatformI platform = 
		(RDBMSPlatformI)Class.forName(platformClass).newInstance();
	    platform.init(connectionUrl, login, password, minIdle, maxIdle, maxWait, maxActive, initialSize, modelConfigXmlFile.getAbsolutePath());

	    boolean drop = cmdLine.hasOption("drop");
	    boolean create = cmdLine.hasOption("create");
	    
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
			createTable(tableName, firstLine, platform);
			
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
		    else {
			System.err.println("Table " + tableName + " already exists; no change made.  To reload this table, first drop it and then create it again");
		    }
		}
	    }
	}
	catch(Exception e){
	    System.err.println(e.getMessage());
	    e.printStackTrace();
	}
    }
    
    private static void createTable(String tableName, String firstLine, 
				    RDBMSPlatformI platform)throws Exception{
	DataSource dataSource = platform.getDataSource();
	
	// substitute plaform indpendent number types for "number(10)"
	String platformCorrectedFirstLine = firstLine;
	String numType = platform.getNumberDataType();

	platformCorrectedFirstLine.replaceAll("number\\(", numType + "(");

	String createTable = "create table " + tableName + 
	    " (" + platformCorrectedFirstLine + ")";
	System.err.println("creating test table with sql " + createTable);
	
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

	// model name
	addOption(options, "model", "the name of the model.  This is used to find the config file ($GUS_HOME/config/model_name-config.xml)");

	// tables
	Option tables = new Option("tables", "a list of files to be parsed and created as tables in the database");
	tables.setArgs(Option.UNLIMITED_VALUES);
	tables.setRequired(true);
	options.addOption(tables);
	
	Option dropDb = new Option("drop", false, "Drop existing test database");
	options.addOption(dropDb);

	Option createDb = new Option("create", false, "Create new test database");
	options.addOption(createDb);

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

	    usage(cmdName, options);
	}

	return cmdLine;
    }

    /**
     * As it currently stands, TestDBManager is called from the command line with wdkTestDb.  That file has its
     * own command line arguments (different from these) so this usage() method will not be called.
     */

    static void usage(String cmdName, Options options) {
        
        String newline = System.getProperty( "line.separator" );
        String cmdlineSyntax = 
            cmdName + 
            " -model model_name" +
            " tables table_list " +
            " [-create | -drop] ";
        
        String header = 
            newline + "Parse flat files representing database tables and insert into database." + newline + newline + "Options:" ;
        
        String footer = "";
        
        //	PrintWriter stderr = new PrintWriter(System.err);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(75, cmdlineSyntax, header, options, footer);
        System.exit(1);
    }


}
