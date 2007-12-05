/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
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

    /**
     * 
     */
    public UserFactoryBean(UserFactory userFactory) {
        this.userFactory = userFactory;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserFactory#createGuestUser()
     */
    public UserBean getGuestUser() throws WdkUserException, WdkModelException {
        return new UserBean(userFactory.createGuestUser());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserFactory#createUser(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    public UserBean createUser(String email, String lastName, String firstName,
            String middleName, String title, String organization,
            String department, String address, String city, String state,
            String zipCode, String phoneNumber, String country,
            Map<String, String> globalPreferences,
            Map<String, String> projectPreferences) throws WdkUserException,
            WdkModelException {
        User user = userFactory.createUser(email, lastName, firstName,
                middleName, title, organization, department, address, city,
                state, zipCode, phoneNumber, country, globalPreferences,
                projectPreferences);
        return new UserBean(user);
    }


    public UserBean createUserNoPass(String email, String lastName, String firstName)
	throws WdkUserException,WdkModelException {
        User user = userFactory.createUser(email, lastName, firstName,
                null, null, null, null, null, null,
                null, null, null, null, null,
                null, false);
        return new UserBean(user);
    }

    public UserBean createUserNoPass(String email, String lastName, String firstName,
            String middleName, String title, String organization,
            String department, String address, String city, String state,
            String zipCode, String phoneNumber, String country,
            Map<String, String> globalPreferences,
            Map<String, String> projectPreferences) throws WdkUserException,
            WdkModelException {
        User user = userFactory.createUser(email, lastName, firstName,
                middleName, title, organization, department, address, city,
                state, zipCode, phoneNumber, country, globalPreferences,
                projectPreferences, false);
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
     *      java.lang.String)
     */
    public UserBean login(UserBean guest, String email, String password)
            throws WdkModelException, WdkUserException {
        User user = userFactory.login(guest.getUser(), email, password);
        return new UserBean(user);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserFactory#sendEmail(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    public void sendEmail(String email, String reply, String subject,
            String content) throws WdkUserException {
        userFactory.sendEmail(email, reply, subject, content);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserFactory#resetPassword(org.gusdb.wdk.model.user.User)
     */
    public void resetPassword(String email) throws WdkUserException,
            WdkModelException {
        userFactory.resetPassword(email);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserFactory#loadUser(java.lang.String)
     */
    public UserBean loadUser(String email) throws WdkModelException,
            WdkUserException {
        User user = userFactory.loadUser(email);
        return new UserBean(user);
    }

    /**
     * @param signature
     * @return
     * @throws WdkUserException
     * @throws WdkModelException
     * @see org.gusdb.wdk.model.user.UserFactory#loadUserBySignature(java.lang.String)
     */
    public UserBean loadUserBySignature( String signature ) throws WdkUserException, WdkModelException {
        User user = userFactory.loadUserBySignature( signature );
        return new UserBean(user);
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.user.UserFactory#loadUser(int)
     */
    public UserBean loadUser(int userId) throws WdkUserException, WdkModelException {
        User user = userFactory.loadUser(userId);
        return new UserBean(user);
    }
    
}
