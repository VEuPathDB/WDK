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

  @Override
  public String getModelVersion() {
    return wdkModel.getVersion();
  }

  @Override
  public String getDisplayName() {
    return wdkModel.getDisplayName();
  }

  @Override
  public String getIntroduction() {
    return wdkModel.getIntroduction();
  }

  @Override
  public String getProjectId() {
      return wdkModel.getProjectId();
  }

  @Override
  public String getName() {
      return wdkModel.getProjectId();
  }

  @Override
  public String getReleaseDate() {
      return wdkModel.getReleaseDate();
  }

  @Override
  public String getBuildNumber() {
      return wdkModel.getBuildNumber();
  }

  @Override
  public String getGusHome() {
      return wdkModel.getGusHome();
  }

}
