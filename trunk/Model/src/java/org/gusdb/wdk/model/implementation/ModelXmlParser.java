package org.gusdb.wdk.model.implementation;

import org.gusdb.wdk.model.Column;
import org.gusdb.wdk.model.FlatVocabParam;
import org.gusdb.wdk.model.ParamSet;
import org.gusdb.wdk.model.QuerySet;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.RecordClassSet;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.ReferenceList;
import org.gusdb.wdk.model.StringParam;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.QuestionSet;
import org.gusdb.wdk.model.TextAttributeField;
import org.gusdb.wdk.model.TextColumn;
import org.gusdb.wdk.model.LinkAttributeField;
import org.gusdb.wdk.model.LinkColumn;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.digester.Digester;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.SinglePropertyMap;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;

public class ModelXmlParser {
    
    private static final String DEFAULT_SCHEMA_NAME = "wdkModel.rng";
    
    public static WdkModel parseXmlFile(URL modelXmlURL, URL modelPropURL, URL schemaURL, URL modelConfigXmlFileURL)
    throws WdkModelException {
        
        if (schemaURL == null) {
            schemaURL = WdkModel.class.getResource(DEFAULT_SCHEMA_NAME);   
        }
        
        // NOTE: we are validating before we substitute in the properties
        // so that the validator will operate on a file instead of a stream.
        // this way the validator spits out line numbers for errors
        if (!validModelFile(modelXmlURL, schemaURL)) {
            throw new WdkModelException("Model validation failed");
        }
        
        Digester digester = configureDigester();
        WdkModel model = null;
        
        try {
            InputStream modelXmlStream = 
                makeModelXmlStream(modelXmlURL, modelPropURL);
            model = (WdkModel)digester.parse(modelXmlStream);
        } catch (SAXException e) {
            throw new WdkModelException(e);
        } catch (IOException e) {
            throw new WdkModelException(e);
        }
        
        setModelDocument(model, modelXmlURL, modelPropURL);
	model.resolveReferences();
        try {
	    
	    model.configure(modelConfigXmlFileURL);
	    model.setResources();
	}
	catch (Exception e){
	    throw new WdkModelException(e);
	}
        return model;
    }
    
    private static InputStream makeModelXmlStream(URL modelXmlURL, URL modelPropURL) throws WdkModelException {
        InputStream modelXmlStream;
        
        if (modelPropURL != null) {
            modelXmlStream = configureModelFile(modelXmlURL, modelPropURL);
        } else {
            try {
                modelXmlStream = modelXmlURL.openStream();
            } catch (FileNotFoundException e) {
                throw new WdkModelException(e);
            } catch (IOException e) {
                throw new WdkModelException(e);
            }
        }
        return modelXmlStream;
    }
    
    private static void setModelDocument(WdkModel model, URL modelXmlURL, URL modelPropURL) throws WdkModelException {
        try {
            InputStream modelXmlStream = 
                makeModelXmlStream(modelXmlURL, modelPropURL);
            model.setDocument(buildDocument(modelXmlStream));
        } catch (SAXException e) {
            throw new WdkModelException(e);
        } catch (IOException e) {
            throw new WdkModelException(e);
        } catch (ParserConfigurationException e) {
            throw new WdkModelException(e);
        }
    }
    
