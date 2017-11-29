package org.gusdb.wdk.model.jspwrap;

import java.util.stream.Collectors;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.FilterParamNew;

public class FilterParamNewBean extends ParamBean<FilterParamNew> {

  public FilterParamNewBean(FilterParamNew param) {
    super(param);
  }

  public String getDependedParamNames() throws WdkModelException {
    return this._param.getDependedParams()
        .stream()
        .map(p -> p.getName())
        .collect(Collectors.joining(","));
  }

}
