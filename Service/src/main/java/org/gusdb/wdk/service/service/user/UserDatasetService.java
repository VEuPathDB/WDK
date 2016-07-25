package org.gusdb.wdk.service.service.user;

import java.util.Map;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetStore;
import org.gusdb.wdk.service.UserBundle;
import org.gusdb.wdk.service.factory.WdkStepFactory;
import org.gusdb.wdk.service.formatter.StepFormatter;
import org.gusdb.wdk.service.formatter.UserDatasetFormatter;
import org.gusdb.wdk.service.request.DataValidationException;
import org.gusdb.wdk.service.request.RequestMisformatException;
import org.gusdb.wdk.service.request.answer.AnswerRequest;
import org.gusdb.wdk.service.request.answer.AnswerRequestFactory;
import org.json.JSONException;
import org.json.JSONObject;

public class UserDatasetService extends UserService {
  
  public UserDatasetService(@PathParam(USER_ID_PATH_PARAM) String uid) {
    super(uid);
  }

  @GET
  @Path("userDataset/{userId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUserDatasets(@PathParam("userId") String stepId, @QueryParam("expand") Boolean expand) throws WdkModelException {
    UserDatasetStore userDatasetStore = getWdkModel().getUserDatasetStore();
    if (userDatasetStore == null) throw new WdkModelException("There is no userDatasetStore installed in the WDK Model.");
    UserBundle userBundle = getTargetUserBundle(Access.PUBLIC);
    Map<Integer, UserDataset> userDatasets = userDatasetStore.getUserDatasets(userBundle.getTargetUser().getUserId());
    return Response.ok(UserDatasetFormatter.getUserDatasetsJson(userDatasets, userDatasetStore, expand).toString()).build();
  }
  
  @POST
  @Path("user/{userId}/meta")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateMetaInfo(String body) throws WdkModelException, DataValidationException {
    try {
      JSONObject json = new JSONObject(body);
      AnswerRequest request = AnswerRequestFactory.createFromJson(json, getWdkModelBean(), getSessionUser());
      Step step = WdkStepFactory.createStep(request, getSessionUser(), getWdkModel().getStepFactory());
      return Response.ok(StepFormatter.getStepJson(step).toString()).build();
    }
    catch (JSONException | RequestMisformatException e) {
      throw new BadRequestException(e);
    }
  }


}
