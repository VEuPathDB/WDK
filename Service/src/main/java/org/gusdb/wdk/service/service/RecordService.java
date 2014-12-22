package org.gusdb.wdk.service.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

@Path("/record")
public class RecordService extends WdkService {

  @GET
  @Path("/dataset")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getInfo() {
    JSONObject json = new JSONObject();
    json.put("key", "value");
    return Response.ok(json).build();
  }

}
