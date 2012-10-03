package org.gusdb.wdk.controller.actionutil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

public abstract class WdkAction implements SecondaryValidator {

	private static final Logger LOG = Logger.getLogger(WdkAction.class.getName());

	// global response strings (i.e. named action forwards)
	public static final String SUCCESS = "success";
	public static final String INPUT = "input";
	public static final String NEEDS_LOGIN = "needs_login";
	public static final String ERROR = "application_error";
	
	// provide empty param map for actions expecting no params
	protected static final Map<String, ParamDef> EMPTY_PARAMS = new HashMap<>();
	
	// accessors for exception information if error is thrown
	public static final String EXCEPTION_PAGE = "exceptionPage";
	public static final String EXCEPTION_USER = "exceptionUser";
	public static final String EXCEPTION_OBJ = "exceptionObj";
	
	public static interface RequestData {
	  public String getRequestUrl();
	  public String getQueryString();
	  public String getFullRequestUrl();
	  public String getBrowser();
	  public String getReferrer();
	  public String getIpAddress();
	  public Object getRequestAttribute(String key);
	}
	
	private WdkModelBean _wdkModel;
	private HttpServlet _servlet;
	private HttpServletRequest _request;
	private HttpServletResponse _response;
	private ParamGroup _params;

  protected abstract ResponseType getResponseType();
  protected abstract boolean shouldValidateParams();
	protected abstract Map<String, ParamDef> getParamDefs();
	protected abstract ActionResult handleRequest(ParamGroup params) throws Exception;

	public final ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response, HttpServlet servlet)
			throws Exception {
		try {
			_request = request;
			_response = response;
			_servlet = servlet;
			_wdkModel = (WdkModelBean)_servlet.getServletContext().getAttribute(CConstants.WDK_MODEL_KEY);
			
			if (requiresLogin() && getCurrentUser().isGuest()) {
				return getForwardFromResult(new ActionResult().setViewName(NEEDS_LOGIN), mapping);
			}
			
			ActionResult result;
			try {
			  _params = createParamGroup(request.getParameterMap());
        _response.addHeader("Content-Type", getResponseType().getMimeType());
        result = handleRequest(_params);
			}
			catch (WdkValidationException wve) {
			  // attach errors to request and return INPUT
			  return getForwardFromResult(new ActionResult()
			      .setRequestAttribute("validator", wve.getValidator())
			      .setViewName(INPUT), mapping);
			}

			if (result == null || result.isEmptyResult()) {
			  return null;
			}
			else if (result.isStream()) {
			  // handle stream response
			  if (result.getFileName().isEmpty()) {
			    result.setFileName(getResponseType().getDefaultFileName());
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
  private ParamGroup createParamGroup(Map parameterMap) throws WdkValidationException {
	  ParamGroup params;
	  @SuppressWarnings("unchecked")
    Map<String, String[]> parameters = new HashMap<>((Map<String, String[]>)parameterMap);
    if (shouldValidateParams()) {
      ParameterValidator validator = new ParameterValidator();
      params = validator.validateParameters(getExpectedParams(), parameters, this);
    }
    else {
      params = buildParamGroup(parameters);
    }
    return params;
  }

	public static void transferStream(OutputStream outputStream, InputStream inputStream)
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
	
  private ParamGroup buildParamGroup(Map<String, String[]> parameters) {
		Map<String, ParamDef> definitions = new HashMap<String, ParamDef>();
		for (String key : parameters.keySet()) {
			String[] values = parameters.get(key);
			definitions.put(key, new ParamDef(Required.REQUIRED,
					values.length > 1 ? Count.MULTIPLE : Count.SINGULAR));
		}
		return new ParamGroup(definitions, parameters);
	}
	
	protected boolean requiresLogin() {
		return false;
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
	
	protected String getWebServerRoot() {
		int port = _request.getServerPort();
		return "http://" + _request.getServerName() +
				(port == 80 ? "" : ":" + port) + _request.getContextPath();
	}
	
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
	
  protected ParamGroup getParams() {
		return _params;
	}
	
	protected String getRequestHeader(String key) {
		return _request.getHeader(key);
	}
	
	protected WdkModelBean getWdkModel() {
		return _wdkModel;
	}
	
	protected UserBean getCurrentUser() throws WdkModelException {
		UserBean user = (UserBean)getSessionAttribute(CConstants.WDK_USER_KEY);
		// if guest is null, means the session is timed out; create the guest again
		if (user == null) {
			user = _wdkModel.getUserFactory().getGuestUser();
			setCurrentUser(user);
		}
		return user;
	}
	
	protected void setCurrentUser(UserBean user) {
		setSessionAttribute(CConstants.WDK_USER_KEY, user);
	}
	
	protected void setSessionAttribute(String key, Object value) {
		_request.getSession().setAttribute(key, value);
	}
	
	protected void unsetSessionAttribute(String key) {
		_request.getSession().setAttribute(key, null);
	}
	
	protected Object getSessionAttribute(String key) {
		return _request.getSession().getAttribute(key);
	}
	
	public void addCookieToResponse(Cookie cookie) {
		_response.addCookie(cookie);
	}
	
	protected boolean wdkResourceExists(String name) {
		return ApplicationInitListener.resourceExists(name, _servlet.getServletContext());
	}
	
	protected ResponseType getRequestedResponseType() {
		String typeStr = _params.getValue(CConstants.WDK_RESPONSE_TYPE_KEY);
		try {
			return ResponseType.valueOf(typeStr);
		}
		catch (IllegalArgumentException iae) {
			return getResponseType();
		}
	}
		
	private Map<String, ParamDef> getExpectedParams() {
		// make a copy of the child-defined set of expected params
		Map<String, ParamDef> definedParams = new HashMap<>(getParamDefs());
		
		// now add params that we may expect from any request
		definedParams.put(CConstants.WDK_RESPONSE_TYPE_KEY,
				new ParamDef(Required.OPTIONAL, Count.SINGULAR, DataType.STRING, new String[]{ getResponseType().toString() }));
		
		return definedParams;
	}

	public RequestData getRequestData() {
    return new RequestData() {

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
    };
	}
	
	public static String getCustomViewDir() {
	  return new StringBuilder()
	    .append(CConstants.WDK_CUSTOM_VIEW_DIR)
	    .append(File.separator)
	    .append(CConstants.WDK_PAGES_DIR)
	    .append(File.separator)
	    .toString();
	}

  public static InputStream getStreamFromBytes(byte[] data) {
    return new ByteArrayInputStream(data);
  }
  
	public static InputStream getStreamFromString(String str) {
	  return getStreamFromBytes(str.getBytes(Charset.defaultCharset()));
	}
	
	public static String escapeHtml(String str) {
	  return str
	      .replaceAll("<", "&lt;")
	      .replaceAll(">", "&gt;")
	      .replaceAll("&", "&amp;")
	      .replaceAll("\n", "<br/>");
	}
}
