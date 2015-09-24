package org.gusdb.wdk.model;

import static org.gusdb.fgputil.FormatUtil.NL;

import org.gusdb.fgputil.functional.TreeNode.MultiLineToString;

public class SelectableItem implements MultiLineToString {

  private final String _name;
  private final String _displayName;
  private final String _help;

  private boolean _isSelected = false;
  private boolean _isDefault = false;
  private boolean _openByDefault = false;

  public SelectableItem(String name, String displayName) {
    this(name, displayName, "");
  }

  public SelectableItem(String name, String displayName, String help) {
    _name = name;
    _displayName = displayName;
    _help = help;
  }

  public String getName()        { return _name; }
  public String getDisplayName() { return _displayName; }
  public String getHelp()        { return _help; }

  public SelectableItem setSelected(boolean isSelected) {
    _isSelected = isSelected;
    return this;
  }

  public boolean isSelected() {
    return _isSelected;
  }

  public SelectableItem setIsDefault(boolean isDefault) {
    _isDefault = isDefault;
    return this;
  }

  public boolean isDefault() {
    return _isDefault;
  }

  public SelectableItem setOpenByDefault(boolean openByDefault) {
    _openByDefault = openByDefault;
    return this;
  }

  public boolean isOpenByDefault() {
    return _openByDefault;
  }

  @Override
  public String toString() {
    return new StringBuilder("Name: ").append(_name)
        .append(", DisplayName: ").append(_displayName).toString();
  }

  @Override
  public String toMultiLineString(String indentation) {
    return new StringBuilder()
        .append(indentation).append("Name: ").append(_name).append(NL)
        .append(indentation).append("DisplayName: ").append(_displayName).append(NL)
        .append(indentation).append("Help: ").append(_help == null ? "null" : _help.length() + " chars").append(NL)
        .append(indentation).append("IsDefault: ").append(_isDefault).append(NL)
        .append(indentation).append("IsSelected: ").append(_isSelected).append(NL)
        .toString();
  }
}
