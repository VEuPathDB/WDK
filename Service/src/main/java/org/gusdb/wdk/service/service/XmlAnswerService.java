package org.gusdb.wdk.service.service;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.xml.XmlAnswerValue;
import org.gusdb.wdk.model.xml.XmlQuestion;
import org.gusdb.wdk.service.formatter.XmlAnswerFormatter;

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
