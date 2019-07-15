package org.gusdb.wdk.service.formatter;

import java.util.List;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDatasetInfo;
import org.gusdb.wdk.model.user.dataset.UserDatasetSession;
import org.json.JSONArray;

public interface UserDatasetsFormatter {

  default JSONArray getUserDatasetsJson(
      UserDatasetSession dsSession,
      List<UserDatasetInfo> userDatasets,
      List<UserDatasetInfo> sharedDatasets) throws WdkModelException {
    JSONArray datasetsJson = new JSONArray();
    for (UserDatasetInfo dataset : userDatasets)
      addUserDatasetInfoToJsonArray(dataset, datasetsJson, dsSession);
    for (UserDatasetInfo dataset : sharedDatasets)
      addSharedDatasetInfoToJsonArray(dataset, datasetsJson, dsSession);
    return datasetsJson;
  }

  void addUserDatasetInfoToJsonArray(UserDatasetInfo dataset, JSONArray datasetsJson,
      UserDatasetSession dsSession) throws WdkModelException;

  default void addSharedDatasetInfoToJsonArray(UserDatasetInfo dataset, JSONArray datasetsJson,
      UserDatasetSession dsSession) throws WdkModelException {
    addUserDatasetInfoToJsonArray(dataset, datasetsJson, dsSession);
  }

}
