package org.gusdb.wdk.service.service;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Tuples.ThreeTuple;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.factory.WdkAnswerFactory;

public abstract class WdkService {

  private static final Logger LOG = Logger.getLogger(WdkService.class);

  public static final String CURRENT_USER_MAGIC_STRING = "current";

  public static class UserBundle extends ThreeTuple<Integer,Boolean,User> {
    public UserBundle(Integer userId, Boolean isCurrentUser, User user) {
      super(userId, isCurrentUser, user);
    }
    public int getUserId() { return getFirst(); }
    public boolean isCurrentUser() { return getSecond(); }
    public User getUser() { return getThird(); }
  }

  protected static final Response getBadRequestBodyResponse(String message) {
    throw new BadRequestException(message);
  }

  protected static final Response getPermissionDeniedResponse() {
    throw new ForbiddenException("Permission Denied.  You do not have access to this resource.");
  }
  
  protected static final Response getNotFoundResponse(String resourceName) {
    throw new NotFoundException("Resource specified [" + resourceName + "] does not exist.");
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

  /**
   * Returns a tuple of <user_id, is_current_user, user> based on the input string.
   * 
   * @param userIdStr potential user ID as string, or special string indicating current user
   * @return Tuple of user information, or null if userIdStr is null or misformatted, or no user by the passed ID could be found
   */
  protected UserBundle parseUserId(String userIdStr) {
    try {
      User currentUser = getCurrentUser();
      if (CURRENT_USER_MAGIC_STRING.equals(userIdStr)) {
        return new UserBundle(currentUser.getUserId(), true, currentUser);
      }
      int userId = Integer.parseInt(userIdStr);
      if (userId == currentUser.getUserId()) {
        return new UserBundle(currentUser.getUserId(), true, currentUser);
      }
      User user = getWdkModel().getUserFactory().getUser(userId);
      return new UserBundle(userId, false, user);
    }
    catch (WdkModelException | NumberFormatException | NullPointerException e) {
      LOG.warn("User requested by ID that is misformatted or does not exist", e);
      return null;
    }
  }
}
