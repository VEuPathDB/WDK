/**
 * 
 */
package org.gusdb.wdk.controller.action;

import org.apache.struts.action.ActionForm;


/**
 * @author xingao
 *
 */
public class PasswordForm extends ActionForm {

    /**
     * 
     */
    private static final long serialVersionUID = -1388072324939394202L;
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
    private String refererUrl;
    
    /**
     * @return Returns the confirmPassword.
     */
    public String getConfirmPassword() {
        return confirmPassword;
    }
    
    /**
     * @param confirmPassword The confirmPassword to set.
     */
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
    
    /**
     * @return Returns the newPassword.
     */
    public String getNewPassword() {
        return newPassword;
    }
    
    /**
     * @param newPassword The newPassword to set.
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
    
    /**
     * @return Returns the oldPassword.
     */
    public String getOldPassword() {
        return oldPassword;
    }
    
    /**
     * @param oldPassword The oldPassword to set.
     */
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
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
