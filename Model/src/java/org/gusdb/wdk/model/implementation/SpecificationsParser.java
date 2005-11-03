package org.gusdb.wdk.model.implementation;
import org.apache.commons.digester.*;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;

import org.gusdb.wdk.model.QuestionSetName;
import org.gusdb.wdk.model.QuestionFullName;
import org.gusdb.wdk.model.Parameter;
import org.gusdb.wdk.model.Page;
import org.gusdb.wdk.model.Defaults;
import org.gusdb.wdk.model.PageDefaults;

import java.lang.*;
import java.util.ArrayList;

import org.gusdb.wdk.model.WdkModelException;

import java.net.URL;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.SinglePropertyMap;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.xml.sax.ErrorHandlerImpl;

/** SpecificationsParser.java
* Created for Custom specifications 
* @author Nivedita Kaluskar
**/

/**
 * Parses the contents of specifications XML file.
 * 
 */
public class SpecificationsParser
{
   private ArrayList pageList;
   private static SpecificationsParser specification;
   private static final String DEFAULT_SCHEMA_NAME = "specifications.rng";  
    /**
     * Prints the contact information to standard output.
     *
     * @param contact the <code>Contact</code> to print out
     */
    public SpecificationsParser() {
          //specification = this;
          pageList = new ArrayList();            
  }
    public void addPage(Page page)
    {
     
      //  System.out.println("In addContact");
      //  System.out.println("NAME: " + page.getName());
              
        pageList.add(page); 
    	//this.contact = contact;
    }

    public ArrayList getPageList() {
	return pageList;
    }
    
