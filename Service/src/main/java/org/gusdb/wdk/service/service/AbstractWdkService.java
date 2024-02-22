package org.gusdb.wdk.service.service;

import static java.lang.String.format;
import static org.gusdb.fgputil.FormatUtil.NL;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.Request;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.events.Events;
import org.gusdb.fgputil.web.RequestData;
import org.gusdb.fgputil.web.SessionProxy;
import org.gusdb.oauth2.client.ValidatedToken;
import org.gusdb.wdk.controller.ContextLookup;
import org.gusdb.wdk.errors.ErrorContext;
import org.gusdb.wdk.errors.ErrorContext.ErrorLocation;
import org.gusdb.wdk.errors.ServerErrorBundle;
import org.gusdb.wdk.events.ErrorEvent;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeFieldContainer;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.UserBundle;

/**
 * This class serves as a superclass for WDK JAX-RS services.  It provides a
 * number of methods that provide access to the session and current user, the
 * WdkModel, URI information, cookies, etc.  It does this by annotating some
 * private fields for JAX-RS dependency injection.  It also provides convenience
 * methods to help WDK services standardize their responses in common
 * conditions, e.g. return a permission denied if the user is not an
 * administrator, etc.  WDK service classes do not have to subclass WdkService,
 * but it is recommended for these reasons.  Since it provides no services of
 * its own is has been made abstract.
 *
 * @author rdoherty
 */
public abstract class AbstractWdkService {

  private static final Logger LOG = Logger.getLogger(AbstractWdkService.class);

  public static final String PERMISSION_DENIED = "Permission Denied.  You do not have access to this resource.";
  public static final String NOT_FOUND = "Resource '%s' does not exist.";

  private static final byte[] PROCESS_STREAMING_FAILURE_MESSAGE = (NL + NL +
      "****** An error occured while processing your request. The response's data may be truncated.").getBytes();

  /**
   * Composes a proper Not Found exception message using the supplied resource.
   *
   * @return - Not Found message with resource embedded.
   */
  public static String formatNotFound(String resource) {
    return format(NOT_FOUND, resource);
  }

  public static String paramToSegment(String pathParamName) {
    return "{" + pathParamName + "}";
  }

  @Context
  private ServletContext _servletContext;

  // NOTE: allowing protected access because in certain cases we want to
  //   directly read JkEnvVar values forwarded from Apache.  These values are
  //   unfortunately not available via HttpServletRequest.getAttributeNames(),
  //   used to generate RequestData wrapper objects
  @Context
  protected HttpServletRequest _servletRequest;

  @Context
  private Request _grizzlyRequest;

  @Context
  private UriInfo _uriInfo;

  @Context
  private HttpHeaders _headers;

  private WdkModel _testWdkModel;

  // public setter for unit tests
  public void testSetup(WdkModel wdkModel) {
    _testWdkModel = wdkModel;
  }

  public WdkModel getWdkModel() {
    return _testWdkModel != null ? _testWdkModel : ContextLookup.getWdkModel(_servletContext);
  }

  public RequestData getRequest() {
    return ContextLookup.getRequest(_servletRequest, _grizzlyRequest);
  }

  protected UriInfo getUriInfo() {
    return _uriInfo;
  }

  protected String getContextUri() {
    return getRequest().getContextUri();
  }

  protected String getServiceUri() {
    return getContextUri() + getServiceUrlSegment();
  }

  private String getServiceUrlSegment() {
    return ContextLookup.getApplicationContext(_servletContext)
        .getInitParameter(Utilities.WDK_SERVICE_ENDPOINT_KEY);
  }

  protected Map<String, Cookie> getRequestCookies() {
    return _headers.getCookies();
  }

  protected Map<String, List<String>> getHeaders() {
    return _headers.getRequestHeaders();
  }

  /**
   * Note!  Use of session should be extremely limited.  WDK is intended to be a
   * stateless service.
   *
   * @return genericized session object (compatible with both Servlet and Grizzly sessions)
   */
  protected SessionProxy getSession() {
    return getRequest().getSession();
  }

  protected User getRequestingUser() {
    User user = (User) getRequest().getAttributeMap().get(Utilities.WDK_USER_KEY);
    if (user != null) {
      // NOTE: user should ALWAYS be non-null in servlet containers with CheckLoginFilter active
      return user;
    }
    throw new IllegalStateException("No user present on request.");
  }

