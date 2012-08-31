package org.gusdb.wdk.jmx.mbeans;

/**
 * Miscellaneous version and related values from the model.
 * The values are obtained from the instantiated WdkModel
 * but typically are derived from the model definition file
 * in $GUS_HOME/lib/wdk/
 */
public class Meta extends BeanBase implements MetaMBean   {
  
  public Meta() {
    super();
  }

  public String getModelVersion() {
    return wdkModel.getVersion();
  }
  
  public String getDisplayName() {
    return wdkModel.getDisplayName();
  }
  
  public String getIntroduction() {
    return wdkModel.getIntroduction();
  }

  public String getProjectId() {
      return wdkModel.getProjectId();
  }

  public String getName() {
      return wdkModel.getProjectId();
  }

  public String getReleaseDate() {
      return wdkModel.getReleaseDate();
  }

}