    /**
     * Configures Digester rules and actions, parses the XML file specified
     * as the first argument.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) 
    {
       URL specXmlURL=null, specSchemaURL=null;
       try {
         SpecificationsParser abp =  parseSpecFile(specXmlURL, specSchemaURL);
        System.out.println(abp.toString());
       } catch (Exception e) {System.err.println(e.getMessage());
      }
    }

    public static SpecificationsParser parseSpecFile(URL specXmlURL, URL specSchemaURL) throws IOException, SAXException, WdkModelException {
 
     //  System.out.println("specSchemaURL " + specSchemaURL);

       //if (specSchemaURL == null) {
       //     specSchemaURL = WdkModel.class.getResource(DEFAULT_SCHEMA_NAME);
       // }

        // NOTE: we are validating before we substitute in the properties
        // so that the validator will operate on a file instead of a stream.
        // this way the validator spits out line numbers for errors
        if (!validModelFile(specXmlURL, specSchemaURL)) {
            throw new WdkModelException("Spec validation failed");
        }

        SpecificationsParser abp = null;
        // instantiate Digester and disable XML validation
       try {
        Digester digester = new Digester();
        digester.setValidating(false);
        
       

        // instantiate SpecificationsParser class
        //digester.addObjectCreate("root", SpecificationsParser.class);
        digester.addObjectCreate("Specifications", SpecificationsParser.class);
        // instantiate Contact class
        digester.addObjectCreate("Specifications/Defaults", Defaults.class);
       digester.addObjectCreate("Specifications/Page", Page.class);
       // digester.addCallMethod("Specifications/Page/PageDefaults", "setPageDefaults",0);
      digester.addObjectCreate("Specifications/Page/PageDefaults", PageDefaults.class);

        digester.addObjectCreate("Specifications/Page/QuestionSetName", QuestionSetName.class);
        digester.addObjectCreate("Specifications/Page/QuestionSetName/QuestionFullName", QuestionFullName.class);
       digester.addObjectCreate("Specifications/Page/QuestionSetName/QuestionFullName/Parameter", Parameter.class);
      digester.addSetNext("Specifications/Page/QuestionSetName/QuestionFullName/Parameter", "addParameter");
      digester.addSetProperties("Specifications/Page/QuestionSetName/QuestionFullName/Parameter");

     digester.addCallMethod("Specifications/Defaults/QuestionType","setQuestionType",0);
     digester.addCallMethod("Specifications/Defaults/FlatVocabFormat","setFlatVocabFormat",0);
     digester.addCallMethod("Specifications/Defaults/NonFlatVocabFormat","setNonFlatVocabFormat",0);
     digester.addCallMethod("Specifications/Defaults/ParamAlign","setParamAlign",0);

   digester.addCallMethod("Specifications/Page/PageDefaults/QuestionType","setQuestionType",0);
     digester.addCallMethod("Specifications/Page/PageDefaults/FlatVocabFormat","setFlatVocabFormat",0);
     digester.addCallMethod("Specifications/Page/PageDefaults/NonFlatVocabFormat","setNonFlatVocabFormat",0);
     digester.addCallMethod("Specifications/Page/PageDefaults/ParamAlign","setParamAlign",0);


      digester.addCallMethod("Specifications/Page/QuestionSetName/QuestionFullName/Parameter/FlatVocabFormat","setFlatVocabFormat",0);
     digester.addCallMethod("Specifications/Page/QuestionSetName/QuestionFullName/Parameter/NonFlatVocabFormat","setNonFlatVocabFormat",0);

      digester.addCallMethod("Specifications/Page/QuestionSetName/QuestionFullName/Parameter/Align","setAlign",0);
        // set type property of Contact instance when 'type' attribute is found
       digester.addSetProperties("Specifications/Page","name", "name" );
       digester.addSetProperties("Specifications/Page/QuestionSetName","name", "name" );
       digester.addSetProperties("Specifications/Page/QuestionSetName","type", "type" );
 
       digester.addSetProperties("Specifications/Page/QuestionSetName/QuestionFullName","name", "name" );
         digester.addSetProperties("Specifications/Page/QuestionSetName/QuestionFullName","hiddenName", "hiddenName" );
    

        // set different properties of Contact instance using specified methods
        digester.addCallMethod("Specifications/Page/QuestionSetName/QuestionFullName/Type", "setType", 0);
        digester.addCallMethod("Specifications/Page/QuestionSetName/QuestionFullName/Page", "setPage", 0); 
       // digester.addCallMethod("Specifications/Page/QuestionSetName/QuestionFullName/Parameter","addParameter",1);
       //digester.addCallParam("Specifications/Page/QuestionSetName/QuestionFullName/Parameter",0);

// call 'addContact' method when the next 'address-book/contact' pattern is seen
      digester.addSetNext("Specifications/Page", "addPage");
        //digester.addCallMethod("Specifications/Defaults", "setDefaults",0);
      digester.addSetNext("Specifications/Page/PageDefaults", "addPageDefaults");
        digester.addSetNext("Specifications/Page/QuestionSetName", "addQuestionSetName");
       digester.addSetNext("Specifications/Page/QuestionSetName/QuestionFullName", "addQuestionFullName");
        
      
        
       // System.out.println("after addContact");
        // now that rules and actions are configured, start the parsing process
        
        File input = new File("/usr/local/tomcat/webapps/WDKToySite1.7/WEB-INF/wdk-model/config/specifications.xml");
        abp = (SpecificationsParser)digester.parse(input);
        System.out.println("abp " + abp);
        System.out.println("Parse done in new SpecificationsParser"); 
        System.err.print(abp.toString());
         
        
        } catch(IOException ioex) { System.err.println(ioex.getMessage()); 
      } catch(SAXException saex) {System.err.println(saex.getMessage()); 
      }
     for (int i=0;i<abp.pageList.size();i++)
      {
       Page page = (Page)abp.pageList.get(i);
     // for (int n=0; n<page.getPageDefaults().size();n++)
     // {
        System.out.println("In pageDefaults loop");
     
     PageDefaults pageDefaults= new PageDefaults();

     if (page.getPageDefaults().size() != 0)
       pageDefaults=(PageDefaults)page.getPageDefaults().get(0);
  
      for (int j=0;j<page.getQuestionSetList().size();j++)
        {
         QuestionSetName qSetName = (QuestionSetName)page.getQuestionSetList().get(j);
        for (int k=0;k<qSetName.getQuestionList().size();k++)
         {
          QuestionFullName qName = (QuestionFullName)qSetName.getQuestionList().get(k);
          
          System.out.println(page.getName() + " Page Default "+ pageDefaults);
        
          if (qName.getType() == "" && pageDefaults != null) 
            qName.setType(pageDefaults.getQuestionType());
          if (qName.getType() == "")
            qName.setType(Defaults.getQuestionType());
  
           for (int m=0;m<qName.getParameters().size();m++)
            { 
            Parameter pName = (Parameter)qName.getParameters().get(m);
            if (pName.getFlatVocabFormat() == "" && pageDefaults != null) {
          //   System.out.println("Page Defaults are present "); 
             pName.setFlatVocabFormat(pageDefaults.getFlatVocabFormat());
            }
            
            if (pName.getFlatVocabFormat() == "") {
           //  System.out.println("FlatVocabFormat is empty");
             pName.setFlatVocabFormat(Defaults.getFlatVocabFormat());
            }
             if (pName.getNonFlatVocabFormat() == "" && pageDefaults != null)
             pName.setNonFlatVocabFormat(pageDefaults.getNonFlatVocabFormat());
            if (pName.getNonFlatVocabFormat() == "")
             pName.setNonFlatVocabFormat(Defaults.getNonFlatVocabFormat());

             if (pName.getAlign() == "" && pageDefaults != null)
             pName.setAlign(pageDefaults.getParamAlign());
            if (pName.getAlign() == "")
             pName.setAlign(Defaults.getParamAlign());
            }
         }
        }
      // } 
      }
     return abp;    
    }
 

 private static boolean validModelFile(URL specXmlURL, URL specSchemaURL) throws WdkModelException {

        System.setProperty("org.apache.xerces.xni.parser.XMLParserConfiguration", "org.apache.xerces.parsers.XIncludeParserConfiguration");

        try {

            ErrorHandler errorHandler = new ErrorHandlerImpl(System.err);
            PropertyMap schemaProperties = new SinglePropertyMap(ValidateProperty.ERROR_HANDLER, errorHandler);
            ValidationDriver vd = new ValidationDriver(schemaProperties, PropertyMap.EMPTY, null);

            vd.loadSchema(ValidationDriver.uriOrFileInputSource(specSchemaURL.toExternalForm()));

            //System.err.println("specXMLURL is  "+specXmlURL);

            InputSource is = ValidationDriver.uriOrFileInputSource(specXmlURL.toExternalForm());
            //            return vd.validate(new InputSource(modelXMLStream));
            return vd.validate(is);

        } catch (SAXException e) {
            throw new WdkModelException(e);
        } catch (IOException e) {
            throw new WdkModelException(e);
        }
    }
 


}

