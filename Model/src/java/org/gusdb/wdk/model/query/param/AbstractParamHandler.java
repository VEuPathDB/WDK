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

  protected WdkModel wdkModel;
  protected Param param;
  protected Map<String, String> properties;

  public AbstractParamHandler() {
    properties = new LinkedHashMap<>();
  }
  
  public AbstractParamHandler(AbstractParamHandler handler, Param param) {
    this.wdkModel = handler.wdkModel;
    this.param = param;
    this.properties = new LinkedHashMap<>(handler.properties);
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
    this.param = param;
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
    this.wdkModel = wdkModel;
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
    this.properties = properties;
  }
}
