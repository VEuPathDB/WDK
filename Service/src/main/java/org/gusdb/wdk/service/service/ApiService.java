package org.gusdb.wdk.service.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class ApiService extends WdkService {

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public Response getServiceApi() {
    return Response.ok("Welcome to the WDK 3.0 Web Service").build();
  }
}