    public static Document buildDocument(InputStream modelXMLStream) throws ParserConfigurationException, SAXException, IOException {
        
        Document doc = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Turn on validation, and turn off namespaces
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        ErrorHandler errorHandler = new ErrorHandlerImpl(System.err);
        //builder.setErrorHandler(errorHandler);
        builder.setErrorHandler(
                new org.xml.sax.ErrorHandler() {
                    // ignore fatal errors (an exception is guaranteed)
                    public void fatalError(SAXParseException exception)
                    throws SAXException {
                        exception.printStackTrace(System.err);
                    }
                    // treat validation errors as fatal
                    public void error(SAXParseException e)
                    throws SAXParseException
                    {
                        e.printStackTrace(System.err);
                        throw e;
                    }
                    
                    // dump warnings too
                    public void warning(SAXParseException err)
                    throws SAXParseException
                    {
                        System.err.println("** Warning"
                                + ", line " + err.getLineNumber()
                                + ", uri " + err.getSystemId());
                        System.err.println("   " + err.getMessage());
                    }
                }
        );  
        
        doc = builder.parse(modelXMLStream);
        return doc;
    }
    
    
    private static boolean validModelFile(URL modelXmlURL, URL schemaURL) throws WdkModelException {
    
        System.setProperty("org.apache.xerces.xni.parser.XMLParserConfiguration", "org.apache.xerces.parsers.XIncludeParserConfiguration"); 
    
        try {
            
            ErrorHandler errorHandler = new ErrorHandlerImpl(System.err);
            PropertyMap schemaProperties = new SinglePropertyMap(ValidateProperty.ERROR_HANDLER, errorHandler);
            ValidationDriver vd = new ValidationDriver(schemaProperties, PropertyMap.EMPTY, null);
        
            vd.loadSchema(ValidationDriver.uriOrFileInputSource(schemaURL.toExternalForm()));
 
            //System.err.println("modelXMLURL is  "+modelXmlURL);
            
            InputSource is = ValidationDriver.uriOrFileInputSource(modelXmlURL.toExternalForm());
            //            return vd.validate(new InputSource(modelXMLStream));
            return vd.validate(is);
        
        } catch (SAXException e) {
            throw new WdkModelException(e);
        } catch (IOException e) {
            throw new WdkModelException(e);
        }
    }   

