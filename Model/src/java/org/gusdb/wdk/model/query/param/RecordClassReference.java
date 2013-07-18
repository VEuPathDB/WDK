package org.gusdb.wdk.model.query.param;

import org.gusdb.wdk.model.WdkModelBase;

/**
 * This class defines a reference to a RecordClass. The reference is used in
 * answerParam and datasetParam, to define the types of data these params can
 * accept.
 * 
 * @author jerric
 * 
 */
public class RecordClassReference extends WdkModelBase {

  String ref;

  public RecordClassReference() {}

  public RecordClassReference(String ref) {
    this.ref = ref;
  }

  /**
   * @return the ref
   */
  public String getRef() {
    return ref;
  }

  /**
   * @param ref
   *          the ref to set
   */
  public void setRef(String ref) {
    this.ref = ref;
  }
}
