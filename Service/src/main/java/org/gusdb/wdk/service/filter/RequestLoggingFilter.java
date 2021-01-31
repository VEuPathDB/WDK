package org.gusdb.wdk.service.filter;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Priority;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.glassfish.jersey.server.ContainerRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Priority(200)
public class RequestLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

  private static final Logger LOG = Logger.getLogger(RequestLoggingFilter.class);

  private static final Level LOG_LEVEL = Level.INFO;

  private static final String EMPTY_ENTITY = "<empty>";
  private static final String FORM_ENTITY = "<form_data>";

  public static boolean isLogEnabled() {
    return LOG.isEnabledFor(LOG_LEVEL);
  }

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    // don't impact performance if logging is turned off
    if (isLogEnabled()) {
      logRequest(
          requestContext.getMethod(),
          requestContext.getUriInfo(),
          getRequestBody(requestContext));
    }
  }
  
  public static void logRequest(String method, UriInfo uriInfo, String body) {
    StringBuilder log = new StringBuilder("HTTP ")
      .append(method).append(" /").append(uriInfo.getPath());

    // add query params if present
    MultivaluedMap<String,String> query = uriInfo.getQueryParameters();
    if (!query.isEmpty()) {
      log.append(NL).append("Query Parameters: ").append(queryToJson(query));
    }

    // add request body if present
    if (body != null && !body.isEmpty() && !EMPTY_ENTITY.equals(body)) {
      log.append(NL).append("Request Body: ").append(body);
    }

    LOG.log(LOG_LEVEL, log.toString());
  }

  public static String queryToJson(MultivaluedMap<String, String> map) {
    JSONObject json = new JSONObject();
    for (Entry<String,List<String>> entry : map.entrySet()) {
      json.put(entry.getKey(), new JSONArray(entry.getValue()));
    }
    return json.toString(2);
  }
  
  private static String getRequestBody(ContainerRequestContext requestContext) {
    String contentType = requestContext.getHeaderString("Content-Type");
    if (contentType == null)
      contentType = MediaType.APPLICATION_JSON; // assume JSON input if unspecified
    switch (contentType) {
      case MediaType.APPLICATION_FORM_URLENCODED:
        return FORM_ENTITY;
      case MediaType.APPLICATION_JSON:
      case MediaType.APPLICATION_JSON + "; charset=utf-8":
      case MediaType.APPLICATION_JSON + "; charset=UTF-8":
        return formatJson(getRequestBodyText(requestContext));
      case MediaType.TEXT_PLAIN:
      case MediaType.TEXT_XML:
        return getRequestBodyText(requestContext);
      default:
        // assume JSON
        return "Indeterminant data of type " + contentType;
    }
  }

  private static String getRequestBodyText(ContainerRequestContext requestContext) {
    ContainerRequest context = (ContainerRequest) requestContext;
    try {
      if (context.bufferEntity()) {
        String entity = context.readEntity(String.class);
        requestContext.setEntityStream(new ByteArrayInputStream(entity.getBytes()));
        return entity;
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

  public static String formatJson(String entity) {
    if (entity != null && !entity.isEmpty()) {
      try {
        if (entity.startsWith("{")) {
          return new JSONObject(entity).toString(2);
        }
        else if (entity.startsWith("[")){
          return new JSONArray(entity).toString(2);
        }
        else {
          return entity;
        }
      }
      catch (JSONException e) {
        return entity;
      }
    }
    return EMPTY_ENTITY;
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
      throws IOException {
    LOG.log(LOG_LEVEL, "Request complete");
  }
}
