package org.gusdb.wdk.service.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.service.formatter.UserFormatter;

@Path("/user")
public class UserService extends WdkService {

  @GET
  @Path("current")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getCurrent() throws WdkModelException {
    return getById(getCurrentUserId());
  }

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getById(@PathParam("id") int userId) throws WdkModelException {
    boolean isOwner = (userId == getCurrentUserId());
    return Response.ok(
        UserFormatter.getUserJson(getWdkModel().getUserFactory().getUser(userId), isOwner).toString()
    ).build();
  }
}
