package org.gusdb.gus.wdk.model;

/**
 * Represents a reference in a wdk model.  Has a two-part name: set and element
 *
 * Created: Tue May 11 15:17:30 EDT 2004
 *
 * @author Steve Fischer
 * @version $Revision$ $Date$ $Author$
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
        setTwoPartName(twoPartName);
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
    public void setTwoPartName(String twoPartName) throws WdkModelException {
        
        if (!twoPartName.matches("\\w+\\.\\w+")) {
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
