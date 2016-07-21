package org.gusdb.wdk.service.formatter;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetCompatibility;
import org.gusdb.wdk.model.user.dataset.UserDatasetDependency;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.UserDatasetShare;
import org.gusdb.wdk.model.user.dataset.UserDatasetStore;
import org.gusdb.wdk.model.user.dataset.UserDatasetType;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserDatasetFormatter {

  public static JSONArray getUserDatasetsJson(Map<Integer, UserDataset> userDatasetsMap, UserDatasetStore userDatasetStore, boolean expand) throws WdkModelException {
    JSONArray datasetsJson = new JSONArray();
    for (Integer datasetId : userDatasetsMap.keySet()) {
      if (!expand) datasetsJson.put(datasetId);
      else {
        UserDataset dataset = userDatasetsMap.get(datasetId);
        UserDatasetCompatibility compat = userDatasetStore.getTypeHandler(dataset.getType()).getCompatibility(dataset);
        datasetsJson.put(getUserDatasetJson(dataset, compat));
      }
    }
    return datasetsJson;
  }
  
  /**
   * Return a JSONObject describing this dataset.  Should not include the contents of data file
   * @return
   * @throws WdkModelException
   * 
   * 
{ type: {name: "RNA Seq", version: "1.0"},   # our API version
  dependencies: 
     [{resourceIdentifier: "pf3d7_genome_rsrc", 
       resourceVersion: "12/2/2015",
       resourceDisplayName: "pfal genome"}, 
      {resourceIdentifier: "hsap_genome_rsrc", 
       resourceVersion: "2.6", 
       resourceDisplayName: "human genome"}
     ],
  userMeta: {
    name: "my happy data",
    description: "la de dah"
    summary: "created while wind surfing"
  },
  owner: 424156,
  size: "200", // kilobytes
  modified: 1248312231083,
  created: 1231238088881,
  uploaded: 1231398508344,
  sharedWith: [{user: 829424, time: 13252342341}, {user: 989921, time: 12332532332}],
  dataFiles: [{name: "blah", size: "10M"}],
  compatibility: { isCompatible: true, notCompatibleReason: "" }
}

   */
  private static JSONObject getUserDatasetJson(UserDataset dataset, UserDatasetCompatibility compatibility) throws WdkModelException {
    JSONObject json = new JSONObject();
    JSONObject typeJson = new JSONObject();
    UserDatasetType type = dataset.getType();
    typeJson.put(type.getName(), type.getVersion());
    json.put("type", typeJson);
    JSONArray dependenciesJson = new JSONArray();    
    for (UserDatasetDependency dependency : dataset.getDependencies()) {
      JSONObject dependencyJson = new JSONObject();
      dependencyJson.put("resourceIdentifier", dependency.getResourceIdentifier());
      dependencyJson.put("resourceVersion", dependency.getResourceVersion());
      dependencyJson.put("resourceDisplayName", dependency.getResourceDisplayName());
      dependenciesJson.put(dependencyJson);
    }
    json.put("dependencies", dependenciesJson);
    JSONObject metaJson = new JSONObject();
    metaJson.put("name", dataset.getMeta().getName());
    metaJson.put("description", dataset.getMeta().getDescription());
    metaJson.put("summary", dataset.getMeta().getSummary());
    json.put("meta", metaJson);
    json.put("owner", dataset.getOwnerId()); // same as user id
    json.put("size", dataset.getSize());
    json.put("modified", dataset.getModifiedDate().getTime());
    json.put("created", dataset.getCreatedDate().getTime());
    json.put("uploaded", dataset.getUploadedDate().getTime());
    JSONArray sharesJson = new JSONArray();
    for (UserDatasetShare share : dataset.getSharedWith()) {
      JSONObject shareJson = new JSONObject();
      shareJson.put("user", share.getUserId());
      shareJson.put("time", share.getTimeShared());
      sharesJson.put(shareJson);
    }
    json.put("sharedWith", sharesJson);
    JSONArray filesJson = new JSONArray();
    for (UserDatasetFile file : dataset.getFiles().values()) {
      JSONObject fileJson = new JSONObject();
      fileJson.put("name", file.getFileName());
      fileJson.put("size", file.getFileSize());
      sharesJson.put(filesJson);
    }
    JSONObject compatJson = new JSONObject();
    compatJson.put("isCompatible", compatibility.isCompatible());
    compatJson.put("notCompatibleReason", compatibility.notCompatibleReason());
    return json;
  }
}
