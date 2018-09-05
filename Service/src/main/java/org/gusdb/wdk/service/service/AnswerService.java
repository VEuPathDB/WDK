package org.gusdb.wdk.service.service;

import static org.gusdb.wdk.model.report.ReporterRef.WDK_SERVICE_JSON_REPORTER_RESERVED_NAME;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.BadRequestException;
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
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.cache.AnswerRequest;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.factory.AnswerValue;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.model.report.Reporter.ContentDisposition;
import org.gusdb.wdk.model.report.ReporterFactory;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.factory.AnswerValueFactory;
import org.gusdb.wdk.service.filter.RequestLoggingFilter;
import org.gusdb.wdk.service.formatter.AnswerFormatter;
import org.gusdb.wdk.service.request.answer.AnswerSpecServiceFormat;
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
 *     format: String,   (reporter internal name. optional.  if not provided, use WDK standard JSON)
 *     formatConfig: Any (sample for JSON, XML, etc. below)
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
   * @param data
   * @return
   * @throws WdkModelException
   * @throws DataValidationException
   */
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response buildResultFromForm(@FormParam("data") String data) throws WdkModelException, DataValidationException {
    // log this request's JSON here since filter will not log form data
    if (RequestLoggingFilter.isLogEnabled()) {
      RequestLoggingFilter.logRequest("POST", getUriInfo(),
          RequestLoggingFilter.formatJson(data));
    }
    return buildResult(data);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response buildResult(String body) throws WdkModelException, DataValidationException {
    try {
      AnswerRequest request = parseAnswerRequest(body, getWdkModel(), getSessionUser());
      return getAnswerResponse(getSessionUser(), request.getAnswerSpec(), request.getFormatting());
    }
    catch (JSONException | RequestMisformatException e) {
      LOG.info("Passed request body deemed unacceptable", e);
      throw new BadRequestException(e);
    }
  }

  public static AnswerRequest parseAnswerRequest(String requestBody, WdkModel wdkModel, User sessionUser)
      throws RequestMisformatException, DataValidationException {
    if (requestBody == null || requestBody.isEmpty()) {
      throw new RequestMisformatException("Request JSON cannot be empty. " +
          "If submitting a form, include the 'data' input parameter.");
    }

    // read request body into JSON object
    JSONObject requestJson = new JSONObject(requestBody);

    // parse answer spec (question, params, etc.) and formatting object
    JSONObject answerSpecJson = requestJson.getJSONObject("answerSpec");
    AnswerSpec answerSpec = AnswerSpecServiceFormat.parse(answerSpecJson, wdkModel, sessionUser, false);
    JSONObject formatting = JsonUtil.getJsonObjectOrDefault(requestJson, "formatting", null);
    return new AnswerRequest(answerSpec, formatting);
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
   * @throws DataValidationException if answerSpec or formatting are syntacticly valid but the data itself is invalid
   * @throws WdkModelException if an application error occurs
   */
  public static Response getAnswerResponse(User sessionUser, AnswerSpec answerSpec, JSONObject formatting)
      throws RequestMisformatException, WdkModelException, DataValidationException {

    // create base answer value from answer spec
    AnswerValue answerValue = new AnswerValueFactory(sessionUser).createFromAnswerSpec(answerSpec);
 
    // parse (optional) request details (columns, pagination, etc.- format dependent on reporter) and configure reporter
    Reporter reporter = getConfiguredReporter(answerValue, formatting);

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
    AnswerSpec answerSpec = AnswerSpecServiceFormat.parse(answerSpecJson, getWdkModel(), getSessionUser(), false);
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
  private static Reporter getConfiguredReporter(AnswerValue answerValue, JSONObject formatting)
      throws RequestMisformatException, WdkModelException, DataValidationException {

    if (formatting == null) {
      // create default service JSON reporter
      return AnswerFormatter.createDefault(answerValue);
    }

    // user passed formatting object; get contents and apply defaults if needed
    String format = JsonUtil.getStringOrDefault(formatting, "format", WDK_SERVICE_JSON_REPORTER_RESERVED_NAME);
    JSONObject formatConfig = JsonUtil.getJsonObjectOrDefault(formatting, "formatConfig", null);

    if (format.equals(WDK_SERVICE_JSON_REPORTER_RESERVED_NAME)) {
      return (formatConfig == null ?
          // create default service JSON reporter
          AnswerFormatter.createDefault(answerValue) :
          // use formatConfig to configure standard JSON reporter
          new AnswerFormatter(answerValue).configure(formatConfig));
    }

    // check to make sure format name is valid for this recordclass
    if (!answerValue.getQuestion().getReporterMap().keySet().contains(format)) {
      throw new DataValidationException("Request for an invalid answer format: " + format);
    }

    // configure reporter requested
    try {
      LOG.debug("Creating reporter '" + format + "'");
      return ReporterFactory.getReporter(answerValue, format, formatConfig);
    }
    catch (WdkUserException userException) {
      throw new RequestMisformatException("Could not configure reporter '" + format + "' with passed formatConfig");
    }
  }

  private static StreamingOutput getAnswerAsStream(final Reporter reporter) {
    return new StreamingOutput() {
      @Override
      public void write(OutputStream stream) throws IOException {
        try {
          reporter.report(stream);
        }
        catch (WdkModelException e) {
          throw new WebApplicationException(e);
        }
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
