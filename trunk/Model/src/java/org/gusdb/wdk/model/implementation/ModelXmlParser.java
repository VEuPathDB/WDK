package org.gusdb.gus.wdk.model.implementation;

import org.apache.commons.digester.Digester;

import java.io.File;
import java.io.IOException;
import org.xml.sax.SAXException;
import org.gusdb.gus.wdk.model.WdkModel;
import org.gusdb.gus.wdk.model.SimpleQuerySet;
import org.gusdb.gus.wdk.model.PageableQuerySet;
import org.gusdb.gus.wdk.model.StringParam;
import org.gusdb.gus.wdk.model.SqlEnumParam;

public class ModelXmlParser {

    public static WdkModel parseXmlFile(File modelXmlFile) throws java.io.IOException, org.xml.sax.SAXException, Exception {
	Digester digester = configureDigester();
	WdkModel model = 
	    (WdkModel)digester.parse(modelXmlFile);
	model.resolveReferences();
	return model;
    }

    static Digester configureDigester() {

	Digester digester = new Digester();
	digester.setValidating( false );
	
	digester.addObjectCreate( "wdkModel", WdkModel.class );
	digester.addSetProperties( "wdkModel");
	
	digester.addObjectCreate( "wdkModel/pageableQuerySet", PageableQuerySet.class );

	digester.addSetProperties( "wdkModel/pageableQuerySet");
	
	digester.addObjectCreate( "wdkModel/pageableQuerySet/pageableSqlQuery", PageableSqlQuery.class );

	digester.addSetProperties( "wdkModel/pageableQuerySet/pageableSqlQuery");

	digester.addSetNext( "wdkModel/pageableQuerySet/pageableSqlQuery", "addQuery" );

	digester.addSetNext( "wdkModel/pageableQuerySet", "addPageableQuerySet" );
	
	digester.addObjectCreate( "wdkModel/simpleQuerySet", SimpleQuerySet.class );

	digester.addSetProperties( "wdkModel/simpleQuerySet");
	
	digester.addObjectCreate( "wdkModel/simpleQuerySet/simpleSqlQuery", SimpleSqlQuery.class );

	digester.addSetProperties( "wdkModel/simpleQuerySet/simpleSqlQuery");
	digester.addBeanPropertySetter( "wdkModel/simpleQuerySet/simpleSqlQuery/sql");

	
	digester.addObjectCreate( "wdkModel/simpleQuerySet/simpleSqlQuery/sqlEnumParam", 
				  SqlEnumParam.class );
	digester.addSetProperties( "wdkModel/simpleQuerySet/simpleSqlQuery/sqlEnumParam");

	digester.addObjectCreate( "wdkModel/simpleQuerySet/simpleSqlQuery/sqlEnumParam/simpleSqlQuery", SimpleSqlQuery.class );

	digester.addBeanPropertySetter( "wdkModel/simpleQuerySet/simpleSqlQuery/sqlEnumParam/simpleSqlQuery/sql");

	digester.addSetNext( "wdkModel/simpleQuerySet/simpleSqlQuery/sqlEnumParam/simpleSqlQuery", "setSimpleSqlQuery");

	digester.addSetNext( "wdkModel/simpleQuerySet/simpleSqlQuery/sqlEnumParam", "addParam" );
	

	digester.addObjectCreate( "wdkModel/simpleQuerySet/simpleSqlQuery/stringParam", 
				  StringParam.class );
	digester.addSetProperties( "wdkModel/simpleQuerySet/simpleSqlQuery/stringParam");


	digester.addSetNext( "wdkModel/simpleQuerySet/simpleSqlQuery/stringParam", "addParam" );
	
	digester.addSetNext( "wdkModel/simpleQuerySet/simpleSqlQuery", "addQuery" );
	digester.addSetNext( "wdkModel/simpleQuerySet", "addSimpleQuerySet" );
	
	return digester;
    }
    
    public static void main( String[] args ) {
	try {
	    File querySetXmlFile = new File(args[0]);
	    WdkModel wdkModel = parseXmlFile(querySetXmlFile);
	    
	    System.out.println( wdkModel.toString() );
	    
	} catch( Exception exc ) {
	    exc.printStackTrace();
	}
    }
}


