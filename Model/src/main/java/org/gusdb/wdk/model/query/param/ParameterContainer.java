package org.gusdb.wdk.model.query.param;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.Group;

public interface ParameterContainer {

  String getFullName();

  Map<String, Param> getParamMap();

  default Param[] getParams() {
    return getParamMap().values().toArray(new Param[0]);
  }

  /**
   * Gets all required parameters for this {@code ParameterContainer} including
   * those required by any depended params.
   *
   * @return a map of all required params keyed on param name.
   */
  default Map<String, Param> getRequiredParams() {
    return getParamMap();
  }

  default Map<Group, Map<String, Param>> getParamMapByGroups() {
    Param[] params = getParams();
    Map<Group, Map<String, Param>> paramGroups = new LinkedHashMap<>();
    for (Param param : params) {
      Group group = param.getGroup();
      Map<String, Param> paramGroup;
      if (paramGroups.containsKey(group)) {
        paramGroup = paramGroups.get(group);
      } else {
        paramGroup = new LinkedHashMap<>();
        paramGroups.put(group, paramGroup);
      }
      paramGroup.put(param.getName(), param);
    }
    return paramGroups;
  }

}
