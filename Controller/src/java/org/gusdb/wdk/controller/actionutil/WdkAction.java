package org.gusdb.wdk.controller.actionutil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.ApplicationInitListener;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.WdkValidationException;
import org.gusdb.wdk.controller.actionutil.ParamDef.Count;
import org.gusdb.wdk.controller.actionutil.ParamDef.DataType;
import org.gusdb.wdk.controller.actionutil.ParamDef.Required;
import org.gusdb.wdk.controller.actionutil.ParameterValidator.SecondaryValidator;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * Abstract class meant to provide a variety of commonly used utilities, a
 * somewhat more restricted API for action development, and a common way to
 * respond to server requests.  Also always takes care of setting the
 * appropriate mime type and file name of requests, and provides a standard
 * framework to validate request parameters and view request attributes before
 * they are sent to the JSP. 
 * 
 * @author rdoherty
 */
public abstract class WdkAction implements SecondaryValidator {

  private static final Logger LOG = Logger.getLogger(WdkAction.class.getName());

  // global response strings (i.e. named action forwards)
  /** global response string indicating success */
  public static final String SUCCESS = "success";
  /** global response string indicating further user input is needed */
  public static final String INPUT = "input";
  /** global response string indicating user needs to login for this action */
  public static final String NEEDS_LOGIN = "needs_login";
  /** global response string indicating an error occurred */
  public static final String ERROR = "application_error";
  
  /** provides empty param map for actions expecting no params */
  protected static final Map<String, ParamDef> EMPTY_PARAMS = new HashMap<>();
  
  // accessors for exception information if error is thrown
  public static final String EXCEPTION_PAGE = "exceptionPage";
  public static final String EXCEPTION_USER = "exceptionUser";
  public static final String EXCEPTION_OBJ = "exceptionObj";

  // internal site URLs often contain this parameter from the auth service
  private static final String AUTH_TICKET = "auth_tkt";

  // default max upload file size
  private static final int DEFAULT_MAX_UPLOAD_SIZE_MB = 10;
  
  /**
   * Provides standard information that came in on the current request
   * 
   * @author rdoherty
   */
  public static interface RequestData {
    public String getWebAppBaseUrl();
    public String getRequestUrl();
    public String getQueryString();
    /** returns the full request URL (including the query string) */
    public String getFullRequestUrl();
    public String getBrowser();
    public String getReferrer();
    public String getIpAddress();
    public Object getRequestAttribute(String key);
    public String getRequestHeader(String key);
  }
  
  private WdkModelBean _wdkModel;
  private HttpServlet _servlet;
  private HttpServletRequest _request;
  private HttpServletResponse _response;
  private ParamGroup _params;
  private ResponseType _responseType;

  protected abstract boolean shouldValidateParams();
  protected abstract Map<String, ParamDef> getParamDefs();
  protected abstract ActionResult handleRequest(ParamGroup params) throws Exception;

  /**
   * Executes this request, delegating business logic processing to the child
   * class.  Then translates the ActionResult returned by the handleRequest()
   * method and translates it into a response that can be understood by the
   * MVC framework.
   * 
   * @param mapping Struts1 action mapping
   * @param form Struts1 action form for this action
   * @param request servlet request
   * @param response servlet response
   * @param servlet servlet
   * @return the appropriate Struts1 ActionForward
   * @throws Exception if response cannot be translated into an ActionForward
   */
  public final ActionForward execute(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response, HttpServlet servlet)
        throws Exception {
    try {
      _request = request;
      _response = response;
      _servlet = servlet;
      _wdkModel = (WdkModelBean)_servlet.getServletContext().getAttribute(CConstants.WDK_MODEL_KEY);
      _responseType = ResponseType.html;
      
      if (requiresLogin() && getCurrentUser().isGuest()) {
        return getForwardFromResult(new ActionResult().setViewName(NEEDS_LOGIN), mapping);
      }
      
      ActionResult result;
      try {
        _params = createParamGroup(request.getParameterMap());
        result = handleRequest(_params);
      }
      catch (WdkValidationException wve) {
        // attach errors to request and return INPUT
        return getForwardFromResult(new ActionResult(_responseType)
            .setRequestAttribute("validator", wve.getValidator())
            .setViewName(INPUT), mapping);
      }

      if (result == null || result.isEmptyResult()) {
        return null;
      }
      else if (result.isStream()) {
        // handle stream response
        if (result.getFileName().isEmpty()) {
          result.setFileName(result.getResponseType().getDefaultFileName());
        }
        _response.addHeader("Content-Disposition", "attachment, filename=\"" + result.getFileName() + "\"");
        transferStream(_response.getOutputStream(), result.getStream());
        return null;
      }
      else {
        // otherwise, handle normal response
        assignAttributesToRequest(result);
        return getForwardFromResult(result, mapping);
      }
    }
    catch (Exception e) {
      // log error here and attach to request; do not depend on MVC framework
      LOG.error("Unable to execute action " + this.getClass().getName(), e);
      _request.setAttribute(EXCEPTION_USER, getCurrentUser());
      _request.setAttribute(EXCEPTION_PAGE, getRequestData().getFullRequestUrl());
      _request.setAttribute(EXCEPTION_OBJ, e);
      return getForwardFromResult(new ActionResult().setViewName(ERROR), mapping);
    }
  }
  
