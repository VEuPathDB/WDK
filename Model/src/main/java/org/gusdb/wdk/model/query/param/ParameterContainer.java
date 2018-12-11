package org.gusdb.wdk.model.query.param;

import java.util.Map;

public interface ParameterContainer {

  Map<String, Param> getParamMap();

  Param[] getParams();

  String getFullName();

}
