package org.gusdb.wdk.jmx.mbeans;

/**
 * A view of the WDK's representation of model-config.xml.
 * The configuration data comes from WDK class instances, not directly 
 * from configuation files on the filesystem, so it's important to note
 * that the WDK may have added or removed or even changed some values
 * relative to the state on the filesystem.
 *
 * @see org.gusdb.wdk.jmx.mbeans.AbstractConfig#setValuesFromGetters
 * @see org.gusdb.wdk.model.ModelConfig
 * @see org.gusdb.wdk.model.ModelConfigUserDB
 * @see org.gusdb.wdk.model.ModelConfigAppDB
 */
public class ModelConfig extends AbstractConfig {

  public ModelConfig() {
    super();
    init();
  }
  
  protected void init() {
    org.gusdb.wdk.model.ModelConfig       modelConfig       = wdkModel.getModelConfig();
    org.gusdb.wdk.model.QueryMonitor      queryMonitor      = modelConfig.getQueryMonitor();
    org.gusdb.wdk.model.ModelConfigUserDB modelConfigUserDB = modelConfig.getUserDB();
    org.gusdb.wdk.model.ModelConfigAppDB  modelConfigAppDB  = modelConfig.getAppDB();

    
    setValuesFromGetters("global", modelConfig);
    setValuesFromGetters("queryMonitor", queryMonitor);
    setValuesFromGetters("userDb", modelConfigUserDB);
    setValuesFromGetters("appDb",  modelConfigAppDB);
  }

}