package org.gusdb.wdk.service.service.user;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.service.UserBundle;
import org.gusdb.wdk.service.service.WdkService;

@Path("/user/{id}")
public abstract class UserService extends WdkService {

  // subclasses must read the following path param to gain access to requested user
  protected static final String USER_ID_PATH_PARAM = "id";

  private static final String USER_RESOURCE = "User ID ";

  protected static enum Access { PUBLIC, PRIVATE, ADMIN; }

  private final String _userIdStr;

  public UserService(@PathParam(USER_ID_PATH_PARAM) String userIdStr) {
    _userIdStr = userIdStr;
  }

  /**
   * Ensures the target user exists and that the session user has the
   * permissions requested.  If either condition is not true, the appropriate
   * exception (corresponding to 404 and 403 respectively) is thrown.
   * 
   * @param userIdStr id string of the target user
   * @param requestedAccess the access requested by the caller
   * @return a userBundle representing the target user and his relationship to the session user
   * @throws WdkModelException if error occurs creating user bundle (probably a DB problem)
   */
  protected UserBundle getUserBundle(Access requestedAccess) throws WdkModelException {
    UserBundle userBundle = parseTargetUserId(_userIdStr);
    if (!userBundle.isValidUserId()) {
      throw new NotFoundException(WdkService.formatNotFound(USER_RESOURCE + userBundle.getTargetUserIdString()));
    }
    if ((!userBundle.isSessionUser() && Access.PRIVATE.equals(requestedAccess)) ||
        (!userBundle.isAdminSession() && Access.ADMIN.equals(requestedAccess))) {
      throw new ForbiddenException(WdkService.PERMISSION_DENIED);
    }
    return userBundle;
  }
}
