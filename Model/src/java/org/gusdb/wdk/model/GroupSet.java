/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: xingao
 * @created: Mar 1, 2007
 * @updated: Mar 1, 2007
 */
public class GroupSet extends WdkModelBase implements ModelSetI {

    private String name;
    private List<Group> groupList = new ArrayList<Group>();
    private Map<String, Group> groupMap = new LinkedHashMap<String, Group>();

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.ModelSetI#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     * the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public Group getGroup(String name) throws WdkModelException {
        Group group = groupMap.get(name);
        if (group == null)
            throw new WdkModelException("Group Set " + getName()
                    + " does not include group " + name);
        return group;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.ModelSetI#getElement(java.lang.String)
     */
    public Object getElement(String elementName) {
        return groupMap.get(elementName);
    }

    public Group[] getGroups() {
        Group[] array = new Group[groupMap.size()];
        groupMap.values().toArray(array);
        return array;
    }

    public void addGroup(Group group) throws WdkModelException {
        groupList.add(group);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.ModelSetI#resolveReferences(org.gusdb.wdk.model.WdkModel)
     */
    public void resolveReferences(WdkModel model) throws WdkModelException {
        for (Group group : groupMap.values()) {
            group.resolveReferences(model);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.ModelSetI#setResources(org.gusdb.wdk.model.WdkModel)
     */
    public void setResources(WdkModel model) throws WdkModelException {
        for (Group group : groupMap.values()) {
            group.setResources(model);
        }
    }

    public String toString() {
        String newline = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer("GroupSet: name='" + name + "'");
        buf.append(newline);
        for (Group group : groupMap.values()) {
            buf.append(newline);
            buf.append(":::::::::::::::::::::::::::::::::::::::::::::");
            buf.append(newline);
            buf.append(group).append(newline);
        }

        return buf.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
     */
    @Override
    public void excludeResources(String projectId) throws WdkModelException {
        // exclude groups
        for (Group group : groupList) {
            if (group.include(projectId)) {
                group.excludeResources(projectId);
                String groupName = group.getName();
                if (groupMap.containsKey(groupName))
                    throw new WdkModelException("Group named " + groupName
                            + " already exists in group set " + name);
                group.setGroupSet(this);
                groupMap.put(groupName, group);
            }
        }
        groupList = null;
    }
}
