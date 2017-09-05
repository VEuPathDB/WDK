/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

/**
 * @author jerric
 * 
 */
public abstract class AbstractParamHandler implements ParamHandler {

  protected WdkModel _wdkModel;
  protected Param _param;
  protected Map<String, String> _properties;

  public AbstractParamHandler() {
    _properties = new LinkedHashMap<>();
  }
  
  public AbstractParamHandler(AbstractParamHandler handler, Param param) {
    this._wdkModel = handler._wdkModel;
    this._param = param;
    this._properties = new LinkedHashMap<>(handler._properties);
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gusdb.wdk.model.query.param.ParamHandlerPlugin#setParam(org.gusdb.wdk
   * .model.query.param.Param)
   */
  @Override
  public void setParam(Param param) {
    this._param = param;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gusdb.wdk.model.query.param.ParamHandlerPlugin#setWdkModel(org.gusdb
   * .wdk.model.WdkModel)
   */
  @Override
  public void setWdkModel(WdkModel wdkModel) {
    this._wdkModel = wdkModel;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gusdb.wdk.model.query.param.ParamHandlerPlugin#setProperties(java.util
   * .Map)
   */
  @Override
  public void setProperties(Map<String, String> properties) throws WdkModelException {
    this._properties = properties;
  }
}
