package org.gusdb.gus.wdk.controller.servlets;

import org.gusdb.gus.wdk.model.ModelConfig;
import org.gusdb.gus.wdk.model.ModelConfigParser;
import org.gusdb.gus.wdk.model.RDBMSPlatformI;
import org.gusdb.gus.wdk.model.Record;
import org.gusdb.gus.wdk.model.RecordInstance;
import org.gusdb.gus.wdk.model.RecordSet;
import org.gusdb.gus.wdk.model.ResultFactory;
import org.gusdb.gus.wdk.model.WdkModel;
import org.gusdb.gus.wdk.model.implementation.ModelXmlParser;
import org.gusdb.gus.wdk.model.implementation.SqlResultFactory;
import org.gusdb.gus.wdk.view.GlobalRepository;

import java.io.File;

import javax.servlet.http.HttpServlet;
import javax.sql.DataSource;



public class RecordTesterServlet extends HttpServlet {

    //////////////////////////////////////////////////////////////////////
    /////////////   static methods   /////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

//    public static void main(String[] args) {
//	
//	String cmdName = System.getProperties().getProperty("cmdName");
//
//	// process args
//	Options options = declareOptions();
//	CommandLine cmdLine = parseOptions(cmdName, options, args);
//
//	File modelConfigXmlFile = 
//	    new File(cmdLine.getOptionValue("configFile"));
//	File modelXmlFile = new File(cmdLine.getOptionValue("modelXmlFile"));
//
//	String recordSetName = cmdLine.getOptionValue("recordSetName");
//	String recordName = cmdLine.getOptionValue("recordName");
//	String primaryKey = cmdLine.getOptionValue("primaryKey");
//
//	try {
//	    // read config info
//	    ModelConfig modelConfig = 
//		ModelConfigParser.parseXmlFile(modelConfigXmlFile);
//	    String connectionUrl = modelConfig.getConnectionUrl();
//	    String login = modelConfig.getLogin();
//	    String password = modelConfig.getPassword();
//	    String instanceTable = modelConfig.getQueryInstanceTable();
//	    String platformClass = modelConfig.getPlatformClass();
//	    
//	    DataSource dataSource = 
//		setupDataSource(connectionUrl,login, password);
//	
//	    RDBMSPlatformI platform = 
//		(RDBMSPlatformI)Class.forName(platformClass).newInstance();
//	    platform.setDataSource(dataSource);
//       
//	    WdkModel wdkModel = 
//		ModelXmlParser.parseXmlFile(modelXmlFile);
//	    ResultFactory resultFactory = wdkModel.getResultFactory();
//	    SqlResultFactory sqlResultFactory = 
//		new SqlResultFactory(dataSource, platform, 
//				     login, instanceTable);
//	    resultFactory.setSqlResultFactory(sqlResultFactory);
//	    RecordSet recordSet = wdkModel.getRecordSet(recordSetName);
//	    Record record = recordSet.getRecord(recordName);
//	    RecordInstance recordInstance = record.makeInstance();
//	    recordInstance.setPrimaryKey(primaryKey);
//	    System.out.println( recordInstance.print() );
//
//	} catch (Exception e) {
//	    e.printStackTrace();
//	    System.exit(1);
//        } 
//    }

//    static Options declareOptions() {
//	Options options = new Options();
//
//	// config file
//	Option configFile = OptionBuilder
//	    .withArgName("configFile")
//	    .hasArg()
//	    .withDescription("An .xml file that specifies a ModelConfig object.")
//	    .isRequired()
//	    .create("configFile");
//	options.addOption(configFile);
//
//	// query set file
//	Option modelXmlFile = OptionBuilder
//	    .withArgName("modelXmlFile")
//	    .hasArg()
//	    .withDescription("An .xml file that specifies a container of Query set objects.")
//	    .isRequired()
//	    .create("modelXmlFile");
//	options.addOption(modelXmlFile);
//
//	// record set name
//	Option recordSetName = OptionBuilder
//	    .withArgName("recordSetName")
//	    .hasArg()
//	    .withDescription("The name of the record set in which to find the record")
//	    .isRequired()
//	    .create("recordSetName");
//	options.addOption(recordSetName);
//
//	// record name
//	Option recordName = OptionBuilder
//	    .withArgName("recordName")
//	    .hasArg()
//	    .withDescription("The name of the record to print.")
//	    .isRequired()
//	    .create("recordName");
//	options.addOption(recordName);
//
//	// primary key
//	Option primaryKey = OptionBuilder
//	    .withArgName("primaryKey")
//	    .hasArg()
//	    .withDescription("The primary key of the record to find.")
//	    .isRequired()
//	    .create("primaryKey");
//	options.addOption(primaryKey);
//
//	return options;
//    }

//    static CommandLine parseOptions(String cmdName, Options options, 
//				    String[] args) {
//
//	CommandLineParser parser = new BasicParser();
//	CommandLine cmdLine = null;
//	try {
//	    // parse the command line arguments
//	    cmdLine = parser.parse( options, args );
//	}
//	catch( ParseException exp ) {
//	    // oops, something went wrong
//	    System.err.println("");
//	    System.err.println( "Parsing failed.  Reason: " + exp.getMessage() ); 
//	    System.err.println("");
//	    usage(cmdName, options);
//	}
//
//	return cmdLine;
//    }

//    static void usage(String cmdName, Options options) {
//
//	String newline = System.getProperty( "line.separator" );
//	String cmdlineSyntax = 
//	    cmdName + 
//	    " -configFile config_file" +
//	    " -modelXmlFile model_xml_file" +
//	    " -recordSetName record_set_name" +
//	    " -recordName record_name" +
//	    " -primaryKey primary_key";
//
//	String header = 
//	    newline + "Print a record found in a WDK Model xml file. Options:" ;
//
//	String footer = "";
//
//	//	PrintWriter stderr = new PrintWriter(System.err);
//	HelpFormatter formatter = new HelpFormatter();
//	formatter.printHelp(75, cmdlineSyntax, header, options, footer);
//	System.exit(1);
//    }

    static DataSource setupDataSource(String connectURI, String login, 
				      String password)  {

        return GlobalRepository.getInstance().getDataSource();
    }
}
    