  protected ValidatedToken getAuthorizationToken() {
    return (ValidatedToken)getRequest().getAttributeMap().get(Utilities.BEARER_TOKEN_KEY);
  }

  protected boolean isSessionUserAdmin() {
    List<String> adminEmails = getWdkModel().getModelConfig().getAdminEmails();
    return adminEmails.contains(getRequestingUser().getEmail());
  }

  protected void assertAdmin() {
    if (!isSessionUserAdmin()) {
      throw new ForbiddenException("Administrative access is required for this function.");
    }
  }

  /**
   * Returns a session-aware user bundle based on the input string.
   *
   * @param userIdStr potential target user ID as string, or special string 'current' indicating session user
   * @return user bundle describing status of the requested user string
   * @throws WdkModelException if error occurs while accessing user data (probably a DB problem)
   */
  protected UserBundle parseTargetUserId(String userIdStr) throws WdkModelException {
    return UserBundle.createFromTargetId(userIdStr, getRequestingUser(), getWdkModel().getUserFactory(), isSessionUserAdmin());
  }

  /**
   * Triggers error events for errors caught during the processing of a service
   * request.  This is for non-fatal errors that admins nevertheless may want to
   * be alerted to.
   *
   * @param errors list of errors for which to trigger error events
   */
  protected void triggerErrorEvents(List<Exception> errors) {
    ErrorContext context = getErrorContext(ErrorLocation.WDK_SERVICE);
    for (Exception e : errors) {
      Events.trigger(new ErrorEvent(new ServerErrorBundle(e), context));
    }
  }

  /**
   * Returns an error context for the current request
   *
   * @return errorLocation location error occurred
   */
  public ErrorContext getErrorContext(ErrorLocation errorLocation) {
    return getErrorContext(getRequest(), getWdkModel(), errorLocation);
  }

  /**
   * Aggregate environment context data into an object for easy referencing
   *
   * @param request current HTTP servlet request
   * @param wdkModel this WDK Model
   * @return errorLocation location error occurred
   */
  public static ErrorContext getErrorContext(RequestData request, WdkModel wdkModel, ErrorLocation errorLocation) {
    return new ErrorContext(
      wdkModel,
      request.getSnapshot(),
      request.getSession().getAttributeMap(),
      errorLocation);
  }

  /**
   * Creates a JAX/RS StreamingOutput object based on incoming data content from
   * a file, database, or other data producer
   *
   * @param content data to be streamed to the client
   * @return streaming output object that will stream content to the client
   */
  public static StreamingOutput getStreamingOutput(InputStream content) {
    return outputStream -> {
      try {
        IoUtil.transferStream(outputStream, content);
        LOG.debug("Finished transferring streaming output");
      }
      catch (IOException e) {
        LOG.error("Unable to complete data stream transfer", e);
        throw new WebApplicationException(e);
      }
    };
  }

  /**
   * Creates a JAX/RS StreamingOutput object based on the standard output
   * stream of the passed process.  The standard output of the process will
   * be streamed until exhausted.  It then waits for the process to complete data
   * content from a file, database, or other data producer
   *
   * @param process process whose stdout will be streamed to the client
   * @param commandLine command line of passed process (used for logging in event of failure)
   * @return streaming output object that will stream content to the client
   */
  public static StreamingOutput getStreamingStandardOutput(Process process, String commandLine) {
    return outputStream -> {
      try {
        IoUtil.transferStream(outputStream, process.getInputStream());
        LOG.debug("Finished transferring streaming output");
        try {
          if (process.waitFor(5, TimeUnit.SECONDS)) {
            // process completed; check exit value
            int exitValue = process.exitValue();
            if (exitValue != 0) {
              LOG.error("A process meant to stream its output as a service " +
                  "response exited abnormally (exit value = " + exitValue +
                  ").  Command line of process: " + commandLine);
              outputStream.write(PROCESS_STREAMING_FAILURE_MESSAGE);
              outputStream.flush();
            }
          }
          else {
            // process is still running; write an error to stream and long and kill
            LOG.error("A process meant to stream its output as a service " +
                "response closed its output stream but is not completing.  " +
                "Waited 5 seconds before killing it.  Command line of process: " + commandLine);
            process.destroyForcibly();
            outputStream.write(PROCESS_STREAMING_FAILURE_MESSAGE);
            outputStream.flush();
          }
        }
        catch (InterruptedException e) {
          // this should probably never happen, but handle anyway
          LOG.error("A process meant to stream its output as a service " +
              "response was interrupted before it could complete.  " +
              "Command line of process: " + commandLine);
          outputStream.write(PROCESS_STREAMING_FAILURE_MESSAGE);
          outputStream.flush();
          
        }
      }
      catch (IOException e) {
        LOG.error("Unable to complete data stream transfer", e);
        throw new WebApplicationException(e);
      }
    };
  }

