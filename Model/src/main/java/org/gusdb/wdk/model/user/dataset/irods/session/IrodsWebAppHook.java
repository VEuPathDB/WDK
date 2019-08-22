package org.gusdb.wdk.model.user.dataset.irods.session;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class IrodsWebAppHook implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    // Nothing to do here
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    IrodsSession.close();
  }
}
