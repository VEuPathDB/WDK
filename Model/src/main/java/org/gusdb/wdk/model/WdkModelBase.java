package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * This class is the common parent for most of the classes that are represented
 * as a tag in the WDK model.
 * 
 * An important feature of this class are the include/exclude projects flags.
 * They are used to support shared model files among different projects. If
 * several projects shared many common definitions such as record class types,
 * attributes, and queries, they can reuse the same model files, and just use
 * include/exclude projects on the project specific elements. If the flags are
 * not set, then an element will be included in all the projects by default. The
 * include/exclude flag can be applied to almost all the model tags.
 * 
 * This class provides property list to all its children classes, and it also
 * supports the include/exclude properties for almost all the tags in the wdk
 * model.
 * 
 * @author Jerric
 */
public abstract class WdkModelBase {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(WdkModelBase.class);

  protected WdkModel _wdkModel;
  protected boolean _resolved = false;

  private Set<String> _includeProjects;
  private Set<String> _excludeProjects;

  private List<PropertyList> _propertyLists;
  private Map<String, String[]> _propertyListMap;

  public WdkModelBase() {
    _includeProjects = new LinkedHashSet<String>();
    _excludeProjects = new LinkedHashSet<String>();
    _propertyLists = new ArrayList<PropertyList>();
    _propertyListMap = new LinkedHashMap<String, String[]>();
  }

  public WdkModelBase(WdkModelBase base) {
    _wdkModel = base._wdkModel;
    _resolved = base._resolved;
    _includeProjects = new LinkedHashSet<String>(base._includeProjects);
    _excludeProjects = new LinkedHashSet<String>(base._excludeProjects);
    if (base._propertyLists != null)
      _propertyLists = new ArrayList<PropertyList>(base._propertyLists);
    if (base._propertyListMap != null)
      _propertyListMap = new LinkedHashMap<String, String[]>(
          base._propertyListMap);
  }

  @Override
  public WdkModelBase clone() {
    try {
      return (WdkModelBase) super.clone();
    }
    catch (CloneNotSupportedException e) {
      // this should never happen since we implement Cloneable
      throw new WdkRuntimeException(e);
    }
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
      _excludeProjects.add(project.trim());
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
      _includeProjects.add(project.trim());
    }
  }

  /**
   * 
   * @param projectId
   * @return true if the object is included in the current project
   */
  public boolean include(String projectId) {
    if (_includeProjects.isEmpty()) { // no inclusions assigned
      return !_excludeProjects.contains(projectId);
    } else { // has inclusions
      return _includeProjects.contains(projectId);
    }
  }

  /**
   * @return the resolved
   */
  public boolean isResolved() {
    return _resolved;
  }

  /**
   * This method is supposed to be called by the digester
   * 
   * @param propertyList
   */
  public void addPropertyList(PropertyList propertyList) {
    _propertyLists.add(propertyList);
  }

  /**
   * if the property list of the given name doesn't exist, it will try to get a
   * default property list from the WdkModel.
   * 
   * @param propertyListName
   * @return list for the given name, or null if no list exists
   */
  public String[] getPropertyList(String propertyListName) {
    if (!_propertyListMap.containsKey(propertyListName))
      return _wdkModel.getDefaultPropertyList(propertyListName);
    return _propertyListMap.get(propertyListName);
  }

  public Map<String, String[]> getPropertyLists() {
    // get the default property lists
    Map<String, String[]> propLists = _wdkModel.getDefaultPropertyLists();
    // replace the default ones with the ones defined in the question
    for (String plName : _propertyListMap.keySet()) {
      String[] values = _propertyListMap.get(plName);
      propLists.put(plName, Arrays.copyOf(values, values.length));
    }
    return propLists;
  }

  /**
   * exclude the resources the object hold which are not included in the current
   * project
   * 
   * @param projectId
   * @throws WdkModelException if error occurs excluding resources
   */
  public void excludeResources(String projectId) throws WdkModelException {
    // exclude property lists
    for (PropertyList propList : _propertyLists) {
      if (propList.include(projectId)) {
        String listName = propList.getName();
        if (_propertyListMap.containsKey(listName)) {
          throw new WdkModelException("The node " + this.getClass().getName()
              + " has more than one " + "propertyList \"" + listName
              + "\" for project " + projectId);
        } else {
          propList.excludeResources(projectId);
          _propertyListMap.put(propList.getName(), propList.getValues());
        }
      }
    }
    _propertyLists = null;

  }

  /**
   * @throws WdkModelException if error occurs resolving references
   */
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    _wdkModel = wdkModel;
  }

  public WdkModel getWdkModel() {
    return _wdkModel;
  }
}
