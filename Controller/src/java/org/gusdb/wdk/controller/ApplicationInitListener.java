package org.gusdb.gus.wdk.controller;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import oracle.jdbc.pool.OracleConnectionCacheImpl;

import org.gusdb.gus.wdk.model.ModelConfig;
import org.gusdb.gus.wdk.model.ModelConfigParser;
import org.gusdb.gus.wdk.model.RDBMSPlatformI;
import org.gusdb.gus.wdk.model.ResultFactory;
import org.gusdb.gus.wdk.model.WdkModel;
import org.gusdb.gus.wdk.model.WdkModelException;
import org.gusdb.gus.wdk.model.implementation.ModelXMLParserRelaxNG;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.jsp.jstl.core.Config;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

  
/**
 * A class that is initialised at the start of the web application. This makes sure global resources 
 * are available to all the contexts that need them
 * 
 */
public class ApplicationInitListener implements ServletContextListener {
  
    private DataSource dataSource;
    
    public void contextInitialized(ServletContextEvent sce) {
  
        ServletContext application  = sce.getServletContext(  );

        
        String loginXML = application.getInitParameter("loginConfig");
        String querySetLocation = application.getInitParameter("querySetConfig");
        String schemaLocation = application.getInitParameter("schemaLocation");
        
        initMemberVars(loginXML, querySetLocation, schemaLocation, application);
        
        Config.set(application, Config.SQL_DATA_SOURCE, dataSource);


    }
  
    public void contextDestroyed(ServletContextEvent sce) {
        // Nothing to do here for now
    }

    
    private void initMemberVars(String loginConfigLocation, String queryConfigLocation, String schemaLocation, ServletContext application) {
        
        File querySetFile = new File(queryConfigLocation);
        File schemaFile = new File(schemaLocation);
        File modelConfigXmlFile = new File(loginConfigLocation);
        
            // read config info
            ModelConfig dbConfig;
            try {
                dbConfig = ModelConfigParser.parseXmlFile(modelConfigXmlFile);
                
                String instanceTable = dbConfig.getQueryInstanceTable();
                String platformClass = dbConfig.getPlatformClass();
                
                dataSource = setupDataSource(dbConfig.getConnectionUrl()
                        , dbConfig.getLogin()
                        , dbConfig.getPassword());
                
                RDBMSPlatformI platform = 
                    (RDBMSPlatformI)Class.forName(platformClass).newInstance();
                platform.setDataSource(dataSource);
                
                WdkModel wdkModel = ModelXMLParserRelaxNG.parseXmlFile(querySetFile, schemaFile);
                
                ResultFactory resultFactory = new ResultFactory(dataSource, platform, 
                        dbConfig.getLogin(), instanceTable);
                wdkModel.setResources(resultFactory, platform);
                
                
                application.setAttribute("wdk.resultfactory", resultFactory);
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
            } catch (ParserConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            //        } catch (WdkUserException e) {
//            System.err.println(e.formatErrors());
//        } catch (Exception e) {
//            e.printStackTrace();
//        } 
        
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

}
