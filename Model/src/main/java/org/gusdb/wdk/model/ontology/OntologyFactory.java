package org.gusdb.wdk.model.ontology;

import org.gusdb.wdk.model.WdkModelException;

public interface OntologyFactory {

  public Ontology getOntology() throws WdkModelException;

  public Ontology getValidatedOntology() throws WdkModelException;

}
