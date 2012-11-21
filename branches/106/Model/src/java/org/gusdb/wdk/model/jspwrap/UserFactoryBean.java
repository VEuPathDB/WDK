/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;
import org.json.JSONException;

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

    /**
     * 
     */
    public UserFactoryBean(UserFactory userFactory) {
        this.userFactory = userFactory;
    }

    public static String encrypt(String str) throws NoSuchAlgorithmException {
        return UserFactory.encrypt(str);
    }

    public static String md5(String str) throws NoSuchAlgorithmException {
        return UserFactory.md5(str);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserFactory#createGuestUser()
     */
    public UserBean getGuestUser() throws WdkModelException {
        return new UserBean(userFactory.createGuestUser());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserFactory#createUser(java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public UserBean createUser(String email, String lastName, String firstName,
            String middleName, String title, String organization,
            String department, String address, String city, String state,
            String zipCode, String phoneNumber, String country, String openId,
            Map<String, String> globalPreferences,
            Map<String, String> projectPreferences) throws WdkUserException,
            WdkModelException {
        User user = userFactory.createUser(email, lastName, firstName,
                middleName, title, organization, department, address, city,
                state, zipCode, phoneNumber, country, openId, globalPreferences,
                projectPreferences);
        return new UserBean(user);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserFactory#getDefaultRole()
     */
    public String getDefaultRole() {
        return userFactory.getDefaultRole();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserFactory#getProjectId()
     */
    public String getProjectId() {
        return userFactory.getProjectId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserFactory#authenticate(java.lang.String,
     * java.lang.String)
     */
    public UserBean login(UserBean guest, String email, String password)
            throws WdkModelException, WdkUserException,
            NoSuchAlgorithmException, SQLException, JSONException {
        User user = userFactory.login(guest.getUser(), email, password);
        return new UserBean(user);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserFactory#authenticate(java.lang.String,
     * java.lang.String)
     */
    public UserBean login(UserBean guest, String openid)
            throws WdkModelException, WdkUserException,
            NoSuchAlgorithmException, SQLException, JSONException {
        User user = userFactory.login(guest.getUser(), openid);
        if (user == null) return null;
        return new UserBean(user);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserFactory#sendEmail(java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String)
     */
    public void sendEmail(String email, String reply, String subject,
            String content) throws WdkUserException, WdkModelException  {
        Utilities.sendEmail(userFactory.getWdkModel(), email, reply, subject,
                content);
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

    /**
     * Looks up user by openId and returns a user bean.  If none can be
     * found, returns null.
     * 
     * @param openId open id to identify user
     * @return user bean, or null if user does not exist
     * @throws WdkModelException if error occurs
     */
    public UserBean getUserByOpenId(String openId) throws WdkModelException {
        User user = userFactory.getUserByOpenId(openId);
        if (user == null) return null;
        return new UserBean(user);
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
     * @throws WdkModelException
     * @see org.gusdb.wdk.model.user.UserFactory#loadUserBySignature(java.lang.String)
     */
    public UserBean getUser(String signature) throws WdkUserException,
            WdkModelException {
        User user = userFactory.getUser(signature);
        return new UserBean(user);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserFactory#loadUser(int)
     */
    public UserBean getUser(int userId) throws WdkUserException,
            WdkModelException, SQLException {
        User user = userFactory.getUser(userId);
        return new UserBean(user);
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public UserBean getUser() throws WdkUserException, WdkModelException {
        return (signature == null) ? null : getUser(signature);
    }
}
