package org.gusdb.wdk.service.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

@Path("/record")
public class RecordsService {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getInfo() {
    JSONObject json = new JSONObject();
    json.put("key", "value");
    return json.toString();
  }

}
