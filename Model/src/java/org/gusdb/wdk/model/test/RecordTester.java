package org.gusdb.gus.wdk.model.test;

import org.gusdb.gus.wdk.model.ModelConfig;
import org.gusdb.gus.wdk.model.ModelConfigParser;
import org.gusdb.gus.wdk.model.RDBMSPlatformI;
import org.gusdb.gus.wdk.model.Record;
import org.gusdb.gus.wdk.model.RecordInstance;
import org.gusdb.gus.wdk.model.RecordSet;
import org.gusdb.gus.wdk.model.ResultFactory;
import org.gusdb.gus.wdk.model.WdkModel;
import org.gusdb.gus.wdk.model.implementation.ModelXmlParser;

import java.io.File;

import javax.sql.DataSource;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;



public class RecordTester {

    //////////////////////////////////////////////////////////////////////
    /////////////   static methods   /////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    public static void main(String[] args) {
	
	String cmdName = System.getProperties().getProperty("cmdName");

	// process args
	Options options = declareOptions();
	CommandLine cmdLine = parseOptions(cmdName, options, args);

	File modelConfigXmlFile = 
	    new File(cmdLine.getOptionValue("configFile"));
	File modelXmlFile = new File(cmdLine.getOptionValue("modelXmlFile"));

	String recordSetName = cmdLine.getOptionValue("recordSetName");
	String recordName = cmdLine.getOptionValue("recordName");
	String primaryKey = cmdLine.getOptionValue("primaryKey");

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
       
	    WdkModel wdkModel = 
		ModelXmlParser.parseXmlFile(modelXmlFile);
	    ResultFactory resultFactory = new ResultFactory(dataSource, platform, 
							    login, instanceTable);
            wdkModel.setResultFactory(resultFactory);
	    wdkModel.setPlatform(platform);

	    RecordSet recordSet = wdkModel.getRecordSet(recordSetName);
	    Record record = recordSet.getRecord(recordName);
	    RecordInstance recordInstance = record.makeInstance();
	    recordInstance.setPrimaryKey(primaryKey);
	    System.out.println( recordInstance.print() );

	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
        } 
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
	// query set file
    addOption(options, "modelXmlFile", "An .xml file that specifies a container of Query set objects.");
	// record set name
    addOption(options, "recordSetName", "The name of the record set in which to find the record");
	// record name
    addOption(options, "recordName", "The name of the record to print.");
	// primary key
    addOption(options, "primaryKey", "The primary key of the record to find.");

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

    static void usage(String cmdName, Options options) {

	String newline = System.getProperty( "line.separator" );
	String cmdlineSyntax = 
	    cmdName + 
	    " -configFile config_file" +
	    " -modelXmlFile model_xml_file" +
	    " -recordSetName record_set_name" +
	    " -recordName record_name" +
	    " -primaryKey primary_key";

	String header = 
	    newline + "Print a record found in a WDK Model xml file. Options:" ;

	String footer = "";

	//	PrintWriter stderr = new PrintWriter(System.err);
	HelpFormatter formatter = new HelpFormatter();
	formatter.printHelp(75, cmdlineSyntax, header, options, footer);
	System.exit(1);
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
    
