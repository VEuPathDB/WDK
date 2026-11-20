package org.gusdb.wdk.service.filter;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.Priority;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.apache.commons.io.output.CountingOutputStream;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.web.RequestData;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.service.SystemService;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Logs receipt of WDK service requests (HTTP method, path, query parameters),
 * and completion (duration, response status, request body (if error)).
 *
 * This filter is also responsible for limiting the size of request bodies
 * (currently 15 MiB, or ~30 mib in memory).  To accomplish this, regardless
 * of Content-Size header, it streams the body into a temporary file and checks
 * file size, before setting the request input stream to a FileInputStream on
 * the file.  ResponseFilter and WriterInterceptor methods are responsible for
 * deleting the file during response completion.
 */
@Priority(200)
public class RequestLoggingFilter extends AbstractBookendFilter {

  public static final Logger LOG = Logger.getLogger(RequestLoggingFilter.class);

  // log level for request log
  private static final Level LOG_LEVEL = Level.INFO;

  private enum RequestBodyAction {
    NONE,                               // no body
    THROW_413_CONTENT_TOO_LARGE,        // body too large to process
    LOG_BODY_ON_ERROR_AND_DELETE;       // body small enough to process
  }

  private static final String OMIT_REQUEST_LOGGING_PROP_KEY = "omitRequestLogging";
  private static final String REQUEST_LOGGING_ACTION_PROP_KEY = "requestLoggingAction";
  private static final String REQUEST_BODY_FILE_PATH_PROP_KEY = "requestBodyFilePath";

  private static final long MAX_REQUEST_BODY_BYTES_IN_MEMORY = 30 /* mib */ * 1048576;
  private static final long MAX_REQUEST_BODY_BYTES_IN_FILE = MAX_REQUEST_BODY_BYTES_IN_MEMORY / 2;
  private static final long MAX_REQUEST_LOGGING_BYTES_IN_FILE = 5000;  // approx 5k chars (will occupy 10k bytes in memory)

  public static boolean isLogEnabled() {
    return LOG.isEnabledFor(LOG_LEVEL);
  }

  @Override
  protected void onRequestBegin(RequestData request, ExtendedUriInfo uriInfo, WdkModel wdkModel, User user,
      ContainerRequestContext requestContext) throws IOException {

    // skip logging for prometheus metrics endpoint which overwhelms the logs
    boolean omitRequestLogging = uriInfo.getPath()
        .equals(SystemService.PROMETHEUS_ENDPOINT_PATH);

    // tell outgoing request classes whether to skip logging
    request.setAttribute(OMIT_REQUEST_LOGGING_PROP_KEY, omitRequestLogging);

    // stream request body to temp file to check size before trying to load into memory
    Optional<Path> requestBodyFile = saveRequestBody(wdkModel.getModelConfig().getWdkTempDir(), requestContext);

    // determine action and set on request
    RequestBodyAction action = determineAction(requestBodyFile);
    request.setAttribute(REQUEST_LOGGING_ACTION_PROP_KEY, action);

    if (action == RequestBodyAction.THROW_413_CONTENT_TOO_LARGE) {

      // tell response filters not to do anything with the file since we are deleting it
      request.setAttribute(REQUEST_LOGGING_ACTION_PROP_KEY, RequestBodyAction.NONE);

      // delete the temporary file
      Files.delete(requestBodyFile.get());

      // abort handling of this request and tell why
      requestContext.abortWith(Response
          .status(Status.REQUEST_ENTITY_TOO_LARGE)
          .entity("Response Body Too Large.  Must not exceed " + (MAX_REQUEST_BODY_BYTES_IN_FILE / 1000000) + " MB.")
          .build());

      return;
    }

    // if present, request body is small enough to process
    if (requestBodyFile.isPresent()) {
      request.setAttribute(REQUEST_BODY_FILE_PATH_PROP_KEY, requestBodyFile.get());
      requestContext.setEntityStream(new FileInputStream(requestBodyFile.get().toFile()));
    }

    // explicitly check if enabled to not impact performance if logging is turned off
    if (!omitRequestLogging && isLogEnabled()) {
      logRequestStart(
          user,
          requestContext.getMethod(),
          uriInfo);
    }
  }

  private RequestBodyAction determineAction(Optional<Path> requestBodyFile) throws IOException {

    if (requestBodyFile.isEmpty()) {
      return RequestBodyAction.NONE;
    }

    Path path = requestBodyFile.get();
    long fileSize = Files.size(path);

    // check against max size limit
    if (fileSize > MAX_REQUEST_BODY_BYTES_IN_FILE) {
      return RequestBodyAction.THROW_413_CONTENT_TOO_LARGE;
    }

    return RequestBodyAction.LOG_BODY_ON_ERROR_AND_DELETE;
  }

