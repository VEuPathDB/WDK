package org.gusdb.gus.wdk.model.implementation;

import org.gusdb.gus.wdk.model.Column;
import org.gusdb.gus.wdk.model.Record;
import org.gusdb.gus.wdk.model.RecordSet;
import org.gusdb.gus.wdk.model.Reference;
import org.gusdb.gus.wdk.model.QuerySet;
import org.gusdb.gus.wdk.model.Param;
import org.gusdb.gus.wdk.model.ParamSet;
import org.gusdb.gus.wdk.model.TextField;
import org.gusdb.gus.wdk.model.WdkModel;
import org.gusdb.gus.wdk.model.ReferenceList;
import org.gusdb.gus.wdk.model.Query;
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

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXParseException;

public class ModelXmlParser {

    public static WdkModel parseXmlFile(File modelXmlFile) throws org.xml.sax.SAXException, WdkModelException {
	return parseXmlFile(modelXmlFile, null);
    }

    public static WdkModel parseXmlFile(File modelXmlFile, File modelPropFile) throws org.xml.sax.SAXException, WdkModelException {
	
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

	Digester digester = configureDigester();
	WdkModel model = null;
	try {
	    model = (WdkModel)digester.parse(modelXmlStream);
	} catch (IOException e) {
	    throw new WdkModelException(e);
	}
	model.resolveReferences();
	return model;
    }

    static Digester configureDigester() {

	Digester digester = new Digester();
	digester.setValidating(true);
	digester.setNamespaceAware(true);
	digester.setSchemaLanguage("http://www.w3.org/2001/XMLSchema");
	digester.setSchema(System.getProperty("schemaFile"));
	digester.setErrorHandler(new ParserErrorHandler());

	//Root -- WDK Model

	digester.addObjectCreate( "wdkModel", WdkModel.class );
	digester.addSetProperties( "wdkModel");


	
	//RecordSet

	/**/ digester.addObjectCreate( "wdkModel/recordSet", RecordSet.class );

	/**/ digester.addSetProperties( "wdkModel/recordSet");
	
	/*  */ digester.addObjectCreate( "wdkModel/recordSet/record", Record.class );

	/*  */ digester.addSetProperties( "wdkModel/recordSet/record");

	/*    */ digester.addObjectCreate( "wdkModel/recordSet/record/fieldsQuery", Reference.class );

	/*    */ digester.addSetProperties( "wdkModel/recordSet/record/fieldsQuery");

	/*    */ digester.addSetNext( "wdkModel/recordSet/record/fieldsQuery", "addFieldsQueryRef" );

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
	
	/*  */ digester.addObjectCreate( "wdkModel/querySet/query", "xsi:type", Query.class );

	/*  */ digester.addSetProperties( "wdkModel/querySet/query");

	/*  */ digester.addBeanPropertySetter( "wdkModel/querySet/query/sql");

	/*    */ digester.addObjectCreate( "wdkModel/querySet/query/paramRef", Reference.class );

	/*    */ digester.addSetProperties( "wdkModel/querySet/query/paramRef");

	/*    */ digester.addSetNext( "wdkModel/querySet/query/paramRef", "addParamRef" );

	/*    */ digester.addObjectCreate( "wdkModel/querySet/query/column", "xsi:type", Column.class );

	/*    */ digester.addSetProperties( "wdkModel/querySet/query/column");

	/*    */ digester.addSetNext( "wdkModel/querySet/query/column", "addColumn" );

	/*  */ digester.addSetNext( "wdkModel/querySet/query", "addQuery" );

	/**/ digester.addSetNext( "wdkModel/querySet", "addQuerySet" );


	//ParamSet

	/**/ digester.addObjectCreate( "wdkModel/paramSet", ParamSet.class );

	/**/ digester.addSetProperties( "wdkModel/paramSet");
	
	/*  */ digester.addObjectCreate( "wdkModel/paramSet/param", "xsi:type", Param.class );

	/*  */ digester.addSetProperties( "wdkModel/paramSet/param");

	/*  */ digester.addSetNext( "wdkModel/paramSet/param", "addParam" );

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
    static InputStream configureModelFile(File modelXmlFile, File modelPropFile) throws WdkModelException {

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
	    
	} catch( SAXParseException e ) {
	    System.exit(1);
	} catch( Exception e ) {
	    System.err.println(e.getMessage());
	    System.err.println("");
	    e.printStackTrace();
	    System.exit(1);
	}
    }
}


