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
 * @see org.gusdb.wdk.model.config.ModelConfigUserDB
 * @see org.gusdb.wdk.model.config.ModelConfigAppDB
 */
public class ModelConfig extends AbstractConfig {

  public ModelConfig() {
    super();
    init();
  }
  
  protected void init() {
    org.gusdb.wdk.model.config.ModelConfig       modelConfig       = wdkModel.getModelConfig();
    org.gusdb.wdk.model.config.QueryMonitor      queryMonitor      = modelConfig.getQueryMonitor();
    org.gusdb.wdk.model.config.ModelConfigUserDB modelConfigUserDB = modelConfig.getUserDB();
    org.gusdb.wdk.model.config.ModelConfigAppDB  modelConfigAppDB  = modelConfig.getAppDB();

    
    setValuesFromGetters("global", modelConfig);
    setValuesFromGetters("queryMonitor", queryMonitor);
    setValuesFromGetters("userDb", modelConfigUserDB);
    setValuesFromGetters("appDb",  modelConfigAppDB);
  }

}