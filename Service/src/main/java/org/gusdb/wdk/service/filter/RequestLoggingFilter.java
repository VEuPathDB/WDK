package org.gusdb.wdk.service.filter;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.glassfish.jersey.server.ContainerRequest;
import org.gusdb.fgputil.FormatUtil;
import org.json.JSONArray;
import org.json.JSONObject;

public class RequestLoggingFilter implements ContainerRequestFilter {

  private static final Logger LOG = Logger.getLogger(RequestLoggingFilter.class);

  private static final Level LOG_LEVEL = Level.INFO;

  private static final String EMPTY_ENTITY = "<empty>";

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {

    // don't impact performance if logging is turned off
    if (!LOG.isEnabledFor(LOG_LEVEL)) return;

    StringBuilder log = new StringBuilder("HTTP Request: ")
      .append(requestContext.getMethod()).append(" /")
      .append(requestContext.getUriInfo().getPath());

    // add query params if present
    MultivaluedMap<String,String> query = requestContext.getUriInfo().getQueryParameters();
    if (!query.isEmpty()) {
      log.append(NL).append("Query Parameters: ").append(toJson(requestContext.getUriInfo().getQueryParameters()));
    }

    // add request body if present
    String body = getRequestBody(requestContext);
    if (!EMPTY_ENTITY.equals(body)) {
      log.append(NL).append("Request Body: ").append(getRequestBody(requestContext));
    }

    LOG.log(LOG_LEVEL, log.toString());
  }

  private String toJson(MultivaluedMap<String, String> map) {
    JSONObject json = new JSONObject();
    for (Entry<String,List<String>> entry : map.entrySet()) {
      json.put(entry.getKey(), FormatUtil.stringCollectionToJsonArray(entry.getValue()));
    }
    return json.toString(2);
  }
  
  private String getRequestBody(ContainerRequestContext requestContext) {
    ContainerRequest context = (ContainerRequest) requestContext;
    try {
      if (context.bufferEntity()) {
        String entity = context.readEntity(String.class);
        try {
          if (!entity.isEmpty()) {
            if (entity.startsWith("{")) {
              return new JSONObject(entity).toString(2);
            }
            else {
              return new JSONArray(entity).toString(2);
            }
          }
          return EMPTY_ENTITY;
        }
        finally {
          requestContext.setEntityStream(new ByteArrayInputStream(entity.getBytes()));
        }
      }
      else {
        return EMPTY_ENTITY;
      }
    }
    catch (ProcessingException e) {
      LOG.error("Could not read request entity.", e);
      return EMPTY_ENTITY;
    }
  }
}
