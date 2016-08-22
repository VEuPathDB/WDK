package org.gusdb.wdk.service.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.wdk.service.formatter.ProjectFormatter;

@Path("/")
public class ProjectService extends WdkService {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getServiceApi() {
    return Response.ok(ProjectFormatter.getWdkProjectInfo(getWdkModel()).toString()).build();
  }

  @GET
  @Path("teapot")
  @Produces(MediaType.TEXT_HTML)
  public Response getTeapot() {
    String assetsUrl = getWdkModel().getModelConfig().getAssetsUrl();
    String imageLink = assetsUrl + "/wdk/images/r2d2_ceramic_teapot.jpg";
    String html = "<!DOCTYPE html><html><body><img src=\"" + imageLink + "\"/></body></html>";
    return Response.status(418).entity(html).build();
  }
}
