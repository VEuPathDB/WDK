package org.gusdb.wdk.service.service;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.wdk.cache.CacheMgr;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.request.AnswerRequest;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONObject;

@Path("/temporary-results")
public class TemporaryResultService extends AbstractWdkService {

  private static final long EXPIRATION_MILLIS = 60 * 60 * 1000; // one hour

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response setTemporaryResult(JSONObject requestJson)
      throws RequestMisformatException, DataValidationException, WdkModelException {
    String questionName = requestJson.getString(JsonKeys.SEARCH_NAME);
    Question question = getWdkModel().getQuestionByName(questionName)
        .orElseThrow(() -> new DataValidationException(questionName + " is not a valid search name."));
    String reporterName = requestJson.getString(JsonKeys.REPORT_NAME);
    AnswerRequest request = AnswerService.parseAnswerRequest(
        question, reporterName, requestJson, getWdkModel(), getSessionUser());
    String id = UUID.randomUUID().toString();
    CacheMgr.get().getAnswerRequestCache().put(id, request);
    return Response.ok(new JSONObject().put(JsonKeys.ID, id).toString())
        .location(getUriInfo().getAbsolutePathBuilder().build(id)).build();
  }

  @GET
  @Path("/{id}")
  public Response getTemporaryResult(@PathParam("id") String id)
      throws RequestMisformatException, WdkModelException, DataValidationException {
    Map<String,AnswerRequest> savedRequests = CacheMgr.get().getAnswerRequestCache();
    AnswerRequest savedRequest = savedRequests.get(id);
    if (savedRequest == null || savedRequest.getCreationDate().getTime() < new Date().getTime() - EXPIRATION_MILLIS) {
      // return Not Found, but expire id first if not gone already
      if (savedRequest != null) {
        savedRequests.remove(id);
      }
      throw new NotFoundException(formatNotFound("temporary result with ID '" + id + "'"));
    }
    return AnswerService.getAnswerResponse(getSessionUser(), savedRequest).getSecond();
  }
}
