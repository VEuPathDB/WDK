package org.gusdb.wdk.controller;

import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.jsp.jstl.core.Config;
import javax.sql.DataSource;

  
/**
 * A class that is initialised at the start of the web application. This makes sure global resources 
 * are available to all the contexts that need them
 * 
 */
public class ApplicationInitListener implements ServletContextListener {
    
    private static final String DEFAULT_LOGIN_CONFIGURATION = "/WEB-INF/wdk-config/login.xml";
    private static final String DEFAULT_MODEL_CONFIGURATION = "/WEB-INF/wdk-config/model.xml";
    private static final String DEFAULT_MODEL_PARSER = "org.gusdb.wdk.model.implementation.ModelXmlParser";
    private static final String DEFAULT_PROPS_LOCATION = "/WEB-INF/wdk-config/macro.props";
  
    private static final Logger logger = Logger.getLogger("org.gusdb.wdk.controller.ApplicationInitListener");

    private DataSource dataSource;
    private RDBMSPlatformI platform;
  
    public void contextDestroyed(ServletContextEvent sce) {
        WdkModel model = (WdkModel) sce.getServletContext().getAttribute("wdk.wdkModel");
        RDBMSPlatformI platform = model.getRDBMSPlatform();
        try {
            platform.close();
        } catch (WdkModelException exp) {
            throw new RuntimeException(exp);
        }
    }
  
    
    public void contextInitialized(ServletContextEvent sce) {
  
        ServletContext application  = sce.getServletContext(  );
       
        String loginXML = application.getInitParameter("loginConfig");
        String querySetLocation = application.getInitParameter("querySetConfig");
        String schemaLocation = application.getInitParameter("schemaLocation");
        String schemaName = application.getInitParameter("schemaName");
        String propsFileLocation = application.getInitParameter("propsFileLocation");
        String loggingFileLocation = application.getInitParameter("loggingFileLocation");
        String parserClass = application.getInitParameter("parserClass");
        int poolMaxWait = Integer.parseInt(application.getInitParameter("connectionPoolMaxWait"));

        
        initMemberVars(loginXML, parserClass, querySetLocation, schemaName, schemaLocation, propsFileLocation, poolMaxWait, application);
        
        Config.set(application, Config.SQL_DATA_SOURCE, dataSource);

        
        // Logging
        if (loggingFileLocation != null) {
            WdkLogManager.INSTANCE.setLogFilename(loggingFileLocation);
            Handler fh;
            try {
                fh = new FileHandler(loggingFileLocation);
                fh.setFormatter(new SimpleFormatter());
                fh.setLevel(Level.ALL);
                Logger.getLogger("org.gusdb").addHandler(fh);
                //Logger.getLogger("org.gusdb").setUseParentHandlers(false);
            } catch (SecurityException exp) {
                application.log("IMPORTANT: Unable to create a logging handler - security exception", exp);
            } catch (IOException exp) {
                application.log("IMPORTANT: Unable to create a logging handler - I/O exception", exp);
            }

        }
    }
    

    private URL createURL(String param, String defaultLoc, ServletContext application) {

        if (param == null) {
            param = defaultLoc;
        }
        
        URL ret = null;
        try {
            ret = application.getResource(param);
            if (ret ==null) {
                RuntimeException e = new RuntimeException("Missing resource. Unable to create URL from "+param);
                logger.throwing(this.getClass().getName(), "createURL", e);
                throw e;
            }
        }
        catch (MalformedURLException exp) {
            RuntimeException e = new RuntimeException(exp);
            logger.throwing(this.getClass().getName(), "createURL", exp);
            throw e;
        }
        return ret;
    }


    protected DataSource getDataSource() {
        return dataSource;
    }

    
    private void initMemberVars(String loginConfigLocation, String parserClass, String queryConfigLocation, 
            String schemaName, String schemaLocation, String propsLocation, int maxWait, ServletContext application) {
        
        if (schemaName != null && schemaLocation != null) {
            throw new RuntimeException("Configuration error. Both schemaName and schemaLocation are specified.");
        }
        
        URL schemaURL = null;
        if (schemaName != null) {
            schemaURL = WdkModel.class.getResource(schemaName);   
        }
        if (schemaLocation != null) {
            schemaURL = createURL(schemaLocation, null, application);
        }
        
        if (parserClass == null) {
            parserClass = DEFAULT_MODEL_PARSER;
        }
        
        URL querySetURL = createURL(queryConfigLocation, DEFAULT_MODEL_CONFIGURATION, application);
        URL modelConfigXmlURL = createURL(loginConfigLocation, DEFAULT_LOGIN_CONFIGURATION, application);
        URL propsURL = createURL(propsLocation, DEFAULT_PROPS_LOCATION, application);
            
        // read config info
        try {
            
            Class parser = Class.forName(parserClass);
            Method build = parser.getDeclaredMethod("parseXmlFile", new Class[] {URL.class, URL.class, URL.class});
            WdkModel wdkModel = (WdkModel) build.invoke(null, new Object[] {querySetURL, propsURL, schemaURL});

            wdkModel.configure(modelConfigXmlURL);
            wdkModel.setResources();
            
            this.dataSource = wdkModel.getRDBMSPlatform().getDataSource();
            
            application.setAttribute("wdk.resultFactory", wdkModel.getResultFactory());
            application.setAttribute("wdk.wdkModel", wdkModel);
         
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }

    }
}
