package org.gusdb.gus.wdk.model.implementation;

import org.gusdb.gus.wdk.model.Column;
import org.gusdb.gus.wdk.model.Record;
import org.gusdb.gus.wdk.model.RecordSet;
import org.gusdb.gus.wdk.model.Reference;
import org.gusdb.gus.wdk.model.QuerySet;
import org.gusdb.gus.wdk.model.SqlEnumParam;
import org.gusdb.gus.wdk.model.StringParam;
import org.gusdb.gus.wdk.model.TextField;
import org.gusdb.gus.wdk.model.WdkModel;
import org.gusdb.gus.wdk.model.QueryNameList;
import org.gusdb.gus.wdk.model.QueryName;
import org.gusdb.gus.wdk.model.TextColumn;


import java.io.File;

import org.apache.commons.digester.Digester;

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
	
	/*  */ digester.addObjectCreate( "wdkModel/querySet/sqlQuery", SqlQuery.class );

	/*  */ digester.addSetProperties( "wdkModel/querySet/sqlQuery");

	/*  */ digester.addBeanPropertySetter( "wdkModel/querySet/sqlQuery/sql");

	/*    */ digester.addObjectCreate( "wdkModel/querySet/sqlQuery/sqlEnumParam", 
				  SqlEnumParam.class );
	/*    */ digester.addSetProperties( "wdkModel/querySet/sqlQuery/sqlEnumParam");

	/*      */ digester.addObjectCreate( "wdkModel/querySet/sqlQuery/sqlEnumParam/sqlQuery", SqlQuery.class );

	/*        */ digester.addBeanPropertySetter( "wdkModel/querySet/sqlQuery/sqlEnumParam/sqlQuery/sql");

	/*      */ digester.addSetNext( "wdkModel/querySet/sqlQuery/sqlEnumParam/sqlQuery", "setSqlQuery");

	/*    */ digester.addSetNext( "wdkModel/querySet/sqlQuery/sqlEnumParam", "addParam" );
	
	/*    */ digester.addObjectCreate( "wdkModel/querySet/sqlQuery/stringParam", 
				  StringParam.class );

	/*    */ digester.addSetProperties( "wdkModel/querySet/sqlQuery/stringParam");

	/*    */ digester.addSetNext( "wdkModel/querySet/sqlQuery/stringParam", "addParam" );
	
	/*    */ digester.addObjectCreate( "wdkModel/querySet/sqlQuery/column", 
				  Column.class );

	/*    */ digester.addSetProperties( "wdkModel/querySet/sqlQuery/column");

	/*    */ digester.addSetNext( "wdkModel/querySet/sqlQuery/column", "addColumn" );

	/*    */ digester.addObjectCreate( "wdkModel/querySet/sqlQuery/textColumn", 
				  TextColumn.class );

	/*    */ digester.addSetProperties( "wdkModel/querySet/sqlQuery/textColumn");

	/*    */ digester.addSetNext( "wdkModel/querySet/sqlQuery/textColumn", "addColumn" );

	/*  */ digester.addSetNext( "wdkModel/querySet/sqlQuery", "addQuery" );

	/**/ digester.addSetNext( "wdkModel/querySet", "addQuerySet" );


	//QueryNameList
	
	/**/ digester.addObjectCreate("wdkModel/queryNameList", QueryNameList.class);

	/**/ digester.addSetProperties("wdkModel/queryNameList");
	
	/*  */ digester.addObjectCreate("wdkModel/queryNameList/fullQueryName", QueryName.class);

	/*  */ digester.addSetProperties("wdkModel/queryNameList/fullQueryName");

	/*  */ digester.addSetNext("wdkModel/queryNameList/fullQueryName", "addQueryName");

	/**/ digester.addSetNext("wdkModel/queryNameList", "addQueryNameList");

	
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


