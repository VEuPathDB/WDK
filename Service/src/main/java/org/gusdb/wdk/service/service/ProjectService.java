package org.gusdb.wdk.service.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.wdk.model.WdkModel;
import org.json.JSONObject;

@Path("/project")
public class ProjectService extends WdkService {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getProjectInfo() {
    return Response.ok(getProjectJson(getWdkModel()).toString()).build();
  }

  private static JSONObject getProjectJson(WdkModel wdkModel) {
    JSONObject json = new JSONObject();
    json.put("projectId", wdkModel.getProjectId());
    json.put("version", wdkModel.getVersion());
    json.put("buildNumber", wdkModel.getBuildNumber());
    json.put("releaseDate", wdkModel.getReleaseDate());
    json.put("introduction", wdkModel.getIntroduction());
    json.put("webAppUrl", wdkModel.getModelConfig().getWebAppUrl());
    return json;
  }
}
