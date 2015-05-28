package org.gusdb.wdk.model.filter;

import java.util.ArrayList;
import java.util.List;

import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;

public abstract class FilterDefinition extends WdkModelBase {

  private String _name;
  private String _display;
  private String _description;
  private String _view;
  private String _implementation;

  private List<WdkModelText> _displays = new ArrayList<>();
  private List<WdkModelText> _descriptions = new ArrayList<>();

  public String getName() {
    return _name;
  }

  public void setName(String name) {
     this._name = name;
  }

  public String getDisplay() {
    return _display;
  }

  public void addDisplay(WdkModelText display) {
    _displays.add(display);
  }

  public void setDisplay(String display) {
    this._display = display;
  }

  public String getDescription() {
    return _description;
  }

  public void addDescription(WdkModelText description) {
    _descriptions.add(description);
  }

  public void setDescription(String description) {
    this._description = description;
  }

  public String getView() {
    return _view;
  }

  public void setView(String view) {
    this._view = view;
  }

  public String getImplementation() {
    return _implementation;
  }

  public void setImplementation(String implementation) {
      this._implementation = implementation;
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    // exclude displays
    for (WdkModelText display : _displays) {
      if (!display.include(projectId))
        continue;

      if (this._display != null)
        throw new WdkModelException("The display of the filter " + _name + " for project " + projectId +
            " is defined more than once.");

      display.excludeResources(projectId);
      this._display = display.getText();
    }
    _displays.clear();
    _displays = null;

    // exclude descriptions
    for (WdkModelText description : _descriptions) {
      if (!description.include(projectId))
        continue;

      if (this._description != null)
        throw new WdkModelException("The description of the filter " + _name + " for project " + projectId +
            " is defined more than once.");

      description.excludeResources(projectId);
      this._description = description.getText();
    }
    _descriptions.clear();
    _descriptions = null;
  }

  protected void initializeFilter(Filter filter) {
    filter.setDisplay(_display);
    filter.setDescription(_description);
    filter.setView(_view);
  }
}
