package org.gusdb.wdk.model;

/**
 * Represents a reference in a wdk model.  Has a two-part name: set and element
 *
 * Created: Tue May 11 15:17:30 EDT 2004
 *
 * @author Steve Fischer
 * @version $Revision$ $Date: 2005-11-29 16:04:35 -0500 (Tue, 29 Nov 2005) $ $Author$
 */

public class Reference {

    private String setName;
    private String elementName;
    private String twoPartName;

    public Reference() {}

    /**
     * @param twoPartName Of the form "set.element"
     */
    public Reference(String twoPartName) throws WdkModelException{
        setRef(twoPartName);
    }
    
    public String getSetName(){
        return this.setName;
    }

    public String getElementName(){
        return this.elementName;
    }

    public String getTwoPartName() {
        return twoPartName;
    }
    
    /**
     * @param twoPartName Of the form "set.element"
     */
    public void setRef(String twoPartName) throws WdkModelException {
        
        if (twoPartName == null) {
            throw new WdkModelException("Error: twoPartName is null");
        }
        if (!twoPartName.matches("\\S+\\.\\S+")) {
            throw new WdkModelException("Error: Reference '" + twoPartName + "' is not in the form 'setName.elementName'");
        }
	    
        String[] parts = twoPartName.split("\\.");
        setName = parts[0];
        elementName = parts[1];
        this.twoPartName = twoPartName;
    }

    public String toString() {
        return "Reference: "+twoPartName;
    }
    
}
