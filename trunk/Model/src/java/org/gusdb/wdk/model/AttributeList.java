/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represents the <attributeList> tag in <question>
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
        // ensure that the list includes primaryKey
        String primaryKey = RecordClass.PRIMARY_KEY_NAME;
        if (!(summaryList.equals(primaryKey)
                || summaryList.startsWith(primaryKey + ",")
                || summaryList.endsWith("," + primaryKey) || summaryList.indexOf(","
                + primaryKey + ",") > 0)) {
            summaryList = primaryKey + "," + summaryList;
        }
        this.summaryAttributeNames = summaryList.split(",\\s*");
    }

    public void setSorting(String sortList) {
        String[] attrCombines = sortList.split(",");
        for (String attrCombine : attrCombines) {
            String[] sorts = attrCombine.trim().split("\\s+");
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

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) {
    // no resource to release, do nothing
    }
}
