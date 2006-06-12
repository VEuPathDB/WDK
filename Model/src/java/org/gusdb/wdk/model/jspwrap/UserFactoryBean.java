/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

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
    public UserBean createGuestUser() {
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
            String zipCode, String phoneNumber, String country)
            throws WdkUserException, WdkModelException {
        User user = userFactory.createUser(email, lastName, firstName,
                middleName, title, organization, department, address, city,
                state, zipCode, phoneNumber, country);
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
    public UserBean authenticate(String email, String password)
            throws WdkModelException, WdkUserException {
        User user = userFactory.authenticate(email, password);
        return new UserBean(user);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserFactory#saveUser(org.gusdb.wdk.model.user.User)
     */
    public void saveUser(UserBean user) throws WdkModelException,
            WdkUserException {
        userFactory.saveUser(user.getUser());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserFactory#sendEmail(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    public void sendEmail(String email, String reply, String subject,
            String content) throws WdkModelException {
        userFactory.sendEmail(email, reply, subject, content);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.UserFactory#resetPassword(org.gusdb.wdk.model.user.User)
     */
    public void resetPassword(UserBean user) throws WdkUserException,
            WdkModelException {
        userFactory.resetPassword(user.getUser());
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
}
