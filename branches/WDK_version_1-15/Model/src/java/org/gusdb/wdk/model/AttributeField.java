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
    
    /**
     * 
     */
    public AttributeField( ) {
        super();
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
}
