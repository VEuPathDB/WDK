/**
 * 
 */
package org.gusdb.wdk.model.dataset;

import java.util.Map;

/**
 * @author jerric
 * 
 */
public abstract class AbstractDatasetParser implements DatasetParser {

  private String name;
  private String display;
  private String description;
  protected Map<String, String> properties;

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.DatasetParser#getName()
   */
  @Override
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.DatasetParser#setName(java.lang.String)
   */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.DatasetParser#getDisplay()
   */
  @Override
  public String getDisplay() {
    return display;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.DatasetParser#setDisplay(java.lang.String)
   */
  @Override
  public void setDisplay(String display) {
    this.display = display;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.DatasetParser#getDescription()
   */
  @Override
  public String getDescription() {
    return description;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.DatasetParser#setDescription(java.lang.String)
   */
  @Override
  public void setDescription(String description) {
    this.description = description;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.DatasetParser#setProperties(java.util.Map)
   */
  @Override
  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

}
