package org.gusdb.wdk.model.toolbundle.impl;

import java.util.Map;

import org.gusdb.wdk.model.toolbundle.ColumnTool;
import org.gusdb.wdk.model.toolbundle.ColumnToolInstance;
import org.json.JSONObject;

public abstract class AbstractColumnTool<T extends ColumnToolInstance> implements ColumnTool<T> {

  private String _key;

  private Map<String, String> _props;

  @Override
  public ColumnTool<T> setKey(final String key) {
    _key = key;
    return this;
  }

  @Override
  public String getKey() {
    return _key;
  }

  @Override
  public ColumnTool<T> setModelProperties(Map<String, String> props) {
    _props = Map.copyOf(props);
    return this;
  }

  @Override
  public String toString() {
    return new JSONObject()
      .put("class", getClass().getName())
      .put("key", _key)
      .put("props", _props)
      .toString();
  }

  protected Map<String, String> getProps() {
    return _props;
  }

}
