package org.gusdb.wdk.service.formatter;

import java.text.DecimalFormat;
import java.util.List;

import org.gusdb.fgputil.json.JsonType;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetDependency;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.UserDatasetInfo;
import org.gusdb.wdk.model.user.dataset.UserDatasetInfo.UserDatasetShareUser;
import org.gusdb.wdk.model.user.dataset.UserDatasetSession;
import org.gusdb.wdk.model.user.dataset.UserDatasetType;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserDatasetFormatter {

  public static JSONArray getUserDatasetsJson(UserDatasetSession dsSession, List<UserDatasetInfo> userDatasets,
      List<UserDatasetInfo> sharedDatasets, boolean expandDatasets) throws WdkModelException {
    JSONArray datasetsJson = new JSONArray();
    putDatasetsIntoJsonArray(dsSession, datasetsJson, userDatasets, expandDatasets, true);
    putDatasetsIntoJsonArray(dsSession, datasetsJson, sharedDatasets, expandDatasets, false);
    return datasetsJson;
  }

  private static void putDatasetsIntoJsonArray(UserDatasetSession dsSession, JSONArray datasetsJson, List<UserDatasetInfo> datasets,
      boolean expand, boolean includeSharingData) throws WdkModelException {
    for (UserDatasetInfo dataset : datasets) {
      datasetsJson.put(expand ?
          getUserDatasetJson(dsSession, dataset, includeSharingData, false) :
          dataset.getDataset().getUserDatasetId());
    }
  }

  /**
   * Return a JSONObject describing this dataset.  Should not include the contents of data file
   * 
   * { id: 12345
   *   type:
   *    {name: "RNA Seq",
   *     version: "1.0",    # our API version
   *     data: [ type specific data ]
   *   },   
   *   dependencies: 
   *      [{resourceIdentifier: "pf3d7_genome_rsrc", 
   *        resourceVersion: "12/2/2015",
   *        resourceDisplayName: "pfal genome"}, 
   *       {resourceIdentifier: "hsap_genome_rsrc", 
   *        resourceVersion: "2.6", 
   *        resourceDisplayName: "human genome"}
   *      ],
   *   projects: ["PlasmoDB", "ToxoDB"],
   *   userMeta: {
   *     name: "my happy data",
   *     description: "la de dah"
   *     summary: "created while wind surfing"
   *   },
   *   owner: 424156,
   *   size: "200", // kilobytes
   *   created: 1231238088881,
   *   sharedWith: [{user: 829424, emailName: "sfischer", time: 13252342341}, {user: 989921, emailName: "dfalke", time: 12332532332}],
   *   dataFiles: [{name: "blah", size: "10M"}],
   *   isInstalled: true
   * }
   * @return json object representing the dataset and associated information
   * @throws WdkModelException
   */
  public static JSONObject getUserDatasetJson(UserDatasetSession dsSession, UserDatasetInfo datasetInfo, boolean includeSharingData, boolean detailedData) throws WdkModelException {
    UserDataset dataset = datasetInfo.getDataset();
    JSONObject json = new JSONObject();
    JSONObject typeJson = new JSONObject();
    UserDatasetType type = dataset.getType();
    typeJson.put("name", type.getName());
    typeJson.put("version", type.getVersion());
    typeJson.put("display", datasetInfo.getTypeDisplay());
    JsonType trackSpecificData = detailedData ?
            datasetInfo.getDetailedTrackSpecificData() : datasetInfo.getTrackSpecificData();
    typeJson.put("data", trackSpecificData == null ? new JsonType(null) : trackSpecificData.get());
    json.put("id", dataset.getUserDatasetId());
    json.put("type", typeJson);
    json.put("isInstalled", datasetInfo.isInstalled());

    JSONArray dependenciesJson = new JSONArray(); 
    for (UserDatasetDependency dependency : dataset.getDependencies()) {
      JSONObject dependencyJson = new JSONObject();
      dependencyJson.put("resourceIdentifier", dependency.getResourceIdentifier());
      dependencyJson.put("resourceVersion", dependency.getResourceVersion());
      dependencyJson.put("resourceDisplayName", dependency.getResourceDisplayName());
      dependenciesJson.put(dependencyJson);
    }
    json.put("dependencies", dependenciesJson);

    JSONArray projectsJson = new JSONArray(); 
    for (String project : dataset.getProjects()) projectsJson.put(project);
    json.put("projects", projectsJson);

    JSONArray questionsJson = new JSONArray(); 
    for (String questionName : datasetInfo.getRelevantQuestionNames()) {
      questionsJson.put(questionName);
    }
    json.put("questions", questionsJson);

    JSONObject metaJson = new JSONObject();
    metaJson.put("name", dataset.getMeta().getName());
    metaJson.put("description", dataset.getMeta().getDescription());
    metaJson.put("summary", dataset.getMeta().getSummary());
    json.put("meta", metaJson);
    json.put("owner", datasetInfo.getOwner().getDisplayName()); 
    json.put("ownerUserId", dataset.getOwnerId());
    json.put("size", dataset.getSize());
    json.put("created", dataset.getCreatedDate());

    // external users don't get to know who else it is shared with
    if (includeSharingData) {
      JSONArray sharesJson = new JSONArray();
      for (UserDatasetShareUser share : datasetInfo.getShares()) {
        JSONObject shareJson = new JSONObject();
        shareJson.put("email", share.getUser().getEmail());
        shareJson.put("time", share.getTimeShared());
        shareJson.put("userDisplayName", share.getUser().getDisplayName());
        sharesJson.put(shareJson);
      }

      json.put("sharedWith", sharesJson);
      // Convert quota from megabytes to bytes
      long quota = 1000000 * datasetInfo.getOwnerQuota();
      DecimalFormat df = new DecimalFormat("#.####");
      json.put("percentQuotaUsed", df.format(dataset.getSize() * 100.0 / quota));
    }
    else {
      json.put("percentQuotaUsed", 0);
    }

    JSONArray filesJson = new JSONArray();
    for (UserDatasetFile file : dataset.getFiles().values()) {
      JSONObject fileJson = new JSONObject();
      fileJson.put("name", file.getFileName(dsSession));
      fileJson.put("size", file.getFileSize(dsSession));
      filesJson.put(fileJson);
    }
    json.put("datafiles", filesJson);

//    JsonType trackSpecificData = detailedData ?
//        datasetInfo.getDetailedTrackSpecificData() : datasetInfo.getTrackSpecificData();
//    json.put("trackSpecificData", trackSpecificData == null ? new JsonType(null) : trackSpecificData.get());

    /* replace this with installation state, when we code that up.
    JSONObject compatJson = new JSONObject();
    compatJson.put("isCompatible", compatibility.isCompatible());
    compatJson.put("notCompatibleReason", compatibility.notCompatibleReason());
    json.put("compatibility", compatJson);
    */
    return json;
  }
}
