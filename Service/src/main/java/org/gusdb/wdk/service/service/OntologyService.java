package org.gusdb.wdk.service.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.service.formatter.OntologyFormatter;
import org.json.JSONException;

@Path("/ontology")
@Produces(MediaType.APPLICATION_JSON)public class OntologyService extends WdkService {
  /**
   * Get a list of all ontologies (names)
   */
  @GET
  public Response getOntologies()
          throws JSONException, WdkModelException, WdkUserException {
    try {
      return Response.ok(OntologyFormatter.getOntologiesJson(getWdkModel().getOntologies()).toString()).build();
    }
    catch (IllegalArgumentException e) {
      return getBadRequestBodyResponse(e.getMessage());
    }
  }


}
