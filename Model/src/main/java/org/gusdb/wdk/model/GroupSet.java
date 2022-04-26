package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A model set used to contain {@code <group>} tags.
 * 
 * @author Jerric
 */
public class GroupSet extends WdkModelBase implements ModelSetI<Group> {

  private String _name;
  private List<Group> _groupList = new ArrayList<Group>();
  private Map<String, Group> _groupMap = new LinkedHashMap<String, Group>();

  @Override
  public String getName() {
    return _name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    _name = name;
  }

  public Group getGroup(String name) throws WdkModelException {
    Group group = _groupMap.get(name);
    if (group == null)
      throw new WdkModelException("Group Set " + getName()
          + " does not include group " + name);
    return group;
  }

  @Override
  public Group getElement(String elementName) {
    return _groupMap.get(elementName);
  }

  public Group[] getGroups() {
    Group[] array = new Group[_groupMap.size()];
    _groupMap.values().toArray(array);
    return array;
  }

  public void addGroup(Group group) {
    _groupList.add(group);
  }

  @Override
  public void resolveReferences(WdkModel model) throws WdkModelException {
    for (Group group : _groupMap.values()) {
      group.resolveReferences(model);
    }
  }

  @Override
  public void setResources(WdkModel model) throws WdkModelException {
    // nothing to do here
  }

  @Override
  public String toString() {
    String newline = System.getProperty("line.separator");
    StringBuffer buf = new StringBuffer("GroupSet: name='" + _name + "'");
    buf.append(newline);
    for (Group group : _groupMap.values()) {
      buf.append(newline);
      buf.append(":::::::::::::::::::::::::::::::::::::::::::::");
      buf.append(newline);
      buf.append(group).append(newline);
    }

    return buf.toString();
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    // exclude groups
    for (Group group : _groupList) {
      if (group.include(projectId)) {
        group.excludeResources(projectId);
        String groupName = group.getName();
        if (_groupMap.containsKey(groupName))
          throw new WdkModelException("Group named " + groupName
              + " already exists in group set " + _name);
        group.setGroupSet(this);
        _groupMap.put(groupName, group);
      }
    }
    _groupList = null;
  }
}
