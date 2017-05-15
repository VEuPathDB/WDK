package org.gusdb.wdk.model.jspwrap;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.GuestUser;
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

    private UserFactory userFactory;

    private volatile String signature;

    public UserFactoryBean(UserFactory userFactory) {
        this.userFactory = userFactory;
    }

    public UserBean getGuestUser() {
        return new UserBean(new GuestUser(userFactory.getWdkModel()));
    }

    public UserBean createUser(String email, 
            Map<String, String> profileProperties,
            Map<String, String> globalPreferences,
            Map<String, String> projectPreferences) throws WdkModelException {
        User user = userFactory.createUser(email, profileProperties,
            globalPreferences, projectPreferences);
        return new UserBean(user);
    }

    public UserBean createUser(String email, 
            Map<String, String> profileProperties,
            Map<String, String> globalPreferences,
            Map<String, String> projectPreferences, boolean resetPw) throws WdkModelException {
        User user = userFactory.createUser(email, profileProperties,
            globalPreferences, projectPreferences, resetPw);
        return new UserBean(user);
    }

    public UserBean login(UserBean guest, String email, String password)
            throws WdkModelException, WdkUserException {
        User user = userFactory.login(guest.getUser(), email, password);
        return new UserBean(user);
    }

    public UserBean login(UserBean guest, long userId)
        throws WdkModelException, WdkUserException {
      User user = userFactory.login(guest.getUser(), userId);
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
        userFactory.resetPassword(email);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserFactory#loadUser(java.lang.String)
     */
    public UserBean getUserByEmail(String email) throws WdkModelException,
            WdkUserException {
        User user = userFactory.getUserByEmail(email);
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
        User user = userFactory.getUser(signature);
        return new UserBean(user);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserFactory#loadUser(int)
     */
    public UserBean getUser(int userId) throws WdkModelException {
        User user = userFactory.getUser(userId);
        return new UserBean(user);
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public UserBean getUser() throws WdkModelException, WdkUserException {
        return (signature == null) ? null : getUser(signature);
    }
}
