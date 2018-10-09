package org.gusdb.wdk.service.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
