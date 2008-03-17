/**
 * 
 */
package org.gusdb.wdk.model;

/**
 * @author Jerric
 * @created Jan 19, 2006
 */
public abstract class AttributeField extends Field {

    /**
     * although this object is in question (application) domain, but the value
     * is set when it is usedf the first time. The value is supposed to be set
     * by the first answer when it is null; if it's not, it will never be set
     * again.
     */
    protected Boolean sortable;
    protected String align;
    protected boolean nowrap;

    /**
     * 
     */
    public AttributeField() {
        super();
        nowrap = false;
    }

    /**
     * @return the sortable
     */
    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        // only set if user specify it as not sortable
        if (!sortable) this.sortable = new Boolean(false);
    }

    /**
     * @return the align
     */
    public String getAlign() {
        return align;
    }

    /**
     * @param align
     *            the align to set
     */
    public void setAlign(String align) {
        this.align = align;
    }

    /**
     * @return the nowrap
     */
    public boolean isNowrap() {
        return nowrap;
    }

    /**
     * @param nowrap
     *            the nowrap to set
     */
    public void setNowrap(boolean nowrap) {
        this.nowrap = nowrap;
    }
}
