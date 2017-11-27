package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.FilterParamNew;

import java.util.List;
import java.util.stream.Collectors;

public class FilterParamNewBean extends ParamBean<FilterParamNew> {

  public FilterParamNewBean(FilterParamNew param) {
    super(param);
    // TODO Auto-generated constructor stub
  }

  public String getDependedParamNames() throws WdkModelException {
    return this._param.getDependedParams()
        .stream()
        .map(p -> p.getName())
        .collect(Collectors.joining(","));
  }

}
