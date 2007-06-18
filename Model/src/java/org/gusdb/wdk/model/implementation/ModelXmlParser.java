package org.gusdb.wdk.model.implementation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;
import org.gusdb.wdk.model.*;
import org.gusdb.wdk.model.xml.XmlAttributeField;
import org.gusdb.wdk.model.xml.XmlQuestion;
import org.gusdb.wdk.model.xml.XmlQuestionSet;
import org.gusdb.wdk.model.xml.XmlRecordClass;
import org.gusdb.wdk.model.xml.XmlRecordClassSet;
import org.gusdb.wdk.model.xml.XmlTableField;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
    
    public static final String PROP_MODEL_NAME = "model";
    public static final String PROP_CONFIG_DIR = "configDir";
    public static final String PROP_SCHEMA_FILE = "schemaFile";
    public static final String PROP_XML_SCHEMA_FILE = "xmlSchemaFile";
    public static final String PROP_XML_DATA_DIR = "xmlDataDir";
    
    private static final String DEFAULT_SCHEMA = "wdkModel.rng";
    private static final String DEFAULT_XML_SCHEMA = "xmlAnswer.rng";
    
    private static final Logger logger = Logger.getLogger( ModelXmlParser.class );
    
    public static WdkModel parseXmlFile( URL modelXmlURL, URL modelPropURL,
            URL modelConfigXmlFileURL ) throws WdkModelException {
        // use default value
        URL schemaURL = WdkModel.class.getResource( DEFAULT_SCHEMA );
        URL xmlSchemaURL = WdkModel.class.getResource( DEFAULT_XML_SCHEMA );
        
        return parseXmlFile( modelXmlURL, modelPropURL, schemaURL,
                xmlSchemaURL, modelConfigXmlFileURL );
    }
    
    public static WdkModel parseXmlFile( URL modelXmlURL, URL modelPropURL,
            URL schemaURL, URL xmlSchemaURL, URL modelConfigXmlFileURL )
            throws WdkModelException {
        // prepare the validator, since it might be used multiple times
        ValidationDriver validator = prepareValidator( schemaURL );
        
        // validate the master model file
        if ( !validateModel( validator, modelXmlURL ) )
            throw new WdkModelException( "Master model validation failed." );
        
        // process any <import> tag in the master model, and build the master
        // document
        Document masterDoc = buildMasterDocument( modelXmlURL, validator );
        
        // load property map
        Map< String, String > properties = getPropMap( modelPropURL );
        InputStream modelXmlStream = substituteProps( masterDoc, properties );
        
        // parse the model xml into java objects
        Digester digester = configureDigester();
        
        try {
            WdkModel model = ( WdkModel ) digester.parse( modelXmlStream );
            
            model.resolveReferences();
            model.setXmlSchema( xmlSchemaURL ); // set schema for xml data
            // source
            
            model.configure( modelConfigXmlFileURL );
            model.setResources();
            model.setProperties( properties );
            
            return model;
        } catch ( SAXException e ) {
            throw new WdkModelException( e );
        } catch ( IOException e ) {
            throw new WdkModelException( e );
        }
    }
    
    private static ValidationDriver prepareValidator( URL schemaURL )
            throws WdkModelException {
        
        System.setProperty(
                "org.apache.xerces.xni.parser.XMLParserConfiguration",
                "org.apache.xerces.parsers.XIncludeParserConfiguration" );
        
        ErrorHandler errorHandler = new ErrorHandlerImpl( System.err );
        PropertyMap schemaProperties = new SinglePropertyMap(
                ValidateProperty.ERROR_HANDLER, errorHandler );
        ValidationDriver validator = new ValidationDriver( schemaProperties,
                PropertyMap.EMPTY, null );
        
        try {
            validator.loadSchema( ValidationDriver.uriOrFileInputSource( schemaURL.toExternalForm() ) );
            return validator;
        } catch ( MalformedURLException ex ) {
            throw new WdkModelException( ex );
        } catch ( SAXException ex ) {
            throw new WdkModelException( ex );
        } catch ( IOException ex ) {
            throw new WdkModelException( ex );
        }
    }
    
    private static boolean validateModel( ValidationDriver validator,
            URL modelXmlURL ) throws WdkModelException {
        try {
            // System.err.println("modelXMLURL is "+modelXmlURL);
            InputSource is = ValidationDriver.uriOrFileInputSource( modelXmlURL.toExternalForm() );
            return validator.validate( is );
        } catch ( SAXException ex ) {
            throw new WdkModelException( ex );
        } catch ( IOException ex ) {
            throw new WdkModelException( ex );
        }
    }
    
    private static Document buildMasterDocument( URL wdkModelURL,
            ValidationDriver validator ) throws WdkModelException {
        // get the xml document of the model
        Document masterDoc = buildDocument( wdkModelURL );
        Node rootNode = masterDoc.getElementsByTagName( "wdkModel" ).item( 0 );
        
        // get the parent url, removing the model file name from it
        String parentURL = wdkModelURL.toExternalForm();
        parentURL = parentURL.substring( 0, parentURL.lastIndexOf( "/" ) + 1 );
        
        // get all imports, and replace each of them with the sub-model
        NodeList importNodes = masterDoc.getElementsByTagName( "import" );
        for ( int i = 0; i < importNodes.getLength(); i++ ) {
            // get url to the first import
            Node importNode = importNodes.item( i );
            String href = importNode.getAttributes().getNamedItem( "href" ).getNodeValue();
            try {
                URL importURL = new URL( parentURL + href );
                
                logger.info( "Importing: " + importURL.toExternalForm() );
                
                Document importDoc = buildDocument( importURL );
                
                // get the children nodes from imported sub-model, and add them
                // into master document
                Node subRoot = importDoc.getElementsByTagName( "wdkModel" ).item(
                        0 );
                NodeList childrenNodes = subRoot.getChildNodes();
                for ( int j = 0; j < childrenNodes.getLength(); j++ ) {
                    Node childNode = childrenNodes.item( j );
                    if ( childNode instanceof Element ) {
                        Node imported = masterDoc.importNode( childNode, true );
                        rootNode.appendChild( imported );
                    }
                }
            } catch ( MalformedURLException ex ) {
                throw new WdkModelException( ex );
            }
        }
        return masterDoc;
    }
    
    private static Document buildDocument( URL modelXmlURL )
            throws WdkModelException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // turn off validation here, since we don't use DTD; validation is done
        // before this point
        factory.setValidating( false );
        factory.setNamespaceAware( false );
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            
            // ErrorHandler errorHandler = new ErrorHandlerImpl(System.err);
            // builder.setErrorHandler(errorHandler);
            builder.setErrorHandler( new org.xml.sax.ErrorHandler() {
                
                // ignore fatal errors (an exception is guaranteed)
                public void fatalError( SAXParseException exception )
                        throws SAXException {
                    exception.printStackTrace( System.err );
                }
                
                // treat validation errors as fatal
                public void error( SAXParseException e )
                        throws SAXParseException {
                    e.printStackTrace( System.err );
                    throw e;
                }
                
                // dump warnings too
                public void warning( SAXParseException err )
                        throws SAXParseException {
                    System.err.println( "** Warning" + ", line "
                            + err.getLineNumber() + ", uri "
                            + err.getSystemId() );
                    System.err.println( "   " + err.getMessage() );
                }
            } );
            
            Document doc = builder.parse( modelXmlURL.openStream() );
            return doc;
        } catch ( ParserConfigurationException ex ) {
            throw new WdkModelException( ex );
        } catch ( SAXException ex ) {
            throw new WdkModelException( ex );
        } catch ( IOException ex ) {
            throw new WdkModelException( ex );
        }
    }
    
    private static Map< String, String > getPropMap( URL modelPropURL )
            throws WdkModelException {
        Map< String, String > propMap = new LinkedHashMap< String, String >();
        try {
            Properties properties = new Properties();
            properties.load( modelPropURL.openStream() );
            Iterator< Object > it = properties.keySet().iterator();
            while ( it.hasNext() ) {
                String propName = ( String ) it.next();
                String value = properties.getProperty( propName );
                propMap.put( propName, value );
            }
        } catch ( IOException e ) {
            throw new WdkModelException( e );
        }
        return propMap;
    }
    
    private static InputStream substituteProps( Document masterDoc,
            Map< String, String > properties ) throws WdkModelException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            
            // transform the DOM doc to a string
            Source source = new DOMSource( masterDoc );
            Result result = new StreamResult( out );
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform( source, result );
            String content = new String( out.toByteArray() );
            
            // substitute prop macros
            for ( String propName : properties.keySet() ) {
                String propValue = properties.get( propName );
                content = content.replaceAll( "\\@" + propName + "\\@",
                        propValue );
            }
            
            // construct input stream
            return new ByteArrayInputStream( content.getBytes() );
        } catch ( TransformerConfigurationException ex ) {
            throw new WdkModelException( ex );
        } catch ( TransformerFactoryConfigurationError ex ) {
            throw new WdkModelException( ex );
        } catch ( TransformerException ex ) {
            throw new WdkModelException( ex );
        }
        
    }
    
    private static Digester configureDigester() {
        
        Digester digester = new Digester();
        digester.setValidating( false );
        
        // Root -- WDK Model
        digester.addObjectCreate( "wdkModel", WdkModel.class );
        digester.addSetProperties( "wdkModel" );
        digester.addBeanPropertySetter( "wdkModel/introduction" );
        digester.addBeanPropertySetter( "wdkModel/historyDatasetLink" );
        
        configureNode( digester, "wdkModel/recordClassSet",
                RecordClassSet.class, "addRecordClassSet" );
        
        configureNode( digester, "wdkModel/recordClassSet/recordClass",
                RecordClass.class, "addRecordClass" );
        
        configureNode( digester,
                "wdkModel/recordClassSet/recordClass/projectParamRef",
                ParamReference.class, "setProjectParamRef" );
        
        configureNode( digester,
                "wdkModel/recordClassSet/recordClass/reporter",
                ReporterRef.class, "addReporterRef" );
        
        /*    */digester.addSetProperties( "wdkModel/recordClassSet/recordClass/reporter" );
        
        /*       */digester.addObjectCreate(
                "wdkModel/recordClassSet/recordClass/reporter/property",
                ReporterProperty.class );
        
        /*       */digester.addSetProperties( "wdkModel/recordClassSet/recordClass/reporter/property" );
        
        /*       */digester.addBeanPropertySetter( "wdkModel/recordClassSet/recordClass/reporter/property/value" );
        
        /*       */digester.addSetNext(
                "wdkModel/recordClassSet/recordClass/reporter/property",
                "addProperty" );
        
        // load attributeQueryRef along with the attributes associated with it
        
        /*    */digester.addObjectCreate(
                "wdkModel/recordClassSet/recordClass/attributeQueryRef",
                AttributeQueryReference.class );
        
        /*    */digester.addSetProperties( "wdkModel/recordClassSet/recordClass/attributeQueryRef" );
        
        /*      */digester.addObjectCreate(
                "wdkModel/recordClassSet/recordClass/attributeQueryRef/columnAttribute",
                ColumnAttributeField.class );
        
        /*      */digester.addSetProperties( "wdkModel/recordClassSet/recordClass/attributeQueryRef/columnAttribute" );
        
        /*      */digester.addSetNext(
                "wdkModel/recordClassSet/recordClass/attributeQueryRef/columnAttribute",
                "addAttributeField" );
        
        /*    */digester.addObjectCreate(
                "wdkModel/recordClassSet/recordClass/attributeQueryRef/linkAttribute",
                LinkAttributeField.class );
        
        /*    */digester.addSetProperties( "wdkModel/recordClassSet/recordClass/attributeQueryRef/linkAttribute" );
        
        /*    */digester.addBeanPropertySetter( "wdkModel/recordClassSet/recordClass/attributeQueryRef/linkAttribute/url" );
        
        /*    */digester.addSetNext(
                "wdkModel/recordClassSet/recordClass/attributeQueryRef/linkAttribute",
                "addAttributeField" );
        
        /*    */digester.addObjectCreate(
                "wdkModel/recordClassSet/recordClass/attributeQueryRef/textAttribute",
                TextAttributeField.class );
        
        /*    */digester.addSetProperties( "wdkModel/recordClassSet/recordClass/attributeQueryRef/textAttribute" );
        
        /*      */digester.addBeanPropertySetter( "wdkModel/recordClassSet/recordClass/attributeQueryRef/textAttribute/text" );
        
        /*    */digester.addSetNext(
                "wdkModel/recordClassSet/recordClass/attributeQueryRef/textAttribute",
                "addAttributeField" );
        
        /*    */digester.addSetNext(
                "wdkModel/recordClassSet/recordClass/attributeQueryRef",
                "addAttributesQueryRef" );
        
        // load the table field along with the attribute associated with it
        /*    */digester.addObjectCreate(
                "wdkModel/recordClassSet/recordClass/table", TableField.class );
        
        /*    */digester.addSetProperties( "wdkModel/recordClassSet/recordClass/table" );
        
        /*      */digester.addObjectCreate(
                "wdkModel/recordClassSet/recordClass/table/columnAttribute",
                ColumnAttributeField.class );
        
        /*      */digester.addSetProperties( "wdkModel/recordClassSet/recordClass/table/columnAttribute" );
        
        /*      */digester.addSetNext(
                "wdkModel/recordClassSet/recordClass/table/columnAttribute",
                "addAttributeField" );
        
        /*    */digester.addObjectCreate(
                "wdkModel/recordClassSet/recordClass/table/linkAttribute",
                LinkAttributeField.class );
        
        /*    */digester.addSetProperties( "wdkModel/recordClassSet/recordClass/table/linkAttribute" );
        
        /*    */digester.addBeanPropertySetter( "wdkModel/recordClassSet/recordClass/table/linkAttribute/url" );
        
        /*    */digester.addSetNext(
                "wdkModel/recordClassSet/recordClass/table/linkAttribute",
                "addAttributeField" );
        
        /*    */digester.addObjectCreate(
                "wdkModel/recordClassSet/recordClass/table/textAttribute",
                TextAttributeField.class );
        
        /*    */digester.addSetProperties( "wdkModel/recordClassSet/recordClass/table/textAttribute" );
        
        /*      */digester.addBeanPropertySetter( "wdkModel/recordClassSet/recordClass/table/textAttribute/text" );
        
        /*    */digester.addSetNext(
                "wdkModel/recordClassSet/recordClass/table/textAttribute",
                "addAttributeField" );
        
        /*    */digester.addSetNext(
                "wdkModel/recordClassSet/recordClass/table", "addTableField" );
        
        // load link attribute & text attribute directly belong to RecordClass
        
        /*    */digester.addObjectCreate(
                "wdkModel/recordClassSet/recordClass/linkAttribute",
                LinkAttributeField.class );
        
        /*    */digester.addSetProperties( "wdkModel/recordClassSet/recordClass/linkAttribute" );
        
        /*    */digester.addBeanPropertySetter( "wdkModel/recordClassSet/recordClass/linkAttribute/url" );
        
        /*    */digester.addSetNext(
                "wdkModel/recordClassSet/recordClass/linkAttribute",
                "addAttributeField" );
        
        /*    */digester.addObjectCreate(
                "wdkModel/recordClassSet/recordClass/textAttribute",
                TextAttributeField.class );
        
        /*    */digester.addSetProperties( "wdkModel/recordClassSet/recordClass/textAttribute" );
        
        /*      */digester.addBeanPropertySetter( "wdkModel/recordClassSet/recordClass/textAttribute/text" );
        
        /*    */digester.addSetNext(
                "wdkModel/recordClassSet/recordClass/textAttribute",
                "addAttributeField" );
        
        /*    */digester.addObjectCreate(
                "wdkModel/recordClassSet/recordClass/nestedRecord",
                NestedRecord.class );
        
        /*    */digester.addSetProperties( "wdkModel/recordClassSet/recordClass/nestedRecord" );
        
        /*    */digester.addSetNext(
                "wdkModel/recordClassSet/recordClass/nestedRecord",
                "addNestedRecordQuestionRef" );
        
        /*    */digester.addObjectCreate(
                "wdkModel/recordClassSet/recordClass/nestedRecordList",
                NestedRecordList.class );
        
        /*    */digester.addSetProperties( "wdkModel/recordClassSet/recordClass/nestedRecordList" );
        
        /*    */digester.addSetNext(
                "wdkModel/recordClassSet/recordClass/nestedRecordList",
                "addNestedRecordListQuestionRef" );
        
        // QuerySet
        configureNode( digester, "wdkModel/querySet", QuerySet.class,
                "addQuerySet" );
        
        /*  */digester.addObjectCreate( "wdkModel/querySet/sqlQuery",
                SqlQuery.class );
        
        /*  */digester.addSetProperties( "wdkModel/querySet/sqlQuery" );
        
        /*  */digester.addBeanPropertySetter( "wdkModel/querySet/sqlQuery/sql" );
        /*  */digester.addBeanPropertySetter( "wdkModel/querySet/sqlQuery/description" );
        
        /*    */digester.addObjectCreate(
                "wdkModel/querySet/sqlQuery/paramRef", ParamReference.class );
        
        /*    */digester.addSetProperties( "wdkModel/querySet/sqlQuery/paramRef" );
        
        /*    */digester.addSetNext( "wdkModel/querySet/sqlQuery/paramRef",
                "addParamRef" );
        
        /*    */digester.addObjectCreate( "wdkModel/querySet/sqlQuery/column",
                Column.class );
        
        /*    */digester.addSetProperties( "wdkModel/querySet/sqlQuery/column" );
        
        /*    */digester.addSetNext( "wdkModel/querySet/sqlQuery/column",
                "addColumn" );
        
        /*  */digester.addSetNext( "wdkModel/querySet/sqlQuery", "addQuery" );
        
        /*  */digester.addObjectCreate( "wdkModel/querySet/wsQuery",
                WSQuery.class );
        
        /*  */digester.addSetProperties( "wdkModel/querySet/wsQuery" );
        
        /*  */digester.addBeanPropertySetter( "wdkModel/querySet/wsQuery/description" );
        
        /*    */digester.addObjectCreate(
                "wdkModel/querySet/wsQuery/paramRef", ParamReference.class );
        
        /*    */digester.addSetProperties( "wdkModel/querySet/wsQuery/paramRef" );
        
        /*    */digester.addSetNext( "wdkModel/querySet/wsQuery/paramRef",
                "addParamRef" );
        
        /*    */digester.addObjectCreate(
                "wdkModel/querySet/wsQuery/wsColumn", Column.class );
        
        /*    */digester.addSetProperties( "wdkModel/querySet/wsQuery/wsColumn" );
        
        /*    */digester.addSetNext( "wdkModel/querySet/wsQuery/wsColumn",
                "addColumn" );
        
        /*  */digester.addSetNext( "wdkModel/querySet/wsQuery", "addQuery" );
        
        // ParamSet
        
        /**/digester.addObjectCreate( "wdkModel/paramSet", ParamSet.class );
        
        /**/digester.addSetProperties( "wdkModel/paramSet" );
        
        /*  */digester.addObjectCreate( "wdkModel/paramSet/stringParam",
                StringParam.class );
        
        /*  */digester.addSetProperties( "wdkModel/paramSet/stringParam" );
        
        /*  */digester.addSetNext( "wdkModel/paramSet/stringParam", "addParam" );
        
        /*  */digester.addObjectCreate( "wdkModel/paramSet/flatVocabParam",
                FlatVocabParam.class );
        
        /*  */digester.addSetProperties( "wdkModel/paramSet/flatVocabParam" );
        
        /*  */digester.addSetNext( "wdkModel/paramSet/flatVocabParam",
                "addParam" );
        
        /*  */digester.addObjectCreate( "wdkModel/paramSet/historyParam",
                HistoryParam.class );
        
        /*  */digester.addSetProperties( "wdkModel/paramSet/historyParam" );
        
        /*  */digester.addSetNext( "wdkModel/paramSet/historyParam",
                "addParam" );
        
        /*  */digester.addObjectCreate( "wdkModel/paramSet/datasetParam",
                DatasetParam.class );
        
        /*  */digester.addSetProperties( "wdkModel/paramSet/datasetParam" );
        
        /*  */digester.addSetNext( "wdkModel/paramSet/datasetParam",
                "addParam" );
        
        /**/digester.addSetNext( "wdkModel/paramSet", "addParamSet" );
        
        // ReferenceList
        
        /**/digester.addObjectCreate( "wdkModel/referenceList",
                ReferenceList.class );
        
        /*  */digester.addSetProperties( "wdkModel/referenceList" );
        
        /*  */digester.addObjectCreate( "wdkModel/referenceList/reference",
                Reference.class );
        
        /*  */digester.addSetProperties( "wdkModel/referenceList/reference" );
        
        /*  */digester.addSetNext( "wdkModel/referenceList/reference",
                "addReference" );
        
        /**/digester.addSetNext( "wdkModel/referenceList", "addReferenceList" );
        
        // QuestionSet
        
        /**/digester.addObjectCreate( "wdkModel/questionSet",
                QuestionSet.class );
        
        /*  */digester.addSetProperties( "wdkModel/questionSet" );
        
        /*  */digester.addBeanPropertySetter( "wdkModel/questionSet/description" );
        /*  */digester.addObjectCreate( "wdkModel/questionSet/question",
                Question.class );
        
        /*    */digester.addSetProperties( "wdkModel/questionSet/question" );
        
        /*    */digester.addBeanPropertySetter( "wdkModel/questionSet/question/description" );
        
        /*    */digester.addBeanPropertySetter( "wdkModel/questionSet/question/summary" );
        
        /*    */digester.addBeanPropertySetter( "wdkModel/questionSet/question/help" );
        
        /*    */digester.addObjectCreate(
                "wdkModel/questionSet/question/dynamicAttributes",
                DynamicAttributeSet.class );
        
        /*      */digester.addSetProperties( "wdkModel/questionSet/question/dynamicAttributes" );
        
        /*      */digester.addObjectCreate(
                "wdkModel/questionSet/question/dynamicAttributes/columnAttribute",
                ColumnAttributeField.class );
        
        /*        */digester.addSetProperties( "wdkModel/questionSet/question/dynamicAttributes/columnAttribute" );
        
        /*      */digester.addSetNext(
                "wdkModel/questionSet/question/dynamicAttributes/columnAttribute",
                "addAttributeField" );
        
        /*      */digester.addObjectCreate(
                "wdkModel/questionSet/question/dynamicAttributes/linkAttribute",
                LinkAttributeField.class );
        
        /*        */digester.addSetProperties( "wdkModel/questionSet/question/dynamicAttributes/linkAttribute" );
        
        /*        */digester.addBeanPropertySetter( "wdkModel/questionSet/question/dynamicAttributes/linkAttribute/url" );
        
        /*      */digester.addSetNext(
                "wdkModel/questionSet/question/dynamicAttributes/linkAttribute",
                "addAttributeField" );
        
        /*      */digester.addObjectCreate(
                "wdkModel/questionSet/question/dynamicAttributes/textAttribute",
                TextAttributeField.class );
        
        /*        */digester.addSetProperties( "wdkModel/questionSet/question/dynamicAttributes/textAttribute" );
        
        /*        */digester.addBeanPropertySetter( "wdkModel/questionSet/question/dynamicAttributes/textAttribute/text" );
        
        /*      */digester.addSetNext(
                "wdkModel/questionSet/question/dynamicAttributes/textAttribute",
                "addAttributeField" );
        
        /*    */digester.addSetNext(
                "wdkModel/questionSet/question/dynamicAttributes",
                "setDynamicAttributeSet" );
        
        /*  */digester.addSetNext( "wdkModel/questionSet/question",
                "addQuestion" );
        
        /**/digester.addSetNext( "wdkModel/questionSet", "addQuestionSet" );
        
        // load XmlQuestionSet
        digester.addObjectCreate( "wdkModel/xmlQuestionSet",
                XmlQuestionSet.class );
        digester.addSetProperties( "wdkModel/xmlQuestionSet" );
        digester.addBeanPropertySetter( "wdkModel/xmlQuestionSet/description" );
        
        // load XmlQuestion
        digester.addObjectCreate( "wdkModel/xmlQuestionSet/xmlQuestion",
                XmlQuestion.class );
        digester.addSetProperties( "wdkModel/xmlQuestionSet/xmlQuestion" );
        digester.addBeanPropertySetter( "wdkModel/xmlQuestionSet/xmlQuestion/description" );
        digester.addBeanPropertySetter( "wdkModel/xmlQuestionSet/xmlQuestion/help" );
        digester.addSetNext( "wdkModel/xmlQuestionSet/xmlQuestion",
                "addQuestion" );
        
        digester.addSetNext( "wdkModel/xmlQuestionSet", "addXmlQuestionSet" );
        
        // load XmlRecordClassSet
        digester.addObjectCreate( "wdkModel/xmlRecordClassSet",
                XmlRecordClassSet.class );
        digester.addSetProperties( "wdkModel/xmlRecordClassSet" );
        
        // load XmlRecordClass
        digester.addObjectCreate( "wdkModel/xmlRecordClassSet/xmlRecordClass",
                XmlRecordClass.class );
        digester.addSetProperties( "wdkModel/xmlRecordClassSet/xmlRecordClass" );
        
        // load XmlAttributeField
        digester.addObjectCreate(
                "wdkModel/xmlRecordClassSet/xmlRecordClass/xmlAttribute",
                XmlAttributeField.class );
        digester.addSetProperties( "wdkModel/xmlRecordClassSet/xmlRecordClass/xmlAttribute" );
        digester.addSetNext(
                "wdkModel/xmlRecordClassSet/xmlRecordClass/xmlAttribute",
                "addAttributeField" );
        
        // load XmlTableField
        digester.addObjectCreate(
                "wdkModel/xmlRecordClassSet/xmlRecordClass/xmlTable",
                XmlTableField.class );
        digester.addSetProperties( "wdkModel/xmlRecordClassSet/xmlRecordClass/xmlTable" );
        
        // load XmlAttributeField within table
        digester.addObjectCreate(
                "wdkModel/xmlRecordClassSet/xmlRecordClass/xmlTable/xmlAttribute",
                XmlAttributeField.class );
        digester.addSetProperties( "wdkModel/xmlRecordClassSet/xmlRecordClass/xmlTable/xmlAttribute" );
        digester.addSetNext(
                "wdkModel/xmlRecordClassSet/xmlRecordClass/xmlTable/xmlAttribute",
                "addAttributeField" );
        
        digester.addSetNext(
                "wdkModel/xmlRecordClassSet/xmlRecordClass/xmlTable",
                "addTableField" );
        
        digester.addSetNext( "wdkModel/xmlRecordClassSet/xmlRecordClass",
                "addRecordClass" );
        
        digester.addSetNext( "wdkModel/xmlRecordClassSet",
                "addXmlRecordClassSet" );
        
        // load GroupSet
        digester.addObjectCreate( "wdkModel/groupSet", GroupSet.class );
        digester.addSetProperties( "wdkModel/groupSet" );
        
        // load XmlQuestion
        configureNode( digester, "wdkModel/groupSet/group", Group.class,
                "addGroup" );
        digester.addBeanPropertySetter( "wdkModel/groupSet/group/description" );
        
        digester.addSetNext( "wdkModel/groupSet", "addGroupSet" );
        
        return digester;
        
    }
    
    private static void configureNode( Digester digester, String path,
            Class nodeClass, String method ) {
        digester.addObjectCreate( path, nodeClass );
        digester.addSetProperties( path );
        digester.addSetNext( path, method );
    }
    
    public static void main( String[ ] args ) {
        try {
            
            String cmdName = System.getProperties().getProperty( "cmdName" );
            File configDir = new File( System.getProperties().getProperty(
                    "configDir" ) );
            
            // process args
            Options options = declareOptions();
            CommandLine cmdLine = parseOptions( cmdName, options, args );
            
            String modelName = cmdLine.getOptionValue( "model" );
            
            File modelConfigXmlFile = new File( configDir, modelName
                    + "-config.xml" );
            File modelXmlFile = new File( configDir, modelName + ".xml" );
            File modelPropFile = new File( configDir, modelName + ".prop" );
            
            File schemaFile = new File( System.getProperty( "schemaFile" ) );
            
            // load schema for xml data source
            File xmlSchemaFile = new File( System.getProperty( "xmlSchemaFile" ) );
            
            WdkModel wdkModel = parseXmlFile( modelXmlFile.toURI().toURL(),
                    modelPropFile.toURI().toURL(), schemaFile.toURI().toURL(),
                    xmlSchemaFile.toURI().toURL(),
                    modelConfigXmlFile.toURI().toURL() );
            
            // load the xml data path
            File xmlDataDir = new File( System.getProperty( "xmlDataDir" ) );
            wdkModel.setXmlDataDir( xmlDataDir );
            
            System.out.println( wdkModel.toString() );
            
        } catch ( Exception e ) {
            System.err.println( e.getMessage() );
            System.err.println( "" );
            e.printStackTrace();
            System.exit( 1 );
        }
    }
    
    private static void addOption( Options options, String argName, String desc ) {
        
        Option option = new Option( argName, true, desc );
        option.setRequired( true );
        option.setArgName( argName );
        
        options.addOption( option );
    }
    
    private static Options declareOptions() {
        Options options = new Options();
        
        // config file
        addOption(
                options,
                "model",
                "the name of the model.  This is used to find the Model XML file ($GUS_HOME/config/model_name.xml) the Model property file ($GUS_HOME/config/model_name.prop) and the Model config file ($GUS_HOME/config/model_name-config.xml)" );
        
        return options;
    }
    
    private static CommandLine parseOptions( String cmdName, Options options,
            String[ ] args ) {
        
        CommandLineParser parser = new BasicParser();
        CommandLine cmdLine = null;
        try {
            // parse the command line arguments
            cmdLine = parser.parse( options, args );
        } catch ( ParseException exp ) {
            // oops, something went wrong
            System.err.println( "" );
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
            System.err.println( "" );
            usage( cmdName, options );
        }
        
        return cmdLine;
    }
    
    private static void usage( String cmdName, Options options ) {
        
        String newline = System.getProperty( "line.separator" );
        String cmdlineSyntax = cmdName + " -model model_name";
        
        String header = newline + "Parse and print out a WDK Model xml file."
                + newline + newline + "Options:";
        
        String footer = "";
        
        // PrintWriter stderr = new PrintWriter(System.err);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( 75, cmdlineSyntax, header, options, footer );
        System.exit( 1 );
    }
    
}
