package org.gusdb.wdk.model.query.param;

public class DatasetParamSuggestion extends ParamSuggestion {

  /**
   * Only used by datasetParam, determines what input type to be selected as default.
   */
  private String defaultType = DatasetParam.TYPE_DATA;

  public DatasetParamSuggestion() {
    super();
    // TODO Auto-generated constructor stub
  }

  public DatasetParamSuggestion(DatasetParamSuggestion suggestion) {
    super(suggestion);
    this.defaultType = suggestion.defaultType;
  }

  public String getDefaultType() {
    return defaultType;
  }

  public void setDefaultType(String defaultType) {
    this.defaultType = defaultType;
  }

}
