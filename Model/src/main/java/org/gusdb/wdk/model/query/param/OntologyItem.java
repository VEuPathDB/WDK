package org.gusdb.wdk.model.query.param;

/**
 * Captures the information from an ontology query used by filter param.
 * Only vaguely related to the official Ontology objects.
 * 
 * @author steve
 */

public class OntologyItem {

  private String ontologyId;
  private String parentOntologyId;
  private String displayName;
  private String description;
  private OntologyItemType type;
  private String units;
  private long precision;  // TODO: this is long only because jdbc gives us a big decimal.
  private boolean isRange;
  private String variableName;

  // package-protect constructor
  OntologyItem(){}

  public void setOntologyId(String ontologyId) {
    this.ontologyId = ontologyId;
  }

  public void setParentOntologyId(String parentOntologyId) {
    this.parentOntologyId = parentOntologyId;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public OntologyItem setType(OntologyItemType type) {
    this.type = type;
    return this;
  }
  
  public void setIsRange(Boolean isRange) {
    this.isRange = isRange;
  }

  public void setUnits(String units) {
    this.units = units;
  }

  public void setPrecision(Long precision) {
    this.precision = precision;
  }

  public void setVariableName(String variableName) {
    this.variableName = variableName;
  }

  public String getOntologyId() {
    return ontologyId;
  }

  public String getParentOntologyId() {
    return parentOntologyId;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getDescription() {
    return description;
  }

  public OntologyItemType getType() {
    return type;
  }

  public String getUnits() {
    return units;
  }

  public Long getPrecision() {
    return precision;
  }
  
  public Boolean getIsRange() {
    return isRange;
  }

  public String getVariableName() {
    return variableName;
  }

  @Override
  public String toString() {
    return "OntologyItem [ontologyId=" + ontologyId + ", parentOntologyId=" + parentOntologyId +
        ", displayName=" + displayName + ", description=" + description + ", type=" + type + ", units=" +
        units + ", precision=" + precision + ", isRange=" + isRange + "]";
  }

}
