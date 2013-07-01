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

import org.gusdb.wdk.controller.wizard.Wizard;
import org.gusdb.wdk.model.ThreadMonitor;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wsf.service.WsfService;
import org.json.JSONException;
import org.xml.sax.SAXException;

/**
 * A class that is initialized at the start of the web application. This makes
 * sure global resources are available to all the contexts that need them
 * 
 */
public class ApplicationInitListener implements ServletContextListener {

    public static boolean resourceExists(String path,
            ServletContext servletContext) {
        try {
            URL url = servletContext.getResource(path);
            return url != null;
        }
        catch (MalformedURLException exp) {
            RuntimeException e = new RuntimeException(exp);
            throw e;
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        try {
            DBPlatform.closeAllPlatforms();
            ThreadMonitor.shutdown();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void contextInitialized(ServletContextEvent sce) {

        ServletContext servletContext = sce.getServletContext();

        String projectId = servletContext.getInitParameter(Utilities.ARGUMENT_PROJECT_ID);
        String gusHome = servletContext.getRealPath(servletContext.getInitParameter(Utilities.SYSTEM_PROPERTY_GUS_HOME));

        String alwaysGoToSummary = servletContext.getInitParameter(CConstants.WDK_ALWAYSGOTOSUMMARY_PARAM);
        String loginUrl = servletContext.getInitParameter(CConstants.WDK_LOGIN_URL_PARAM);

        try {
            initMemberVars(servletContext, projectId, gusHome,
                    alwaysGoToSummary, loginUrl);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void initMemberVars(ServletContext servletContext, String projectId,
            String gusHome, String alwaysGoToSummary, String loginUrl)
            throws WdkModelException, NoSuchAlgorithmException,
            ParserConfigurationException, TransformerFactoryConfigurationError,
            TransformerException, IOException, SAXException, SQLException,
            JSONException, WdkUserException, InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        WdkModel wdkModelRaw = WdkModel.construct(projectId, gusHome);

        WdkModelBean wdkModel = new WdkModelBean(wdkModelRaw);

        // load wizard
        Wizard wizard = Wizard.loadWizard(gusHome, wdkModel);

        servletContext.setAttribute(CConstants.WDK_MODEL_KEY, wdkModel);
        servletContext.setAttribute(CConstants.WDK_WIZARD_KEY, wizard);
        servletContext.setAttribute(CConstants.WDK_ALWAYSGOTOSUMMARY_KEY,
                alwaysGoToSummary);
        servletContext.setAttribute(CConstants.WDK_LOGIN_URL_KEY, loginUrl);
        servletContext.setAttribute(CConstants.GUS_HOME_KEY, gusHome);

        // set the context to WsfService so that it can be accessed in the local
        // mode.
        WsfService.SERVLET_CONTEXT = servletContext;
    }
}
