package org.gusdb.wdk.model.filter;


public abstract class AbstractFilter implements Filter {

  private final String _key;
  private String _display;
  private String _description;
  private String _view;
  
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
}