  @SuppressWarnings("rawtypes")
  private ParamGroup createParamGroup(Map parameterMap) throws WdkValidationException, WdkUserException {
    ParamGroup params;
    @SuppressWarnings("unchecked")
    Map<String, String[]> parameters = (Map<String, String[]>) (parameterMap == null ?
        new HashMap<>() : new HashMap<>((Map<String, String[]>)parameterMap));
    Map<String, DiskFileItem> uploads = getFileUploads();
    if (shouldValidateParams()) {
      ParameterValidator validator = new ParameterValidator();
      params = validator.validateParameters(getExpectedParams(), parameters, uploads, this);
    }
    else {
      params = buildParamGroup(parameters, uploads);
    }
    return params;
  }

  private Map<String, DiskFileItem> getFileUploads() throws WdkUserException {
    LOG.info("Loading file uploads.");
    // if not multi-part, then just return empty map
    if (!ServletFileUpload.isMultipartContent(new ServletRequestContext(_request))) {
      LOG.info("Request is not multi-part.  Returning empty map.");
      return new HashMap<String, DiskFileItem>();
    }
    try {
      ServletFileUpload uploadHandler = new ServletFileUpload(new DiskFileItemFactory());
      uploadHandler.setSizeMax(getMaxUploadSize()*1024*1024);
      @SuppressWarnings("unchecked")
      List<DiskFileItem> uploadList = uploadHandler.parseRequest(_request);
      Map<String, DiskFileItem> uploadMap = new HashMap<String, DiskFileItem>();
      for (DiskFileItem upload : uploadList) {
        LOG.info("Got a disk item from request named " + upload.getFieldName() + ": " + upload);
        uploadMap.put(upload.getFieldName(), upload);
      }
      return uploadMap;
    }
    catch (FileUploadException e) {
      throw new WdkUserException("Error handling upload field.", e);
    }
  }
  
  /**
   * Returns the maximum file upload size.  This can be overridden by subclasses.
   * 
   * @return max file size in megabytes
   */
  protected int getMaxUploadSize() {
    return DEFAULT_MAX_UPLOAD_SIZE_MB;
  }
  
  private static void transferStream(OutputStream outputStream, InputStream inputStream)
      throws IOException {
    try {
      byte[] buffer = new byte[1024]; // send 1kb at a time
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
    }
    finally {
      // only close input stream; container will close output stream
      inputStream.close();
    }
  }
  
  private ParamGroup buildParamGroup(Map<String, String[]> parameters, Map<String, DiskFileItem> uploads) {
    // generate param definitions based on passed params so calling code
    //   sees a complete ParamGroup structure
    Map<String, ParamDef> definitions = new HashMap<String, ParamDef>();
    for (String key : parameters.keySet()) {
      String[] values = parameters.get(key);
      definitions.put(key, new ParamDef(Required.OPTIONAL,
          values.length > 1 ? Count.MULTIPLE : Count.SINGULAR));
    }
    for (String key : uploads.keySet()) {
      definitions.put(key, new ParamDef(Required.OPTIONAL, DataType.FILE));
    }
    return new ParamGroup(definitions, parameters, uploads);
  }
  
  /**
   * Tells WdkAction whether execution of this request requires a user to be
   * logged in.  Default is false.  Can be overridden by children.
   * 
   * @return true if login required, else false
   */
  protected boolean requiresLogin() {
    return false;
  }
  
  /**
   * Allows children to explicitly set response type.  Note that this value is
   * only used if an exception is throw during handleRequest().  Under normal
   * conditions, the response type on the ActionResult is used to determine the
   * type of response.
   * 
   * @param responseType
   */
  protected void setResponseType(ResponseType responseType) {
    _responseType = responseType;
  }
  
