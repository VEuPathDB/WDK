package org.gusdb.wdk.service.service;

import static org.gusdb.fgputil.StringUtil.rtrim;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.fgputil.Timer;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetSession;
import org.gusdb.wdk.model.user.dataset.UserDatasetStore;
import org.gusdb.wdk.service.formatter.ProjectFormatter;
import org.gusdb.wdk.service.service.user.UserDatasetService;
import org.json.JSONObject;

@Path("/")
public class ProjectService extends AbstractWdkService {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public JSONObject getServiceApi() throws WdkModelException {
    String serviceEndpoint = rtrim(getUriInfo().getBaseUri().toString(), '/');
    return addSupplementalProjectInfo(ProjectFormatter.getWdkProjectInfo(getWdkModel(), serviceEndpoint));
  }

  /**
   * @throws WdkModelException if error occurs while fetching supplemental info 
   */
  protected JSONObject addSupplementalProjectInfo(JSONObject projectJson) throws WdkModelException {
    return projectJson;
  }

  @GET
  @Path("api")
  public Response redirectToRamlApiDoc() throws URISyntaxException {
    return Response.seeOther(new URI("../../service-api.html")).build();
  }

  @GET
  @Path("teapot")
  @Produces(MediaType.TEXT_PLAIN)
  public Response getTeapot() {
    String teapot = "" +
     "                 ;,'\n" +
     "     _o_    ;:;'\n" +
     " ,-.'---`.__ ;\n" +
     "((j`=====',-'\n" +
     " `-\\     /\n" +
     "    `-=-' \n";
    return Response.status(418).entity(teapot).build();
  }

  @GET
  @Path("cpu-test")
  @Produces(MediaType.TEXT_PLAIN)
  public String doCpuTest(@QueryParam("numTrials") Long numTrials) {
    assertAdmin();

    if (numTrials == null || numTrials < 1)
      numTrials = 1L;

    Timer t = new Timer();
    long totalTime = 0;
    StringBuilder output = new StringBuilder();

    for (int i = 0; i < numTrials; i++) {
      t.restart();
      for(long j = 0; i < 50E8; i++) {
        @SuppressWarnings("unused")
        long a = j;
      }
      long trialTime = t.getElapsed();
      totalTime += trialTime;
      output.append("Trial ").append(i + 1).append(" time: ")
            .append(Timer.getDurationString(trialTime)).append("\n");
    }

    output.append("Total time: ").append(Timer.getDurationString(totalTime))
          .append(" (").append(Timer.getDurationString(totalTime / numTrials)).append(" avg)\n");

    return output.toString();
  }

  /**
   * A public access service that reports the default quota in MB and can double
   * as a health check of the user dataset store.
   */
  @GET
  @Path("user-datasets/config")
  @Produces(MediaType.APPLICATION_JSON)
  public JSONObject getDefaultQuota() throws WdkModelException {
    UserDatasetStore dsStore = UserDatasetService.getUserDatasetStore(getWdkModel());
    try (UserDatasetSession dsSession = dsStore.getSession()) {
      return new JSONObject()
        .put("default_quota", dsSession.getDefaultQuota(true));
    }
  }
}
