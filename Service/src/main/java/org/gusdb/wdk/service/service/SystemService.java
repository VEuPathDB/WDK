package org.gusdb.wdk.service.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.fgputil.db.pool.DatabaseInstance;

@Path("system")
public class SystemService extends WdkService {

  @GET
  @Path("userdb/connections")
  @Produces(MediaType.TEXT_HTML)
  public Response getUserDbInfo() {
    return getConnectionInfoResponse(getWdkModel().getUserDb());
  }

  @GET
  @Path("appdb/connections")
  @Produces(MediaType.TEXT_HTML)
  public Response getAppDbInfo() {
    return getConnectionInfoResponse(getWdkModel().getAppDb());
  }

  private Response getConnectionInfoResponse(DatabaseInstance db) {
    assertAdmin();
    String connectionInfo = db.getUnclosedConnectionInfo();
    String html = "<!DOCTYPE html><html><body><pre>" + connectionInfo + "</pre></body></html>";
    return Response.ok(html).build();
  }
}
