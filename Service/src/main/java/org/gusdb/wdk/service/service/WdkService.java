package org.gusdb.wdk.service.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.events.Events;
import org.gusdb.fgputil.web.HttpRequestData;
import org.gusdb.wdk.errors.ServerErrorBundle;
import org.gusdb.wdk.errors.ErrorContext;
import org.gusdb.wdk.errors.ErrorContext.ErrorLocation;
import org.gusdb.wdk.errors.ValueMaps;
import org.gusdb.wdk.errors.ValueMaps.RequestAttributeValueMap;
import org.gusdb.wdk.errors.ValueMaps.ServletContextValueMap;
import org.gusdb.wdk.errors.ValueMaps.SessionAttributeValueMap;
import org.gusdb.wdk.events.ErrorEvent;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.UserBundle;

public abstract class WdkService {

  public static final String PERMISSION_DENIED = "Permission Denied.  You do not have access to this resource.";
  public static final String NOT_FOUND = "Resource specified [%s] does not exist.";

  /**
   * Composes a proper Not Found exception message using the supplied resource.
   * @param resource
   * @return - Not Found message with resource embedded.
   */
  public static String formatNotFound(String resource) {
    return String.format(NOT_FOUND, resource);
  }

  @Context
  private HttpServletRequest _request;

  @Context
  private UriInfo _uriInfo;

  private ServletContext _servletContext;
  private WdkModelBean _wdkModelBean;
  private UserBean _user;
  private String _serviceEndpoint;

  // public setter for unit tests
  public void testSetup(WdkModel wdkModel, User user) {
    _wdkModelBean = new WdkModelBean(wdkModel);
    _user = new UserBean(user);
  }

  protected WdkModelBean getWdkModelBean() {
    return _wdkModelBean;
  }

  protected WdkModel getWdkModel() {
    return _wdkModelBean.getModel();
  }

  protected UriInfo getUriInfo() {
    return _uriInfo;
  }

  protected String getServiceEndpoint() {
    return _serviceEndpoint;
  }

  protected Cookie[] getCookies() {
    return _request.getCookies();
  }

  protected Map<String,List<String>> getHeaders() {
    Map<String, List<String>> headers = new HashMap<>();
    Enumeration<String> headerNames = _request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      Enumeration<String> headerValues = _request.getHeaders(headerName);
      List<String> values = new ArrayList<>();
      while (headerValues.hasMoreElements()) {
        values.add(headerValues.nextElement());
      }
      headers.put(headerName, values);
    }
    return headers;
  }

  protected HttpSession getSession() {
    return _request.getSession();
  }
  
  protected HttpSession getSession(boolean newSession) {
    return _request.getSession(newSession);
  }

  protected UserBean getSessionUserBean() {
    return (_user != null ? _user : (UserBean)_request.getSession().getAttribute(Utilities.WDK_USER_KEY));
  }

  protected long getSessionUserId() {
    return getSessionUserBean().getUserId();
  }

  protected User getSessionUser() {
    return getSessionUserBean().getUser();
  }

  protected boolean isSessionUserAdmin() {
    List<String> adminEmails = getWdkModel().getModelConfig().getAdminEmails();
    return adminEmails.contains(getSessionUser().getEmail());
  }

  protected void assertAdmin() {
    if (!isSessionUserAdmin()) {
      throw new ForbiddenException("Administrative access is required for this function.");
    }
  }

  @Context
  protected void setServletContext(ServletContext context) {
    _servletContext = context;
    _wdkModelBean = (WdkModelBean)context.getAttribute(Utilities.WDK_MODEL_KEY);
    _serviceEndpoint = context.getInitParameter(Utilities.WDK_SERVICE_ENDPOINT_KEY);
  }

  /**
   * Returns a session-aware user bundle based on the input string.
   * 
   * @param userIdStr potential target user ID as string, or special string 'current' indicating session user
   * @return user bundle describing status of the requested user string
   * @throws WdkModelException if error occurs while accessing user data (probably a DB problem)
   */
  protected UserBundle parseTargetUserId(String userIdStr) throws WdkModelException {
    return UserBundle.createFromTargetId(userIdStr, getSessionUser(), getWdkModel().getUserFactory(), isSessionUserAdmin());
  }

  /**
   * Triggers error events for errors caught during the processing of a service request.  This is for
   * non-fatal errors that admins nevertheless may want to be alerted to.
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
   * @return error context for the current request
   */
  public ErrorContext getErrorContext(ErrorLocation errorLocation) {
    return getErrorContext(_servletContext, _request, _wdkModelBean.getModel(), errorLocation);
  }

  /**
   * Aggregate environment context data into an object for easy referencing
   * 
   * @param context current servlet context
   * @param request current HTTP servlet request
   * @param wdkModel this WDK Model
   * @return context data for this error
   */
  public static ErrorContext getErrorContext(ServletContext context,
          HttpServletRequest request, WdkModel wdkModel, ErrorLocation errorLocation) {
    return new ErrorContext(
      wdkModel,
      new HttpRequestData(request),
      ValueMaps.toMap(new ServletContextValueMap(context)),
      ValueMaps.toMap(new RequestAttributeValueMap(request)),
      ValueMaps.toMap(new SessionAttributeValueMap(request.getSession())),
      errorLocation);
  }

  /**
   * Creates a JAX/RS StreamingOutput object based on incoming data
   * content from a file, database, or other data producer
   * 
   * @param content data to be streamed to the client
   * @return streaming output object that will stream content to the client
   */
  protected StreamingOutput getStreamingOutput(InputStream content) {
    return outputStream -> {
      try {
        IoUtil.transferStream(outputStream, content);
      }
      catch (IOException e) {
        throw new WebApplicationException(e);
      }
      finally {
        content.close();
      }
    };
  }

  /**
   * Returns an unboxed version of the passed value or the default
   * boolean flag value (false) if the passed value is null.
   * 
   * @param boolValue flag value passed to service
   * @return unboxed value or false if null
   */
  protected static boolean getFlag(Boolean boolValue) {
    return (boolValue == null ? false : boolValue);
  }

  /**
   * Returns an unboxed version of the passed value or the default
   * boolean flag value if the passed value is null.
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
}