  private Optional<Path> saveRequestBody(Path tmpDir, ContainerRequestContext requestContext) {
    if (requestContext.getMethod().equals(HttpMethod.GET)) {
      // no body expected
      return Optional.empty();
    }
    try (InputStream in = requestContext.getEntityStream()) {
      if (in == null) {
        return Optional.empty();
      }
      Path tempFile = Files.createTempFile(tmpDir, "wdkRequest-", null, IoUtil.getOpenPosixPermsAsFileAttribute());
      try (OutputStream out = new FileOutputStream(tempFile.toFile())) {
        in.transferTo(out);
        return Optional.of(tempFile);
      }
    }
    catch (IOException e) {
      throw new RuntimeException("Unable to write request body to temporary file.", e);
    }
  }

  public static void logRequestStart(User user, String method, UriInfo uriInfo) {

    StringBuilder log = new StringBuilder(getUserInfoStr(user))
        .append(" HTTP ").append(method).append(" /").append(uriInfo.getPath());

    // add query params if present
    MultivaluedMap<String,String> query = uriInfo.getQueryParameters();
    if (!query.isEmpty()) {
      log.append(NL).append("Query Parameters: ").append(queryToJson(query));
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

  @Override
  protected void onRequestCompleteNoContent(RequestData request, ExtendedUriInfo uriInfo, WdkModel wdkModel,
      User user, int httpStatus) {
    // request is complete; log status
    logRequestCompletion(request, user, httpStatus, "no content");
  }

  @Override
  protected void performContentWrite(RequestData request, ExtendedUriInfo uriInfo, WdkModel wdkModel, User user,
      int httpStatus, WriterInterceptorContext context) throws IOException {
    try {
      // replace the context's OutputStream with a wrapper that records the size of the request
      CountingOutputStream countingOut = new CountingOutputStream(context.getOutputStream());
      context.setOutputStream(countingOut);

      // write the response
      context.proceed();

      // response written successfully; log status
      logRequestCompletion(request, user, httpStatus, countingOut.getByteCount() + " bytes written successfully");
    }
    catch (IOException e) {
      // exception while writing response body; log error, then status
      LOG.error("Failure to write response body", e);
      logRequestCompletion(request, user, httpStatus, "response write failed");
      throw e;
    }
  }

  private static void logRequestCompletion(RequestData request, User user, int httpStatus, String bodyWriteStatus) {

    // gather parameters for what action should be taken, then remove props from context object
    Boolean omitRequestLogging = getAndRemoveProp(request, OMIT_REQUEST_LOGGING_PROP_KEY, Boolean.class);
    RequestBodyAction action = getAndRemoveProp(request, REQUEST_LOGGING_ACTION_PROP_KEY, RequestBodyAction.class);
    Path filePath = getAndRemoveProp(request, REQUEST_BODY_FILE_PATH_PROP_KEY, Path.class); // will be null if action is NONE

    boolean isErrorStatus = httpStatus >= 500 && httpStatus < 600;

    //LOG.info("requestCompletion, omitRequestLogging=" + omitRequestLogging + ", action=" + action + ", isErrorStatus=" + isErrorStatus);

    // if property not found, then this request went unmatched; can ignore.
    if (omitRequestLogging == null) return;

    // decide whether and what to log
    if (!omitRequestLogging && isLogEnabled()) {
      String details = isErrorStatus && action != RequestBodyAction.NONE ? ", Request Body:\n" + readRequestBody(filePath) : "";
      LOG.log(LOG_LEVEL, getUserInfoStr(user) + " Request completed [" + httpStatus + "] " + bodyWriteStatus + details);
    }

    // delete the temporary request body file
    if (filePath != null) {
      try {
        Files.delete(filePath);
      }
      catch (IOException e) {
        LOG.warn("Unable to delete temporary body file: " + filePath);
      }
    }
  }

  private static String getUserInfoStr(User user) {
    String userId = user == null ? "none" : String.valueOf(user.getUserId());
    String guest = user == null || user.isGuest() ? "G" : "R";
    return "user(" + guest + ")=" + userId;
  }

  /**
   * Reads the request body file but only up to the max number of logging bytes and returns as a String
   *
   * @param filePath path of temporary file
   * @return possibly truncated content of request body
   * @throws IOException 
   */
  private static String readRequestBody(Path filePath) {
    try (Reader r = new BufferedReader(new FileReader(filePath.toFile()))) {
      StringBuilder resultBuilder = new StringBuilder();
      int count = 0;
      int next;
      while (((next = r.read()) != -1) && count < MAX_REQUEST_LOGGING_BYTES_IN_FILE) {
        resultBuilder.append((char)next);
        count++;
      }
      return resultBuilder.toString();
    }
    catch (IOException e) {
      return "Error reading request body from file: " + e.getMessage();
    }
  }

}
