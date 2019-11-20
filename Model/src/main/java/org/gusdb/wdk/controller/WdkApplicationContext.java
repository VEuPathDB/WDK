package org.gusdb.wdk.controller;

import java.io.IOException;
import java.net.URI;

import org.gusdb.fgputil.server.BasicApplicationContext;
import org.gusdb.wdk.model.Utilities;

public class WdkApplicationContext extends BasicApplicationContext {

  public WdkApplicationContext(
      String gusHome,
      String projectId,
      URI serviceUri
  ) {

    // basically the replacement for config contained in web.xml; set init parameters
    setInitParameter(WdkInitializer.GUS_HOME_KEY, gusHome);
    setInitParameter(Utilities.ARGUMENT_PROJECT_ID, projectId);
    setInitParameter(Utilities.WDK_SERVICE_ENDPOINT_KEY, serviceUri.getPath());

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
