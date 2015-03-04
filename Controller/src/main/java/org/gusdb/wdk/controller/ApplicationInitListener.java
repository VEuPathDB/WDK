package org.gusdb.wdk.controller;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * A class that is initialized at the start of the web application. This makes
 * sure global resources are available to all the contexts that need them
 * 
 */
public class ApplicationInitListener implements ServletContextListener {

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    WdkInitializer.initializeWdk(sce.getServletContext());
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    WdkInitializer.terminateWdk(sce.getServletContext());
  }
}
