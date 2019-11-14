package org.gusdb.wdk.server;

import org.glassfish.jersey.server.ResourceConfig;
import org.gusdb.fgputil.Tuples.ThreeTuple;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.fgputil.server.RESTServer;
import org.gusdb.fgputil.web.ApplicationContext;
import org.gusdb.wdk.controller.WdkApplicationContext;
import org.gusdb.wdk.service.WdkServiceApplication;
import org.json.JSONObject;

public class Server extends RESTServer {

  public static void main(String[] args) {
    new Server(args).start();
  }

  private String _projectId;

  public Server(String[] commandLineArgs) {
    super(commandLineArgs);
  }

  @Override
  protected ResourceConfig getResourceConfig() {
    return new ResourceConfig().registerClasses(
        new WdkServiceApplication().getClasses());
  }

  @Override
  protected ApplicationContext createApplicationContext(JSONObject config) {
    return new WdkApplicationContext(
        // basically the replacement for config contained in web.xml; set init parameters
        GusHome.getGusHome(), // get from ENV
        _projectId            // get from command line
    );
  }

  @Override
  protected ThreeTuple<String, Integer, JSONObject> parseConfig(String[] args) {
    // expected: projectId, port
    if (args.length != 2) {
      System.err.println("USAGE: fgpJava " + getClass().getName() + " <projectId> <port>");
      System.exit(1);
    }
    _projectId = args[0];
    int port = Integer.parseInt(args[1]);
    return new ThreeTuple<>("http://localhost/", port, new JSONObject());
  }
}
