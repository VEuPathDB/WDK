package org.gusdb.wdk.service.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.RecordClassBean;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.service.request.RequestMisformatException;
import org.gusdb.wdk.service.request.WdkAnswerRequest;
import org.gusdb.wdk.service.request.WdkAnswerRequestSpecifics;
import org.gusdb.wdk.service.stream.AnswerStreamer;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>JSON input format:</p>
 * <pre>
 * {
 *   “questionDefinition”: {
 *     “questionName”: String,
 *     “params”: [ {
 *       “name”: String, “value”: Any
 *     } ],
 *     (optional) "legacyFilterName": String,
 *     (optional) “filters”: [ {
 *       “name”: String, value: Any
 *     } ],
 *     (optional) “viewFilters”: [ {
 *       “name”: String, value: Any
 *     } ]
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
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response buildResult(String body) throws WdkModelException, WdkUserException {
    try {
      LOG.debug("POST submission to /answer with body:\n" + body);
      JSONObject json = new JSONObject(body);
      
      // expect two parts to this request
      // 1. Parse result request (question, params, etc.)
      JSONObject questionDefJson = json.getJSONObject("questionDefinition");
      WdkAnswerRequest request = WdkAnswerRequest.createFromJson(questionDefJson, getWdkModelBean());
      
      // 2. Parse (optional) request specifics (columns, pagination, etc.)
      JSONObject displayInfo = null;
      String format = null;
      JSONObject formatConfig = null;
      
      WdkAnswerRequestSpecifics requestSpecifics = null;

      if (json.has("displayInfo")) {
        // only try to parse if present; if not present then pass null to createResult
        displayInfo = json.getJSONObject("displayInfo");
	if (displayInfo.has("formatConfig")) {
	    formatConfig = displayInfo.getJSONObject("formatConfig");
	    requestSpecifics = WdkAnswerRequestSpecifics.createFromJson(							formatConfig, request.getQuestion().getRecordClass());
	}
        format = displayInfo.has("format")? displayInfo.getString("format") : null; // the internal format name
      }
      
      // make an answer value. 
      AnswerValueBean answerValue = getResultFactory().createResult(request, requestSpecifics);
     
      // if no format specified, standard answer request
      if (format == null) {
        return Response.ok(AnswerStreamer.getAnswerAsStream(answerValue)).build();       
      } 
      
      // formatted answer request (eg, for download).  This option ignores the requestSpecifics, instead building its own configuration
      else {
        if (formatConfig == null)
          throw new WdkUserException(
              "Requested answer format '" + format + "' requires a non-null formatConfig");
        Reporter reporter = getReporter(answerValue, format, formatConfig);
        ResponseBuilder builder = Response.ok(AnswerStreamer.getAnswerAsStream(reporter));
        builder.type(reporter.getHttpContentType());
        String fileName = reporter.getDownloadFileName();
        if (fileName == null)
          builder.header("Pragma", "Public");
        else
          builder.header("Content-disposition", "attachment; filename=" + reporter.getDownloadFileName());

        return builder.build();
      }
    }
    catch (JSONException | RequestMisformatException e) {
      LOG.info("Passed request body deemed unacceptable", e);
      return getBadRequestBodyResponse(e.getMessage());
    }
  }
  
  private Reporter getReporter(AnswerValueBean answerValue, String format, JSONObject formatConfig) throws WdkUserException, WdkModelException {
    RecordClassBean recordClass = answerValue.getQuestion().getRecordClass();
    if (!recordClass.getReporterMap().keySet().contains(format)) {
      throw new WdkUserException("Request for an invalid WDK answer format: " + format);
    }
    
    Reporter reporter = answerValue.createReport(format, formatConfig);
    return reporter;
  }
}
