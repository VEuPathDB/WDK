/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: xingao
 * @created: Mar 1, 2007
 * @updated: Mar 1, 2007
 */
public class Group extends WdkModelBase {

    private static Group empty;

    private String name;
    private String displayName;

    private List<WdkModelText> descriptions;
    private String description;

    private String displayType;
    private boolean visible;

    private GroupSet groupSet;

    public synchronized static Group Empty() {
        if (empty == null) {
            empty = new Group();
            empty.displayType = "empty";
        }
        return empty;
    }

    public Group() {
        // initialize an empty group
        name = "empty";
        displayName = "";
        description = "";
        descriptions = new ArrayList<WdkModelText>();
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void addDescription(WdkModelText description) {
        this.descriptions.add(description);
    }

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName
     *            the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the groupSet
     */
    public GroupSet getGroupSet() {
        return groupSet;
    }

    /**
     * @param groupSet
     *            the groupSet to set
     */
    public void setGroupSet(GroupSet groupSet) {
        this.groupSet = groupSet;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        if (groupSet != null) return groupSet.getName() + "." + name;
        else return name;
    }

    /**
     * @return the displayType
     */
    public String getDisplayType() {
        return displayType;
    }

    /**
     * @param displayType
     *            the displayType to set
     */
    public void setDisplayType(String displayType) {
        this.displayType = displayType;
    }

    public void resolveReferences(WdkModel model) throws WdkModelException {
    // do nothing
    }

    public void setResources(WdkModel model) throws WdkModelException {
    // do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude descriptions
        boolean hasDescription = false;
        for (WdkModelText description : descriptions) {
            if (description.include(projectId)) {
                if (hasDescription) {
                    throw new WdkModelException("The group " + getFullName()
                            + " has more than one description for project "
                            + projectId);
                } else {
                    this.description = description.getText();
                    hasDescription = true;
                }
            }
        }
        descriptions = null;
    }

    /**
     * @return the visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * @param visible
     *            the visible to set
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
