package org.gusdb.wdk.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.gusdb.wdk.controller.wizard.Wizard;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wsf.service.WsfService;
import org.xml.sax.SAXException;

/**
 * A class that is initialized at the start of the web application. This makes
 * sure global resources are available to all the contexts that need them
 * 
 */
public class ApplicationInitListener implements ServletContextListener {

    private static final Logger logger = Logger.getLogger(ApplicationInitListener.class);
    
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

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            ServletContext context = sce.getServletContext();
            WdkModelBean wdkModel = (WdkModelBean)context.getAttribute(CConstants.WDK_MODEL_KEY);
            if (wdkModel != null) {
              // insulate in case model never properly loaded
              logger.info("Releasing resources for WDK Model.");
              wdkModel.getModel().releaseResources();
            }
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        logger.info("Initializing WDK web application");
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
            throws WdkModelException, IOException, SAXException {
        
        logger.info("Initializing model...");
        WdkModel wdkModelRaw = WdkModel.construct(projectId, gusHome);
        WdkModelBean wdkModel = new WdkModelBean(wdkModelRaw);
        logger.info("Initialized model object.  Setting on servlet context.");
        servletContext.setAttribute(CConstants.WDK_MODEL_KEY, wdkModel);

        // set assetsUrl attribtue
        servletContext.setAttribute(CConstants.WDK_ASSETS_URL_KEY, getAssetsUrl(wdkModel, servletContext));

        // load wizard
        Wizard wizard = Wizard.loadWizard(gusHome, wdkModel);
        servletContext.setAttribute(CConstants.WDK_WIZARD_KEY, wizard);
        servletContext.setAttribute(CConstants.WDK_ALWAYSGOTOSUMMARY_KEY,
                alwaysGoToSummary);
        servletContext.setAttribute(CConstants.WDK_LOGIN_URL_KEY, loginUrl);
        servletContext.setAttribute(CConstants.GUS_HOME_KEY, gusHome);

        // set the context to WsfService so that it can be accessed in the local mode.
        WsfService.SERVLET_CONTEXT = servletContext;
    }

    private String getAssetsUrl(WdkModelBean wdkModel, ServletContext servletContext) {
        String url = wdkModel.getModel().getModelConfig().getAssetsUrl();
        if (url == null || url == "") {
          // set context url
          url = servletContext.getContextPath();
        }
        logger.debug("Assets URL: " + url);
        return url;
    }
}