  private void assignAttributesToRequest(ActionResult result) throws WdkModelException {
    // assign the current request URL for access by the resulting page
    _request.setAttribute(CConstants.WDK_REFERRER_URL_KEY, getRequestData().getReferrer());
    _request.setAttribute(CConstants.WDK_USER_KEY, getCurrentUser());
    _request.setAttribute(CConstants.WDK_MODEL_KEY, getWdkModel());
    for (String attribKey : result) {
      _request.setAttribute(attribKey, result.getRequestAttribute(attribKey));
    }
  }
  
  /**
   * @returns the full path of the web application root URL
   *   (e.g. "http://subdomain.myserver.com:8080/mywebapp")
   */
  protected String getWebAppRoot() {
    int port = _request.getServerPort();
    return "http://" + _request.getServerName() +
        (port == 80 ? "" : ":" + port) + _request.getContextPath();
  }
  
  /**
   * Allows children to define additional parameter validation.  This validation
   * will occur after primary parameter validation has completed.
   */
  @Override
  public void performAdditionalValidation(ParamGroup params) throws WdkValidationException {
    // do nothing here; to be overridden by subclass
  }
  
  private ActionForward getForwardFromResult(ActionResult result, ActionMapping mapping)
      throws WdkModelException {
    if (result.usesExplicitPath()) {
      ActionForward forward = new ActionForward();
      forward.setPath(result.getViewPath());
      forward.setRedirect(result.isRedirect());
      return forward;
    }
    else {
      ActionForward strutsForward = mapping.findForward(result.getViewName());
      if (strutsForward == null) {
        String msg = "No forward exists with name " + result.getViewName() +
            " for action " + this.getClass().getName();
        LOG.error(msg);
        throw new WdkModelException(msg);
      }
      LOG.info("Returning Struts forward with name " + result.getViewName() + ": " + strutsForward);
      return strutsForward;
    }
  }
  
  /**
   * Allows class-wide access to validated params
   * 
   * @return param group for this request
   */
  protected ParamGroup getParams() {
    return _params;
  }
  
  /**
   * @return reference to the site's WdkModelBean
   */
  protected WdkModelBean getWdkModel() {
    return _wdkModel;
  }
  
  /**
   * @return GUS_HOME web app parameter
   */
  protected String getGusHome() {
    ServletContext context = _servlet.getServletContext();
    String gusHomeBase = context.getInitParameter(Utilities.SYSTEM_PROPERTY_GUS_HOME);
    return context.getRealPath(gusHomeBase);
  }
  
  /**
   * Invalidates the current session and establishes a new one
   */
  protected void resetSession() {
    HttpSession session = _request.getSession();
    if (session != null) {
      session.invalidate();
    }
    // create new session
    _request.getSession(true);
  }
  
  /**
   * Returns the current user.  If no user is logged in and a guest user has
   * not yet been created, creates a guest user, adds it to the session, and
   * returns it.  This method should never return null; thus no request should
   * make it to handleParams() without having a user (guest or otherwise) on
   * the session.
   * 
   * @return the current user (logged in or guest)
   * @throws WdkModelException
   */
  protected UserBean getCurrentUser() throws WdkModelException {
    UserBean user = (UserBean)getSessionAttribute(CConstants.WDK_USER_KEY);
    // if guest is null, means the session is timed out; create the guest again
    if (user == null) {
      user = _wdkModel.getUserFactory().getGuestUser();
      setCurrentUser(user);
    }
    return user;
  }
  
  /**
   * Sets the current user.  This method should only be called by login and
   * logout operations.
   * 
   * @param user new user for this session
   */
  protected void setCurrentUser(UserBean user) {
    setSessionAttribute(CConstants.WDK_USER_KEY, user);
  }
  
  /**
   * Sets an attribute on the session
   * 
   * @param key name of the attribute
   * @param value value of the attribute
   */
  protected void setSessionAttribute(String key, Object value) {
    _request.getSession().setAttribute(key, value);
  }
  
  /**
   * Removes an attribute from the session
   * 
   * @param key name of the attribute to remove
   */
  protected void unsetSessionAttribute(String key) {
    _request.getSession().setAttribute(key, null);
  }
  
  /**
   * Retrieves an attribute from the session
   * 
   * @param key name of the attribute to retrieve
   * @return session attribute
   */
  protected Object getSessionAttribute(String key) {
    return _request.getSession().getAttribute(key);
  }
  
