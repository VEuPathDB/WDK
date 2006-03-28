package org.gusdb.wdk.controller;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

import org.gusdb.wdk.model.QuestionFullName;
import org.gusdb.wdk.model.implementation.SpecificationsParser;


/**
 * A class that is initialised at the start of the web application. This makes
 * sure global resources are available to all the contexts that need them
 * 
 */
public class ApplicationInitListener implements ServletContextListener {

    private RDBMSPlatformI platform;

    public void contextDestroyed(ServletContextEvent sce) {
        try {
            getPlatform().close();
        } catch (WdkModelException exp) {
            throw new RuntimeException(exp);
        }
    }

    public void contextInitialized(ServletContextEvent sce) {

        ServletContext application = sce.getServletContext();

        String configXml = application.getInitParameter(CConstants.WDK_MODELCONFIGXML_PARAM);
        String modelXml = application.getInitParameter(CConstants.WDK_MODELXML_PARAM);
        String schema = application.getInitParameter(CConstants.WDK_MODELSCHEMA_PARAM);
        String props = application.getInitParameter(CConstants.WDK_MODELPROPS_PARAM);
        String parserClass = application.getInitParameter(CConstants.WDK_MODELPARSER_PARAM);
        String customViewDir = application.getInitParameter(CConstants.WDK_CUSTOMVIEWDIR_PARAM);
        String xmlSchema = application.getInitParameter(CConstants.WDK_XMLSCHEMA_PARAM);
        String xmlDataDir =application.getInitParameter(CConstants.WDK_XMLDATA_DIR_PARAM);
        
   String specXml = application.getInitParameter(CConstants.WDK_SPECXML_PARAM);
   String specSchema =  application.getInitParameter(CConstants.WDK_SPECSCHEMA_PARAM);
   String specParserClass = application.getInitParameter(CConstants.WDK_SPECPARSER_PARAM);


        xmlDataDir = application.getRealPath(xmlDataDir);
        
        initMemberVars(configXml, modelXml, schema, props, parserClass,
                customViewDir, xmlSchema, xmlDataDir, application,specXml, specSchema, specParserClass);

        // Config.set(application, Config.SQL_DATA_SOURCE, dataSource);
    }

    public static boolean resourceExists(String path,
            ServletContext servletContext) {
        try {
            URL url = servletContext.getResource(path);
            return url != null;
        } catch (MalformedURLException exp) {
            RuntimeException e = new RuntimeException(exp);
            throw e;
        }
    }

    private static URL createURL(String param, String defaultLoc,
            ServletContext application) {

        if (param == null) {
            param = defaultLoc;
        }

        URL ret = null;
        try {
            ret = application.getResource(param);
            if (ret == null) {
                RuntimeException e = new RuntimeException(
                        "Missing resource. Unable to create URL from " + param);
                throw e;
            }
        } catch (MalformedURLException exp) {
            RuntimeException e = new RuntimeException(exp);
            throw e;
        }
        return ret;
    }

    protected RDBMSPlatformI getPlatform() {
        return platform;
    }

    protected void setPlatform(RDBMSPlatformI platform) {
        this.platform = platform;
    }

    private void initMemberVars(String configXml, String modelXml,
            String schema, String props, String parserClass,
            String customViewDir, String xmlschema, String xmlDataDir, ServletContext application, String specXml, String specSchema, String specParserClass) {

        URL schemaURL = null;
        if (schema != null) {
            schemaURL = createURL(schema, null, application);
        } else {
            throw new RuntimeException(
                    "can not start application because schema is absent");
        }
        

   URL specSchemaURL = null;

   if (specSchema != null) {
            specSchemaURL = createURL(specSchema, null, application);
   } else {
            throw new RuntimeException("can not start application because spec schema is absent");
   }



        if (parserClass == null) {
            parserClass = CConstants.DEFAULT_WDKMODELPARSER;
        }
        if (customViewDir == null) {
            customViewDir = CConstants.DEFAULT_WDKCUSTOMVIEWDIR;
        }
        if (xmlDataDir == null) {
            xmlDataDir = CConstants.DEFAULT_XMLDATA_DIR;
        }


   if (specParserClass == null) {
       specParserClass = CConstants.DEFAULT_SPECMODELPARSER;}
                                                                                  



        URL modelURL = createURL(modelXml, CConstants.DEFAULT_WDKMODELXML,
                application);
        URL configURL = createURL(configXml,
                CConstants.DEFAULT_WDKMODELCONFIGXML, application);
        URL propsURL = createURL(props, CConstants.DEFAULT_WDKMODELPROPS,
                application);
        // load the schema URL for xml data source
        URL xmlSchemaURL = createURL(xmlschema, CConstants.DEFAULT_XMLSCHEMA, application);


   URL specURL = createURL(specXml, CConstants.DEFAULT_SPECMODELXML, application);


        // read config info
        try {

            Class parser = Class.forName(parserClass);
            Method build = parser.getDeclaredMethod("parseXmlFile",
                    new Class[] { URL.class, URL.class, URL.class, URL.class, URL.class });
            WdkModel wdkModelRaw = (WdkModel) build.invoke(null, new Object[] {
                    modelURL, propsURL, schemaURL, xmlSchemaURL, configURL });
            
            // set schema for xml data source to the model
            wdkModelRaw.setXmlSchema(xmlSchemaURL);
            
            // set the path for xml data files, this must be absolute path
            wdkModelRaw.setXmlDataDir(new File(xmlDataDir));

            WdkModelBean wdkModel = new WdkModelBean(wdkModelRaw);

            setPlatform(wdkModelRaw.getRDBMSPlatform());
            application.setAttribute(CConstants.WDK_MODEL_KEY, wdkModel);
            application.setAttribute(CConstants.WDK_CUSTOMVIEWDIR_KEY,
                    customViewDir);
        } catch (Exception exp) {
            exp.printStackTrace();
            throw new RuntimeException(exp);
        }


// read spec info
   try {
                                                                                  
                
            Class parser2 = Class.forName(specParserClass);
            System.out.println("Parser: " + parser2);
            Method build2 = parser2.getDeclaredMethod("parseSpecFile", new Class[]
 {URL.class, URL.class});
            System.out.println("Build2 : " + build2);
            System.out.println("\nBEFORE instantiation of contactRAw\n");


            SpecificationsParser contactRaw = (SpecificationsParser) build2.invoke(null, new Object[]{specURL, specSchemaURL});


            System.out.println("contactRaw: " + contactRaw);
               
            //WdkModelBean wdkModel = new WdkModelBean(wdkModelRaw);
                                                                                                  
            //setPlatform(wdkModelRaw.getRDBMSPlatform());
            application.setAttribute(CConstants.SPEC_MODEL_KEY, contactRaw);
            //application.setAttribute(CConstants.WDK_CUSTOMVIEWDIR_KEY, customViewDir);
   } catch (Exception exp) {
            exp.printStackTrace();
            throw new RuntimeException(exp);
   }



   } //end initMemberVars
} //end class

