package org.gusdb.wdk.model.ontology;

import org.gusdb.wdk.model.RngAnnotations.RngRequired;

public class OntologyAttribute {

  private String _name;

  @RngRequired
  public void setName(String name) {
    _name = name;
  }

  public String getName() {
    return _name;
  }
}
