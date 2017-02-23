package org.gusdb.wdk.model.ontology;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

public interface OntologyFactory {

  public Ontology getOntology(WdkModel wdkModel) throws WdkModelException;

  public Ontology getValidatedOntology(WdkModel wdkModel) throws WdkModelException;

}
