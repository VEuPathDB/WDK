package org.gusdb.wdk.model.user.dataset;

import static org.gusdb.fgputil.functional.Functions.fSwallow;
import static org.gusdb.fgputil.functional.Functions.mapToList;
import static org.gusdb.fgputil.functional.Functions.zipToList;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
import org.gusdb.fgputil.json.JsonType;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.User;

public class UserDatasetFactory {

  private final WdkModel _wdkModel;
  private final String _userDatasetSchema;
  private final UserDatasetStore _userDatasetStore;

  public UserDatasetFactory(WdkModel wdkModel) throws WdkModelException {
    _wdkModel = wdkModel;
    _userDatasetStore = wdkModel.getUserDatasetStore().getStore()
        .orElseThrow(() -> new WdkModelException("Cannot create user dataset factory without user dataset store."));
    // TODO: put in model-config.xml + masterConfig.yaml and fetch from there
    _userDatasetSchema = "ApiDBUserDatasets.";
  }

  /**
   * Return the dataset IDs the provided user can use in this website, i.e. that
   * are installed and has access to (which can include those owned by or shared
   * with a user)
   *
   * @param userId
   *   user for whom to get installed dataset ids
   *
   * @return set of IDs of installed datasets
   *
   * @throws WdkModelException
   *   if error occurs querying
   */
  public Set<Long> getInstalledUserDatasets(long userId) throws WdkModelException {
    try {
      String sql = "select user_dataset_id from " + _userDatasetSchema + "userDatasetAccessControl where user_id = ?";
      return new SQLRunner(_wdkModel.getAppDb().getDataSource(), sql, "installed-datasets-by-user")
        .executeQuery(new Object[] { userId }, new Integer[] { Types.BIGINT }, rs -> {
          Set<Long> datasetIds = new HashSet<>();
          while (rs.next()) {
            datasetIds.add(rs.getLong(1));
          }
          return datasetIds;
        });
    }
    catch (SQLRunnerException e) {
      return WdkModelException.unwrap(e);
    }
  }

  /**
   * Determine whether the dataset, given by its datasetId, is installed.
   *
   * @return - true if installed and false otherwise
   * @throws WdkModelException
   */
  public boolean isUserDatasetInstalled(long datasetId) throws WdkModelException {
    try {
      String sql = "select user_dataset_id from " + _userDatasetSchema + "userDatasetAccessControl where dataset_id = ?";
      return new SQLRunner(_wdkModel.getAppDb().getDataSource(), sql, "is-user-dataset-installed")
        .executeQuery(new Object[] { datasetId }, new Integer[] { Types.BIGINT }, rs -> rs.next());
    }
    catch (SQLRunnerException e) {
      return WdkModelException.unwrap(e);
    }
  }

  /**
   * Adds (non-detailed) type-specific data to the passed user datasets.
   */
  public void addTypeSpecificData(WdkModel wdkModel, List<UserDatasetInfo> userDatasets, User user)
      throws WdkModelException {
    Function<UserDatasetInfo,UserDatasetType> f = fSwallow(ud -> ud.getDataset().getType());
    Set<UserDatasetType> types = userDatasets.stream().map(f).collect(Collectors.toSet());
    for (UserDatasetType type : types) {
      List<UserDatasetInfo> typedUdis = userDatasets.stream().filter(ud -> type.equals(f.apply(ud))).collect(Collectors.toList());
      List<UserDatasetInfo> installedTypedUdis = typedUdis.stream().filter(tudi -> tudi.isInstalled()).collect(Collectors.toList());
      List<JsonType> typeSpecificInfo = new ArrayList<>();
      if(!installedTypedUdis.isEmpty()) {
        typeSpecificInfo = _userDatasetStore.getTypeHandler(type).getTypeSpecificData(wdkModel, mapToList(installedTypedUdis, udi -> udi.getDataset()), user);
      }
      zipToList(installedTypedUdis, typeSpecificInfo, (udi, json) -> { udi.setTypeSpecificData(json); return udi; }, false);
    }
  }
}