    private static Digester configureDigester() {
    
        Digester digester = new Digester();
        digester.setValidating(false);
        
        //Root -- WDK Model
        
        digester.addObjectCreate( "wdkModel", WdkModel.class );
        digester.addSetProperties( "wdkModel");

	/**/ digester.addBeanPropertySetter( "wdkModel/introduction");
        
        //RecordClassSet
        
        /**/ digester.addObjectCreate( "wdkModel/recordClassSet", RecordClassSet.class );
        
        /**/ digester.addSetProperties( "wdkModel/recordClassSet");
        
        /*  */ digester.addObjectCreate( "wdkModel/recordClassSet/recordClass", RecordClass.class );
        
        /*  */ digester.addSetProperties( "wdkModel/recordClassSet/recordClass");
        
        /*    */ digester.addObjectCreate( "wdkModel/recordClassSet/recordClass/attributeQueryRef", Reference.class );
        
        /*    */ digester.addSetProperties( "wdkModel/recordClassSet/recordClass/attributeQueryRef");
        
        /*    */ digester.addSetNext( "wdkModel/recordClassSet/recordClass/attributeQueryRef", "addAttributesQueryRef" );
        
        /*    */ digester.addObjectCreate( "wdkModel/recordClassSet/recordClass/tableQueryRef", Reference.class );
        
        /*    */ digester.addSetProperties( "wdkModel/recordClassSet/recordClass/tableQueryRef");
        
        /*    */ digester.addSetNext( "wdkModel/recordClassSet/recordClass/tableQueryRef", "addTableQueryRef" );
        
        /*    */ digester.addObjectCreate( "wdkModel/recordClassSet/recordClass/linkAttribute", LinkAttributeField.class );
        
        /*    */ digester.addSetProperties( "wdkModel/recordClassSet/recordClass/linkAttribute");
        
         /*    */ digester.addSetNext( "wdkModel/recordClassSet/recordClass/linkAttribute", "addLinkAttribute" );
        
       /*    */ digester.addObjectCreate( "wdkModel/recordClassSet/recordClass/textAttribute", TextAttributeField.class );
        
        /*    */ digester.addSetProperties( "wdkModel/recordClassSet/recordClass/textAttribute");

        /*      */ digester.addBeanPropertySetter( "wdkModel/recordClassSet/recordClass/textAttribute/text");
        
        /*    */ digester.addSetNext( "wdkModel/recordClassSet/recordClass/textAttribute", "addTextAttribute" );
        
        /*  */ digester.addSetNext( "wdkModel/recordClassSet/recordClass", "addRecordClass" );
        
        /**/ digester.addSetNext( "wdkModel/recordClassSet", "addRecordClassSet" );
        
        
        //QuerySet
        
        /**/ digester.addObjectCreate( "wdkModel/querySet", QuerySet.class );
        
        /**/ digester.addSetProperties( "wdkModel/querySet");
        

        /*  */ digester.addObjectCreate( "wdkModel/querySet/sqlQuery", SqlQuery.class );
        
        /*  */ digester.addSetProperties( "wdkModel/querySet/sqlQuery");
        
        /*  */ digester.addBeanPropertySetter( "wdkModel/querySet/sqlQuery/sql");
	/*  */ digester.addBeanPropertySetter( "wdkModel/querySet/sqlQuery/description");
        
        /*    */ digester.addObjectCreate( "wdkModel/querySet/sqlQuery/paramRef", Reference.class );
        
        /*    */ digester.addSetProperties( "wdkModel/querySet/sqlQuery/paramRef");
        
        /*    */ digester.addSetNext( "wdkModel/querySet/sqlQuery/paramRef", "addParamRef" );
        
        /*    */ digester.addObjectCreate( "wdkModel/querySet/sqlQuery/column", Column.class );
        
        /*    */ digester.addSetProperties( "wdkModel/querySet/sqlQuery/column");
        
        /*    */ digester.addSetNext( "wdkModel/querySet/sqlQuery/column", "addColumn" );
        
        /*    */ digester.addObjectCreate( "wdkModel/querySet/sqlQuery/linkColumn", LinkColumn.class );
        
        /*    */ digester.addSetProperties( "wdkModel/querySet/sqlQuery/linkColumn");
        
        /*    */ digester.addSetNext( "wdkModel/querySet/sqlQuery/linkColumn", "addColumn" );
        
        /*    */ digester.addObjectCreate( "wdkModel/querySet/sqlQuery/textColumn", TextColumn.class );
        
        /*    */ digester.addSetProperties( "wdkModel/querySet/sqlQuery/textColumn");
        
        /*    */ digester.addSetNext( "wdkModel/querySet/sqlQuery/textColumn", "addColumn" );
        
        /*  */ digester.addSetNext( "wdkModel/querySet/sqlQuery", "addQuery" );
        
        /**/ digester.addSetNext( "wdkModel/querySet", "addQuerySet" );
        
        
        //ParamSet
        
        /**/ digester.addObjectCreate( "wdkModel/paramSet", ParamSet.class );
        
        /**/ digester.addSetProperties( "wdkModel/paramSet");
        
        /*  */ digester.addObjectCreate( "wdkModel/paramSet/stringParam", StringParam.class );
        
        /*  */ digester.addSetProperties( "wdkModel/paramSet/stringParam");
        
        /*  */ digester.addSetNext( "wdkModel/paramSet/stringParam", "addParam" );
        
        /*  */ digester.addObjectCreate( "wdkModel/paramSet/flatVocabParam", FlatVocabParam.class );
        
        /*  */ digester.addSetProperties( "wdkModel/paramSet/flatVocabParam");
        
        /*  */ digester.addSetNext( "wdkModel/paramSet/flatVocabParam", "addParam" );
        
        /**/ digester.addSetNext( "wdkModel/paramSet", "addParamSet" );
        
        
        //ReferenceList
        
        /**/ digester.addObjectCreate("wdkModel/referenceList", ReferenceList.class);
        
        /**/ digester.addSetProperties("wdkModel/referenceList");
        
        /*  */ digester.addObjectCreate("wdkModel/referenceList/reference", Reference.class);
        
        /*  */ digester.addSetProperties("wdkModel/referenceList/reference");
        
        /*  */ digester.addSetNext("wdkModel/referenceList/reference", "addReference");
        
        /**/ digester.addSetNext("wdkModel/referenceList", "addReferenceList");
        
        //QuestionSet
        
        /**/ digester.addObjectCreate("wdkModel/questionSet", QuestionSet.class);
        
        /**/ digester.addSetProperties("wdkModel/questionSet");
        
        /**/ digester.addBeanPropertySetter( "wdkModel/questionSet/description");
        /*  */ digester.addObjectCreate("wdkModel/questionSet/question", Question.class);
        
        /*  */ digester.addSetProperties("wdkModel/questionSet/question");
        
	/*  */ digester.addBeanPropertySetter("wdkModel/questionSet/question/description");

	/*  */ digester.addBeanPropertySetter("wdkModel/questionSet/question/help");
        
        /*  */ digester.addSetNext("wdkModel/questionSet/question", "addQuestion");
        
        /**/ digester.addSetNext("wdkModel/questionSet", "addQuestionSet");
        
        return digester;
        
    }
    
