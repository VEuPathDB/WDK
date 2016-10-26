package org.gusdb.wdk.service.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.fgputil.Timer;
import org.gusdb.wdk.service.formatter.ProjectFormatter;

@Path("/")
public class ProjectService extends WdkService {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getServiceApi() {
    return Response.ok(ProjectFormatter.getWdkProjectInfo(getWdkModel()).toString()).build();
  }

  @GET
  @Path("teapot")
  @Produces(MediaType.TEXT_HTML)
  public Response getTeapot() {
    String assetsUrl = getWdkModel().getModelConfig().getAssetsUrl();
    String imageLink = assetsUrl + "/wdk/images/r2d2_ceramic_teapot.jpg";
    String html = "<!DOCTYPE html><html><body><img src=\"" + imageLink + "\"/></body></html>";
    return Response.status(418).entity(html).build();
  }

  @GET
  @Path("cpuTest")
  @Produces(MediaType.TEXT_PLAIN)
  public Response doCpuTest(@QueryParam("numTrials") Long numTrials) {
    if (numTrials == null || numTrials < 1) numTrials = 1L;
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
    return Response.ok(output.toString()).build();
  }
}
