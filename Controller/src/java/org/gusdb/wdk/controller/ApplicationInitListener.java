package org.gusdb.gus.wdk.controller;

import oracle.jdbc.pool.OracleConnectionCacheImpl;

import org.gusdb.gus.wdk.model.ModelConfig;
import org.gusdb.gus.wdk.model.ModelConfigParser;
import org.gusdb.gus.wdk.model.RDBMSPlatformI;
import org.gusdb.gus.wdk.model.ResultFactory;
import org.gusdb.gus.wdk.model.WdkModel;
import org.gusdb.gus.wdk.model.WdkModelException;
import org.gusdb.gus.wdk.model.implementation.ModelXmlParser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
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

import org.xml.sax.SAXException;

  
/**
 * A class that is initialised at the start of the web application. This makes sure global resources 
 * are available to all the contexts that need them
 * 
 */
public class ApplicationInitListener implements ServletContextListener {
  
    private static final Logger logger = Logger.getLogger("org.gusdb.gus.wdk.controller.ApplicationInitListener");
    
    private static final String DEFAULT_LOGIN_CONFIGURATION = "/WEB-INF/wdk-config/login.xml";
    private static final String DEFAULT_MODEL_CONFIGURATION = "/WEB-INF/wdk-config/model.xml";
    private static final String DEFAULT_PROPS_LOCATION = "/WEB-INF/wdk-config/macro.props";

    private DataSource dataSource;
    
    public void contextInitialized(ServletContextEvent sce) {
  
        ServletContext application  = sce.getServletContext(  );
       
        String loginXML = application.getInitParameter("loginConfig");
        String querySetLocation = application.getInitParameter("querySetConfig");
        String schemaLocation = application.getInitParameter("schemaLocation");
        String schemaName = application.getInitParameter("schemaName");
        String propsFileLocation = application.getInitParameter("propsFileLocation");
        String loggingFileLocation = application.getInitParameter("loggingFileLocation");
        
        initMemberVars(loginXML, querySetLocation, schemaName, schemaLocation, propsFileLocation, application);
        
        Config.set(application, Config.SQL_DATA_SOURCE, dataSource);

        
        // Logging
        if (loggingFileLocation != null) {
            WdkLogManager.INSTANCE.setLogFilename(loggingFileLocation);
            Handler fh;
            try {
                fh = new FileHandler(loggingFileLocation);
                fh.setFormatter(new SimpleFormatter());
                fh.setLevel(Level.ALL);
                Logger.getLogger("org.gusdb.gus").addHandler(fh);
                //Logger.getLogger("org.gusdb.gus").setUseParentHandlers(false);
            } catch (SecurityException exp) {
                application.log("IMPORTANT: Unable to create a logging handler - security exception", exp);
            } catch (IOException exp) {
                application.log("IMPORTANT: Unable to create a logging handler - I/O exception", exp);
            }

        }
    }
  
    public void contextDestroyed(ServletContextEvent sce) {
        // Nothing to do here for now
    }

    
    private void initMemberVars(String loginConfigLocation, String queryConfigLocation, 
            String schemaName, String schemaLocation, String propsLocation, ServletContext application) {
        
        if (schemaName != null && schemaLocation != null) {
            throw new RuntimeException("Configuration error. Both schemaName and schemaLocation are specified.");
        }
        
        URL schemaURL = null;   
        if (schemaName != null) {
            schemaURL = WdkModel.INSTANCE.getClass().getResource(schemaName);   
        }
        if (schemaLocation != null) {
            schemaURL = createURL(schemaLocation, null, application);
        }
        
        URL querySetURL = createURL(queryConfigLocation, DEFAULT_MODEL_CONFIGURATION, application);
        URL modelConfigXmlURL = createURL(loginConfigLocation, DEFAULT_LOGIN_CONFIGURATION, application);
        URL propsURL = createURL(propsLocation, DEFAULT_PROPS_LOCATION, application);
            
        // read config info
        ModelConfig dbConfig;
        try {
            dbConfig = ModelConfigParser.parseXmlFile(modelConfigXmlURL);
            
            String instanceTable = dbConfig.getQueryInstanceTable();
            String platformClass = dbConfig.getPlatformClass();
            
            dataSource = setupDataSource(dbConfig.getConnectionUrl()
                    , dbConfig.getLogin()
                    , dbConfig.getPassword());
            
            RDBMSPlatformI platform = 
                (RDBMSPlatformI)Class.forName(platformClass).newInstance();
            platform.setDataSource(dataSource);
            

            WdkModel wdkModel = ModelXmlParser.parseXmlFile(querySetURL, propsURL, schemaURL);
            
            ResultFactory resultFactory = new ResultFactory(dataSource, platform, 
                    dbConfig.getLogin(), instanceTable);
            wdkModel.setResources(resultFactory, platform);
            
            application.setAttribute("wdk.resultFactory", resultFactory);
            application.setAttribute("wdk.wdkModel", wdkModel);
         
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (WdkModelException e) {
            throw new RuntimeException(e);
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
    
    private DataSource setupDataSource(String connectURI, String login, 
              String password)  {

        try {
            OracleConnectionCacheImpl ds = new oracle.jdbc.pool.OracleConnectionCacheImpl();
            ds.setURL(connectURI);

            ds.setPassword(password);
            ds.setUser(login);
            return (DataSource) ds;
        }
        catch (SQLException exp) {
            exp.printStackTrace();
        }

        return null;
    }

    protected DataSource getDataSource() {
        return dataSource;
    }
}