    /**
     * Substitute property values into model xml
     */
    public static InputStream configureModelFile(URL modelXmlURL, URL modelPropURL) throws WdkModelException {
        
        try {
            StringBuffer substituted = new StringBuffer();
            Properties properties = new Properties();
            properties.load(modelPropURL.openStream());
            BufferedReader reader = 
                new BufferedReader(new InputStreamReader(modelXmlURL.openStream()));
            while (reader.ready()) {
                String line = reader.readLine();
                line = substituteProps(line, properties);
                substituted.append(line);
            }
            
            return new ByteArrayInputStream(substituted.toString().getBytes());
        } catch (FileNotFoundException e) {
            throw new WdkModelException(e);
        } catch (IOException e) {
            throw new WdkModelException(e);
        }
    }
    
    static String substituteProps(String string, Properties properties) {
        Enumeration propNames = properties.propertyNames();
        String newString = string;
        while (propNames.hasMoreElements()) {
            String propName = (String)propNames.nextElement();
            String value = properties.getProperty(propName);
            newString = newString.replaceAll("\\@" + propName + "\\@", value);
        }
        return newString;
    }
    
    public static void main( String[] args ) {
        try {
	    
	    String cmdName = System.getProperties().getProperty("cmdName");
	    
	    // process args
	    Options options = declareOptions();
	    CommandLine cmdLine = parseOptions(cmdName, options, args);
	    
	    File modelConfigXmlFile = 
		new File(cmdLine.getOptionValue("configFile"));
	    File modelXmlFile = new File(cmdLine.getOptionValue("modelXmlFile"));
	    File modelPropFile = new File(cmdLine.getOptionValue("modelPropFile"));

            File schemaFile = new File(System.getProperty("schemaFile"));
            WdkModel wdkModel = parseXmlFile(modelXmlFile.toURL(), modelPropFile.toURL(), schemaFile.toURL(), modelConfigXmlFile.toURL());
            
            System.out.println( wdkModel.toString() );
            
        } catch( Exception e ) {
            System.err.println(e.getMessage());
            System.err.println("");
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
	addOption(options, "configFile", "the model config .xml file");

	// model file
	addOption(options, "modelXmlFile", "An .xml file that specifies WDK Model.");
	// model prop file
	addOption(options, "modelPropFile", "A .prop file that specifies key=value pairs to substitute into the model file.");
	
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
            " -modelPropFile model_prop_file" +
            " -modelXmlFile model_xml_file";
        
        String header = 
            newline + "Parse and print out a WDK Model xml file." + newline + newline + "Options:" ;
        
        String footer = "";
        
        //	PrintWriter stderr = new PrintWriter(System.err);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(75, cmdlineSyntax, header, options, footer);
        System.exit(1);
    }


}
