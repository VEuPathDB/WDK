/**
 * 
 */
package org.gusdb.wdk.model;


/**
 * @author Jerric
 * @created Feb 16, 2006
 */
public class ParamReference extends Reference {

    /**
     * 
     */
    private static final long serialVersionUID = -7829729638618781482L;
    private String defaultValue;
    private Boolean allowEmpty;
    private Boolean multiPick;
    private Boolean useTermOnly;
    private String queryRef;
    private Boolean quote;
    private String emptyValue;
    private String displayType;
    private Boolean visible;

    public ParamReference() {
    }

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
     *            The defaultValue to set.
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
     *            the allowEmpty to set
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
     *            the multiPick to set
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
     *            the useTermOnly to set
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
     *            the queryRef to set
     */
    public void setQueryRef(String queryRef) {
        this.queryRef = queryRef;
    }

    /**
     * @return the quote
     */
    public Boolean getQuote() {
        return quote;
    }

    /**
     * @param quote
     *            the quote to set
     */
    public void setQuote(Boolean quote) {
        this.quote = quote;
    }

    public String getEmptyValue() {
        return emptyValue;
    }

    public void setEmptyValue(String emptyValue) {
        this.emptyValue = emptyValue;
    }

    /**
     * @return the displayType
     */
    public String getDisplayType() {
        return displayType;
    }

    /**
     * @param displayType the displayType to set
     */
    public void setDisplayType(String displayType) {
        this.displayType = displayType;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
}
