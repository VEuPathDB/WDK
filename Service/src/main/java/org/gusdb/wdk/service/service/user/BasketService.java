package org.gusdb.wdk.service.service.user;

import java.util.Map;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.UserBundle;
import org.json.JSONObject;

public class BasketService extends UserService {

  public BasketService(@PathParam(USER_ID_PATH_PARAM) String userIdStr) {
    super(userIdStr);
  }
  
  @GET
  @Path("basket")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getBaskets() throws WdkModelException {
    UserBundle userBundle = getUserBundle(Access.PRIVATE);
    User user = userBundle.getTargetUser();
    if (user.isGuest()) {
      throw new ForbiddenException(PERMISSION_DENIED);
    }
    Map<RecordClass, Integer> counts = getWdkModel().getBasketFactory().getBasketCounts(user);
    JSONObject countsJson = new JSONObject();
    for (Map.Entry<RecordClass, Integer> entry: counts.entrySet()) {
      countsJson.put(entry.getKey().getFullName(), entry.getValue());
    }
    return Response.ok(countsJson.toString()).build();
  }

}
