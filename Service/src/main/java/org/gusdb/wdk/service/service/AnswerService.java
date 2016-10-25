package org.gusdb.wdk.service.service;

import static org.gusdb.wdk.model.report.ReporterRef.WDK_SERVICE_JSON_REPORTER_RESERVED_NAME;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.model.report.Reporter.ContentDisposition;
import org.gusdb.wdk.model.report.ReporterFactory;
import org.gusdb.wdk.service.factory.AnswerValueFactory;
import org.gusdb.wdk.service.filter.RequestLoggingFilter;
import org.gusdb.wdk.service.formatter.AnswerFormatter;
import org.gusdb.wdk.service.request.answer.AnswerSpec;
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
public class AnswerService extends WdkService {

  private static final Logger LOG = Logger.getLogger(AnswerService.class);

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response buildResultFromForm(@FormParam("data") String data) throws WdkModelException, DataValidationException {
    // log this request's JSON here since filter will not log form data
    if (RequestLoggingFilter.isLogEnabled()) {
      RequestLoggingFilter.logRequest("POST", getUriInfo(),
          RequestLoggingFilter.toJsonBodyString(data));
    }
    return buildResult(data);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response buildResult(String body) throws WdkModelException, DataValidationException {
    try {
      if (body == null || body.isEmpty()) {
        throw new RequestMisformatException("Request JSON cannot be empty. " +
            "If submitting a form, include the 'data' input parameter.");
      }

      // read request body into JSON object
      JSONObject requestJson = new JSONObject(body);

      // parse answer spec (question, params, etc.) and create base answer value
      JSONObject answerSpecJson = requestJson.getJSONObject("answerSpec");
      AnswerSpec answerSpec = AnswerSpecFactory.createFromJson(answerSpecJson, getWdkModelBean(), getSessionUser());
      AnswerValue answerValue = new AnswerValueFactory(getSessionUser()).createFromAnswerSpec(answerSpec);

      // parse (optional) request details (columns, pagination, etc.- format dependent on reporter) and configure reporter
      Reporter reporter = getConfiguredReporter(answerValue,
          JsonUtil.getJsonObjectOrDefault(requestJson, "formatting", null));

      // build response from stream, apply delivery details, and return
      ResponseBuilder builder = Response.ok(getAnswerAsStream(reporter)).type(reporter.getHttpContentType());
      return applyDisposition(builder, reporter.getContentDisposition(), reporter.getDownloadFileName()).build();

    }
    catch (JSONException | RequestMisformatException e) {
      LOG.info("Passed request body deemed unacceptable", e);
      throw new BadRequestException(e);
    }
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
    if (!answerValue.getQuestion().getRecordClass().getReporterMap().keySet().contains(format)) {
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
