/**
 * 
 */
package org.gusdb.wdk.model.answer;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.RecordClass;

/**
 * An object representation of {@code <answerFilterLayout>/<instance>};. It
 * is a reference to the actual {@link AnswerFilterInstance}.
 * 
 * @author xingao
 * 
 */
public class AnswerFilterInstanceReference extends WdkModelBase {

  private String ref;

  private RecordClass recordClass;

  private AnswerFilterInstance instance;

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

  /**
   * @return the recordClass
   */
  public RecordClass getRecordClass() {
    return recordClass;
  }

  /**
   * @param recordClass
   *          the recordClass to set
   */
  void setRecordClass(RecordClass recordClass) {
    this.recordClass = recordClass;
  }

  /**
   * @return the instance
   */
  public AnswerFilterInstance getInstance() {
    return instance;
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    // nothing to exclude
  }

  @Override
  public void resolveReferences(WdkModel wodkModel) throws WdkModelException {
    // resolve the instance reference
    this.instance = recordClass.getFilterInstance(ref)
        .orElseThrow(() -> new WdkModelException("Filter doesn't exist: " + ref));
    _resolved = true;
  }
}
