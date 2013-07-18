package org.gusdb.wdk.model;

/**
 * Represents a reference in a wdk model.  Has a two-part name: set and element
 *
 * Created: Tue May 11 15:17:30 EDT 2004
 *
 * @author Steve Fischer
 * @version $Revision$ $Date$ $Author$
 */

public class Reference extends WdkModelBase {

    private String setName;
    private String elementName;
    private String twoPartName;
    private String groupRef;

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
    
    public void setGroupRef(String groupRef) throws WdkModelException {
        
        if (groupRef == null) {
            throw new WdkModelException("Error: twoPartName is null");
        }
        if (!groupRef.matches("\\S+\\.\\S+")) {
            throw new WdkModelException("Error: Group Reference '" + groupRef + "' is not in the form 'setName.elementName'");
        }
        
        this.groupRef = groupRef;
    }
    
    /**
     * @return the groupRef
     */
    public String getGroupRef() {
        return groupRef;
    }

    @Override
    public String toString() {
        return "Reference: "+twoPartName;
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // do nothing
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wodkModel) throws WdkModelException {
        // do nothing
    }
}
