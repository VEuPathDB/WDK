package org.gusdb.wdk.service.service;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.UserBundle;
import org.gusdb.wdk.service.factory.WdkAnswerFactory;
import org.jfree.util.Log;

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

  private WdkModelBean _wdkModelBean;
  private WdkAnswerFactory _resultFactory;

  protected WdkModelBean getWdkModelBean() {
    return _wdkModelBean;
  }

  protected WdkModel getWdkModel() {
    return _wdkModelBean.getModel();
  }

  protected UriInfo getUriInfo() {
    return _uriInfo;
  }

  protected HttpSession getSession() {
    return _request.getSession();
  }

  protected UserBean getSessionUserBean() {
    return ((UserBean)_request.getSession().getAttribute("wdkUser"));
  }

  protected int getSessionUserId() throws WdkModelException {
    return getSessionUserBean().getUserId();
  }

  protected User getSessionUser() {
    return getSessionUserBean().getUser();
  }

  protected boolean isSessionUserAdmin() throws WdkModelException {
    List<String> adminEmails = getWdkModel().getModelConfig().getAdminEmails();
    return adminEmails.contains(getSessionUser().getEmail());
  }

  protected void assertAdmin() throws WdkModelException {
    if (!isSessionUserAdmin()) {
      throw new ForbiddenException("Administrative access is required for this function.");
    }
  }

  protected WdkAnswerFactory getResultFactory() {
    if (_resultFactory == null) {
      _resultFactory = new WdkAnswerFactory(getSessionUserBean());
    }
    return _resultFactory;
  }

  @Context
  protected void setServletContext(ServletContext context) {
    _wdkModelBean = ((WdkModelBean)context.getAttribute("wdkModel"));
  }

  /**
   * Returns an unboxed version of the passed value or the default
   * boolean flag value (false) if the passed value is null.
   * 
   * @param boolValue flag value passed to service
   * @return unboxed value or false if null
   */
  protected boolean getFlag(Boolean boolValue) {
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
  protected boolean getFlag(Boolean boolValue, boolean defaultValue) {
    return (boolValue == null ? defaultValue : boolValue);
  }

  /**
   * Returns a session-aware user bundle based on the input string.
   * 
   * @param userIdStr potential target user ID as string, or special string 'current' indicating session user
   * @return user bundle describing status of the requested user string
   * @throws WdkModelException if error occurs while accessing user data (probably a DB problem)
   */
  protected UserBundle parseTargetUserId(String userIdStr) throws WdkModelException {
    return UserBundle.createFromTargetId(userIdStr, getSessionUser(), getWdkModel().getUserFactory());
  }
}
