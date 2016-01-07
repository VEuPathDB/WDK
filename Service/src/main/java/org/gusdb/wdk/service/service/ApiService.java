package org.gusdb.wdk.service.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.wdk.service.formatter.ProjectFormatter;

@Path("/")
public class ApiService extends WdkService {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getServiceApi() {
    return Response.ok(ProjectFormatter.getWdkProjectInfo(getWdkModel()).toString()).build();
  }
}
