package org.gusdb.wdk.model.query.param;

import java.util.Map;

public interface ParameterContainer {

  public Map<String, Param> getParamMap();

  public Param[] getParams();

  public String getFullName();

}
