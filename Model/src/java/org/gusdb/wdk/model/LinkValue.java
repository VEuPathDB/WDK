package org.gusdb.wdk.model;

/**
 * A value representing a hyperlink
 */
public class LinkValue {

    String visible;
    String url;
    LinkAttributeField field;
    private boolean inSummary;

    public LinkValue(String visible, String url, LinkAttributeField field) {
        this.visible = visible;
        this.url = url;
        this.field = field;
        inSummary = false;
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.Field#getDisplayName()
     */
    public String getDisplayName() {
        return this.field.getDisplayName();
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.Field#getName()
     */
    public String getName() {
        return this.field.getName();
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.Field#getInReportMaker()
     */
    public boolean getInReportMaker() {
        return this.field.getInReportMaker();
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.Field#getHelp()
     */
    public String getHelp() {
        return this.field.getHelp();
    }

    public String getVisible() {
	return visible;
    }

    public boolean getInternal() {
	return field.getInternal();
    }

    public String getUrl() {
	return url;
    }

    /**
     * @return Returns the inSummary.
     */
    public boolean getInSummary() {
        return this.inSummary;
    }

    /**
     * @param inSummary The inSummary to set.
     */
    void setInSummary(boolean inSummary) {
        this.inSummary = inSummary;
    }
    
    public String getValue() {
        return getVisible() + "(" + getUrl() + ")";
    }
    
    public String toString() {
        String newline = System.getProperty("line.separator");
        String classnm = this.getClass().getName();
        StringBuffer buf = new StringBuffer(classnm + ": name = '" + getName()
                + "'" + newline + "  displayName = '" + getDisplayName() + "'"
                + newline + "  help = '" + getHelp() + "'" + newline
                + "  inSummary? = '" + getInSummary() + "'" + newline
                + "  visible = '" + getVisible() + "'" + newline + "  url = "
                + getUrl() + newline);
        return buf.toString();
    }
}
