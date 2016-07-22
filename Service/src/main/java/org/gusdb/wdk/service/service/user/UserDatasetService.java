package org.gusdb.wdk.service.service.user;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetStore;
import org.gusdb.wdk.service.formatter.UserDatasetFormatter;

public class UserDatasetService extends UserService {
  
  public UserDatasetService(@PathParam(USER_ID_PATH_PARAM) String uid) {
    super(uid);
  }

  @GET
  @Path("userDataset")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUserDatasets(@QueryParam("expand") Boolean expand) throws WdkModelException {
    UserDatasetStore userDatasetStore = getWdkModel().getUserDatasetStore();
    if (userDatasetStore == null) throw new WdkModelException("There is no userDatasetStore installed in the WDK Model.");
    Map<Integer, UserDataset> userDatasets = userDatasetStore.getUserDatasets(getSessionUserId());
    return Response.ok(UserDatasetFormatter.getUserDatasetsJson(userDatasets, userDatasetStore, expand).toString()).build();
  }

}
