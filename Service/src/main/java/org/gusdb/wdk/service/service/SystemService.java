package org.gusdb.wdk.service.service;

import java.util.Date;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.fgputil.Timer;
import org.gusdb.fgputil.cache.InMemoryCache;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.cache.CacheMgr;
import org.json.JSONArray;
import org.json.JSONObject;

@Path("system")
public class SystemService extends AbstractWdkService {

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

  @GET
  @Path("caches")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getInMemoryCacheSnapshot() {
    assertAdmin();
    JSONArray array = new JSONArray();
    for (Entry<String, InMemoryCache<?,?>> entry : CacheMgr.get().getAllCaches().entrySet()) {
      InMemoryCache<?,?> cache = entry.getValue();
      int size = cache.getSize();
      Date lastTrim = cache.getLastTrimDate();
      array.put(new JSONObject()
          .put("name", entry.getKey())
          .put("capacity", cache.getCapacity())
          .put("size", size)
          .put("pct-full", (100.0 * size / cache.getCapacity()) + "%")
          .put("trim-amount", cache.getNumToTrimOnCapacity())
          .put("last-trim", (lastTrim == null ? "never" : lastTrim.toString()))
          .put("time-since-last-trim", (lastTrim == null ? "N/A" :
            Timer.getDurationString(new Date().getTime() - lastTrim.getTime()))));
    }
    return Response.ok(array.toString(2)).build();
  }
}
