package org.gusdb.wdk.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.bind.ValidationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.implementation.ModelXmlParser;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.xml.sax.SAXException;

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

        String gusHome = application.getRealPath(application.getInitParameter(ModelXmlParser.GUS_HOME));
        String modelName = application.getInitParameter(ModelXmlParser.MODEL_NAME);

        String customViewDir = application.getInitParameter(CConstants.WDK_CUSTOMVIEWDIR_PARAM);
        String alwaysGoToSummary = application.getInitParameter(CConstants.WDK_ALWAYSGOTOSUMMARY_PARAM);
        String loginUrl = application.getInitParameter(CConstants.WDK_LOGIN_URL_PARAM);

        try {
            initMemberVars(application, gusHome, modelName, customViewDir,
                    alwaysGoToSummary, loginUrl);
        } catch (WdkModelException ex) {
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

    protected RDBMSPlatformI getPlatform() {
        return platform;
    }

    protected void setPlatform(RDBMSPlatformI platform) {
        this.platform = platform;
    }

    private void initMemberVars(ServletContext application, String gusHome,
            String modelName, String customViewDir, String alwaysGoToSummary,
            String loginUrl) throws WdkModelException {
        try {
            ModelXmlParser parser = new ModelXmlParser(gusHome);
            WdkModel wdkModelRaw = parser.parseModel(modelName);

            WdkModelBean wdkModel = new WdkModelBean(wdkModelRaw);

            setPlatform(wdkModelRaw.getRDBMSPlatform());
            application.setAttribute(CConstants.WDK_MODEL_KEY, wdkModel);
            application.setAttribute(CConstants.WDK_CUSTOMVIEWDIR_KEY,
                    customViewDir);
            application.setAttribute(CConstants.WDK_ALWAYSGOTOSUMMARY_KEY,
                    alwaysGoToSummary);
            application.setAttribute(CConstants.WDK_LOGIN_URL_KEY, loginUrl);
        } catch (SAXException ex) {
            throw new WdkModelException(ex);
        } catch (IOException ex) {
            throw new WdkModelException(ex);
        } catch (ValidationException ex) {
            throw new WdkModelException(ex);
        } catch (ParserConfigurationException ex) {
            throw new WdkModelException(ex);
        } catch (TransformerFactoryConfigurationError ex) {
            throw new WdkModelException(ex);
        } catch (TransformerException ex) {
            throw new WdkModelException(ex);
        }
    }
}
