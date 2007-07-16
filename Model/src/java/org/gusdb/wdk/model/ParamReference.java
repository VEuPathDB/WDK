/**
 * 
 */
package org.gusdb.wdk.model;

import java.io.Serializable;

/**
 * @author Jerric
 * @created Feb 16, 2006
 */
public class ParamReference extends Reference implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7829729638618781482L;
    private String defaultValue;
    private Boolean allowEmpty;
    private Boolean multiPick;
    private Boolean useTermOnly;
    private String queryRef;

    public ParamReference() {}

    /**
     * @param twoPartName
     * @throws WdkModelException
     */
    public ParamReference(String twoPartName) throws WdkModelException {
        super(twoPartName);
        // TODO Auto-generated constructor stub
    }

    /**
     * @return Returns the defaultValue.
     */
    public String getDefault() {
        return this.defaultValue;
    }

    /**
     * @param defaultValue
     * The defaultValue to set.
     */
    public void setDefault(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return the allowEmpty
     */
    public Boolean isAllowEmpty() {
        return this.allowEmpty;
    }

    /**
     * @param allowEmpty
     * the allowEmpty to set
     */
    public void setAllowEmpty(boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
    }

    /**
     * @return the multiPick
     */
    public Boolean isMultiPick() {
        return this.multiPick;
    }

    /**
     * @param multiPick
     * the multiPick to set
     */
    public void setMultiPick(boolean multiPick) {
        this.multiPick = multiPick;
    }

    /**
     * @return the useTermOnly
     */
    public Boolean getUseTermOnly() {
        return this.useTermOnly;
    }

    /**
     * @param useTermOnly
     * the useTermOnly to set
     */
    public void setUseTermOnly(Boolean useTermOnly) {
        this.useTermOnly = useTermOnly;
    }

    /**
     * @return the queryRef
     */
    public String getQueryRef() {
        return this.queryRef;
    }

    /**
     * @param queryRef
     * the queryRef to set
     */
    public void setQueryRef(String queryRef) {
        this.queryRef = queryRef;
    }
}
