package org.gusdb.wdk.service.init;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.gusdb.wdk.controller.ContextLookup;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.service.service.RecordService;

/**
 * Servlet context listener that initializes service-layer caches after the WdkModel has been created.
 * This must run after ApplicationInitListener has set up the model.
 *
 * To use this listener, add it to web.xml AFTER ApplicationInitListener:
 */
public class ServiceCacheInitializer implements ServletContextListener {

  private static final Logger LOG = Logger.getLogger(ServiceCacheInitializer.class);

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    try {
      LOG.info("Initializing service caches...");

      WdkModel wdkModel = ContextLookup.getWdkModel(sce.getServletContext());

      if (wdkModel == null) {
        throw new RuntimeException("WdkModel not found in context. Ensure ApplicationInitListener runs before ServiceCacheInitializer.");
      }

      // Generate expanded record classes cache for /record-types?format=expanded endpoint
      LOG.info("Generating expanded record classes cache...");
      RecordService.generateExpandedRecordClassesCache(wdkModel);
      LOG.info("Service cache initialization complete.");

    } catch (Exception e) {
      throw new RuntimeException("Unable to initialize WDK service caches.", e);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    // Nothing to clean up
  }
}
