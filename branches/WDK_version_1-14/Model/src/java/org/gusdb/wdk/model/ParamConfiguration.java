/**
 * @description
 */
package org.gusdb.wdk.model;

/**
 * @author Jerric
 * @created Jul 5, 2007
 * @modified Jul 5, 2007
 */
public class ParamConfiguration extends WdkModelBase {

    private boolean value;

    /**
     * @return the value
     */
    public boolean isValue() {
        return this.value;
    }

    /**
     * @param value
     *        the value to set
     */
    public void setValue(boolean value) {
        this.value = value;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) {
        // do nothing
    }
}
