package org.gusdb.wsf.service;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;
import org.gusdb.wsf.common.ResponseStatus;
import org.gusdb.wsf.common.WsfRequest;
import org.gusdb.wsf.plugin.PluginExecutor;

/**
 * The WSF Web service entry point.
 *
 * @author Jerric
 * @since Nov 2, 2005
 */
@Path("/")
public class WsfService {

  public static final String VERSION = "3.0.0";

  private static final Logger LOG = Logger.getLogger(WsfService.class);

  public WsfService() {
    // set up the config dir
    // String gusHome = System.getProperty("GUS_HOME");
    // if (gusHome != null) {
    // String configPath = gusHome + "/config/";
    // STATIC_CONTEXT.put(Plugin.CTX_CONFIG_PATH, configPath);
    // }
    LOG.debug("WsfService initialized");
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response invoke(@FormParam(WsfRequest.PARAM_REQUEST) final String jsonRequest) {
    long start = System.currentTimeMillis();

    // open a StreamingOutput
    StreamingOutput output = new StreamingOutput() {

      @Override
      public void write(OutputStream outStream) throws IOException {
        // prepare to run the plugin
        PluginExecutor executor = new PluginExecutor();
        ResponseStatus status = new ResponseStatus();

        // prepare response
        ObjectOutputStream objectStream = new ObjectOutputStream(outStream);
        StreamingPluginResponse pluginResponse = new StreamingPluginResponse(objectStream);
        int checksum = 0;
        try {
          ServiceRequest request = new ServiceRequest(jsonRequest);
          checksum = request.getChecksum();
          LOG.debug("Invoking WSF: checksum=" + checksum + "\n" + jsonRequest);

          // invoke plugin
          int signal = executor.execute(request.getPluginClass(), request, pluginResponse);
          status.setSignal(signal);
        }
        catch (Exception ex) {
          status.setSignal(-1);
          status.setException(ex);
        }
        finally {
          // send signal back
          objectStream.writeObject(status);
          objectStream.flush();
          objectStream.close();

          LOG.debug("WSF Service finished: checksum=" + checksum + ", status=" + status + ", #rows=" +
              pluginResponse.getRowCount() + ", #attch=" + pluginResponse.getAttachmentCount());
        }
      }
    };

    // get the response
    Response response = Response.ok(output).build();
    long end = System.currentTimeMillis();
    LOG.info("WsfService call finished in " + ((end - start) / 1000D) + " seconds");
    return response;
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getInfo() {
    return this.getClass().getSimpleName() + " version " + VERSION;
  }
}
