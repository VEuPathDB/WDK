package org.gusdb.wdk.model.query.param;

public class EnumParamSuggestion extends ParamSuggestion {

  /**
   * only used by abstractEnumParam
   */
  private String selectMode = AbstractEnumParam.SELECT_MODE_ALL;

  public EnumParamSuggestion() {
    // TODO Auto-generated constructor stub
  }

  public EnumParamSuggestion(EnumParamSuggestion suggestion) {
    super(suggestion);
    this.selectMode = suggestion.selectMode;
  }

  /**
   * @return the selectMode
   */
  public String getSelectMode() {
    return selectMode;
  }

  /**
   * @param selectMode
   *          the selectMode to set
   */
  public void setSelectMode(String selectMode) {
    this.selectMode = selectMode;
  }

}
