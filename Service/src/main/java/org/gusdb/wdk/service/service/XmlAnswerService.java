package org.gusdb.wdk.service.service;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.xml.*;
import org.gusdb.wdk.service.formatter.JsonKeys;
import org.gusdb.wdk.service.formatter.XmlAnswerFormatter;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.stream.Collectors;

@Path("/xml-answer")
public class XmlAnswerService extends AbstractWdkService {

  @GET
  @Path("/{xmlQuestionName}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getXmlAnswer(@PathParam("xmlQuestionName") String xmlQuestionName ) throws WdkModelException {
    XmlQuestion xmlQuestion = getWdkModel().getXmlQuestionByFullName(xmlQuestionName);
    XmlAnswerValue xmlAnswer = xmlQuestion.getFullAnswer();
    return Response.ok(XmlAnswerFormatter.formatXmlAsnwerValue(xmlAnswer)).build();
  }

}
