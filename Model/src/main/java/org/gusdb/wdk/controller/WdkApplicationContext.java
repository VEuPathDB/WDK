package org.gusdb.wdk.controller;

import java.io.IOException;

import org.gusdb.fgputil.server.BasicApplicationContext;
import org.gusdb.wdk.model.Utilities;

public class WdkApplicationContext extends BasicApplicationContext {

  private static final String DEFAULT_SERVICE_ENDPOINT = "";

  public WdkApplicationContext(
      String gusHome,
      String projectId
  ) {
    this(gusHome, projectId, DEFAULT_SERVICE_ENDPOINT);
  }

  public WdkApplicationContext(
      String gusHome,
      String projectId,
      String serviceEndpoint
  ) {

    // basically the replacement for config contained in web.xml; set init parameters
    setInitParameter(WdkInitializer.GUS_HOME_KEY, gusHome);
    setInitParameter(Utilities.ARGUMENT_PROJECT_ID, projectId);
    setInitParameter(Utilities.WDK_SERVICE_ENDPOINT_KEY, serviceEndpoint);

    // initialize the application
    initialize();
  }

  protected void initialize() {
    WdkInitializer.initializeWdk(this);
  }

  @Override
  public void close() throws IOException {
    WdkInitializer.terminateWdk(this);
  }

}
