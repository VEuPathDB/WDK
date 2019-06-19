package org.gusdb.wdk.service.service;

import org.gusdb.fgputil.Timer;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.cache.CacheMgr;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Date;

@Path("system")
public class SystemService extends AbstractWdkService {

  @GET
  @Path("userdb/connections")
  @Produces(MediaType.TEXT_HTML)
  public String getUserDbInfo() {
    return getConnectionInfoResponse(getWdkModel().getUserDb());
  }

  @GET
  @Path("appdb/connections")
  @Produces(MediaType.TEXT_HTML)
  public String getAppDbInfo() {
    return getConnectionInfoResponse(getWdkModel().getAppDb());
  }

  private String getConnectionInfoResponse(DatabaseInstance db) {
    assertAdmin();
    return "<!DOCTYPE html><html><body><pre>"
      + db.getUnclosedConnectionInfo()
      + "</pre></body></html>";
  }

  @GET
  @Path("caches")
  @Produces(MediaType.APPLICATION_JSON)
  public String getInMemoryCacheSnapshot() {
    assertAdmin();
    JSONArray array = new JSONArray();

    for (var entry : CacheMgr.get().getAllCaches().entrySet()) {
      var cache    = entry.getValue();
      var size     = cache.getSize();
      var lastTrim = cache.getLastTrimDate();

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

    return array.toString(2);
  }
}
