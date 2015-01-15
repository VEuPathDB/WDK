package org.gusdb.wdk.beans;

import org.gusdb.wdk.model.query.param.Param;

/**
 * Wraps a parameter value in the system and handles conversion of that value
 * for different uses.  The following uses are provided:
 * 
 * - (JSON Object) Incoming value from client
 * - (JSON) Value stored in step table
 * - (JSON) Outgoing value to client (for revise)
 * - (String) Value for user-independent checksum for cache table
 * - (String?) Value for injection into Query (SQL or Process)
 * 
 * @author ryan
 */
public class ParamValue {

  private Param _param;
  private Object _valueObject;
  
  public ParamValue(Param param, Object valueObject) {
    _param = param;
    _valueObject = valueObject;
  }

  public String getName() {
    return _param.getName();
  }

  public Object getObjectValue() {
    return _valueObject;
  }

}
