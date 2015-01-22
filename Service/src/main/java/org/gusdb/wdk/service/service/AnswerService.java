package org.gusdb.wdk.service.service;

import java.util.Collections;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

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
JSON input format:
{
  “questionDefinition”: {
    “questionName”: String,
    “params”: [ {
      “name”: String, “value”: Any
    } ],
    “filters”: [ {
      “name”: String, value: Any
    } ]
  },
  displayInfo: {
    pagination: { offset: Number, numRecords: Number },
    attributes: [ attributeName: String ],
    tables: [ tableName: String ],
    sorting: [ { attributeName: String, direction: Enum[ASC,DESC] } ]
  }
}
*/
@Path("/answer")
public class AnswerService extends WdkService {

  private static final Logger LOG = Logger.getLogger(AnswerService.class);

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response buildResult(String body) throws WdkModelException {
    try {
      LOG.info("POST submission to /answer with body:\n" + body);
      JSONObject json = new JSONObject(body);

      // expect two parts to this request
      // 1. Parse result request (question, params, etc.)
      JSONObject questionDefJson = json.getJSONObject("questionDefinition");
      WdkAnswerRequest request = WdkAnswerRequest.createFromJson(getCurrentUser(), questionDefJson, getWdkModelBean());
      
      // 2. Parse request specifics (columns, pagination, etc.)
      JSONObject specJson = json.getJSONObject("displayInfo");
      WdkAnswerRequestSpecifics requestSpecifics = WdkAnswerRequestSpecifics.createFromJson(specJson, getWdkModelBean());

      // seemed to parse ok; create answer and format
      AnswerValueBean answerValue = getResultFactory().createResult(request, requestSpecifics);
      return Response.ok(AnswerStreamer.getAnswerAsStream(answerValue)).build();
    }
    catch (JSONException | RequestMisformatException e) {
      LOG.info("Passed request body deemed unacceptable", e);
      return Response.notAcceptable(Collections.<Variant>emptyList()).build();
    }
  }
}
