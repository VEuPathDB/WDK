/**
 * 
 */
package org.gusdb.wdk.controller.form;

/**
 * @author xingao
 * 
 */
public class RegisterForm extends ProfileForm {

    /**
     * 
     */
    private static final long serialVersionUID = 7468913260780026018L;
    private String email;

    /**
     * @return Returns the email.
     */
    @Override
    public String getEmail() {
        return email;
    }

    /**
     * @param email
     *            The email to set.
     */
    @Override
    public void setEmail(String email) {
        this.email = email;
    }
}
