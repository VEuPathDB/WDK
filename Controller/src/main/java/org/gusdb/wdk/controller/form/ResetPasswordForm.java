/**
 * 
 */
package org.gusdb.wdk.controller.form;

import org.apache.struts.action.ActionForm;


/**
 * @author xingao
 *
 */
public class ResetPasswordForm extends ActionForm {

    /**
     * 
     */
    private static final long serialVersionUID = 6407388663355528016L;
    private String email;
    private String refererUrl;
    
    /**
     * @return Returns the email.
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * @param email The email to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * @return Returns the refererUrl.
     */
    public String getRefererUrl() {
        return refererUrl;
    }
    
    /**
     * @param refererUrl The refererUrl to set.
     */
    public void setRefererUrl(String refererUrl) {
        this.refererUrl = refererUrl;
    }
}
