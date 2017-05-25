package org.gusdb.wdk.service.service.user;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.service.WdkService;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/user")
public class UserCreationService extends WdkService {

  private static final Logger LOG = Logger.getLogger(UserCreationService.class);
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createNewUser(String body) throws RequestMisformatException {
    try {
      JSONObject requestJson = new JSONObject(body);
      
      LOG.info("Creating user with: " + requestJson.toString(2));
      
      return Response.ok(requestJson.toString()).build();
    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.getMessage(), e);
    }
  }
}
