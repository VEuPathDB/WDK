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
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dbms.CacheFactory;
import org.gusdb.wdk.service.WdkServiceApplication;
import org.json.JSONObject;

public class Server extends RESTServer {

  private static final String CLEAN_CACHE_FLAG = "-cleanCacheAtStartup";

  public static void main(String[] args) throws WdkModelException {
    Server server = new Server(args);
    if (server._cleanCacheAtStartup) {
      new CacheFactory(getWdkModel()).recreateCache(false, true);
    }
    server.start();
  }

  public static WdkModel getWdkModel() {
    return (WdkModel)getApplicationContext().get(Utilities.WDK_MODEL_KEY);
  }

  private String _projectId;
  private boolean _cleanCacheAtStartup;

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
    if (!(args.length == 2 || (args.length == 3 && args[2].equals(CLEAN_CACHE_FLAG)))) {
      System.err.println("\n" +
          "USAGE: fgpJava " + getClass().getName() + " <projectId> (<serviceUri>|<port>) [" + CLEAN_CACHE_FLAG + "]\n\n" +
          "Notes: If the second argument is an integer, service will be hosted at http://localhost:<port>\n" +
          "       Optional argument " + CLEAN_CACHE_FLAG + " will do exactly what it says\n\n");
      System.exit(1);
    }
    _projectId = args[0];
    _cleanCacheAtStartup = args.length == 3;
    if (FormatUtil.isInteger(args[1])) {
      // caller sent a port number
      return new ThreeTuple<>("http://localhost/", Integer.parseInt(args[1]), new JSONObject());
    }
    URI serviceUri = UriBuilder.fromUri(args[1]).build();
    return new ThreeTuple<>(serviceUri.toString(), serviceUri.getPort(), new JSONObject());
  }
}
