package org.gusdb.wdk.controller.actionutil;

import java.util.HashMap;
import java.util.Map;

/**
 * Convenience class for building a Map of parameter definitions.
 * 
 * @author rdoherty
 */
public class ParamDefMapBuilder {

  private Map<String, ParamDef> _defs = new HashMap<String, ParamDef>();
  
  /**
   * Adds parameter to the map.
   * 
   * @param name name of this parameter (i.e. name used to look up in request)
   * @param paramDef parameter definition
   * @return
   */
  public ParamDefMapBuilder addParam(String name, ParamDef paramDef) {
    _defs.put(name, paramDef);
    return this;
  }
  
  /**
   * Returns the contained map
   * 
   * @return the contained map
   */
  public Map<String, ParamDef> toMap() {
    return _defs;
  }
}
