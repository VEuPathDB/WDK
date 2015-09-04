package org.gusdb.wdk.service.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
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
 *   displayInfo: {
 *     reporter: String,
 *     reporterConfig: Any (sample for JSON, XML, etc. below)
 *   }
 * }
 * </pre>
 * <p>Sample input for our standard reporters:</p>
 * <pre>
 * reporterConfig: {
 *   pagination: { offset: Number, numRecords: Number },
 *   attributes: [ attributeName: String ],
 *   tables: [ tableName: String ],
 *   sorting: [ { attributeName: String, direction: Enum[ASC,DESC] } ]
 * }
 * </pre>
 */
@Path("/answer")
public class AnswerService extends WdkService {

  private static final Logger LOG = Logger.getLogger(AnswerService.class);

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response buildResult(String body) throws WdkModelException {
    try {
      LOG.debug("POST submission to /answer with body:\n" + body);
      JSONObject json = new JSONObject(body);

      // expect two parts to this request
      // 1. Parse result request (question, params, etc.)
      JSONObject questionDefJson = json.getJSONObject("questionDefinition");
      WdkAnswerRequest request = WdkAnswerRequest.createFromJson(questionDefJson, getWdkModelBean());
      
      // 2. Parse (optional) request specifics (columns, pagination, etc.)
      WdkAnswerRequestSpecifics requestSpecifics = null;
      if (json.has("displayInfo")) {
        // only try to parse if present; if not present then pass null to createResult
        JSONObject specJson = json.getJSONObject("displayInfo");
        requestSpecifics = WdkAnswerRequestSpecifics.createFromJson(
            specJson, request.getQuestion().getRecordClass());
      }
      
      // seemed to parse ok; create answer and format
      AnswerValueBean answerValue = getResultFactory().createResult(request, requestSpecifics);
      return Response.ok(AnswerStreamer.getAnswerAsStream(answerValue)).build();
    }
    catch (JSONException | RequestMisformatException e) {
      LOG.info("Passed request body deemed unacceptable", e);
      return getBadRequestBodyResponse(e.getMessage());
    }
  }
}
