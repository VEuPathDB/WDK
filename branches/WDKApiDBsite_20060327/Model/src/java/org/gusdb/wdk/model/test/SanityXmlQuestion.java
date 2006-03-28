package org.gusdb.wdk.model.test;

import org.gusdb.wdk.model.WdkModelException;

/**
 * Object used in running a sanity test; represents a query or question in a wdk
 * model.
 * 
 * Created: Mon August 23 12:00:00 2004 EDT
 * 
 * @author David Barkan
 * @version $Revision$ $Date: 2004-10-28 13:46:23 -0400 (Thu, 28 Oct 2004)
 *          $Author$
 */

public class SanityXmlQuestion implements SanityElementI {

    private String ref;
    private int pageStart;
    private int pageEnd;
    private String type;
    private int minOutputLength;
    private int maxOutputLength;
    private String xmlData;

    public SanityXmlQuestion() {}

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.test.SanityElementI#getCommand(java.lang.String)
     */
    public String getCommand(String globalArgs) throws WdkModelException {
        StringBuffer sb = new StringBuffer("wdkXmlQuestion ");
        sb.append(globalArgs);
        sb.append(" -question " + ref);
        sb.append(" -rows " + pageStart + " " + pageEnd);
        if (xmlData != null) sb.append(" -xmlData " + xmlData);
        return sb.toString();
    }

    /**
     * @return Returns the maxOutputLength.
     */
    public int getMaxOutputLength() {
        return this.maxOutputLength;
    }

    /**
     * @param maxOutputLength The maxOutputLength to set.
     */
    public void setMaxOutputLength(int maxOutputLength) {
        this.maxOutputLength = maxOutputLength;
    }

    /**
     * @return Returns the minOutputLength.
     */
    public int getMinOutputLength() {
        return this.minOutputLength;
    }

    /**
     * @param minOutputLength The minOutputLength to set.
     */
    public void setMinOutputLength(int minOutputLength) {
        this.minOutputLength = minOutputLength;
    }

    /**
     * @return Returns the pageEnd.
     */
    public int getPageEnd() {
        return this.pageEnd;
    }

    /**
     * @param pageEnd The pageEnd to set.
     */
    public void setPageEnd(int pageEnd) {
        this.pageEnd = pageEnd;
    }

    /**
     * @return Returns the pageStart.
     */
    public int getPageStart() {
        return this.pageStart;
    }

    /**
     * @param pageStart The pageStart to set.
     */
    public void setPageStart(int pageStart) {
        this.pageStart = pageStart;
    }

    /**
     * @return Returns the ref.
     */
    public String getRef() {
        return this.ref;
    }

    /**
     * @param ref The ref to set.
     */
    public void setRef(String ref) {
        this.ref = ref;
    }

    /**
     * @return Returns the type.
     */
    public String getType() {
        return this.type;
    }

    /**
     * @param type The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return Returns the xmlData.
     */
    public String getXmlData() {
        return this.xmlData;
    }

    /**
     * @param xmlData The xmlData to set.
     */
    public void setXmlData(String xmlData) {
        this.xmlData = xmlData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.test.SanityElementI#getName()
     */
    public String getName() {
        return ref;
    }
}
