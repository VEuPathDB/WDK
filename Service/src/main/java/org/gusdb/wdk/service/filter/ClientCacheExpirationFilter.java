package org.gusdb.wdk.service.filter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.Priority;
import javax.servlet.ServletContext;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.gusdb.wdk.controller.ContextLookup;

@Priority(50)
public class ClientCacheExpirationFilter implements ContainerRequestFilter {

  private static final String CLIENT_WDK_TIMESTAMP_HEADER = "x-client-wdk-timestamp";
  private static final String TIMESTAMP_CONFLICT_MESSAGE_ENTITY = "WDK-TIMESTAMP-MISMATCH";

  @Context
  ServletContext _servletContext;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {

    // get incoming client timestamp
    String incomingTimestamp = getClientTimestamp(requestContext.getHeaders());

    // if client did not send a WDK timestamp, then don't filter request
    if (incomingTimestamp == null) return;

    // get current server timestamp of WDK model
    String modelTimestamp = String.valueOf(ContextLookup.getWdkModel(_servletContext).getStartupTime());

    if (!incomingTimestamp.equals(modelTimestamp)) {
      requestContext.abortWith(Response
          .status(Status.CONFLICT)
          .type(MediaType.TEXT_PLAIN)
          .entity(TIMESTAMP_CONFLICT_MESSAGE_ENTITY)
          .build());
    }
  }

  public static String getClientTimestamp(Map<String,List<String>> headers) {
    List<String> clientWdkTimestamps = headers.get(CLIENT_WDK_TIMESTAMP_HEADER);
    // if no headers with this name are found, return null
    if (clientWdkTimestamps == null || clientWdkTimestamps.isEmpty()) return null;
    // if >1 header is found, return the last one
    //   (seems most sensible- the last one is the one the client intended)
    return clientWdkTimestamps.get(clientWdkTimestamps.size() - 1);
  }
}
