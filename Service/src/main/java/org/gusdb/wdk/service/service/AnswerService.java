package org.gusdb.wdk.service.service;

import static org.gusdb.fgputil.FormatUtil.NL;
import static org.gusdb.wdk.model.answer.request.AnswerFormattingParser.DEFAULT_REPORTER_PARSER;
import static org.gusdb.wdk.model.answer.request.AnswerFormattingParser.SPECIFIED_REPORTER_PARSER;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.request.AnswerFormatting;
import org.gusdb.wdk.model.answer.request.AnswerFormattingParser;
import org.gusdb.wdk.model.answer.request.AnswerRequest;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.model.report.Reporter.ContentDisposition;
import org.gusdb.wdk.model.report.ReporterConfigException;
import org.gusdb.wdk.model.report.util.ReporterFactory;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.factory.AnswerValueFactory;
import org.gusdb.wdk.service.filter.RequestLoggingFilter;
import org.gusdb.wdk.service.request.answer.AnswerSpecFactory;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>JSON input format:</p>
 * <pre>
 * {
 *   "answerSpec": {
 *       see AnswerRequestFactory for details
 *   },
 *   formatting: {
 *     format: String, (reporter internal name, required)
 *     formatConfig: Object (sample for JSON, XML, etc. below)
 *   }
 * }
 * </pre>
 * <p>Sample input for our standard reporters:</p>
 * <pre>
 * formatConfig: {
 *   pagination: { offset: Number, numRecords: Number },   [only used by WDK standard JSON]
 *   attributes: [ attributeName: String ],
 *   tables: [ tableName: String ],
 *   sorting: [ { attributeName: String, direction: Enum[ASC,DESC] } ]  [so far, only used by WDK standard JSON]
 *   attachmentType: String    [eg "excel".  optional. if not provided, return in browser (default disposition of inline), using the default content type of the reporter. if provided, disposition is attachment, of this type, and file extension reflects this type.]
 *   includeEmptyTables: true/false
 * }
 * </pre>
 */
@Path("/answer")
public class AnswerService extends AbstractWdkService {

  private static final Logger LOG = Logger.getLogger(AnswerService.class);

  /**
   * This endpoint that takes a FORM input is used by the client to push the provided data
   * to a new http target (ie, a tab), for example, a download report
   * 
   * @param data JSON data representing an answer request, passed in the 'data' form param
   * @return generated report
   * @throws RequestMisformatException if request body is not JSON or has incorrect JSON structure
   * @throws DataValidationException if JSON structure is correct but values contained are invalid
   * @throws WdkModelException if an error occurs while processing the request
   */
  @POST
  @Path("/report")
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response buildResultFromForm(@FormParam("data") String data) throws WdkModelException, DataValidationException, RequestMisformatException {
    // log this request's JSON here since filter will not log form data
    if (RequestLoggingFilter.isLogEnabled()) {
      RequestLoggingFilter.logRequest("POST", getUriInfo(),
          RequestLoggingFilter.formatJson(data));
    }
    return buildResult(data);
  }

  /**
   * Processes an answer request (answer spec + formatting information) by creating an answer from the
   * answer spec, then calling the specified reporter, passing a configuration, and streaming back the
   * reporter's result.
   * 
   * @param body request body containing answer spec, format string, format configuration
   * @return generated report
   * @throws RequestMisformatException if request body is not JSON or has incorrect JSON structure
   * @throws DataValidationException if JSON structure is correct but values contained are invalid
   * @throws WdkModelException if an error occurs while processing the request
   */
  @POST
  @Path("/report")
  @Consumes(MediaType.APPLICATION_JSON)
  // Produces an unknown media type; varies depending on reporter selected
  public Response buildResult(String body) throws WdkModelException, DataValidationException, RequestMisformatException {
    AnswerRequest request = parseAnswerRequest(body, getWdkModel(), getSessionUser(), SPECIFIED_REPORTER_PARSER);
    return getAnswerResponse(getSessionUser(), request);
  }

