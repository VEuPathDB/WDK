package org.gusdb.wdk.service.service;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.gusdb.fgputil.events.Events;
import org.gusdb.wdk.errors.ClientErrorBundle;
import org.gusdb.wdk.errors.ErrorContext.ErrorLocation;
import org.gusdb.wdk.events.ErrorEvent;
import org.gusdb.wdk.service.filter.ClientCacheExpirationFilter;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/client-errors")
public class ClientErrorReportingService extends AbstractWdkService {

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response reportError(String body) throws RequestMisformatException {
    try {
      if (requestValid()) {
        Events.trigger(new ErrorEvent(new ClientErrorBundle(new JSONObject(body)), getErrorContext(ErrorLocation.WDK_CLIENT)));
        return Response.noContent().build();
      }
      else {
        return Response.notAcceptable(null).build();
      }
    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.getMessage());
    }
  }

  private boolean requestValid() {
    try {
      long wdkStartupTime = getWdkModel().getStartupTime();
      long sentTime = Long.parseLong(ClientCacheExpirationFilter.getClientTimestamp(getHeaders()));
      return wdkStartupTime == sentTime;
    }
    catch (NumberFormatException e) {
      // sent header is in the wrong format
      return false;
    }
  }
}
