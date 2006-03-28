/**
 * 
 */
package org.gusdb.wdk.model;

/**
 * @author  Jerric
 * @created Feb 16, 2006
 */
public class ParamReference extends Reference {

    private String defaultValue;
    
    /**
     * 
     */
    public ParamReference() {
        super();
        // TODO Auto-generated constructor stub
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
     * @param defaultValue The defaultValue to set.
     */
    public void setDefault(String defaultValue) {
        this.defaultValue = defaultValue;
    }

}