  /**
   * Adds HTTP cookie onto response
   * 
   * @param cookie cookie to add
   */
  public void addCookieToResponse(Cookie cookie) {
    _response.addCookie(cookie);
  }
  
  /**
   * Looks for a named resource within the web application and returns whether
   * it exists or not.
   * 
   * @param name path to the resource from web application root
   * @return true if resource exists, otherwise false
   */
  protected boolean wdkResourceExists(String name) {
    return ApplicationInitListener.resourceExists(name, _servlet.getServletContext());
  }
  
  /**
   * Returns requested response type.  This is a "global" parameter that may
   * or may not be honored by the child action; however, it is always available.
   * 
   * @return
   */
  protected ResponseType getRequestedResponseType() {
    String typeStr = _params.getValue(CConstants.WDK_RESPONSE_TYPE_KEY);
    try {
      return ResponseType.valueOf(typeStr);
    }
    catch (IllegalArgumentException iae) {
      return null;
    }
  }
    
  private Map<String, ParamDef> getExpectedParams() {
    // make a copy of the child-defined set of expected params
    Map<String, ParamDef> definedParams = new HashMap<>(getParamDefs());
    
    // now add params that we may expect from any request (i.e. global params)
    definedParams.put(AUTH_TICKET, new ParamDef(Required.OPTIONAL));
    definedParams.put(CConstants.WDK_RESPONSE_TYPE_KEY, new ParamDef(Required.OPTIONAL));
    
    return definedParams;
  }

  /**
   * Returns encapsulated request information
   * 
   * @return request data object
   */
  public RequestData getRequestData() {
    return new RequestData() {

      @Override
      public String getWebAppBaseUrl() {
        return new StringBuilder()
          .append(_request.getScheme())
          .append("://")
          .append(_request.getServerName())
          .append(_request.getServerPort() == 80 ||
                  _request.getServerPort() == 443 ?
                  "" : ":" + _request.getServerPort())
          .append(_request.getContextPath())
          .toString();
      }
      
      @Override
      public String getRequestUrl() {
        return _request.getRequestURL().toString();
      }
      
      @Override
      public String getQueryString() {
        return _request.getQueryString();
      }
      
      @Override
      public String getFullRequestUrl() {
        return _request.getRequestURL()
            .append(_request.getQueryString() == null ? "" : "?" + _request.getQueryString())
            .toString();
      }
      
      @Override
      public String getBrowser() {
        return _request.getHeader("User-Agent");
      }

      @Override
      public String getReferrer() {
        return _request.getHeader("Referer");
      }

      @Override
      public String getIpAddress() {
        return _request.getRemoteAddr();
      }
      
      @Override
      public Object getRequestAttribute(String key) {
        return _request.getAttribute(key);
      }

      @Override
      public String getRequestHeader(String key) {
        return _request.getHeader(key);
      }
    };
  }
  
  /**
   * Returns the standard location for custom JSP overrides.
   * TODO: this should not be needed.  Users of this software can write their
   * own JSPs and add them to struts config as desired.
   * 
   * @return
   */
  public static String getCustomViewDir() {
    return new StringBuilder()
      .append(CConstants.WDK_CUSTOM_VIEW_DIR)
      .append(File.separator)
      .append(CConstants.WDK_PAGES_DIR)
      .append(File.separator)
      .toString();
  }

  /**
   * Converts binary data into an input stream.  This can be used if the result
   * type is a stream, and the content to be returned already exists in memory
   * as a string.  This is simply a wrapper around the ByteArrayInputStream
   * constructor.
   * 
   * @param data data to be converted
   * @return stream representing the data
   */
  public static InputStream getStreamFromBytes(byte[] data) {
    return new ByteArrayInputStream(data);
  }
  
  /**
   * Converts a string into an open input stream.  This can be used if the
   * result type is a stream, and the content to be returned already exists in
   * memory as a string.
   * 
   * @param str string to be converted
   * @return input stream representing the string
   */
  public static InputStream getStreamFromString(String str) {
    return getStreamFromBytes(str.getBytes(Charset.defaultCharset()));
  }
  
  /**
   * Attempts to convert the given text into HTML, replacing special characters
   * with their HTML equivalents.
   * TODO: this method should be improved!
   * 
   * @param str string to convert
   * @return converted string
   */
  public static String escapeHtml(String str) {
    return str
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("&", "&amp;")
        .replaceAll("\n", "<br/>");
  }
}
