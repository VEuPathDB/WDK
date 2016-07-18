package org.gusdb.wdk.service.service.user;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.gusdb.wdk.service.UserBundle;
import org.gusdb.wdk.service.service.WdkService;

@Path("/user/{id}")
public abstract class UserService extends WdkService {

  // subclasses must read the following path param to gain access to requested user
  protected static final String USER_ID_PATH_PARAM = "id";

  private static final String USER_RESOURCE = "User ID ";

  protected static enum Access { PUBLIC, PRIVATE; }

  private final String _userIdStr;

  public UserService(@PathParam(USER_ID_PATH_PARAM) String userIdStr) {
    _userIdStr = userIdStr;
  }

  /**
   * Ensures the subject user exists and that the calling user has the
   * permissions requested.  If either condition is not true, the appropriate
   * exception (corresponding to 404 and 403 respectively) is thrown.
   * 
   * @param userIdStr id string of the subject user
   * @param requestedAccess the access requested by the caller
   * @return a userBundle representing that user
   */
  protected UserBundle getUserBundle(Access requestedAccess) {
    UserBundle userBundle = parseUserId(_userIdStr);
    if (!userBundle.isValidUserId())
      throw new NotFoundException(WdkService.formatNotFound(USER_RESOURCE + userBundle.getRequestedUserId()));
    if (!userBundle.isCurrentUser() && Access.PRIVATE.equals(requestedAccess))
      throw new ForbiddenException(WdkService.PERMISSION_DENIED);
    return userBundle;
  }
}
