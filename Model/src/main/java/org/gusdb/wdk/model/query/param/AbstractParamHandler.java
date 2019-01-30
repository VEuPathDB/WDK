package org.gusdb.wdk.model.query.param;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

/**
 * @author jerric
 */
public abstract class AbstractParamHandler implements ParamHandler {

  protected WdkModel _wdkModel;
  protected Param _param;
  protected Map<String, String> _properties;

  public AbstractParamHandler() {
    _properties = new LinkedHashMap<>();
  }

  public AbstractParamHandler(AbstractParamHandler handler, Param param) {
    _wdkModel = handler._wdkModel;
    _param = param;
    _properties = new LinkedHashMap<>(handler._properties);
  }

  @Override
  public void setParam(Param param) {
    _param = param;
  }

  @Override
  public void setWdkModel(WdkModel wdkModel) {
    _wdkModel = wdkModel;
  }

  @Override
  public void setProperties(Map<String, String> properties) {
    _properties = properties;
  }
}
