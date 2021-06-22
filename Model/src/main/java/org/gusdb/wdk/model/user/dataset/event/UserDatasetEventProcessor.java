package org.gusdb.wdk.model.user.dataset.event;

import javax.sql.DataSource;

import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.config.ModelConfig;
import org.gusdb.wdk.model.config.ModelConfigParser;
import org.gusdb.wdk.model.user.dataset.UserDatasetSession;
import org.gusdb.wdk.model.user.dataset.UserDatasetStore;

public abstract class UserDatasetEventProcessor
{
  // TODO: get from model config
  private static final String UD_SCHEMA_NAME = "ApiDBUserDatasets.";

  private final String           projectID;

  private final UserDatasetStore userDatasetStore;

  private final ModelConfig      modelConfig;

  protected UserDatasetEventProcessor(String projectID) throws WdkModelException {
    this.projectID        = projectID;
    this.modelConfig      = parseModelConfig(projectID);
    this.userDatasetStore = getUserDatasetStore(this.modelConfig);
  }

  public UserDatasetStore getUserDatasetStore() {
    return userDatasetStore;
  }

  public String getUserDatasetSchemaName() {
    return UD_SCHEMA_NAME;
  }

  public ModelConfig getModelConfig() {
    return modelConfig;
  }

  public String getProjectId() {
    return projectID;
  }

  protected DatabaseInstance openAppDB() {
    return new DatabaseInstance(
      getModelConfig().getAppDB(),
      WdkModel.DB_INSTANCE_APP,
      true
    );
  }

  private static UserDatasetStore getUserDatasetStore(ModelConfig conf) throws WdkModelException {
    return conf.getUserDatasetStoreConfig().getUserDatasetStore(conf.getWdkTempDir());
  }

  private static ModelConfig parseModelConfig(String projectId) throws WdkModelException {
    try {
      var gusHome     = GusHome.getGusHome();
      var parser      = new ModelConfigParser(gusHome);
      var modelConfig = parser.parseConfig(projectId).build();

      QueryLogger.initialize(modelConfig.getQueryMonitor());

      return modelConfig;
    } catch (Exception e) {
      throw new WdkModelException(e);
    }
  }
}
