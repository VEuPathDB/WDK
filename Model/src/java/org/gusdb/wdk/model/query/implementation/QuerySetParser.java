package org.gusdb.gus.wdk.model.query.implementation;

import org.apache.commons.digester.Digester;

import java.io.File;
import java.io.IOException;
import org.xml.sax.SAXException;
import org.gusdb.gus.wdk.model.query.QuerySetContainer;
import org.gusdb.gus.wdk.model.query.SimpleQuerySet;
import org.gusdb.gus.wdk.model.query.StringParam;
import org.gusdb.gus.wdk.model.query.SqlEnumParam;

public class QuerySetParser {

    public static QuerySetContainer parseXmlFile(File querySetXmlFile) throws java.io.IOException, org.xml.sax.SAXException {
	Digester digester = configureDigester();
	return (QuerySetContainer)digester.parse(querySetXmlFile);
    }

    static Digester configureDigester() {

	Digester digester = new Digester();
	digester.setValidating( false );
	
	digester.addObjectCreate( "querySetContainer", QuerySetContainer.class );
	digester.addSetProperties( "querySetContainer");
	
	digester.addObjectCreate( "querySetContainer/simpleQuerySet", SimpleQuerySet.class );
	digester.addSetProperties( "querySetContainer/simpleQuerySet");
	
	digester.addObjectCreate( "querySetContainer/simpleQuerySet/simpleSqlQuery", SimpleSqlQuery.class );
	digester.addSetProperties( "querySetContainer/simpleQuerySet/simpleSqlQuery");
	digester.addBeanPropertySetter( "querySetContainer/simpleQuerySet/simpleSqlQuery/sql");

	
	digester.addObjectCreate( "querySetContainer/simpleQuerySet/simpleSqlQuery/sqlEnumParam", 
				  SqlEnumParam.class );
	digester.addSetProperties( "querySetContainer/simpleQuerySet/simpleSqlQuery/sqlEnumParam");

	digester.addObjectCreate( "querySetContainer/simpleQuerySet/simpleSqlQuery/sqlEnumParam/simpleSqlQuery", SimpleSqlQuery.class );
	digester.addBeanPropertySetter( "querySetContainer/simpleQuerySet/simpleSqlQuery/sqlEnumParam/simpleSqlQuery/sql");
	digester.addSetNext( "querySetContainer/simpleQuerySet/simpleSqlQuery/sqlEnumParam/simpleSqlQuery", "setSimpleSqlQuery");

	digester.addSetNext( "querySetContainer/simpleQuerySet/simpleSqlQuery/sqlEnumParam", "addParam" );
	

	digester.addObjectCreate( "querySetContainer/simpleQuerySet/simpleSqlQuery/stringParam", 
				  StringParam.class );
	digester.addSetProperties( "querySetContainer/simpleQuerySet/simpleSqlQuery/stringParam");


	digester.addSetNext( "querySetContainer/simpleQuerySet/simpleSqlQuery/stringParam", "addParam" );
	
	digester.addSetNext( "querySetContainer/simpleQuerySet/simpleSqlQuery", "addQuery" );
	digester.addSetNext( "querySetContainer/simpleQuerySet", "addSimpleQuerySet" );
	
	return digester;
    }
    
    public static void main( String[] args ) {
	try {
	    File querySetXmlFile = new File(args[0]);
	    QuerySetContainer querySetContainer = parseXmlFile(querySetXmlFile);
	    
	    System.out.println( querySetContainer.toString() );
	    
	} catch( Exception exc ) {
	    exc.printStackTrace();
	}
    }
}


