package org.gusdb.wdk.model.query.param;

import org.gusdb.wdk.model.WdkModelBase;

public class ParamValue extends WdkModelBase {

  private String _name;
  private String _value;
  private SelectMode _selectMode;

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  public String getValue() {
    return _value;
  }

  public void setValue(String value) {
    _value = value;
  }

  public String getSelectMode() {
    return _selectMode.toString();
  }

  public SelectMode getSelectModeEnum() {
    return _selectMode;
  }

  public void setSelectMode(String selectMode) {
    _selectMode = SelectMode.valueOf(selectMode.toUpperCase());
  }
}
