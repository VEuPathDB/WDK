package org.gusdb.wdk.controller;

import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.WdkModelException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

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
    
    private RDBMSPlatformI platform;
  
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            getPlatform().close();
        } catch (WdkModelException exp) {
            throw new RuntimeException(exp);
        }
    }
  
    
    public void contextInitialized(ServletContextEvent sce) {
  
        ServletContext application  = sce.getServletContext(  );
       
        String configXml = application.getInitParameter(CConstants.WDK_MODELCONFIGXML_PARAM);
        String modelXml = application.getInitParameter(CConstants.WDK_MODELXML_PARAM);
        String schema = application.getInitParameter(CConstants.WDK_MODELSCHEMA_PARAM);
        String props = application.getInitParameter(CConstants.WDK_MODELPROPS_PARAM);
	String parserClass = application.getInitParameter(CConstants.WDK_MODELPARSER_PARAM);
	String customViewDir = application.getInitParameter(CConstants.WDK_CUSTOMVIEWDIR_PARAM);
      
        initMemberVars(configXml, modelXml, schema, props, parserClass, customViewDir, application);
        
        //Config.set(application, Config.SQL_DATA_SOURCE, dataSource);
    }
    
    public static boolean resourceExists(String path, ServletContext servletContext) {
	URL url = createURL(path, null, servletContext);
	return url != null;
    }

    private static URL createURL(String param, String defaultLoc, ServletContext application) {

        if (param == null) {
            param = defaultLoc;
        }
        
        URL ret = null;
        try {
            ret = application.getResource(param);
            if (ret ==null) {
                RuntimeException e = new RuntimeException("Missing resource. Unable to create URL from "+param);
                throw e;
            }
        }
        catch (MalformedURLException exp) {
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
    
    private void initMemberVars(String configXml, String modelXml, String schema,
				String props, String parserClass, String customViewDir,
				ServletContext application) {
        URL schemaURL = null;
        if (schema != null) {
            schemaURL = createURL(schema, null, application);
        } else {
	    throw new RuntimeException("can not start application because schema is absent");
	}
        
        if (parserClass == null) {
            parserClass = CConstants.DEFAULT_WDKMODELPARSER;
        }
	if (customViewDir == null) {
            customViewDir = CConstants.DEFAULT_WDKCUSTOMVIEWDIR;
	}
        
        URL modelURL = createURL(modelXml, CConstants.DEFAULT_WDKMODELXML, application);
        URL configURL = createURL(configXml, CConstants.DEFAULT_WDKMODELCONFIGXML, application);
        URL propsURL = createURL(props, CConstants.DEFAULT_WDKMODELPROPS, application);
            
        // read config info
        try {
            
            Class parser = Class.forName(parserClass);
            Method build = parser.getDeclaredMethod("parseXmlFile", new Class[] {URL.class, URL.class, URL.class, URL.class});
            WdkModel wdkModelRaw = (WdkModel) build.invoke(null, new Object[] {modelURL, propsURL, schemaURL, configURL});
	    WdkModelBean wdkModel = new WdkModelBean(wdkModelRaw);

            setPlatform(wdkModelRaw.getRDBMSPlatform());
	    application.setAttribute(CConstants.WDK_MODEL_KEY, wdkModel);         
	    application.setAttribute(CConstants.WDK_CUSTOMVIEWDIR_KEY, customViewDir);
        } catch (Exception exp) {
	    exp.printStackTrace();
            throw new RuntimeException(exp);
        }

    }
}
