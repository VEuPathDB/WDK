package org.gusdb.wdk.model.filter;


public abstract class AbstractFilter implements Filter {

  private final String key;
  private String display;
  private String description;
  private String view;
  
  public AbstractFilter(String key) {
    this.key = key;
  }

  @Override
  public String getKey() {
    return key;
  }
  
  @Override
  public String getDisplay() {
    return display;
  }
  
  @Override  
  public void setDisplay(String display) {
    this.display = display;
  }

  @Override
  public String getDescription() {
    return description;
  }
  
  @Override  
  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String getView() {
    return view;
  }

  @Override  
  public void setView(String view) {
    this.view = view;
  }
}
