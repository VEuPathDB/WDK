package org.gusdb.gus.wdk.model.implementation;

import org.gusdb.gus.wdk.model.Column;
import org.gusdb.gus.wdk.model.Param;
import org.gusdb.gus.wdk.model.ParamSet;
import org.gusdb.gus.wdk.model.Query;
import org.gusdb.gus.wdk.model.Record;
import org.gusdb.gus.wdk.model.RecordSet;
import org.gusdb.gus.wdk.model.Reference;
import org.gusdb.gus.wdk.model.QuerySet;
import org.gusdb.gus.wdk.model.FlatCVParam;
import org.gusdb.gus.wdk.model.StringParam;
import org.gusdb.gus.wdk.model.TextField;
import org.gusdb.gus.wdk.model.WdkModel;
import org.gusdb.gus.wdk.model.ReferenceList;
import org.gusdb.gus.wdk.model.TextColumn;
import org.gusdb.gus.wdk.model.SummarySet;
import org.gusdb.gus.wdk.model.Summary;
import org.gusdb.gus.wdk.model.WdkModelException;


import java.util.Properties;
import java.util.Enumeration;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.FileReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.digester.Digester;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.InputSource;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.SinglePropertyMap;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;

public class ModelXmlParser {

    public static WdkModel parseXmlFile(File modelXmlFile) throws org.xml.sax.SAXException, WdkModelException {
	return parseXmlFile(modelXmlFile, null);
    }

    public static WdkModel parseXmlFile(File modelXmlFile, File modelPropFile)
            throws WdkModelException {

	
	// NOTE: we are validating before we substitute in the properties
	// so that the validator will operate on a file instead of a stream.
	// this way the validator spits out line numbers for errors
        if (!validModelFile(modelXmlFile)) {
            throw new WdkModelException("Model validation failed");
        }

        Digester digester = configureDigester();
	WdkModel model = null;

	try {
	    InputStream modelXmlStream = 
		makeModelXmlStream(modelXmlFile, modelPropFile);
	    model = (WdkModel)digester.parse(modelXmlStream);
	} catch (SAXException e) {
	    throw new WdkModelException(e);
	} catch (IOException e) {
	    throw new WdkModelException(e);
	}

	setModelDocument(model, modelXmlFile, modelPropFile);

	model.resolveReferences();

	return model;
    }

    private static InputStream makeModelXmlStream(File modelXmlFile, File modelPropFile) throws WdkModelException {
	InputStream modelXmlStream;
	
	if (modelPropFile != null) {
	    modelXmlStream = configureModelFile(modelXmlFile, modelPropFile);
	} else {
	    try {
		modelXmlStream = new FileInputStream(modelXmlFile);
	    } catch (FileNotFoundException e) {
		throw new WdkModelException(e);
	    }
	}
	return modelXmlStream;
    }

