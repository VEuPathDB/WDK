/**
 * 
 */
package org.gusdb.wdk.model.answer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.record.RecordClass;

/**
 * TODO - need to describe what it does. reference is ambiguous
 * 
 * <p>
 * An object representation of the {@code <answerFilter>} tag in the model. An
 * answer filter has a reference to the actual filter query.
 * </p>
 * 
 * <p>
 * The filter query requires exactly one {@link AnswerParam}, and the step the filter is
 * applied on will be used as the input to that answerParam. The query can have
 * other params, and the values of all the params, except the {@link AnswerParam}, need
 * to be specified in the {@link AnswerFilterInstance}.
 * </p>
 * 
 * @author Jerric
 * 
 */
public class AnswerFilter extends WdkModelBase {

  private String queryRef;

  private List<AnswerFilterInstance> instanceList = new ArrayList<AnswerFilterInstance>();
  private Map<String, AnswerFilterInstance> instanceMap = new LinkedHashMap<String, AnswerFilterInstance>();

  private RecordClass recordClass;

  /**
   * @param queryRef
   *          the queryRef to set
   */
  public void setQueryRef(String queryRef) {
    this.queryRef = queryRef;
  }

  public void addInstance(AnswerFilterInstance instance) {
    instance.setRecordClass(recordClass);
    this.instanceList.add(instance);
  }

  public Map<String, AnswerFilterInstance> getInstances() {
    return new LinkedHashMap<String, AnswerFilterInstance>(instanceMap);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
   */
  /**
   * @param recordClass
   *          the recordClass to set
   */
  public void setRecordClass(RecordClass recordClass) {
    this.recordClass = recordClass;
    if (instanceList != null) {
      for(AnswerFilterInstance instance : instanceList) {
        instance.setRecordClass(recordClass);
      }
    } else if (instanceMap != null) {
      for(AnswerFilterInstance instance : instanceMap.values()) {
        instance.setRecordClass(recordClass);
      }
    }
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    // exclude resources for the instances
    for (AnswerFilterInstance instance : instanceList) {
      if (instance.include(projectId)) {
        instance.excludeResources(projectId);
        String instanceName = instance.getName();
        if (instanceMap.containsKey(instanceName))
          throw new WdkModelException("answerFilterInstance [" + instanceName
              + "] of type " + recordClass.getFullName() + " is defined more "
              + "than once.");
        instanceMap.put(instanceName, instance);
      }
    }
    instanceList = null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model.
   * WdkModel)
   */
  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    if (resolved)
      return;
    // resolve the reference to the filter query
    SqlQuery query = (SqlQuery) wdkModel.resolveReference(queryRef);
    query = (SqlQuery) query.clone();

    // all the filter query should has a weight column
    query.setHasWeight(true);

    // make sure the query has exactly one answerParam
    AnswerParam answerParam = null;
    for (Param param : query.getParams()) {
      if (!(param instanceof AnswerParam))
        continue;
      if (answerParam != null)
        throw new WdkModelException("Only one answerParam is allowed "
            + "in filterQuery [" + queryRef + "]");
      answerParam = (AnswerParam) param;
    }

    // resolve the references in the instance
    if (instanceMap.size() > 0) {
    for (AnswerFilterInstance instance : instanceMap.values()) {
      // set the references first
      instance.setFilterQuery(query);
      instance.setAnswerParam(answerParam);
      instance.resolveReferences(wdkModel);
    }
    } else { // if no instance is defined, will create instances from the param.
      
    }
    resolved = true;
  }
  
  private void createFilterInstances(SqlQuery filterQuery) {
    // look up all the enum/flatVocab params
    List<AbstractEnumParam> params = new ArrayList<>();
    for (Param param : filterQuery.getParams()) {
      if (param instanceof AbstractEnumParam) {
        params.add((AbstractEnumParam)param);
      }
    }
  }
}
