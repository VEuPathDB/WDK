package org.gusdb.wdk.server;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.server.ResourceConfig;
import org.gusdb.fgputil.FormatUtil;
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
  protected ApplicationContext createApplicationContext(URI baseUri, JSONObject config) {
    return new WdkApplicationContext(
        // basically the replacement for config contained in web.xml; set init parameters
        GusHome.getGusHome(), // get from ENV
        _projectId,           // get from command line
        baseUri               // defined below
    );
  }

  @Override
  protected ThreeTuple<String, Integer, JSONObject> parseConfig(String[] args) {
    // expected: projectId, port
    if (args.length != 2) {
      System.err.println("\nUSAGE: fgpJava " + getClass().getName() + " <projectId> [<serviceUri>|<port>]\n\n" +
          "Note: If the second argument is an integer, service will be hosted at http://localhost:<port>\n");
      System.exit(1);
    }
    _projectId = args[0];
    if (FormatUtil.isInteger(args[1])) {
      // caller sent a port number
      return new ThreeTuple<>("http://localhost/", Integer.parseInt(args[1]), new JSONObject());
    }
    URI serviceUri = UriBuilder.fromUri(args[1]).build();
    return new ThreeTuple<>(serviceUri.toString(), serviceUri.getPort(), new JSONObject());
  }
}