    private static void setModelDocument(WdkModel model, File modelXmlFile, File modelPropFile) throws WdkModelException {
	try {
	    InputStream modelXmlStream = 
		makeModelXmlStream(modelXmlFile, modelPropFile);
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


    private static boolean validModelFile(File modelXMLFile) throws WdkModelException {

	File schemaFile = new File(System.getProperty("schemaFile"));

        System.setProperty("org.apache.xerces.xni.parser.XMLParserConfiguration", "org.apache.xerces.parsers.XIncludeParserConfiguration"); 
        
        VerifierFactory factory = null;
        try {
            factory = VerifierFactory.newInstance("http://relaxng.org/ns/structure/1.0");

	    ErrorHandler errorHandler = new ErrorHandlerImpl(System.err);
            PropertyMap schemaProperties = new SinglePropertyMap(ValidateProperty.ERROR_HANDLER, errorHandler);
            ValidationDriver vd = new ValidationDriver(schemaProperties, PropertyMap.EMPTY, null);
            
            vd.loadSchema(ValidationDriver.fileInputSource(schemaFile));
	    
            

	    //            return vd.validate(new InputSource(modelXMLStream));
            return vd.validate(ValidationDriver.fileInputSource(modelXMLFile));
            
        } catch (VerifierConfigurationException e) {
            throw new WdkModelException(e);
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


        
        //RecordSet

        /**/ digester.addObjectCreate( "wdkModel/recordSet", RecordSet.class );

        /**/ digester.addSetProperties( "wdkModel/recordSet");
        
        /*  */ digester.addObjectCreate( "wdkModel/recordSet/record", Record.class );

        /*  */ digester.addSetProperties( "wdkModel/recordSet/record");

        /*    */ digester.addObjectCreate( "wdkModel/recordSet/record/attributeQuery", Reference.class );

        /*    */ digester.addSetProperties( "wdkModel/recordSet/record/attributeQuery");

        /*    */ digester.addSetNext( "wdkModel/recordSet/record/attributeQuery", "addFieldsQueryRef" );

        /*    */ digester.addObjectCreate( "wdkModel/recordSet/record/tableQuery", Reference.class );

        /*    */ digester.addSetProperties( "wdkModel/recordSet/record/tableQuery");

        /*    */ digester.addSetNext( "wdkModel/recordSet/record/tableQuery", "addTableQueryRef" );

        /*    */ digester.addObjectCreate( "wdkModel/recordSet/record/textField", TextField.class );

        /*    */ digester.addSetProperties( "wdkModel/recordSet/record/textField");

        /*      */ digester.addBeanPropertySetter( "wdkModel/recordSet/record/textField/text");

        /*    */ digester.addSetNext( "wdkModel/recordSet/record/textField", "addTextField" );

        /*  */ digester.addSetNext( "wdkModel/recordSet/record", "addRecord" );

        /**/ digester.addSetNext( "wdkModel/recordSet", "addRecordSet" );


        //QuerySet

        /**/ digester.addObjectCreate( "wdkModel/querySet", QuerySet.class );

        /**/ digester.addSetProperties( "wdkModel/querySet");
        
        /*  */ digester.addObjectCreate( "wdkModel/querySet/sqlQuery", SqlQuery.class );

        /*  */ digester.addSetProperties( "wdkModel/querySet/sqlQuery");

	/*  */ digester.addBeanPropertySetter( "wdkModel/querySet/sqlQuery/sql");

        /*    */ digester.addObjectCreate( "wdkModel/querySet/sqlQuery/paramRef", Reference.class );

        /*    */ digester.addSetProperties( "wdkModel/querySet/sqlQuery/paramRef");

        /*    */ digester.addSetNext( "wdkModel/querySet/sqlQuery/paramRef", "addParamRef" );

        /*    */ digester.addObjectCreate( "wdkModel/querySet/sqlQuery/column", Column.class );

        /*    */ digester.addSetProperties( "wdkModel/querySet/sqlQuery/column");

        /*    */ digester.addSetNext( "wdkModel/querySet/sqlQuery/column", "addColumn" );

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

        /*  */ digester.addObjectCreate( "wdkModel/paramSet/flatCVParam", FlatCVParam.class );

        /*  */ digester.addSetProperties( "wdkModel/paramSet/flatCVParam");

        /*  */ digester.addSetNext( "wdkModel/paramSet/flatCVParam", "addParam" );
        
        /**/ digester.addSetNext( "wdkModel/paramSet", "addParamSet" );


        //ReferenceList
        
        /**/ digester.addObjectCreate("wdkModel/referenceList", ReferenceList.class);

        /**/ digester.addSetProperties("wdkModel/referenceList");
        
        /*  */ digester.addObjectCreate("wdkModel/referenceList/reference", Reference.class);

        /*  */ digester.addSetProperties("wdkModel/referenceList/reference");

        /*  */ digester.addSetNext("wdkModel/referenceList/reference", "addReference");

        /**/ digester.addSetNext("wdkModel/referenceList", "addReferenceList");

        //SummarySet
        
        /**/ digester.addObjectCreate("wdkModel/summarySet", SummarySet.class);

        /**/ digester.addSetProperties("wdkModel/summarySet");

        /*  */ digester.addObjectCreate("wdkModel/summarySet/summary", Summary.class);

        /*  */ digester.addSetProperties("wdkModel/summarySet/summary");

        /*  */ digester.addSetNext("wdkModel/summarySet/summary", "addSummary");

        /**/ digester.addSetNext("wdkModel/summarySet", "addSummarySet");

        return digester;

    }

    /**
     * Substitute property values into model xml
     */
   public static InputStream configureModelFile(File modelXmlFile, File modelPropFile) throws WdkModelException {

	try {
	    StringBuffer substituted = new StringBuffer();
	    Properties properties = new Properties();
	    properties.load(new FileInputStream(modelPropFile));
	    BufferedReader reader = 
		new BufferedReader(new FileReader(modelXmlFile));
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
	    File modelXmlFile = new File(args[0]);
	    File modelPropFile = null;
	    if (args.length > 1) { 
		modelPropFile = new File(args[1]);
	    } 
	    WdkModel wdkModel = parseXmlFile(modelXmlFile, modelPropFile);

	    System.out.println( wdkModel.toString() );
	    
	} catch( Exception e ) {
	    System.err.println(e.getMessage());
	    System.err.println("");
	    e.printStackTrace();
	    System.exit(1);
	}
    }
}


