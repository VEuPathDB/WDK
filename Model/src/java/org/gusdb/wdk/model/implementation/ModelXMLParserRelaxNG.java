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
import org.gusdb.gus.wdk.model.RecordListSet;
import org.gusdb.gus.wdk.model.RecordList;


import java.io.File;
import java.io.IOException;

import org.apache.commons.digester.Digester;
import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.iso_relax.verifier.VerifierFilter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ModelXMLParserRelaxNG {

    public static WdkModel parseXmlFile(WdkModel model, File modelXMLFile, File schemaFile)
            throws java.io.IOException, org.xml.sax.SAXException, Exception {
        if (!validModelFile(modelXMLFile, schemaFile)) {
            // TODO Throw some kind of exception
        }
        Digester digester = configureDigester(model);
        model = (WdkModel) digester.parse(modelXMLFile);
        model.resolveReferences();
        return model;
    }

    private static boolean validModelFile(File modelXMLFile, File schemaFile) {

        System.setProperty("org.apache.xerces.xni.parser.XMLParserConfiguration", "org.apache.xerces.parsers.XIncludeParserConfiguration"); 
        
        VerifierFactory factory = null;
        try {
            factory = VerifierFactory.newInstance("http://relaxng.org/ns/structure/1.0");
            Schema schema = factory.compileSchema(schemaFile); /* compile schema */;

            VerifierFilter filter = schema.newVerifier().getVerifierFilter();
            Digester digester = configureDigester(null);
            filter.setContentHandler( digester );

            filter.parse(new InputSource(modelXMLFile.getAbsolutePath()));
            if(filter.isValid()) {
                // the parsed document was valid
            } else {
                // invalid
            }
           
////          wrap it into a JAXP
//            SAXParserFactory parserFactory = new ValidatingSAXParserFactory(schema);
//
////          create a new XMLReader from it
//         parserFactory.setNamespaceAware(true);
//         XMLReader reader = parserFactory.newSAXParser().getXMLReader();
            
        } catch (VerifierConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        // TODO - should check!
        return true;
    }
    
    private static Digester configureDigester(WdkModel model) {

        Digester digester = new Digester();
        digester.setValidating(false);

        //Root -- WDK Model

        if ( model == null) {
            model = new WdkModel();
        } else {
            // TODO Implement reset or clear on model
            //model.clear();
        }
        
        digester.push(model);
        digester.addSetProperties("wdkModel");

        //RecordSet

        /**/digester.addObjectCreate("wdkModel/recordSet", RecordSet.class);

        /**/digester.addSetProperties("wdkModel/recordSet");

        /*  */digester.addObjectCreate("wdkModel/recordSet/record",
                Record.class);

        /*  */digester.addSetProperties("wdkModel/recordSet/record");

        /*    */digester.addObjectCreate("wdkModel/recordSet/record/fieldsQuery", Reference.class);

        /*    */digester.addSetProperties("wdkModel/recordSet/record/fieldsQuery");

        /*    */digester.addSetNext("wdkModel/recordSet/record/fieldsQuery", "addFieldsQueryRef");

        /*    */digester.addObjectCreate("wdkModel/recordSet/record/tableQuery", Reference.class);

        /*    */digester.addSetProperties("wdkModel/recordSet/record/tableQuery");

        /*    */digester.addSetNext("wdkModel/recordSet/record/tableQuery", "addTableQueryRef");

        /*    */digester.addObjectCreate("wdkModel/recordSet/record/textField", TextField.class);

        /*    */digester.addSetProperties("wdkModel/recordSet/record/textField");

        /*      */digester.addBeanPropertySetter("wdkModel/recordSet/record/textField/text");

        /*    */digester.addSetNext("wdkModel/recordSet/record/textField", "addTextField");

        /*  */digester.addSetNext("wdkModel/recordSet/record", "addRecord");

        /**/digester.addSetNext("wdkModel/recordSet", "addRecordSet");

        //QuerySet

        /**/digester.addObjectCreate("wdkModel/querySet", QuerySet.class);

        /**/digester.addSetProperties("wdkModel/querySet");

        /*  */digester.addObjectCreate("wdkModel/querySet/sqlQuery",
                SqlQuery.class);

        /*  */digester.addSetProperties("wdkModel/querySet/sqlQuery");

        /*  */digester.addBeanPropertySetter("wdkModel/querySet/sqlQuery/sql");

        /*    */digester.addObjectCreate(
                "wdkModel/querySet/sqlQuery/sqlEnumParam", SqlEnumParam.class);
        /*    */digester.addSetProperties("wdkModel/querySet/sqlQuery/sqlEnumParam");

        /*      */digester.addObjectCreate("wdkModel/querySet/sqlQuery/sqlEnumParam/sqlQuery", SqlQuery.class);

        /*        */digester.addBeanPropertySetter("wdkModel/querySet/sqlQuery/sqlEnumParam/sqlQuery/sql");

        /*        */digester.addObjectCreate("wdkModel/querySet/sqlQuery/sqlEnumParam/sqlQuery/column", Column.class);

        /*        */digester.addSetProperties("wdkModel/querySet/sqlQuery/sqlEnumParam/sqlQuery/column");

        /*        */digester.addSetNext("wdkModel/querySet/sqlQuery/sqlEnumParam/sqlQuery/column", "addColumn");

        /*      */digester.addSetNext("wdkModel/querySet/sqlQuery/sqlEnumParam/sqlQuery", "setSqlQuery");

        /*    */digester.addSetNext("wdkModel/querySet/sqlQuery/sqlEnumParam", "addParam");

        /*    */digester.addObjectCreate("wdkModel/querySet/sqlQuery/stringParam", StringParam.class);

        /*    */digester.addSetProperties("wdkModel/querySet/sqlQuery/stringParam");

        /*    */digester.addSetNext("wdkModel/querySet/sqlQuery/stringParam", "addParam");

        /*    */digester.addObjectCreate("wdkModel/querySet/sqlQuery/column", Column.class);

        /*    */digester.addSetProperties("wdkModel/querySet/sqlQuery/column");

        /*    */digester.addSetNext("wdkModel/querySet/sqlQuery/column", "addColumn");

        /*    */digester.addObjectCreate("wdkModel/querySet/sqlQuery/textColumn", TextColumn.class);

        /*    */digester.addSetProperties("wdkModel/querySet/sqlQuery/textColumn");

        /*    */digester.addSetNext("wdkModel/querySet/sqlQuery/textColumn", "addColumn");

        /*  */digester.addSetNext("wdkModel/querySet/sqlQuery", "addQuery");

        /**/digester.addSetNext("wdkModel/querySet", "addQuerySet");

        //QueryNameList

        /**/digester.addObjectCreate("wdkModel/queryNameList", QueryNameList.class);

        /**/digester.addSetProperties("wdkModel/queryNameList");

        /*  */digester.addObjectCreate("wdkModel/queryNameList/fullQueryName", QueryName.class);

        /*  */digester.addSetProperties("wdkModel/queryNameList/fullQueryName");

        /*  */digester.addSetNext("wdkModel/queryNameList/fullQueryName", "addQueryName");

        /**/digester.addSetNext("wdkModel/queryNameList", "addQueryNameList");

        //RecordListSet

        /**/digester.addObjectCreate("wdkModel/recordListSet", RecordListSet.class);

        /**/digester.addSetProperties("wdkModel/recordListSet");

        /*  */digester.addObjectCreate("wdkModel/recordListSet/recordList", RecordList.class);

        /*  */digester.addSetProperties("wdkModel/recordListSet/recordList");

        /*  */digester.addSetNext("wdkModel/recordListSet/recordList", "addRecordList");

        /**/digester.addSetNext("wdkModel/recordListSet", "addRecordListSet");

        return digester;
    }

    public static void main(String[] args) {
        try {
            File modelXmlFile = new File(args[0]);
            File schemaFile = new File(args[1]);
            WdkModel wdkModel = parseXmlFile(null, modelXmlFile, schemaFile);

            System.out.println(wdkModel.toString());

        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}


