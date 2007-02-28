/**
 * 
 */
package org.gusdb.wdk.model;

/**
 * @author Jerric
 * @created Jan 19, 2006
 */
public abstract class AttributeField extends Field {

    protected boolean sortable;

    /**
     * 
     */
    public AttributeField() {
        super();
    }

    /**
     * @return the sortable
     */
    boolean isSortable() {
        return sortable;
    }

    /**
     * @param sortable
     *            the sortable to set
     */
    void setSortable(boolean sortable) {
        this.sortable = sortable;
    }
}
