package org.gusdb.wdk.service.service;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

@Path("/answer")
public class AnswerService extends WdkService {

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public Response submitQuestion() {
    // TODO: fill in
    JSONObject json = new JSONObject();
    json.put("key", "value");
    return Response.ok(json.toString()).build();
  }

  @GET
  @Path("{checksum}/result")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAnswer(@PathParam("checksum") String answerChecksum) {
    // TODO: fill in
    JSONObject json = new JSONObject();
    json.put("key", "value");
    return Response.ok(json.toString()).build();
  }
  
}
