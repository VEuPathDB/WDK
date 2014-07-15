/**
 * @description
 */
package org.gusdb.wdk.model.query.param;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;

/**
 * The param configuration represents a tag with boolean property named "value".
 * Currently, it is only used to map <noTranslation> tag into a object model.
 * 
 * @author Jerric
 * @created Jul 5, 2007
 * @modified Jul 5, 2007
 */
public class ParamConfiguration extends WdkModelBase {

  private boolean value;

  /**
   * @return the value
   */
  public boolean isValue() {
    return this.value;
  }

  /**
   * @param value
   *          the value to set
   */
  public void setValue(boolean value) {
    this.value = value;
  }

  /**
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
   */
  @Override
  public void excludeResources(String projectId) {
    // do nothing
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model.
   * WdkModel)
   */
  @Override
  public void resolveReferences(WdkModel wodkModel) throws WdkModelException {
    // do nothing
  }
}
