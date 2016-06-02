package org.gusdb.wdk.service.filter;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.gusdb.wdk.model.jspwrap.WdkModelBean;

public class ClientCacheExpirationFilter implements ContainerRequestFilter {

  private static final String CLIENT_WDK_TIMESTAMP_HEADER = "X-CLIENT-WDK-TIMESTAMP";
  private static final String TIMESTAMP_CONFLICT_MESSAGE_ENTITY = "WDK-TIMESTAMP-MISMATCH";

  private WdkModelBean _wdkModelBean;

  @Context
  protected void setServletContext(ServletContext context) {
    _wdkModelBean = ((WdkModelBean)context.getAttribute("wdkModel"));
  }

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {

    // get incoming client timestamp
    String incomingTimestamp = getClientTimestamp(requestContext);

    // if client did not send a WDK timestamp, then don't filter request
    if (incomingTimestamp == null) return;

    // get current server timestamp of WDK model
    String modelTimestamp = String.valueOf(_wdkModelBean.getModel().getStartupTime());
    
    if (!incomingTimestamp.equals(modelTimestamp)) {
      requestContext.abortWith(Response
          .status(Status.CONFLICT)
          .type(MediaType.TEXT_PLAIN)
          .entity(TIMESTAMP_CONFLICT_MESSAGE_ENTITY)
          .build());
    }
  }

  private String getClientTimestamp(ContainerRequestContext requestContext) {
    List<String> clientWdkTimestamps = requestContext.getHeaders().get(CLIENT_WDK_TIMESTAMP_HEADER);
    // if no headers with this name are found, return null
    if (clientWdkTimestamps.isEmpty()) return null;
    // if >1 header is found, return the last one
    //   (seems most sensible- the last one is the one the client intended)
    return clientWdkTimestamps.get(clientWdkTimestamps.size() - 1);
  }
}
