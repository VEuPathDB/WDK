package org.gusdb.wdk.service.service;

import static org.gusdb.wdk.core.api.JsonKeys.ID;
import static org.gusdb.wdk.core.api.JsonKeys.REPORT_CONFIG;
import static org.gusdb.wdk.core.api.JsonKeys.REPORT_NAME;
import static org.gusdb.wdk.core.api.JsonKeys.SEARCH_CONFIG;
import static org.gusdb.wdk.core.api.JsonKeys.SEARCH_NAME;
import static org.gusdb.wdk.core.api.JsonKeys.STEP_ID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidationException;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.request.AnswerFormatting;
import org.gusdb.wdk.model.answer.request.AnswerRequest;
import org.gusdb.wdk.model.answer.request.TemporaryResultFactory;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONObject;

/**
 * This service provides the ability to store a combination of the specification
 * for a search + a specification for a report (i.e. everything WDK needs to
 * produce a reporter output data stream), to be accessed later via a produced ID.
 *
 * There are two endpoints:
 *   - POST /temporary-results validates and stores the spec, returning an ID
 *   - GET  /temporary-results/{id} looks up the spec, executes the search, creates
 *             the specified report, and streams the result
 *
 * @author rdoherty
 */
@Path("/temporary-results")
public class TemporaryResultService extends AbstractWdkService {

  /**
   * Creates a temporary result specification and stores in the answer request cache.
   * The submitted JSON must contain either a step ID or a search name+config, i.e.
   * {
   *   stepId: integer,      // valid step ID for this user
   *   reportName: string,   // name of report
   *   reportConfig: object  // configuration of this report
   * }
   *   - OR -
   * {
   *   searchName: string,   // question name = url segment
   *   searchConfig: object, // standard answer spec object
   *   reportName: string,   // name of report
   *   reportConfig: object  // configuration of this report
   * }
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response setTemporaryResult(JSONObject requestJson)
      throws RequestMisformatException, DataValidationException, WdkModelException, ValidationException {
    AnswerRequest request = parseRequest(requestJson);
    String id = TemporaryResultFactory.insertTemporaryResult(getRequestingUser(), request);
    return Response.ok(new JSONObject().put(ID, id).toString())
        .location(getUriInfo().getAbsolutePathBuilder().build(id)).build();
  }

  @GET
  @Path("/{id}")
  public Response getTemporaryResult(@PathParam("id") String id)
      throws WdkModelException, DataValidationException {
    try {
      TwoTuple<User,AnswerRequest> temporaryResultSpec = TemporaryResultFactory.retrieveTemporaryResult(id, getWdkModel());

      // request is valid; produce result as originally requested
      return AnswerService.getAnswerResponse(
          temporaryResultSpec.getFirst(),
          temporaryResultSpec.getSecond(),
          getErrorContext()
      ).getSecond();
    }
    catch (WdkUserException e) {
      throw new NotFoundException(formatNotFound(e.getMessage()));
    }
  }

  private AnswerRequest parseRequest(JSONObject requestJson)
      throws DataValidationException, RequestMisformatException, WdkModelException, ValidationException {

    // get the reporter name
    String reporterName = requestJson.getString(REPORT_NAME);

    // Option 1: user specified a step ID
    if (requestJson.has(STEP_ID)) {

      // load the step, validate against user, and ensure runnability
      long stepId = requestJson.getLong(STEP_ID);
      Step step = new StepFactory(getRequestingUser())
          .getStepById(stepId, ValidationLevel.RUNNABLE)
          .orElseThrow(() -> new DataValidationException("No step found with ID " + stepId));
      if (step.getOwningUser().getUserId() != getRequestingUser().getUserId()) {
        throw new DataValidationException("No step found with ID " + stepId + " for this user.");
      }
      RunnableObj<AnswerSpec> answerSpec = Step.getRunnableAnswerSpec(step.getRunnable()
          .getOrThrow(invalidStep -> new ValidationException(invalidStep.getValidationBundle())));

      // create answer format
      JSONObject reportConfig = requestJson.getJSONObject(REPORT_CONFIG);
      AnswerFormatting format = new AnswerFormatting(reporterName, reportConfig);

      // return request
      return new AnswerRequest(answerSpec, format, false);
    }

    // Option 2: user specified search name and config (i.e. answer spec
    else if (requestJson.has(SEARCH_NAME) && requestJson.has(SEARCH_CONFIG)){

      // validate question
      String questionName = requestJson.getString(SEARCH_NAME);
      Question question = getWdkModel().getQuestionByName(questionName).orElseThrow(
          () -> new DataValidationException(questionName + " is not a valid search name."));

      // parse answer request as we would in answer service
      return AnswerService.parseAnswerRequest(
          question, reporterName, requestJson, getWdkModel(), getRequestingUser(), false);
    }
    else {
      throw new RequestMisformatException("Either " + STEP_ID + " or (" +
          SEARCH_NAME + " and " + SEARCH_CONFIG + ") must be submitted.");
    }
  }
}
