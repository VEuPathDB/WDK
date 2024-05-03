package org.gusdb.wdk.service.filter;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;

import jakarta.annotation.Priority;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.glassfish.jersey.server.ContainerRequest;
import org.gusdb.wdk.service.service.SystemService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Priority(200)
public class RequestLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter, WriterInterceptor {

  private static final Logger LOG = Logger.getLogger(RequestLoggingFilter.class);

  private static final Level LOG_LEVEL = Level.INFO;

  private static final String EMPTY_ENTITY = "<empty>";
  private static final String FORM_ENTITY = "<form_data>";

  private static final String OMIT_REQUEST_LOGGING_PROP_KEY = "omitRequestLogging";
  private static final String HTTP_RESPONSE_STATUS_PROP_KEY = "httpResponseStatus";

  public static boolean isLogEnabled() {
    return LOG.isEnabledFor(LOG_LEVEL);
  }

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {

    // skip logging for prometheus metrics endpoint which overwhelms the logs
    boolean omitRequestLogging = requestContext.getUriInfo().getPath()
        .equals(SystemService.PROMETHEUS_ENDPOINT_PATH);

    // tell outgoing request classes whether to skip logging
    requestContext.setProperty(OMIT_REQUEST_LOGGING_PROP_KEY, omitRequestLogging);

    // explicitly check if enabled to not impact performance if logging is turned off
    if (!omitRequestLogging && isLogEnabled()) {
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

  private static String queryToJson(MultivaluedMap<String, String> map) {
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
  public void filter(ContainerRequestContext context, ContainerResponseContext responseContext)
      throws IOException {
    // check to see if this response has a body; if so, defer completion logging to the WriterInterceptor method
    boolean hasResponseBody = responseContext.getEntity() != null;
    int httpStatus = responseContext.getStatus();
    if (hasResponseBody) {
      // pass along the response status to the interceptor to log after response body is written
      context.setProperty(HTTP_RESPONSE_STATUS_PROP_KEY, httpStatus);
    }
    else {
      // request is complete; log status
      logRequestCompletion(httpStatus, "empty", context::getProperty, context::removeProperty);
    }
  }

  @Override
  public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
    // get HTTP status passed along by the filter() method, then remove
    int httpStatus = (Integer)context.getProperty(HTTP_RESPONSE_STATUS_PROP_KEY);
    context.removeProperty(HTTP_RESPONSE_STATUS_PROP_KEY);
    try {
      // write the response
      context.proceed();
      // response written successfully; log status
      logRequestCompletion(httpStatus, "written successfully", context::getProperty, context::removeProperty);
    }
    catch (Exception e) {
      // exception while writing response body; log error, then status
      LOG.error("Failure to write response body", e);
      logRequestCompletion(httpStatus, "write failed", context::getProperty, context::removeProperty);
      throw e;
    }
  }

  private static void logRequestCompletion(int httpStatus, String bodyWriteStatus,
      Function<String,Object> getter, Consumer<String> remover) {
    // skip logging as requested
    Boolean omitRequestLogging = (Boolean)getter.apply(OMIT_REQUEST_LOGGING_PROP_KEY);
    remover.accept(OMIT_REQUEST_LOGGING_PROP_KEY);

    // if property not found, then this request went unmatched; can ignore.  If present, omit as requested.
    if (omitRequestLogging != null && !omitRequestLogging) {
      LOG.log(LOG_LEVEL, "Request completed [" + httpStatus + "]; response body " + bodyWriteStatus);
    }
  }


}
