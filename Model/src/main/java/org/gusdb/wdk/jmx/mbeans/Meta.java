package org.gusdb.wdk.jmx.mbeans;

import org.gusdb.wdk.jmx.BeanBase;

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

  @Override
  public String getModelVersion() {
    return getWdkModel().getVersion();
  }

  @Override
  public String getDisplayName() {
    return getWdkModel().getDisplayName();
  }

  @Override
  public String getIntroduction() {
    return getWdkModel().getIntroduction();
  }

  @Override
  public String getProjectId() {
      return getWdkModel().getProjectId();
  }

  @Override
  public String getName() {
      return getWdkModel().getProjectId();
  }

  @Override
  public String getReleaseDate() {
      return getWdkModel().getReleaseDate();
  }

  @Override
  public String getBuildNumber() {
      return getWdkModel().getBuildNumber();
  }

  @Override
  public String getGusHome() {
      return getWdkModel().getGusHome();
  }

}
