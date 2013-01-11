/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Currently, a group is only used to group {@link Param}s together in the
 * question page for display/layout purpose.
 * </p>
 * 
 * <p>If a {@link Param} is not assigned to any group, it will be assigned to the
 * default {@link Group#Empty} group.</p>
 * 
 * @author: xingao
 * @created: Mar 1, 2007
 * @updated: Mar 1, 2007
 */
public class Group extends WdkModelBase {

  private static final String DISPLAY_EMPTY = "empty";
  private static Group empty;

  private String name;
  private String displayName;

  private List<WdkModelText> descriptions;
  private String description;

  private String displayType;
  private boolean visible;

  private GroupSet groupSet;

  /**
   * The default display style of a group, which will render nothing around the
   * group of params.
   * 
   * @return
   */
  public synchronized static Group Empty() {
    if (empty == null) {
      empty = new Group();
      empty.displayType = DISPLAY_EMPTY;
    }
    return empty;
  }

  /**
   * Creates a default group with display type as "empty". with no display name
   * and description.
   */
  public Group() {
    // initialize an empty group
    name = DISPLAY_EMPTY;
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
   *          the description to set
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
   *          the displayName to set
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
   *          the groupSet to set
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
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  public String getFullName() {
    if (groupSet != null)
      return groupSet.getName() + "." + name;
    else
      return name;
  }

  /**
   * The display type determines how a group of parameters is displayed in the
   * question form.
   * 
   * @return the displayType
   */
  public String getDisplayType() {
    return displayType;
  }

  /**
   * @param displayType
   *          the displayType to set
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
              + " has more than one description for project " + projectId);
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
   *          the visible to set
   */
  public void setVisible(boolean visible) {
    this.visible = visible;
  }
}
