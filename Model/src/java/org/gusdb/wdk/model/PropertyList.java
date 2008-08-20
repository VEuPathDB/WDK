/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jerric
 * 
 */
public class PropertyList extends WdkModelBase {

    private String name;
    private List<WdkModelText> valueTexts = new ArrayList<WdkModelText>();
    private Set<String> values = new LinkedHashSet<String>();

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name
     *                the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public void addValue(WdkModelText value) {
        valueTexts.add(value);
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
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude property values
        for (WdkModelText valueText : valueTexts) {
            if (valueText.include(projectId)) {
                valueText.excludeResources(projectId);
                String value = valueText.getText();
                if (values.contains(value)) {
                    throw new WdkModelException("The property value \"" + value
                            + "\" is included more than once in property "
                            + "list: " + name);
                } else {
                    values.add(value);
                }
            }
        }
        valueTexts = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wodkModel) throws WdkModelException {
    // do nothing
    }
}
