/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represents the <attributeList> tag in <question>. It is used to
 * define summary attribute list and sorting attribute list.
 * 
 * @author Jerric
 * 
 */
public class AttributeList extends WdkModelBase {

    private String[] summaryAttributeNames;
    private Map<String, Boolean> sortingAttributeMap;

    public AttributeList() {
        sortingAttributeMap = new LinkedHashMap<String, Boolean>();
    }

    public void setSummary(String summaryList) {
        this.summaryAttributeNames = summaryList.split(",\\s*");
    }

    public void setSorting(String sortList) throws WdkModelException {
        String[] attrCombines = sortList.split(",");
        for (String attrCombine : attrCombines) {
            String[] sorts = attrCombine.trim().split("\\s+");
            if (sorts.length != 2)
                throw new WdkModelException("The sorting format is wrong: "
                        + sortList);
            String attrName = sorts[0].trim();
            String strAscend = sorts[1].trim().toLowerCase();
            boolean ascending = strAscend.equals("asc");
            if (!sortingAttributeMap.containsKey(attrName))
                sortingAttributeMap.put(attrName, ascending);
        }
    }

    /**
     * @return the sortingAttributeMap
     */
    public Map<String, Boolean> getSortingAttributeMap() {
        return this.sortingAttributeMap;
    }

    /**
     * @return the summaryAttributeNames
     */
    public String[] getSummaryAttributeNames() {
        return this.summaryAttributeNames;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) {
    // no resource to release, do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    @Override
    public void resolveReferences(WdkModel wodkModel) throws WdkModelException {
    // nothing to resolve.
    }
}
