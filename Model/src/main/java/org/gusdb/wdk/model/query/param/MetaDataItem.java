package org.gusdb.wdk.model.query.param;

public class MetaDataItem {
  private String ontologyId;
  private String internal;
  private String stringValue;
  private String numberValue;
  private String dateValue;
  public String getOntologyId() {
    return ontologyId;
  }
  public void setOntologyId(String ontologyId) {
    this.ontologyId = ontologyId;
  }
  public String getInternal() {
    return internal;
  }
  public void setInternal(String internal) {
    this.internal = internal;
  }
  public String getStringValue() {
    return stringValue;
  }
  public void setStringValue(String stringValue) {
    this.stringValue = stringValue;
  }
  public String getNumberValue() {
    return numberValue;
  }
  public void setNumberValue(String numberValue) {
    this.numberValue = numberValue;
  }
  public String getDateValue() {
    return dateValue;
  }
  public void setDateValue(String dateValue) {
    this.dateValue = dateValue;
  }

}
