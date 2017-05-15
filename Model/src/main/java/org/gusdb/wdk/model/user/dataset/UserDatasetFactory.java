package org.gusdb.wdk.model.user.dataset;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunner.ResultSetHandler;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

public class UserDatasetFactory {

  private final WdkModel _wdkModel;
  private final String _userDatasetSchema;

  public UserDatasetFactory(WdkModel wdkModel) {
    _wdkModel = wdkModel;
    // TODO: put in model-config.xml + masterConfig.yaml and fetch from there
    _userDatasetSchema = "ApiDBUserDatasets.";
  }

  /**
   * Return the dataset IDs the provided user can see, i.e. that are installed and has access to (which can
   * include those owned by or shared with a user)
   * 
   * @param userId user for whom to get installed dataset ids
   * @return set of IDs of installed datasets
   * @throws WdkModelException if error occurs querying
   */
  public Set<Long> getInstalledUserDatasets(long userId) throws WdkModelException {
    String sql = "select user_dataset_id from " + _userDatasetSchema + "userDatasetAccessControl where user_id = ?";
    final Set<Long> datasetIds = new HashSet<>();
    new SQLRunner(_wdkModel.getAppDb().getDataSource(), sql, "installed-datasets-by-user")
        .executeQuery(new Object[] { userId }, new Integer[] { Types.BIGINT }, new ResultSetHandler() {
          @Override public void handleResult(ResultSet rs) throws SQLException {
            while (rs.next()) {
              datasetIds.add(rs.getLong(1));
            }}});
    return datasetIds;
  }
}
