/**
 * 
 */
package org.gusdb.wdk.model;

/**
 * @author xingao
 * 
 */
public class ModelConfigAppDB extends ModelConfigDB {

    private String userDbLink;

    /**
     * @return the userDbLink
     */
    public String getUserDbLink() {
        return userDbLink;
    }

    /**
     * @param userDbLink
     *            the userDbLink to set
     */
    public void setUserDbLink(String userDbLink) {
        if (userDbLink.length() > 0 && !userDbLink.startsWith("@"))
            userDbLink = "@" + userDbLink;
        this.userDbLink = userDbLink;
    }
}
