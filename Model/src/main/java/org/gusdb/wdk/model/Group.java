package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Currently, a group is only used to group {@link org.gusdb.wdk.model.query.param.Param}s together in the
 * question page for display/layout purpose.
 * </p>
 * 
 * <p>If a {@link org.gusdb.wdk.model.query.param.Param} is not assigned to any group, it will be assigned to the
 * default {@link Group#Empty} group.</p>
 * 
 * @author xingao
 */
public class Group extends WdkModelBase {

  private static final String DISPLAY_EMPTY = "empty";
  private static final String DISPLAY_HIDDEN = "hidden";
  private static Group empty;
  private static Group hidden;

  private String _name;
  private String _displayName;

  private List<WdkModelText> _descriptions;
  private String _description;

  private String _displayType;
  private boolean _visible;

  private GroupSet _groupSet;

  /**
   * The default display style of a group, which will render nothing around the
   * group of params.
   * 
   * @return
   */
  public static Group Empty() {
    if (empty == null) {
      empty = new Group();
      empty._displayType = DISPLAY_EMPTY;
      empty._visible = true;
    }
    return empty;
  }

  /**
   * The group used for parameters with no explicit group and with visibility
   * set to false.
   * 
   * @return
   */
  public static Group Hidden() {
    if (hidden == null) {
      hidden = new Group();
      hidden._displayType = DISPLAY_HIDDEN;
      hidden._name = "_hidden";
    }
    return hidden;
  }

  /**
   * Creates a default group with display type as "empty". with no display name
   * and description.
   */
  public Group() {
    // initialize an empty group
    _name = DISPLAY_EMPTY;
    _displayName = "";
    _description = "";
    _descriptions = new ArrayList<WdkModelText>();
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return _description;
  }

  /**
   * @param description
   *          the description to set
   */
  public void addDescription(WdkModelText description) {
    _descriptions.add(description);
  }

  /**
   * @return the displayName
   */
  public String getDisplayName() {
    return _displayName;
  }

  /**
   * @param displayName
   *          the displayName to set
   */
  public void setDisplayName(String displayName) {
    _displayName = displayName;
  }

  /**
   * @return the groupSet
   */
  public GroupSet getGroupSet() {
    return _groupSet;
  }

  /**
   * @param groupSet
   *          the groupSet to set
   */
  public void setGroupSet(GroupSet groupSet) {
    _groupSet = groupSet;
  }

  /**
   * @return the name
   */
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

  public String getFullName() {
    if (_groupSet != null)
      return _groupSet.getName() + "." + _name;
    else
      return _name;
  }

  /**
   * The display type determines how a group of parameters is displayed in the
   * question form.
   * 
   * @return the displayType
   */
  public String getDisplayType() {
    return _displayType;
  }

  /**
   * @param displayType
   *          the displayType to set
   */
  public void setDisplayType(String displayType) {
    _displayType = displayType;
  }

  @Override
  public void resolveReferences(WdkModel model) throws WdkModelException {
    // do nothing
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    // exclude descriptions
    boolean hasDescription = false;
    for (WdkModelText description : _descriptions) {
      if (description.include(projectId)) {
        if (hasDescription) {
          throw new WdkModelException("The group " + getFullName()
              + " has more than one description for project " + projectId);
        } else {
          _description = description.getText();
          hasDescription = true;
        }
      }
    }
    _descriptions = null;
  }

  /**
   * @return the visible
   */
  public boolean isVisible() {
    return _visible;
  }

  /**
   * @param visible
   *          the visible to set
   */
  public void setVisible(boolean visible) {
    _visible = visible;
  }
}
