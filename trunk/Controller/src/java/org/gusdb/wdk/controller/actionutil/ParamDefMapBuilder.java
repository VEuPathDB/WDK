package org.gusdb.wdk.controller.actionutil;

import java.util.HashMap;
import java.util.Map;

public class ParamDefMapBuilder {

  private Map<String, ParamDef> _defs = new HashMap<String, ParamDef>();
  
  public ParamDefMapBuilder addParam(String name, ParamDef paramDef) {
    _defs.put(name, paramDef);
    return this;
  }
  
  public Map<String, ParamDef> toMap() {
    return _defs;
  }
}