  /**
   * Special case answer provider that does not require a format.  The default reporter is used.  This
   * exists so we can provide a concrete JSON schema for the response, since the /answer/report endpoint
   * may not even return JSON, depending on which reporter is specified.
   * 
   * @param body JSON request body
   * @return standard WDK answer JSON
   * @throws RequestMisformatException if request body is not JSON or has incorrect JSON structure
   * @throws DataValidationException if JSON structure is correct but values contained are invalid
   * @throws WdkModelException if an error occurs while processing the request
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response buildDefaultReporterResult(String body) throws RequestMisformatException, WdkModelException, DataValidationException {
    AnswerRequest request = parseAnswerRequest(body, getWdkModel(), getSessionUser(), DEFAULT_REPORTER_PARSER);
    return getAnswerResponse(getSessionUser(), request);
  }

  public static AnswerRequest parseAnswerRequest(String requestBody, WdkModel wdkModel,
      User sessionUser, AnswerFormattingParser formatParser)
      throws RequestMisformatException, DataValidationException {
    if (requestBody == null || requestBody.isEmpty()) {
      throw new RequestMisformatException("Request JSON cannot be empty. " +
          "If submitting a form, include the 'data' input parameter.");
    }
    try {
      // read request body into JSON object
      JSONObject requestJson = new JSONObject(requestBody);

      // parse answer spec (question, params, etc.) and formatting object
      JSONObject answerSpecJson = requestJson.getJSONObject("answerSpec");
      AnswerSpec answerSpec = AnswerSpecFactory.createFromJson(answerSpecJson, wdkModel, sessionUser, false);
      AnswerFormatting formatting = Functions.mapException(
          () -> formatParser.apply(requestJson),
          e -> new RequestMisformatException(e.getMessage()));
      return new AnswerRequest(answerSpec, formatting);
    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.getMessage());
    }
  }

  /**
   * Creates a streaming answer response as the passed user from the passed answer spec and formatting
   * configuration.  To get the default (i.e. standard WDK service JSON) reporter with default configuration,
   * pass null as formatting.
   *
   * @param sessionUser user answer is to be generated as
   * @param answerSpec answer spec determining result ID set
   * @param formatting reporter configuration or null for default reporter/config
   * @return streaming response representing the formatted answer
   * @throws RequestMisformatException if reporter does not support the passed formatConfig object
   * @throws DataValidationException if answerSpec or formatting are syntactically valid but the data itself is invalid
   * @throws WdkModelException if an application error occurs
   */
  public static Response getAnswerResponse(User sessionUser, AnswerRequest request)
      throws RequestMisformatException, WdkModelException, DataValidationException {

    // create base answer value from answer spec
    AnswerValue answerValue = new AnswerValueFactory(sessionUser).createFromAnswerSpec(request.getAnswerSpec());

    // parse (optional) request details (columns, pagination, etc.- format dependent on reporter) and configure reporter
    Reporter reporter = getConfiguredReporter(answerValue, request.getFormatting());

    // build response from stream, apply delivery details, and return
    ResponseBuilder builder = Response.ok(getAnswerAsStream(reporter)).type(reporter.getHttpContentType());
    return applyDisposition(builder, reporter.getContentDisposition(), reporter.getDownloadFileName()).build();
  }

  @POST
  @Path("filter-summary/{filterName}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response displayFilterResults(@PathParam("filterName") String filterName, String body) throws WdkModelException, WdkUserException, DataValidationException {
    JSONObject requestJson = new JSONObject(body);
    JSONObject answerSpecJson = requestJson.getJSONObject("answerSpec");
    AnswerSpec answerSpec = AnswerSpecFactory.createFromJson(answerSpecJson, getWdkModel(), getSessionUser(), false);
    AnswerValue answerValue = new AnswerValueFactory(getSessionUser()).createFromAnswerSpec(answerSpec);
    JSONObject filterSummaryJson = answerValue.getFilterSummaryJson(filterName);
    return Response.ok(filterSummaryJson.toString()).build();
  }

  /**
   * Returns configured reporter based on passed answer value and formatting JSON
   *
   * @param answerValue answer value for which reporter should be constructed
   * @param formatting formatting object if one was passed, else null
   * @return configured reporter
   * @throws RequestMisformatException if required property is not present or the wrong type
   * @throws DataValidationException if a value passed in the configuration is invalid
   * @throws WdkModelException if unable to create reporter due to another reason
   */
  private static Reporter getConfiguredReporter(AnswerValue answerValue, AnswerFormatting formatting)
      throws RequestMisformatException, WdkModelException, DataValidationException {
    String format = formatting.getFormat();
    try {

      // check to make sure format name is valid for this recordclass
      if (!answerValue.getQuestion().getReporterMap().containsKey(format)) {
        throw new DataValidationException("Request for an invalid answer format: " + format);
      }

      // configure reporter requested
      LOG.debug("Creating and configuring reporter for format '" + format + "'");
      return ReporterFactory.getReporter(answerValue, format, formatting.getFormatConfig());
    }
    catch (JSONException e) {
      throw new RequestMisformatException("Invalid JSON structure: " + e.getMessage());
    }
    catch (ReporterConfigException e) {
      throw new RequestMisformatException("Could not configure reporter '" + format + "' with passed formatConfig. " + e.getMessage());
    }
  }

  private static StreamingOutput getAnswerAsStream(final Reporter reporter) {
    return stream -> {
      try {
        reporter.report(stream);
      }
      catch (WdkModelException | WdkRuntimeException e) {
        stream.write((" ********************************************* " + NL + 
            " ********************************************* " + NL + 
            " *************** ERROR **************** " + NL + 
            "We're sorry, but an error occurred while streaming your result and your request cannot be completed.  " + NL + 
            "Please contact us with a description of your download." + NL + NL).getBytes());
        throw new WebApplicationException(e);
      }
    };
  }

  private static ResponseBuilder applyDisposition(ResponseBuilder response,
      ContentDisposition disposition, String filename) throws WdkModelException {
    switch(disposition) {
      case INLINE:
        response.header("Pragma", "Public");
        break;
      case ATTACHMENT:
        response.header("Content-disposition", "attachment; filename=" + filename);
        break;
      default:
        throw new WdkModelException("Unsupported content disposition: " + disposition);
    }
    return response;
  }
}
