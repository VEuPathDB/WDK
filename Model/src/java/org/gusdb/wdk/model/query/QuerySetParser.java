package org.gusdb.gus.wdk.model.query;

import org.apache.commons.digester.Digester;

import java.io.File;
import java.io.IOException;
import org.xml.sax.SAXException;

public class QuerySetParser {

    public static QuerySet parseXmlFile(File querySetXmlFile) throws java.io.IOException, org.xml.sax.SAXException {
	Digester digester = configureDigester();
	return (QuerySet)digester.parse(querySetXmlFile);
    }

    static Digester configureDigester() {

	Digester digester = new Digester();
	digester.setValidating( false );
	
	digester.addObjectCreate( "querySet", QuerySet.class );
	digester.addSetProperties( "querySet");
	
	digester.addObjectCreate( "querySet/sqlQuery", SqlQuery.class );
	digester.addSetProperties( "querySet/sqlQuery");
	digester.addBeanPropertySetter( "querySet/sqlQuery/sql");

	
	digester.addObjectCreate( "querySet/sqlQuery/sqlEnumParam", 
				  SqlEnumParam.class );
	digester.addSetProperties( "querySet/sqlQuery/sqlEnumParam");

	digester.addObjectCreate( "querySet/sqlQuery/sqlEnumParam/sqlQuery", SqlQuery.class );
	digester.addBeanPropertySetter( "querySet/sqlQuery/sqlEnumParam/sqlQuery/sql");
	digester.addSetNext( "querySet/sqlQuery/sqlEnumParam/sqlQuery", "setSqlQuery");

	digester.addSetNext( "querySet/sqlQuery/sqlEnumParam", "addParam" );
	

	digester.addObjectCreate( "querySet/sqlQuery/stringParam", 
				  StringParam.class );
	digester.addSetProperties( "querySet/sqlQuery/stringParam");


	digester.addSetNext( "querySet/sqlQuery/stringParam", "addParam" );
	
	digester.addSetNext( "querySet/sqlQuery", "addQuery" );
	
	return digester;
    }
    
    public static void main( String[] args ) {
	try {
	    File querySetXmlFile = new File(args[0]);
	    QuerySet querySet = parseXmlFile(querySetXmlFile);
	    
	    System.out.println( querySet.toString() );
	    
	} catch( Exception exc ) {
	    exc.printStackTrace();
	}
    }
}


