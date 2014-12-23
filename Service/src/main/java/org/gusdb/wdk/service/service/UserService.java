package org.gusdb.wdk.service.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.User;
import org.json.JSONObject;

@Path("/user")
public class UserService extends WdkService {

  @GET
  @Path("current")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getCurrent() {
    return createJsonResponse(getCurrentUser(), true);
  }

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getById(@PathParam("id") int userId) throws WdkModelException {
    boolean isOwner = (userId == getCurrentUserId());
    return createJsonResponse(getWdkModel().getUserFactory().getUser(userId), isOwner);
  }

  private static Response createJsonResponse(User user, boolean isOwner) {
    JSONObject json = new JSONObject();
    json.put("id", user.getUserId());
    json.put("firstName", user.getFirstName());
    json.put("middleName", user.getMiddleName());
    json.put("lastName", user.getLastName());
    json.put("organization", user.getOrganization());
    // private fields viewable only by owner
    if (isOwner) {
      json.put("email", user.getEmail());
    }
    return Response.ok(json.toString()).build();
  }
}
