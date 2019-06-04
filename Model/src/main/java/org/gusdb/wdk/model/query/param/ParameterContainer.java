package org.gusdb.wdk.model.query.param;

import java.util.Map;

public interface ParameterContainer {

  Map<String, Param> getParamMap();

  default Param[] getParams() {
    return getParamMap().values().toArray(new Param[0]);
  }

  String getFullName();

  /**
   * Gets all required parameters for this {@code ParameterContainer} including
   * those required by any depended params.
   *
   * @return a map of all required params keyed on param name.
   */
  default Map<String, Param> getRequiredParams() {
    return getParamMap();
  }
}
