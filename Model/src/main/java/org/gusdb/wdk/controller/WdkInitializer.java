package org.gusdb.wdk.controller;

import static org.gusdb.wdk.model.ThreadMonitor.getThreadMonitorConfig;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.wdk.controller.wizard.Wizard;
import org.gusdb.wdk.model.MDCUtil;
import org.gusdb.wdk.model.ThreadMonitor;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;

import com.github.fge.msgsimple.provider.LoadingMessageSourceProvider;

public class WdkInitializer {

  private static final Logger LOG = Logger.getLogger(WdkInitializer.class);

  public static final String GUS_HOME_KEY = "GUS_HOME";
  public static final String WDK_MODEL_KEY = "wdkModel";
  public static final String WDK_WIZARD_KEY = "wdkWizard";
  public static final String WDK_ASSETS_URL_KEY = "assetsUrl";

  public static void initializeWdk(ServletContext servletContext) {
    try {
      MDCUtil.setNonRequestThreadVars("init");
      LOG.info("Initializing WDK web application");

      // get gus home and set on context
      String gusHome = GusHome.webInit(servletContext);
      servletContext.setAttribute(GUS_HOME_KEY, gusHome);

      LOG.info("Initializing model...");
      String projectId = servletContext.getInitParameter(Utilities.ARGUMENT_PROJECT_ID);
      WdkModel wdkModel = WdkModel.construct(projectId, gusHome);

      LOG.info("Initialized model object.  Setting on servlet context.");
      servletContext.setAttribute(Utilities.WDK_MODEL_KEY, wdkModel);

      // Set assetsUrl attribute. It will be null if not defined in the model
      servletContext.setAttribute(WDK_ASSETS_URL_KEY,
          wdkModel.getModelConfig().getAssetsUrl());

      // assign select init parameters as context attributes
      assignInitParamToAttribute(servletContext, Utilities.WDK_SERVICE_ENDPOINT_KEY);

      // load wizard
      LOG.info("Loading wizard configuration.");
      Wizard wizard = Wizard.loadWizard(gusHome, wdkModel);
      servletContext.setAttribute(WDK_WIZARD_KEY, wizard);

      // start up thread monitor
      ThreadMonitor.start(getThreadMonitorConfig(wdkModel));

      LOG.info("WDK web application initialization complete.");
    }
    catch (Exception e) {
      LOG.error("Unable to initialize WDK web application.", e);
      // throw an exception to keep the webapp from loading
      throw new RuntimeException("Unable to initialize WDK web application.", e);
    }
    finally {
      MDCUtil.clearValues();
    }
  }

  public static void terminateWdk(ServletContext servletContext) {
    try {
      MDCUtil.setNonRequestThreadVars("term");
      LOG.info("Terminating WDK web application");

      // shut down thread monitor
      ThreadMonitor.shutDown();

      WdkModel wdkModel = getWdkModel(servletContext);
      if (wdkModel != null) {
        // insulate in case model never properly loaded
        LOG.info("Releasing resources for WDK Model.");
        wdkModel.close();
        LOG.info("WDK resource release complete.");
      }

      // WDK service schema validation code dependency starts hidden threadpool; shut down
      LoadingMessageSourceProvider.shutDownThreadPools();
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    finally {
      MDCUtil.clearValues();
    }
  }

  public static WdkModel getWdkModel(ServletContext servletContext) {
    return (WdkModel)servletContext.getAttribute(Utilities.WDK_MODEL_KEY);
  }

  private static void assignInitParamToAttribute(ServletContext servletContext, String key) {
    servletContext.setAttribute(key, servletContext.getInitParameter(key));
  }
}
