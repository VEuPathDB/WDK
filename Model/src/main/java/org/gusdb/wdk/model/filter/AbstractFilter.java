package org.gusdb.wdk.model.filter;


public abstract class AbstractFilter implements Filter {

  private final String _key;
  private String _display;
  private String _description;
  private String _view;
  private boolean _isViewOnly;
  
  public AbstractFilter(String key) {
    _key = key;
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
    _display = display;
  }

  @Override
  public String getDescription() {
    return _description;
  }
  
  @Override  
  public void setDescription(String description) {
    _description = description;
  }

  @Override
  public String getView() {
    return _view;
  }

  @Override  
  public void setView(String view) {
    _view = view;
  }

  @Override
  public boolean getIsViewOnly() {
    return _isViewOnly;
  }

  @Override
  public void setIsViewOnly(boolean isViewOnly) {
    _isViewOnly = isViewOnly;
  }
}
