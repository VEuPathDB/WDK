/**
 * 
 */
package org.gusdb.wdk.controller.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * @author xingao
 * 
 */
public class LoginForm extends ActionForm {

    /**
     * 
     */
    private static final long serialVersionUID = -5586154119449411209L;
    private String email;
    private String password;
    private String refererUrl;

    /**
     * @return Returns the email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email
     *            The email to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return Returns the password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password
     *            The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return Returns the refererUrl.
     */
    public String getRefererUrl() {
        return refererUrl;
    }

    /**
     * @param refererUrl
     *            The refererUrl to set.
     */
    public void setRefererUrl(String refererUrl) {
        this.refererUrl = refererUrl;
    }

    public ActionErrors validate(ActionMapping mapping,
            HttpServletRequest request) {
      throw new NullPointerException("Test exception");
    }
}
