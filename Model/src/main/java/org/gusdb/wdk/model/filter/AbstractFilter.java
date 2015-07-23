package org.gusdb.wdk.model.filter;

import org.json.JSONObject;


public abstract class AbstractFilter implements Filter {

  private final String _key;
  private String _display;
  private String _description;
  private String _view;
  protected JSONObject _defaultValue;
  
  public AbstractFilter(String key) {
    this._key = key;
  }

  @Override
  public String getKey() {
    return _key;
  }
  
  @Override
  public String getDisplay() {
    return _display;
  }
  
  @Override  
  public void setDisplay(String display) {
    this._display = display;
  }

  @Override
  public String getDescription() {
    return _description;
  }
  
  @Override  
  public void setDescription(String description) {
    this._description = description;
  }

  @Override
  public String getView() {
    return _view;
  }

  @Override  
  public void setView(String view) {
    this._view = view;
  }
  
  @Override
  public JSONObject getDefaultValue() {
	  return _defaultValue;
  }
  
  @Override
  public void setDefaultValue(JSONObject defaultValue) {
    _defaultValue = defaultValue;
  }
  
}
