package org.gusdb.wdk.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.json.JSONException;
import org.xml.sax.SAXException;

/**
 * A class that is initialized at the start of the web application. This makes
 * sure global resources are available to all the contexts that need them
 * 
 */
public class ApplicationInitListener implements ServletContextListener {

    public void contextDestroyed(ServletContextEvent sce) {
        try {
            DBPlatform.closeAllPlatforms();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void contextInitialized(ServletContextEvent sce) {

        ServletContext application = sce.getServletContext();

        String modelName = application.getInitParameter(Utilities.ARGUMENT_PROJECT_ID);
        String gusHome = application.getRealPath(application.getInitParameter(Utilities.SYSTEM_PROPERTY_GUS_HOME));

        String customViewDir = application.getInitParameter(CConstants.WDK_CUSTOMVIEWDIR_PARAM);
        String alwaysGoToSummary = application.getInitParameter(CConstants.WDK_ALWAYSGOTOSUMMARY_PARAM);
        String loginUrl = application.getInitParameter(CConstants.WDK_LOGIN_URL_PARAM);

        try {
            initMemberVars(application, modelName, gusHome, customViewDir,
                    alwaysGoToSummary, loginUrl);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
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

    private void initMemberVars(ServletContext application, String modelName,
            String gusHome, String customViewDir, String alwaysGoToSummary,
            String loginUrl) throws WdkModelException,
            NoSuchAlgorithmException, ParserConfigurationException,
            TransformerFactoryConfigurationError, TransformerException,
            IOException, SAXException, SQLException, JSONException,
            WdkUserException, InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        WdkModel wdkModelRaw = WdkModel.construct(modelName, gusHome);

        WdkModelBean wdkModel = new WdkModelBean(wdkModelRaw);

        application.setAttribute(CConstants.WDK_MODEL_KEY, wdkModel);
        application.setAttribute(CConstants.WDK_CUSTOMVIEWDIR_KEY,
                customViewDir);
        application.setAttribute(CConstants.WDK_ALWAYSGOTOSUMMARY_KEY,
                alwaysGoToSummary);
        application.setAttribute(CConstants.WDK_LOGIN_URL_KEY, loginUrl);
    }
}
