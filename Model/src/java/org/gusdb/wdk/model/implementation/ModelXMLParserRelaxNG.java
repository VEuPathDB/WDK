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
import org.gusdb.gus.wdk.model.ReferenceList;
import org.gusdb.gus.wdk.model.Reference;
import org.gusdb.gus.wdk.model.TextColumn;
import org.gusdb.gus.wdk.model.SummarySet;
import org.gusdb.gus.wdk.model.Summary;
import org.gusdb.gus.wdk.model.WdkModelException;


import java.io.File;
import java.io.IOException;

import org.apache.commons.digester.Digester;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.SinglePropertyMap;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;

public class ModelXMLParserRelaxNG {

    public static WdkModel parseXmlFile(WdkModel model, File modelXMLFile, File schemaFile)
            throws IOException, SAXException, WdkModelException {
        if (!validModelFile(modelXMLFile, schemaFile)) {
            throw new WdkModelException("Model validation failed");
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

            System.err.println("schemaFile is "+schemaFile.getAbsolutePath());
            
            ErrorHandler errorHandler = new ErrorHandlerImpl(System.err);
            PropertyMap schemaProperties = new SinglePropertyMap(ValidateProperty.ERROR_HANDLER, errorHandler);
            ValidationDriver vd = new ValidationDriver(schemaProperties, PropertyMap.EMPTY, null);
            
            vd.loadSchema(ValidationDriver.fileInputSource(schemaFile));
            
            return vd.validate(ValidationDriver.fileInputSource(modelXMLFile));
            
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

        return false;
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

        //ReferenceList

        /**/digester.addObjectCreate("wdkModel/referenceList", ReferenceList.class);

        /**/digester.addSetProperties("wdkModel/referenceList");

        /*  */digester.addObjectCreate("wdkModel/referenceList/twoPartName", Reference.class);

        /*  */digester.addSetProperties("wdkModel/referenceList/twoPartName");

        /*  */digester.addSetNext("wdkModel/referenceList/twoPartName", "addReference");

        /**/digester.addSetNext("wdkModel/referenceList", "addReferenceList");

        //SummarySet

        /**/digester.addObjectCreate("wdkModel/summarySet", SummarySet.class);

        /**/digester.addSetProperties("wdkModel/summarySet");

        /*  */digester.addObjectCreate("wdkModel/summarySet/summary", Summary.class);

        /*  */digester.addSetProperties("wdkModel/summarySet/summary");

        /*  */digester.addSetNext("wdkModel/summarySet/summary", "addSummary");

        /**/digester.addSetNext("wdkModel/summarySet", "addSummarySet");

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


