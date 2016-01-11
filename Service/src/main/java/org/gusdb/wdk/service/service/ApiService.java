package org.gusdb.wdk.service.service;

import java.lang.reflect.Field;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.gusdb.wdk.service.formatter.Keys;
import org.gusdb.wdk.service.formatter.ProjectFormatter;
import org.json.JSONObject;

@Path("/api")
public class ApiService extends WdkService {

  private static final Logger LOG = Logger.getLogger(ApiService.class);

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public Response getServiceApi() {
    return Response.ok(ProjectFormatter.WELCOME_MESSAGE).build();
  }

  @GET
  @Path("keys")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPropertyKeys() {
    try {
      JSONObject json = new JSONObject();
      for (Field field : Keys.class.getDeclaredFields()) {
        json.put(field.getName(), field.get(null));
      }
      return Response.ok(json.toString()).build();
    }
    catch (IllegalAccessException e) {
      LOG.error("Error serving API Keys response", e);
      return Response.serverError().build();
    }
  }
}
