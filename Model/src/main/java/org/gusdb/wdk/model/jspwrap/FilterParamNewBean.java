package org.gusdb.wdk.model.jspwrap;

import java.util.Set;
import java.util.stream.Collectors;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.FilterParamNew;
import org.gusdb.wdk.model.query.param.Param;

public class FilterParamNewBean extends ParamBean<FilterParamNew> {

  public FilterParamNewBean(FilterParamNew param) {
    super(param);
  }

  @Override
  public String getDependedParamNames() throws WdkModelException {
    Set<Param> dependedParams = _param.getDependedParams();
    if (dependedParams == null) return null;
    return dependedParams
        .stream()
        .map(p -> p.getName())
        .collect(Collectors.joining(","));
  }

}