  /**
   * Returns an unboxed version of the passed value or the default boolean flag
   * value (false) if the passed value is null.
   *
   * @param boolValue flag value passed to service
   * @return unboxed value or false if null
   */
  protected static boolean getFlag(Boolean boolValue) {
    return (boolValue == null ? false : boolValue);
  }

  /**
   * Returns an unboxed version of the passed value or the default boolean flag
   * value if the passed value is null.
   *
   * @param boolValue flag value passed to service
   * @param defaultValue default value if boolValue is null
   * @return unboxed value or defaultValue if null
   */
  protected static boolean getFlag(Boolean boolValue, boolean defaultValue) {
    return (boolValue == null ? defaultValue : boolValue);
  }

  /**
   * Attempts to parse the passed string into a long int.  If successful,
   * returns it; if not, a service NotFoundException is thrown.
   *
   * @param resourceType type of resource to display if not found
   * @param idString string to parse to ID (type long)
   * @return successfully passed long value
   * @throws NotFoundException if unable to parse
   */
  protected static long parseIdOrNotFound(String resourceType, String idString) {
    try {
      return Long.parseLong(idString);
    }
    catch (NumberFormatException e) {
      throw new NotFoundException(formatNotFound(resourceType + ": " + idString));
    }
  }

  protected RecordClass getRecordClassOrNotFound(String recordClassUrlSegment) {
    return getWdkModel().getRecordClassByUrlSegment(recordClassUrlSegment)
      .orElseThrow(() ->
        // record class of the name provided cannot be found
        new NotFoundException(formatNotFound("record type: " + recordClassUrlSegment)));
  }

  protected Question getQuestionOrNotFound(String questionUrlSegment) {
    return getWdkModel().getQuestionByName(questionUrlSegment)
      .orElseThrow(() ->
      // question of the name provided cannot be found
      new NotFoundException(formatNotFound("search: " + questionUrlSegment)));
  }

  protected Question getQuestionOrNotFound(String recordClassUrlSegment,
    String questionUrlSegment) {
    return getQuestionOrNotFound(
      getRecordClassOrNotFound(recordClassUrlSegment),
      questionUrlSegment
    );
  }

  protected Question getQuestionOrNotFound(RecordClass record, String searchName) {
    Question question = getQuestionOrNotFound(searchName);
    if (!question.getRecordClassName().equals(record.getFullName()))
      throw new NotFoundException(format(
        "There is no search \"%s\" associated with record type \"%s\"",
        question.getName(),
        record.getName()
      ));
    return question;
  }

  /**
   * Look up a {@link RecordClass} and {@link Question} by name, then lookup an
   * {@link AttributeField} by name on the {@code Question}.
   * <p>
   * If the {@code RecordClass}, {@code Question}, or the {@code AttributeField}
   * is not found, throws a {@link NotFoundException}.
   *
   * @param container
   *   Name of the {@code Question} to look up
   * @param column
   *   Name of the {@code AttributeField} to look up
   *
   * @return The named {@code AttributeField} from the named {@code
   * RecordClass}.
   *
   * @throws NotFoundException
   *   Throw if either the {@code RecordClass} or {@code AttributeField} could
   *   not be found.
   */
  protected static AttributeField getColumnOrNotFound(
    final AttributeFieldContainer container,
    final String column
  ) {
    return container.getAttributeField(column)
      .orElseThrow(() -> new NotFoundException(
        format("Invalid column \"%s\"", column)));
  }
}
