package org.gusdb.wdk.service.service;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.ontology.Ontology;
import org.gusdb.wdk.service.formatter.OntologyFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/ontology")
@Produces(MediaType.APPLICATION_JSON)
public class OntologyService extends WdkService {

  /**
   * Get a list of all ontologies (names)
   */
  @GET
  public Response getOntologies() throws JSONException {
    return Response.ok(FormatUtil.stringCollectionToJsonArray(
        getWdkModel().getOntologyNames()).toString()).build();
  }

  /**
   * Get the information about a specific ontology.
   */
  @GET
  @Path("{ontologyName}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getOntology(@PathParam("ontologyName") String ontologyName) throws WdkModelException {
    Ontology ontology = getWdkModel().getOntology(ontologyName);
    if (ontology == null)
      return getNotFoundResponse(ontologyName);
    return Response.ok(OntologyFormatter.getOntologyJson(ontology).toString()).build();
  }

  @POST
  @Path("{ontologyName}/path")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPathsToMatchingNodes(@PathParam("ontologyName") String ontologyName, String body) throws WdkModelException {
    Ontology ontology = getWdkModel().getOntology(ontologyName);
    if (ontology == null)
      return getNotFoundResponse(ontologyName);
    try {
      JSONObject criteriaJson = new JSONObject(body);
      Map<String,String> criteria = new HashMap<String,String>();
      for (String key : JSONObject.getNames(criteriaJson)) {
        criteria.put(key, criteriaJson.getString(key));
      }
      JSONArray pathsList = OntologyFormatter.pathsToJson(ontology.getAllPaths(criteria));
      return Response.ok(pathsList.toString()).build();
    }
    catch (JSONException e) {
      return getBadRequestBodyResponse(e.toString());
    }
  }
}
