package org.gusdb.wdk.model.user.dataset;

import static org.gusdb.fgputil.functional.Functions.fSwallow;
import static org.gusdb.fgputil.functional.Functions.mapToList;
import static org.gusdb.fgputil.functional.Functions.toJavaFunction;
import static org.gusdb.fgputil.functional.Functions.zipToList;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Wrapper;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.json.JsonType;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.User;

public class UserDatasetFactory {
  private static Logger LOG = Logger.getLogger(UserDatasetFactory.class);

  private final WdkModel _wdkModel;
  private final String _userDatasetSchema;

  public UserDatasetFactory(WdkModel wdkModel) {

    _wdkModel = wdkModel;
    // TODO: put in model-config.xml + masterConfig.yaml and fetch from there
    _userDatasetSchema = "ApiDBUserDatasets.";
  }

  /**
   * Return the dataset IDs the provided user can use in this website, i.e. that are installed and has access to (which can
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
      .executeQuery(new Object[] { userId }, new Integer[] { Types.BIGINT }, rs -> {
        while (rs.next()) {
          datasetIds.add(rs.getLong(1));
        }});
    return datasetIds;
  }
  
  /**
   * Determine whether the dataset, given by its datasetId, is installed.
   * @param datasetId
   * @return - true if installed and false otherwise
   * @throws WdkModelException
   */
  public boolean isUserDatasetInstalled(long datasetId) throws WdkModelException {
    String sql = "select user_dataset_id from " + _userDatasetSchema + "userDatasetAccessControl where dataset_id = ?";
    Wrapper<Boolean> wrapper = new Wrapper<>();
    wrapper.set(false);
    new SQLRunner(_wdkModel.getAppDb().getDataSource(), sql, "is-user-dataset-installed")
      .executeQuery(new Object[] { datasetId }, new Integer[] { Types.BIGINT }, rs -> {
        if (rs.next()) {
          wrapper.set(true);
        }});
    return wrapper.get();
  }

  /**
   * Adds (non-detailed) type-specific data to the passed user datasets.
   * 
   * @param userDatasets
   * @throws WdkModelException 
   */
  public void addTypeSpecificData(WdkModel wdkModel, List<UserDatasetInfo> userDatasets, User user)
      throws WdkModelException{
    UserDatasetStore store = wdkModel.getUserDatasetStore();
    Function<UserDatasetInfo,UserDatasetType> f = toJavaFunction(fSwallow(ud -> ud.getDataset().getType()));
    Set<UserDatasetType> types = userDatasets.stream().map(f).collect(Collectors.toSet());
    for (UserDatasetType type : types) {
      List<UserDatasetInfo> typedUdis = userDatasets.stream().filter(ud -> type.equals(f.apply(ud))).collect(Collectors.toList());
      List<UserDatasetInfo> installedTypedUdis = typedUdis.stream().filter(tudi -> tudi.isInstalled()).collect(Collectors.toList());
      List<JsonType> typeSpecificInfo = new ArrayList<>();
      if(!installedTypedUdis.isEmpty()) {
        typeSpecificInfo = store.getTypeHandler(type).getTypeSpecificData(wdkModel, mapToList(installedTypedUdis, udi -> udi.getDataset()), user);
      }
      zipToList(installedTypedUdis, typeSpecificInfo, (udi, json) -> { udi.setTypeSpecificData(json); return udi; }, false);
    }
  }
}
