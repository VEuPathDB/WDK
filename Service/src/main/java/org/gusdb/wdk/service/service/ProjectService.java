package org.gusdb.wdk.service.service;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.json.JSONObject;

@Path("/project")
public class ProjectService {

  private WdkModel _wdkModel;
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getProjectInfo() {
    JSONObject json = new JSONObject();
    json.put("projectId", _wdkModel.getProjectId());
    json.put("version", _wdkModel.getVersion());
    json.put("buildNumber", _wdkModel.getBuildNumber());
    json.put("releaseDate", _wdkModel.getReleaseDate());
    json.put("introduction", _wdkModel.getIntroduction());
    json.put("webAppUrl", _wdkModel.getModelConfig().getWebAppUrl());
    json.put("assetsUrl", _wdkModel.getModelConfig().getAssetsUrl());
    return Response.ok(json.toString()).build();
  }

  @Context
  public void setServletContext(ServletContext context) {
    _wdkModel = ((WdkModelBean)context.getAttribute("wdkModel")).getModel();
  }
}
