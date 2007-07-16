/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Jerric
 * 
 */
public class PropertyList extends WdkModelBase {

    private String name;
    private Set<String> values;

    public PropertyList() {
        values = new LinkedHashSet<String>();
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name
     * the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public void addValue(String value) {
        values.add(value);
    }
    
    public String[] getValues() {
        String[] array = new String[values.size()];
        values.toArray(array);
        return array;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) {
        // nothing to do
    }
}
