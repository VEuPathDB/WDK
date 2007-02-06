package org.gusdb.wdk.model.implementation;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.digester.Digester;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.gusdb.wdk.model.*;
import org.gusdb.wdk.model.xml.XmlAttributeField;
import org.gusdb.wdk.model.xml.XmlQuestion;
import org.gusdb.wdk.model.xml.XmlQuestionSet;
import org.gusdb.wdk.model.xml.XmlRecordClass;
import org.gusdb.wdk.model.xml.XmlRecordClassSet;
import org.gusdb.wdk.model.xml.XmlTableField;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.SinglePropertyMap;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;

public class ModelXmlParser {

    static Logger logger = Logger.getRootLogger();

    private static final String DEFAULT_SCHEMA_NAME = "wdkModel.rng";

    public static WdkModel parseXmlFile(URL modelXmlURL, URL modelPropURL,
            URL schemaURL, URL xmlSchemaURL, URL modelConfigXmlFileURL)
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
            InputStream modelXmlStream = makeModelXmlStream(modelXmlURL,
                    modelPropURL);

            model = (WdkModel) digester.parse(modelXmlStream);

        } catch (SAXException e) {
            throw new WdkModelException(e);
        } catch (IOException e) {
            throw new WdkModelException(e);
        }

        setModelDocument(model, modelXmlURL, modelPropURL);
        model.resolveReferences();
        model.setXmlSchema(xmlSchemaURL); // set schema for xml data source

        try {
            model.configure(modelConfigXmlFileURL);
            model.setResources();
	    model.setProperties(getPropMap(modelPropURL));
        } catch (Exception e) {
            throw new WdkModelException(e);
        }
        return model;
    }

    private static InputStream makeModelXmlStream(URL modelXmlURL,
            URL modelPropURL) throws WdkModelException {
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

    private static void setModelDocument(WdkModel model, URL modelXmlURL,
            URL modelPropURL) throws WdkModelException {
        try {
            InputStream modelXmlStream = makeModelXmlStream(modelXmlURL,
                    modelPropURL);
            model.setDocument(buildDocument(modelXmlStream));
        } catch (SAXException e) {
            throw new WdkModelException(e);
        } catch (IOException e) {
            throw new WdkModelException(e);
        } catch (ParserConfigurationException e) {
            throw new WdkModelException(e);
        }
    }

    public static Document buildDocument(InputStream modelXMLStream)
            throws ParserConfigurationException, SAXException, IOException {

        Document doc = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Turn on validation, and turn off namespaces
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        // ErrorHandler errorHandler = new ErrorHandlerImpl(System.err);
        // builder.setErrorHandler(errorHandler);
        builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
            // ignore fatal errors (an exception is guaranteed)
            public void fatalError(SAXParseException exception)
                    throws SAXException {
                exception.printStackTrace(System.err);
            }

            // treat validation errors as fatal
            public void error(SAXParseException e) throws SAXParseException {
                e.printStackTrace(System.err);
                throw e;
            }

            // dump warnings too
            public void warning(SAXParseException err) throws SAXParseException {
                System.err.println("** Warning" + ", line "
                        + err.getLineNumber() + ", uri " + err.getSystemId());
                System.err.println("   " + err.getMessage());
            }
        });

        doc = builder.parse(modelXMLStream);
        return doc;
    }

    private static boolean validModelFile(URL modelXmlURL, URL schemaURL)
            throws WdkModelException {

        System.setProperty(
                "org.apache.xerces.xni.parser.XMLParserConfiguration",
                "org.apache.xerces.parsers.XIncludeParserConfiguration");

        try {

            ErrorHandler errorHandler = new ErrorHandlerImpl(System.err);
            PropertyMap schemaProperties = new SinglePropertyMap(
                    ValidateProperty.ERROR_HANDLER, errorHandler);
            ValidationDriver vd = new ValidationDriver(schemaProperties,
                    PropertyMap.EMPTY, null);

            vd.loadSchema(ValidationDriver.uriOrFileInputSource(schemaURL.toExternalForm()));

            // System.err.println("modelXMLURL is "+modelXmlURL);

            InputSource is = ValidationDriver.uriOrFileInputSource(modelXmlURL.toExternalForm());
            // return vd.validate(new InputSource(modelXMLStream));
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

        // Root -- WDK Model

        digester.addObjectCreate("wdkModel", WdkModel.class);
        digester.addSetProperties("wdkModel");

        /**/digester.addBeanPropertySetter("wdkModel/introduction");

        /**/digester.addBeanPropertySetter("wdkModel/historyDatasetLink");

        // RecordClassSet

        /**/digester.addObjectCreate("wdkModel/recordClassSet",
                RecordClassSet.class);

        /**/digester.addSetProperties("wdkModel/recordClassSet");

        /*  */digester.addObjectCreate("wdkModel/recordClassSet/recordClass",
                RecordClass.class);

        /*  */digester.addSetProperties("wdkModel/recordClassSet/recordClass");

        // By Jerric - parse projectParamRef
        /*    */digester.addObjectCreate(
                "wdkModel/recordClassSet/recordClass/projectParamRef",
                ParamReference.class);

        /*    */digester.addSetProperties("wdkModel/recordClassSet/recordClass/projectParamRef");

        /*    */digester.addSetNext(
                "wdkModel/recordClassSet/recordClass/projectParamRef",
                "setProjectParamRef");
        // end by jerric

        /*    */digester.addObjectCreate(
                "wdkModel/recordClassSet/recordClass/reporter",
                ReporterRef.class);

        /*    */digester.addSetProperties("wdkModel/recordClassSet/recordClass/reporter");

        /*    */digester.addSetNext(
                "wdkModel/recordClassSet/recordClass/reporter",
                "addReporterRef");

        // load attributeQueryRef along with the attributes associated with it
        
        /*    */digester.addObjectCreate(
                "wdkModel/recordClassSet/recordClass/attributeQueryRef",
                AttributeQueryReference.class);

        /*    */digester.addSetProperties(
                "wdkModel/recordClassSet/recordClass/attributeQueryRef");


        /*      */digester.addObjectCreate(
                "wdkModel/recordClassSet/recordClass/attributeQueryRef/columnAttribute",
                ColumnAttributeField.class);

        /*      */digester.addSetProperties(
                "wdkModel/recordClassSet/recordClass/attributeQueryRef/columnAttribute");

        /*      */digester.addSetNext(
                "wdkModel/recordClassSet/recordClass/attributeQueryRef/columnAttribute",
                "addAttributeField");

        /*    */digester.addObjectCreate(
                "wdkModel/recordClassSet/recordClass/attributeQueryRef/linkAttribute",
                LinkAttributeField.class);

        /*    */digester.addSetProperties(
                "wdkModel/recordClassSet/recordClass/attributeQueryRef/linkAttribute");

        /*    */digester.addBeanPropertySetter(
                "wdkModel/recordClassSet/recordClass/attributeQueryRef/linkAttribute/url");

        /*    */digester.addSetNext(
                "wdkModel/recordClassSet/recordClass/attributeQueryRef/linkAttribute",
                "addAttributeField");

        /*    */digester.addObjectCreate(
                "wdkModel/recordClassSet/recordClass/attributeQueryRef/textAttribute",
                TextAttributeField.class);

        /*    */digester.addSetProperties(
                "wdkModel/recordClassSet/recordClass/attributeQueryRef/textAttribute");

        /*      */digester.addBeanPropertySetter(
                "wdkModel/recordClassSet/recordClass/attributeQueryRef/textAttribute/text");

        /*    */digester.addSetNext(
                "wdkModel/recordClassSet/recordClass/attributeQueryRef/textAttribute",
                "addAttributeField");
        
       
        /*    */digester.addSetNext(
                "wdkModel/recordClassSet/recordClass/attributeQueryRef",
                "addAttributesQueryRef");
        
        
        // load the table field along with the attribute associated with it
        /*    */digester.addObjectCreate( "wdkModel/recordClassSet/recordClass/table",
                TableField.class);

        /*    */digester.addSetProperties("wdkModel/recordClassSet/recordClass/table");
        
        
        /*      */digester.addObjectCreate(
                "wdkModel/recordClassSet/recordClass/table/columnAttribute",
                ColumnAttributeField.class);

        /*      */digester.addSetProperties(
                "wdkModel/recordClassSet/recordClass/table/columnAttribute");

        /*      */digester.addSetNext(
                "wdkModel/recordClassSet/recordClass/table/columnAttribute",
                "addAttributeField");

        /*    */digester.addObjectCreate(
                "wdkModel/recordClassSet/recordClass/table/linkAttribute",
                LinkAttributeField.class);

        /*    */digester.addSetProperties(
                "wdkModel/recordClassSet/recordClass/table/linkAttribute");

        /*    */digester.addBeanPropertySetter(
                "wdkModel/recordClassSet/recordClass/table/linkAttribute/url");

        /*    */digester.addSetNext(
                "wdkModel/recordClassSet/recordClass/table/linkAttribute",
                "addAttributeField");

        /*    */digester.addObjectCreate(
                "wdkModel/recordClassSet/recordClass/table/textAttribute",
                TextAttributeField.class);

        /*    */digester.addSetProperties(
                "wdkModel/recordClassSet/recordClass/table/textAttribute");

        /*      */digester.addBeanPropertySetter(
                "wdkModel/recordClassSet/recordClass/table/textAttribute/text");

        /*    */digester.addSetNext(
                "wdkModel/recordClassSet/recordClass/table/textAttribute",
                "addAttributeField");
        

        /*    */digester.addSetNext("wdkModel/recordClassSet/recordClass/table",
                "addTableField");
        
        // load link attribute & text attribute directly belong to RecordClass

        /*    */digester.addObjectCreate(
                "wdkModel/recordClassSet/recordClass/linkAttribute",
                LinkAttributeField.class);

        /*    */digester.addSetProperties("wdkModel/recordClassSet/recordClass/linkAttribute");

        /*    */digester.addBeanPropertySetter("wdkModel/recordClassSet/recordClass/linkAttribute/url");

        /*    */digester.addSetNext(
                "wdkModel/recordClassSet/recordClass/linkAttribute",
                "addAttributeField");

        /*    */digester.addObjectCreate(
                "wdkModel/recordClassSet/recordClass/textAttribute",
                TextAttributeField.class);

        /*    */digester.addSetProperties("wdkModel/recordClassSet/recordClass/textAttribute");

        /*      */digester.addBeanPropertySetter("wdkModel/recordClassSet/recordClass/textAttribute/text");

        /*    */digester.addSetNext(
                "wdkModel/recordClassSet/recordClass/textAttribute",
                "addAttributeField");

        /*    */digester.addObjectCreate(
                "wdkModel/recordClassSet/recordClass/nestedRecord",
                NestedRecord.class);

        /*    */digester.addSetProperties("wdkModel/recordClassSet/recordClass/nestedRecord");

        /*    */digester.addSetNext(
                "wdkModel/recordClassSet/recordClass/nestedRecord",
                "addNestedRecordQuestionRef");

        /*    */digester.addObjectCreate(
                "wdkModel/recordClassSet/recordClass/nestedRecordList",
                NestedRecordList.class);

        /*    */digester.addSetProperties("wdkModel/recordClassSet/recordClass/nestedRecordList");

        /*    */digester.addSetNext(
                "wdkModel/recordClassSet/recordClass/nestedRecordList",
                "addNestedRecordListQuestionRef");

        /*  */digester.addSetNext("wdkModel/recordClassSet/recordClass",
                "addRecordClass");

        /**/digester.addSetNext("wdkModel/recordClassSet", "addRecordClassSet");

        // QuerySet

        /**/digester.addObjectCreate("wdkModel/querySet", QuerySet.class);

        /**/digester.addSetProperties("wdkModel/querySet");
        /*  */digester.addObjectCreate("wdkModel/querySet/sqlQuery",
                SqlQuery.class);

        /*  */digester.addSetProperties("wdkModel/querySet/sqlQuery");

        /*  */digester.addBeanPropertySetter("wdkModel/querySet/sqlQuery/sql");
        /*  */digester.addBeanPropertySetter("wdkModel/querySet/sqlQuery/description");

        /*    */digester.addObjectCreate(
                "wdkModel/querySet/sqlQuery/paramRef", ParamReference.class);

        /*    */digester.addSetProperties("wdkModel/querySet/sqlQuery/paramRef");

        /*    */digester.addSetNext("wdkModel/querySet/sqlQuery/paramRef",
                "addParamRef");

        /*    */digester.addObjectCreate("wdkModel/querySet/sqlQuery/column",
                Column.class);

        /*    */digester.addSetProperties("wdkModel/querySet/sqlQuery/column");

        /*    */digester.addSetNext("wdkModel/querySet/sqlQuery/column",
                "addColumn");

        /*  */digester.addSetNext("wdkModel/querySet/sqlQuery", "addQuery");

        /*  */digester.addObjectCreate("wdkModel/querySet/wsQuery",
                WSQuery.class);

        /*  */digester.addSetProperties("wdkModel/querySet/wsQuery");

        /*  */digester.addBeanPropertySetter("wdkModel/querySet/wsQuery/description");

        /*    */digester.addObjectCreate(
                "wdkModel/querySet/wsQuery/paramRef", ParamReference.class);

        /*    */digester.addSetProperties("wdkModel/querySet/wsQuery/paramRef");

        /*    */digester.addSetNext("wdkModel/querySet/wsQuery/paramRef",
                "addParamRef");

        /*    */digester.addObjectCreate("wdkModel/querySet/wsQuery/wsColumn",
                Column.class);

        /*    */digester.addSetProperties("wdkModel/querySet/wsQuery/wsColumn");

        /*    */digester.addSetNext("wdkModel/querySet/wsQuery/wsColumn",
                "addColumn");

        /*  */digester.addSetNext("wdkModel/querySet/wsQuery", "addQuery");


        /**/digester.addSetNext("wdkModel/querySet", "addQuerySet");

        // ParamSet

        /**/digester.addObjectCreate("wdkModel/paramSet", ParamSet.class);

        /**/digester.addSetProperties("wdkModel/paramSet");

        /*  */digester.addObjectCreate("wdkModel/paramSet/stringParam",
                StringParam.class);

        /*  */digester.addSetProperties("wdkModel/paramSet/stringParam");

        /*  */digester.addSetNext("wdkModel/paramSet/stringParam", "addParam");

        /*  */digester.addObjectCreate("wdkModel/paramSet/flatVocabParam",
                FlatVocabParam.class);

        /*  */digester.addSetProperties("wdkModel/paramSet/flatVocabParam");

        /*  */digester.addSetNext("wdkModel/paramSet/flatVocabParam",
                "addParam");
        
        /*  */digester.addObjectCreate("wdkModel/paramSet/historyParam",
                HistoryParam.class);

        /*  */digester.addSetProperties("wdkModel/paramSet/historyParam");

        /*  */digester.addSetNext("wdkModel/paramSet/historyParam", "addParam");
        
        /*  */digester.addObjectCreate("wdkModel/paramSet/datasetParam",
                DatasetParam.class);

        /*  */digester.addSetProperties("wdkModel/paramSet/datasetParam");

        /*  */digester.addSetNext("wdkModel/paramSet/datasetParam", "addParam");
        

        /**/digester.addSetNext("wdkModel/paramSet", "addParamSet");

        // ReferenceList

        /**/digester.addObjectCreate("wdkModel/referenceList",
                ReferenceList.class);

        /*  */digester.addSetProperties("wdkModel/referenceList");

        /*  */digester.addObjectCreate("wdkModel/referenceList/reference",
                Reference.class);

        /*  */digester.addSetProperties("wdkModel/referenceList/reference");

        /*  */digester.addSetNext("wdkModel/referenceList/reference",
                "addReference");

        /**/digester.addSetNext("wdkModel/referenceList", "addReferenceList");

        // QuestionSet

        /**/digester.addObjectCreate("wdkModel/questionSet", QuestionSet.class);

        /*  */digester.addSetProperties("wdkModel/questionSet");

        /*  */digester.addBeanPropertySetter("wdkModel/questionSet/description");
        /*  */digester.addObjectCreate("wdkModel/questionSet/question",
                Question.class);

        /*    */digester.addSetProperties("wdkModel/questionSet/question");

        /*    */digester.addBeanPropertySetter("wdkModel/questionSet/question/description");

        /*    */digester.addBeanPropertySetter("wdkModel/questionSet/question/summary");

        /*    */digester.addBeanPropertySetter("wdkModel/questionSet/question/help");

        /*    */digester.addObjectCreate(
                "wdkModel/questionSet/question/dynamicAttributes",
                DynamicAttributeSet.class);

        /*      */digester.addSetProperties(
                "wdkModel/questionSet/question/dynamicAttributes");

        /*      */digester.addObjectCreate(
                "wdkModel/questionSet/question/dynamicAttributes/columnAttribute", 
                ColumnAttributeField.class);

        /*        */digester.addSetProperties(
                "wdkModel/questionSet/question/dynamicAttributes/columnAttribute");

        /*      */digester.addSetNext(
                "wdkModel/questionSet/question/dynamicAttributes/columnAttribute", 
                "addAttributeField");
        
        /*      */digester.addObjectCreate(
                    "wdkModel/questionSet/question/dynamicAttributes/linkAttribute",
                    LinkAttributeField.class);

        /*        */digester.addSetProperties(
                "wdkModel/questionSet/question/dynamicAttributes/linkAttribute");

        /*        */digester.addBeanPropertySetter(
                "wdkModel/questionSet/question/dynamicAttributes/linkAttribute/url");

        /*      */digester.addSetNext(
                  "wdkModel/questionSet/question/dynamicAttributes/linkAttribute",
                  "addAttributeField");

        /*      */digester.addObjectCreate(
                  "wdkModel/questionSet/question/dynamicAttributes/textAttribute",
                  TextAttributeField.class);

        /*        */digester.addSetProperties(
                "wdkModel/questionSet/question/dynamicAttributes/textAttribute");

        /*        */digester.addBeanPropertySetter(
                "wdkModel/questionSet/question/dynamicAttributes/textAttribute/text");

        /*      */digester.addSetNext(
                  "wdkModel/questionSet/question/dynamicAttributes/textAttribute",
                  "addAttributeField");

        /*    */digester.addSetNext("wdkModel/questionSet/question/dynamicAttributes", "setDynamicAttributeSet");

        /*  */digester.addSetNext("wdkModel/questionSet/question",
                "addQuestion");

        /**/digester.addSetNext("wdkModel/questionSet", "addQuestionSet");

        // load XmlQuestionSet
        digester.addObjectCreate("wdkModel/xmlQuestionSet",
                XmlQuestionSet.class);
        digester.addSetProperties("wdkModel/xmlQuestionSet");
        digester.addBeanPropertySetter("wdkModel/xmlQuestionSet/description");

        // load XmlQuestion
        digester.addObjectCreate("wdkModel/xmlQuestionSet/xmlQuestion",
                XmlQuestion.class);
        digester.addSetProperties("wdkModel/xmlQuestionSet/xmlQuestion");
        digester.addBeanPropertySetter("wdkModel/xmlQuestionSet/xmlQuestion/description");
        digester.addBeanPropertySetter("wdkModel/xmlQuestionSet/xmlQuestion/help");
        digester.addSetNext("wdkModel/xmlQuestionSet/xmlQuestion",
                "addQuestion");

        digester.addSetNext("wdkModel/xmlQuestionSet", "addXmlQuestionSet");

        // load XmlRecordClassSet
        digester.addObjectCreate("wdkModel/xmlRecordClassSet",
                XmlRecordClassSet.class);
        digester.addSetProperties("wdkModel/xmlRecordClassSet");

        // load XmlRecordClass
        digester.addObjectCreate("wdkModel/xmlRecordClassSet/xmlRecordClass",
                XmlRecordClass.class);
        digester.addSetProperties("wdkModel/xmlRecordClassSet/xmlRecordClass");

        // load XmlAttributeField
        digester.addObjectCreate(
                "wdkModel/xmlRecordClassSet/xmlRecordClass/xmlAttribute",
                XmlAttributeField.class);
        digester.addSetProperties(
                "wdkModel/xmlRecordClassSet/xmlRecordClass/xmlAttribute");
        digester.addSetNext(
                "wdkModel/xmlRecordClassSet/xmlRecordClass/xmlAttribute",
                "addAttributeField");

        // load XmlTableField
        digester.addObjectCreate(
                "wdkModel/xmlRecordClassSet/xmlRecordClass/xmlTable",
                XmlTableField.class);
        digester.addSetProperties(
                "wdkModel/xmlRecordClassSet/xmlRecordClass/xmlTable");

        // load XmlAttributeField within table
        digester.addObjectCreate(
                "wdkModel/xmlRecordClassSet/xmlRecordClass/xmlTable/xmlAttribute",
                XmlAttributeField.class);
        digester.addSetProperties(
                "wdkModel/xmlRecordClassSet/xmlRecordClass/xmlTable/xmlAttribute");
        digester.addSetNext(
                "wdkModel/xmlRecordClassSet/xmlRecordClass/xmlTable/xmlAttribute",
                "addAttributeField");

        digester.addSetNext(
                "wdkModel/xmlRecordClassSet/xmlRecordClass/xmlTable",
                "addTableField");

        digester.addSetNext("wdkModel/xmlRecordClassSet/xmlRecordClass",
                "addRecordClass");

        digester.addSetNext("wdkModel/xmlRecordClassSet",
                "addXmlRecordClassSet");

        return digester;

    }

    /**
     * Substitute property values into model xml
     */
    public static InputStream configureModelFile(URL modelXmlURL,
            URL modelPropURL) throws WdkModelException {

        try {
            StringBuffer substituted = new StringBuffer();
            Properties properties = new Properties();
            properties.load(modelPropURL.openStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    modelXmlURL.openStream()));
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
            String propName = (String) propNames.nextElement();
            String value = properties.getProperty(propName);
            newString = newString.replaceAll("\\@" + propName + "\\@", value);
        }
        return newString;
    }

    static Map<String, String> getPropMap(URL modelPropURL)  throws WdkModelException {
	Map<String, String> propMap = new LinkedHashMap<String, String>();
        try {
	    Properties properties = new Properties();
	    properties.load(modelPropURL.openStream());
	    Enumeration propNames = properties.propertyNames();
	    while (propNames.hasMoreElements()) {
		String propName = (String) propNames.nextElement();
		String value = properties.getProperty(propName);
		propMap.put(propName, value);
	    }
	} catch (IOException e) {
            throw new WdkModelException(e);
        }
	return propMap;
    }

    public static void main(String[] args) {
	BasicConfigurator.configure(); // logger
	logger.setLevel(Level.ERROR);

        try {

            String cmdName = System.getProperties().getProperty("cmdName");
            File configDir = new File(System.getProperties().getProperty(
                    "configDir"));

            // process args
            Options options = declareOptions();
            CommandLine cmdLine = parseOptions(cmdName, options, args);

            String modelName = cmdLine.getOptionValue("model");

            File modelConfigXmlFile = new File(configDir, modelName
                    + "-config.xml");
            File modelXmlFile = new File(configDir, modelName + ".xml");
            File modelPropFile = new File(configDir, modelName + ".prop");

            File schemaFile = new File(System.getProperty("schemaFile"));

            // load schema for xml data source
            File xmlSchemaFile = new File(System.getProperty("xmlSchemaFile"));

            WdkModel wdkModel = parseXmlFile(modelXmlFile.toURL(),
                    modelPropFile.toURL(), schemaFile.toURL(),
                    xmlSchemaFile.toURL(), modelConfigXmlFile.toURL());

            // load the xml data path
            File xmlDataDir = new File(System.getProperty("xmlDataDir"));
            wdkModel.setXmlDataDir(xmlDataDir);
            
            System.out.println(wdkModel.toString());

        } catch (Exception e) {
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
        addOption(
                options,
                "model",
                "the name of the model.  This is used to find the Model XML file ($GUS_HOME/config/model_name.xml) the Model property file ($GUS_HOME/config/model_name.prop) and the Model config file ($GUS_HOME/config/model_name-config.xml)");

        return options;
    }

    static CommandLine parseOptions(String cmdName, Options options,
            String[] args) {

        CommandLineParser parser = new BasicParser();
        CommandLine cmdLine = null;
        try {
            // parse the command line arguments
            cmdLine = parser.parse(options, args);
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("");
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            System.err.println("");
            usage(cmdName, options);
        }

        return cmdLine;
    }

    static void usage(String cmdName, Options options) {

        String newline = System.getProperty("line.separator");
        String cmdlineSyntax = cmdName + " -model model_name";

        String header = newline + "Parse and print out a WDK Model xml file."
                + newline + newline + "Options:";

        String footer = "";

        // PrintWriter stderr = new PrintWriter(System.err);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(75, cmdlineSyntax, header, options, footer);
        System.exit(1);
    }

}
