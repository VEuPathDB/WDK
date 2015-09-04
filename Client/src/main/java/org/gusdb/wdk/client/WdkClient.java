package org.gusdb.wdk.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.glassfish.jersey.server.mvc.Template;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Returns HTML by rendering the index template, for all HTML paths.
 * 
 * For POST requests, the "payload" parameter is parsed as a JSONObject and
 * passed to the template as the model.
 *
 *
 * @author dfalke
 *
 */
@Path("/{any: .*}")
@Produces("text/html")
public class WdkClient {
  
  private static final String PAYLOAD_PARAM = "payload";

  @GET
  @Template(name = "/index")
  public JSONObject get() {
    return new JSONObject();
  }

  // FIXME Error handling
  @POST
  @Consumes("application/x-www-form-urlencoded")
  @Template(name = "/index")
  public JSONObject post(@FormParam(PAYLOAD_PARAM) String payload) {
    return isEmpty(payload) ? new JSONObject()
      : new JSONObject(new JSONTokener(payload));
  }
  
  private static boolean isEmpty(String string) {
    return (string == null || string.isEmpty() || string.equals("undefined"));
  }

}