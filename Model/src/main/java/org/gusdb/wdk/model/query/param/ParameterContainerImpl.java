package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;

public abstract class ParameterContainerImpl extends WdkModelBase implements ParameterContainer {

  // temp list, will be discarded after resolve references
  protected List<ParamReference> paramRefList;
  protected Map<String, Param> paramMap;

  protected ParameterContainerImpl() {
    paramRefList = new ArrayList<>();
    paramMap = new LinkedHashMap<>();
  }

  protected ParameterContainerImpl(ParameterContainerImpl container) {
    super(container);
    if (container.paramRefList != null)
      this.paramRefList = new ArrayList<>(container.paramRefList);
    this.paramMap = new LinkedHashMap<>();

    // clone params
    for (String paramName : container.paramMap.keySet()) {
      Param param = container.paramMap.get(paramName).clone();
      param.setContainer(this);
      paramMap.put(paramName, param);
    }
  }

  public void addParamRef(ParamReference paramRef) {
    this.paramRefList.add(paramRef);
  }

  public void addParam(Param param) {
    param.setContainer(this);
    paramMap.put(param.getName(), param);
  }

  @Override
  public Map<String, Param> getParamMap() {
    return new LinkedHashMap<>(paramMap);
  }

  @Override
  public Param[] getParams() {
    Param[] array = new Param[paramMap.size()];
    paramMap.values().toArray(array);
    return array;
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    // exclude paramRefs
    List<ParamReference> paramRefs = new ArrayList<>();
    for (ParamReference paramRef : paramRefList) {
      if (paramRef.include(projectId)) {
        paramRef.excludeResources(projectId);
        paramRefs.add(paramRef);
      }
    }
    paramRefList = paramRefs;
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    super.resolveReferences(wdkModel);
    for (ParamReference paramRef : paramRefList) {
      Param param =
          (paramRef.getSetName().equals(Utilities.INTERNAL_PARAM_SET) &&
             paramRef.getElementName().equals(Utilities.PARAM_USER_ID)) ?
          getUserParam(wdkModel) :
          ParamReference.resolveReference(wdkModel, paramRef, this);
      String paramName = param.getName();
      if (paramMap.containsKey(paramName)) {
        throw new WdkModelException("The param '" + paramName + "' is duplicated in query " + getFullName());
      }
      else {
        paramMap.put(paramName, param);
      }
    }
    paramRefList = null;

    // resolve reference for those params
    for (Param param : paramMap.values()) {
      param.resolveReferences(wdkModel);
    }
  }

  /**
   * Create or get an internal user param, which is a stringParam with a pre-defined name. This param will be
   * added to all the queries, and the value of it will be the current user id, and is assigned automatically.
   * 
   * @return
   * @throws WdkModelException
   */
  public static Param getUserParam(WdkModel wdkModel) throws WdkModelException {
    // create the missing user_id param for the attribute query
    ParamSet paramSet = wdkModel.getParamSet(Utilities.INTERNAL_PARAM_SET);
    if (paramSet.contains(Utilities.PARAM_USER_ID))
      return paramSet.getParam(Utilities.PARAM_USER_ID);

    StringParam userParam = new StringParam();
    userParam.setName(Utilities.PARAM_USER_ID);
    userParam.setNumber(true);

    userParam.excludeResources(wdkModel.getProjectId());
    userParam.resolveReferences(wdkModel);
    userParam.setResources(wdkModel);
    paramSet.addParam(userParam);
    return userParam;
  }

  public Param getUserParam() throws WdkModelException {
    return getUserParam(_wdkModel);
  }

  public void validateDependentParams() throws WdkModelException {
    validateDependentParams(getFullName(), paramMap);
  }

  private static void validateDependentParams(String queryName, Map<String, Param> paramMap) throws WdkModelException {
    // TODO: Need to validate that no params in the rootQuery paramMap have a short name that in fact refers
    //       to different params (i.e., params with different full names but the same short name).
    for (Param param : paramMap.values()) {
      if (param instanceof AbstractDependentParam) {
        ((AbstractDependentParam) param).checkParam(queryName, null, paramMap, new ArrayList<>());
      }
    }
  }
}
