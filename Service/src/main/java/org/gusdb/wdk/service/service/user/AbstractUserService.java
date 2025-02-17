package org.gusdb.wdk.service.service.user;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.UserBundle;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.service.AbstractWdkService;

@Path(AbstractUserService.NAMED_USER_PATH)
public abstract class AbstractUserService extends AbstractWdkService {

  private static final String NOT_LOGGED_IN = "You must log in to use this functionality.";

  // subclasses must read the following path param to gain access to requested user
  protected static final String USER_ID_PATH_PARAM = "userId";

  protected static final String USER_RESOURCE = "User ID ";
  public static final String STEP_RESOURCE = "Step ID ";

  protected static final String NAMED_USER_PATH = UserUtilityServices.USERS_PATH + "/{"+USER_ID_PATH_PARAM+"}";

  protected enum Access { PUBLIC, PRIVATE, ADMIN; }

  private final String _userIdStr;

  protected AbstractUserService(@PathParam(USER_ID_PATH_PARAM) String userIdStr) {
    _userIdStr = userIdStr;
  }

  protected boolean isUserIdSpecialString(String specialString) {
    return _userIdStr.equals(specialString);
  }

  /**
   * Ensures the target user exists and that the session user has the
   * permissions requested.  If either condition is not true, the appropriate
   * exception (corresponding to 404 and 403 respectively) is thrown.
   *
   * @param requestedAccess the access requested by the caller
   * @return a userBundle representing the target user and his relationship to the session user
   * @throws WdkModelException if error occurs creating user bundle (probably a DB problem)
   */
  public UserBundle getUserBundle(Access requestedAccess) throws WdkModelException {
    UserBundle userBundle = parseTargetUserId(_userIdStr);
    if (!userBundle.isValidTargetUserId()) {
      throw new NotFoundException(AbstractWdkService.formatNotFound(USER_RESOURCE + _userIdStr));
    }
    if ((!userBundle.isTargetRequestingUser() && Access.PRIVATE.equals(requestedAccess)) ||
        (!getRequestingUser().isAdmin() && Access.ADMIN.equals(requestedAccess))) {
      throw new ForbiddenException(AbstractWdkService.PERMISSION_DENIED);
    }
    return userBundle;
  }

  protected User getPrivateRegisteredUser() throws WdkModelException {
    UserBundle userBundle = getUserBundle(Access.PRIVATE);
    User user = userBundle.getRequestingUser();
    if (user.isGuest()) {
      throw new ForbiddenException(NOT_LOGGED_IN);
    }
    return user;
  }

  protected Step getStepForCurrentUser(long stepId, ValidationLevel level) throws WdkModelException {
    User user = getUserBundle(Access.PRIVATE).getRequestingUser();
    return new StepFactory(user)
        .getStepByIdAndUserId(
            stepId,
            user.getUserId(),
            level)
        .orElseThrow(
            () -> new NotFoundException(formatNotFound(STEP_RESOURCE + stepId)));
  }

  protected RunnableObj<Step> getRunnableStepForCurrentUser(long stepId) throws WdkModelException, DataValidationException {
    return getStepForCurrentUser(stepId, ValidationLevel.RUNNABLE)
        .getRunnable()
        .getOrThrow(step -> new DataValidationException(
            "This operation can only be performed on a runnable step. Please " +
            "revise your step and try again.  The following errors were found: " +
            FormatUtil.NL + step.getValidationBundle().toString(2)));
  }
}
