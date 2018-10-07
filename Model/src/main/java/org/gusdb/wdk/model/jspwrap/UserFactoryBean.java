package org.gusdb.wdk.model.jspwrap;

import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.UnregisteredUser.UnregisteredUserType;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;

/**
 * @author: Jerric
 * @created: May 25, 2006
 * @modified by: Jerric
 * @modified at: May 25, 2006
 * 
 */
public class UserFactoryBean {

    private final UserFactory _userFactory;
    private final WdkModel _wdkModel;

    // set and gotten during a request to refer to a single user within this factory bean
    //   RRD: don't really like this; should be removed when we ditch most of the beans and JSP
    private volatile String _signature;

    public UserFactoryBean(WdkModel wdkModel, UserFactory userFactory) {
        _userFactory = userFactory;
        _wdkModel = wdkModel;
    }

    public UserBean getGuestUser() {
        return new UserBean(_wdkModel.getUserFactory().createUnregistedUser(UnregisteredUserType.GUEST));
    }

    public UserBean createUser(String email, 
            Map<String, String> profileProperties,
            Map<String, String> globalPreferences,
            Map<String, String> projectPreferences) throws WdkModelException, WdkUserException {
        User user = _userFactory.createUser(email, profileProperties,
            globalPreferences, projectPreferences);
        return new UserBean(user);
    }

    public UserBean createUser(String email, 
            Map<String, String> profileProperties,
            Map<String, String> globalPreferences,
            Map<String, String> projectPreferences, boolean resetPw) throws WdkModelException, WdkUserException {
        User user = _userFactory.createUser(email, profileProperties,
            globalPreferences, projectPreferences, resetPw);
        return new UserBean(user);
    }

    public UserBean login(UserBean guest, String email, String password)
            throws WdkModelException, WdkUserException {
        User user = _userFactory.login(guest.getUser(), email, password);
        return new UserBean(user);
    }

    public UserBean login(UserBean guest, long userId)
        throws WdkModelException, WdkUserException {
      User user = _userFactory.login(guest.getUser(), userId);
      if (user == null) return null;
      return new UserBean(user);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.user.UserFactory#resetPassword(org.gusdb.wdk.model
     * .user.User)
     */
    public void resetPassword(String email) throws WdkUserException, WdkModelException {
        _userFactory.resetPassword(email);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserFactory#loadUser(java.lang.String)
     */
    public UserBean getUserByEmail(String email) throws WdkModelException,
            WdkUserException {
        User user = _userFactory.getUserByEmail(email);
        if (user == null) {
          throw new WdkUserException("Cannot find user with email: " + email);
        }
        return new UserBean(user);
    }

    /**
     * @param signature
     * @return
     * @throws WdkUserException 
     * @see org.gusdb.wdk.model.user.UserFactory#loadUserBySignature(java.lang.String)
     */
    public UserBean getUser(String signature) throws WdkModelException, WdkUserException {
        User user = _userFactory.getUserBySignature(signature);
        return new UserBean(user);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserFactory#loadUser(int)
     */
    public UserBean getUser(int userId) throws WdkModelException {
        User user = _userFactory.getUserById(userId);
        return new UserBean(user);
    }

    public void setSignature(String signature) {
        _signature = signature;
    }

    public UserBean getUser() throws WdkModelException, WdkUserException {
        return (_signature == null) ? null : getUser(_signature);
    }

    public UserFactory getUserFactory() {
      return _userFactory;
    }
}
