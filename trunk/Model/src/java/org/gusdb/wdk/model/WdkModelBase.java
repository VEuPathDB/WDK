/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is the common parent for most of the classes that are represented
 * as a tag in the WDK model.
 * 
 * An important feature of this class are the include/exclude projects flags.
 * They are used to support shared model files among different projects. If
 * several projects shared many common definitions such as record class types,
 * attributes, and queries, they can reuse the same model files, and just use
 * include/exclude projects on hte project specific elements. If the flags are
 * not set, then an element will be included in all the projects by default. The
 * include/exclude flag can be applied to almost all the model tags.
 * 
 * This class provides property list to all its children classes, and it also
 * supports the include/exclude properies for almost all the tags in the wdk
 * model.
 * 
 * 
 * @author Jerric
 * 
 */
public abstract class WdkModelBase {

  private Set<String> includeProjects;
  private Set<String> excludeProjects;

  protected boolean resolved = false;

  private List<PropertyList> propertyLists;
  private Map<String, String[]> propertyListMap;

  private WdkModel wdkModel;

  public WdkModelBase() {
    includeProjects = new LinkedHashSet<String>();
    excludeProjects = new LinkedHashSet<String>();
    propertyLists = new ArrayList<PropertyList>();
    propertyListMap = new LinkedHashMap<String, String[]>();
  }

  public WdkModelBase(WdkModelBase base) {
    this.wdkModel = base.wdkModel;
    resolved = base.resolved;
    includeProjects = new LinkedHashSet<String>(base.includeProjects);
    excludeProjects = new LinkedHashSet<String>(base.excludeProjects);
    if (base.propertyLists != null)
      propertyLists = new ArrayList<PropertyList>(base.propertyLists);
    if (base.propertyListMap != null)
      propertyListMap = new LinkedHashMap<String, String[]>(
          base.propertyListMap);
  }

  /**
   * @param excludeProjects
   *          the excludeProjects to set
   */
  public void setExcludeProjects(String excludeProjects) {
    excludeProjects = excludeProjects.trim();
    if (excludeProjects.length() == 0)
      return;

    String[] projects = excludeProjects.split(",");
    for (String project : projects) {
      this.excludeProjects.add(project.trim());
    }
  }

  /**
   * @param includeProjects
   *          the includeProjects to set
   */
  public void setIncludeProjects(String includeProjects) {
    includeProjects = includeProjects.trim();
    if (includeProjects.length() == 0)
      return;

    String[] projects = includeProjects.split(",");
    for (String project : projects) {
      this.includeProjects.add(project.trim());
    }
  }

  /**
   * 
   * @param projectId
   * @return true if the object is included in the current project
   */
  public boolean include(String projectId) {
    if (includeProjects.isEmpty()) { // no inclusions assigned
      return !excludeProjects.contains(projectId);
    } else { // has inclusions
      return includeProjects.contains(projectId);
    }
  }

  /**
   * @return the resolved
   */
  public boolean isResolved() {
    return resolved;
  }

  /**
   * This method is supposed to be called by the digester
   * 
   * @param propertyList
   */
  public void addPropertyList(PropertyList propertyList) {
    this.propertyLists.add(propertyList);
  }

  /**
   * if the property list of the given name doesn't exist, it will try to get a
   * default property list from the WdkModel.
   * 
   * @param propertyListName
   * @return
   */
  public String[] getPropertyList(String propertyListName) {
    if (!propertyListMap.containsKey(propertyListName))
      return wdkModel.getDefaultPropertyList(propertyListName);
    return propertyListMap.get(propertyListName);
  }

  public Map<String, String[]> getPropertyLists() {
    // get the default property lists
    Map<String, String[]> propLists = wdkModel.getDefaultPropertyLists();
    // replace the default ones with the ones defined in the question
    for (String plName : propertyListMap.keySet()) {
      String[] values = propertyListMap.get(plName);
      String[] array = new String[values.length];
      System.arraycopy(values, 0, array, 0, array.length);
      propLists.put(plName, array);
    }
    return propLists;
  }

  /**
   * exclude the resources the object hold which are not included in the current
   * project
   * 
   * @param projectId
   * @throws WdkModelException
   */
  public void excludeResources(String projectId) throws WdkModelException {
    // exclude property lists
    for (PropertyList propList : propertyLists) {
      if (propList.include(projectId)) {
        String listName = propList.getName();
        if (propertyListMap.containsKey(listName)) {
          throw new WdkModelException("The node " + this.getClass().getName()
              + " has more than one " + "propertyList \"" + listName
              + "\" for project " + projectId);
        } else {
          propList.excludeResources(projectId);
          propertyListMap.put(propList.getName(), propList.getValues());
        }
      }
    }
    propertyLists = null;

  }

  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    this.wdkModel = wdkModel;
  }

  protected WdkModel getWdkModel() {
    return wdkModel;
  }
}
