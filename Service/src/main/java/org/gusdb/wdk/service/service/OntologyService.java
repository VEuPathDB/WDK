package org.gusdb.wdk.service.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.ontology.Ontology;
import org.gusdb.wdk.service.formatter.OntologyFormatter;
import org.json.JSONException;

@Path("/ontology")
@Produces(MediaType.APPLICATION_JSON)public class OntologyService extends WdkService {
  /**
   * Get a list of all ontologies (names)
   */
  @GET
  public Response getOntologies()
          throws JSONException {
    try {
      return Response.ok(OntologyFormatter.getOntologiesJson(getWdkModel().getOntologies()).toString()).build();
    }
    catch (IllegalArgumentException e) {
      return getBadRequestBodyResponse(e.getMessage());
    }
  }

  /**
   * Get the information about a specific ontology.  
   */
  @GET
  @Path("/{ontologyName}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getQuestion(
      @PathParam("ontologyName") String ontologyName)
          throws WdkUserException, WdkModelException {
    
    Ontology ontology = getWdkModel().getOntology(ontologyName);
    
    return Response.ok(OntologyFormatter.getOntologyJson(ontology).toString()).build();
  }

}
