package org.gusdb.wdk.model.query.param;

import org.gusdb.wdk.model.query.param.SelectMode;

public class EnumParamSuggestion extends ParamSuggestion {

  /**
   * only used by abstractEnumParam
   */
  private SelectMode selectMode = SelectMode.ALL;

  public EnumParamSuggestion() { }

  public EnumParamSuggestion(EnumParamSuggestion suggestion) {
    super(suggestion);
    this.selectMode = suggestion.selectMode;
  }

  /**
   * @return the selectMode
   */
  public String getSelectMode() {
    return selectMode.toString();
  }

  public SelectMode getSelectModeEnum() {
    return selectMode;
  }

  /**
   * @param selectMode
   *          the selectMode to set
   */
  public void setSelectMode(String selectMode) {
    this.selectMode = SelectMode.valueOf(selectMode.toUpperCase());
  }

}
