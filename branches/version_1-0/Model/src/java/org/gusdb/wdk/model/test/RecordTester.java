package org.gusdb.wdk.model.test;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.Record;
import org.gusdb.wdk.model.RecordInstance;
import org.gusdb.wdk.model.RecordSet;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.implementation.ModelXmlParser;

import java.io.File;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public class RecordTester {

    //////////////////////////////////////////////////////////////////////
    /////////////   static methods   /////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    public static void main(String[] args) {
	
	String cmdName = System.getProperties().getProperty("cmdName");

	// process args
	Options options = declareOptions();
	CommandLine cmdLine = parseOptions(cmdName, options, args);
	
	File modelXmlFile = new File(cmdLine.getOptionValue("modelXmlFile"));
        File modelPropFile = new File(cmdLine.getOptionValue("modelPropFile"));
	File modelConfigXmlFile = new File(cmdLine.getOptionValue("configFile"));
	File schemaFile = new File(System.getProperty("schemaFile"));

	String recordSetName = cmdLine.getOptionValue("recordSetName");
	String recordName = cmdLine.getOptionValue("recordName");
	String primaryKey = cmdLine.getOptionValue("primaryKey");

	try {

	    WdkModel wdkModel = 
		ModelXmlParser.parseXmlFile(modelXmlFile.toURL(), modelPropFile.toURL(), schemaFile.toURL(), modelConfigXmlFile.toURL()) ;

	    RecordSet recordSet = wdkModel.getRecordSet(recordSetName);
	    Record record = recordSet.getRecord(recordName);
	    RecordInstance recordInstance = record.makeRecordInstance();
	    recordInstance.setPrimaryKey(primaryKey);
	    System.out.println( recordInstance.print() );

        } catch (WdkUserException e) {
	  System.err.println(e.formatErrors());
	  System.exit(1);
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
	// model file
	addOption(options, "modelXmlFile", "An .xml file that specifies WDK Model.");
	// model prop file
	addOption(options, "modelPropFile", "A .prop file that specifies key=value pairs to substitute into the model file.");
	
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
            " -modelPropFile model_prop_file" +
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
}
    
