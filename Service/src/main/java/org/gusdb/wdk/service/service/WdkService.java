package org.gusdb.wdk.service.service;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.factory.WdkAnswerFactory;

public abstract class WdkService {

  protected static final Response getBadRequestBodyResponse(String message) {
    return Response.status(Status.BAD_REQUEST).entity(
        "Improperly formatted or incomplete request body: " + message).build();
  }

  protected static final Response getPermissionDeniedResponse() {
    return Response.status(Status.FORBIDDEN).entity(
        "Permission Denied.  You do not have access to this resource.").build();
  }

  protected static final Response getNotFoundResponse(String resourceName) {
    return Response.status(Status.NOT_FOUND).entity(
        "Resource specified [" + resourceName + "] does not exist.").build();
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

  protected UserBean getCurrentUserBean() {
    return ((UserBean)_request.getSession().getAttribute("wdkUser"));
  }
  
  protected int getCurrentUserId() throws WdkModelException {
    return getCurrentUserBean().getUserId();
  }

  protected User getCurrentUser() {
    return getCurrentUserBean().getUser();
  }

  protected WdkAnswerFactory getResultFactory() {
    if (_resultFactory == null) {
      _resultFactory = new WdkAnswerFactory(getCurrentUserBean());
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
}
