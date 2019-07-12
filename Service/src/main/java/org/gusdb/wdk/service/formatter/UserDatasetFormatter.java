package org.gusdb.wdk.service.formatter;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import org.gusdb.fgputil.json.JsonType;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserFactory;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetMeta;
import org.gusdb.wdk.model.user.dataset.UserDatasetCompatibility;
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
                                              List<UserDatasetInfo> sharedDatasets, boolean expandDatasets, boolean jbrowse, String publicOrganismAbbrev) throws WdkModelException {
    JSONArray datasetsJson = new JSONArray();
    putDatasetsIntoJsonArray(dsSession, datasetsJson, userDatasets, expandDatasets, true, jbrowse, publicOrganismAbbrev);
    putDatasetsIntoJsonArray(dsSession, datasetsJson, sharedDatasets, expandDatasets, false, jbrowse, publicOrganismAbbrev);
    return datasetsJson;
  }

  private static void putDatasetsIntoJsonArray(UserDatasetSession dsSession, JSONArray datasetsJson, List<UserDatasetInfo> datasets,
                                               boolean expand, boolean includeSharingData, boolean jbrowse, String publicOrganismAbbrev) throws WdkModelException {
    for (UserDatasetInfo dataset : datasets) {

        if(jbrowse) {
            JSONArray samples = getUserDatasetJBrowseJson(dsSession, dataset, includeSharingData, false, publicOrganismAbbrev);

            

            for (int i = 0 ; i < samples.length(); i++) {
                datasetsJson.put(samples.getJSONObject(i));
            }

        }
        else {
            datasetsJson.put(expand ?
                             getUserDatasetJson(dsSession, dataset, includeSharingData, false) :
                             dataset.getDataset().getUserDatasetId());
        }
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
    UserDatasetCompatibility compatibility = datasetInfo.getUserDatasetCompatibility();
    JSONObject json = new JSONObject();
    JSONObject typeJson = new JSONObject();
    UserDatasetType type = dataset.getType();
    typeJson.put("name", type.getName());
    typeJson.put("version", type.getVersion());
    typeJson.put("display", datasetInfo.getTypeDisplay());
    JsonType trackSpecificData = detailedData ?
            datasetInfo.getDetailedTrackSpecificData() : datasetInfo.getTrackSpecificData();
    typeJson.put("data", trackSpecificData == null ? JSONObject.NULL : trackSpecificData.get());
    json.put("id", dataset.getUserDatasetId());
    json.put("type", typeJson);
    json.put("isInstalled", datasetInfo.isInstalled());

    JSONArray dependenciesJson = new JSONArray();
    for (UserDatasetDependency dependency : dataset.getDependencies()) {
      JSONObject dependencyJson = new JSONObject();
      dependencyJson.put("resourceIdentifier", dependency.getResourceIdentifier());
      dependencyJson.put("resourceVersion", dependency.getResourceVersion());
      dependencyJson.put("resourceDisplayName", dependency.getResourceDisplayName());
      dependencyJson.put("compatibilityInfo",compatibility != null ?
          compatibility.getCompatibilityInfoJson(): JSONObject.NULL);
      dependenciesJson.put(dependencyJson);
    }
    json.put("dependencies", dependenciesJson);
    json.put("isCompatible", compatibility != null ? compatibility.isCompatible() : JSONObject.NULL);

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
    json.put("age", System.currentTimeMillis() - dataset.getCreatedDate());

    // external users don't get to know who else it is shared with
    if (includeSharingData) {
      JSONArray sharesJson = new JSONArray();
      for (UserDatasetShareUser share : datasetInfo.getShares()) {
        JSONObject shareJson = new JSONObject();
        shareJson.put("email", share.getUser().getEmail());
        shareJson.put("user", share.getUser().getUserId());
        shareJson.put("time", share.getTimeShared());
        shareJson.put("userDisplayName", share.getUser().getDisplayName());
        sharesJson.put(shareJson);
      }

      json.put("sharedWith", sharesJson);
      // Convert quota from megabytes to bytes
      long quota = 1000000 * datasetInfo.getOwnerQuota();
      // Display quota in gigabytes (more appropriate place would be once per listing but that
      // requires a change from JSONArray to JSONObject).
      json.put("quota", datasetInfo.getOwnerQuota() / 1000.0);
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



    public static JSONArray getUserDatasetJBrowseJson(UserDatasetSession dsSession, UserDatasetInfo datasetInfo, boolean includeSharingData, boolean detailedData, String publicOrganismAbbrev) throws WdkModelException {

        JSONArray samplesJson = new JSONArray();

        String genomeSuffix = "_" + publicOrganismAbbrev + "_Genome";
        int maxScore = 1000;

        UserDataset dataset = datasetInfo.getDataset();
        UserDatasetType type = dataset.getType();
        String datasetType = type.getName();
        if(datasetType.equals("RnaSeq"))
           datasetType = "RNASeq";

        boolean matchesOrganismAbbrev = false;

        for (UserDatasetDependency dependency : dataset.getDependencies()) {
            if(dependency.getResourceIdentifier().endsWith(genomeSuffix)) 
                matchesOrganismAbbrev = true;
        }

        if(!matchesOrganismAbbrev) 
            return samplesJson;


        Long datasetId = dataset.getUserDatasetId();
        UserDatasetMeta datasetMeta = dataset.getMeta();
        String datasetName = datasetMeta.getName();
        String datasetSummary = datasetMeta.getSummary();

        for (UserDatasetFile file : dataset.getFiles().values()) {
            String fileName = file.getFileName(dsSession);
            if(!fileName.toUpperCase().endsWith(".BW"))
                continue ;

            String urlTemplate = "/a/service/users/current/user-datasets/" + datasetId + "/user-datafiles/" + fileName;

            JSONObject json = new JSONObject();
            json.put("storeClass", "JBrowse/Store/SeqFeature/BigWig");
            json.put("urlTemplate", urlTemplate);
            json.put("yScalePosition",  "left");
            json.put("key", datasetName + " " + fileName);
            json.put("label", datasetName + " " + fileName);
            json.put("type", "JBrowse/View/Track/Wiggle/XYPlot");
            json.put("category", "My Data from Galaxy");
            json.put("min_score",0);
            json.put("max_score", maxScore);


            JSONObject metadata = new JSONObject();
            metadata.put("subcategory", datasetType);
            metadata.put("dataset", datasetName);
            metadata.put("trackType", "Coverage");
            metadata.put("mdescription", datasetSummary);

            json.put("metadata", metadata);

            samplesJson.put(json);
        }
        
        return samplesJson;
  }


  public static JSONObject getUserDatasetSharesJson(UserFactory userFactory, Map<String, Map<Long, Set<Long>>> userDatasetShareMap) throws WdkModelException {
    JSONObject json = new JSONObject();
    for (String key : userDatasetShareMap.keySet()) {
      if ("add".equals(key) || "delete".equals(key)) {
    	JSONObject jsonOperation = new JSONObject();
        Set<Long> targetDatasetIds = userDatasetShareMap.get(key).keySet();
        for (Long targetDatasetId : targetDatasetIds) {
          JSONArray sharesJson = new JSONArray();
          for (long targetUserId : userDatasetShareMap.get(key).get(targetDatasetId)) {
            JSONObject shareJson = new JSONObject();
            User user = userFactory.getUserById(targetUserId);
            shareJson.put("email", user.getEmail());
            shareJson.put("user", user.getUserId());
            shareJson.put("userDisplayName", user.getDisplayName());
            sharesJson.put(shareJson);
          }
          jsonOperation.put(Long.toString(targetDatasetId), sharesJson);
        }
        json.put(key, jsonOperation);
      }
    }
    return json;
  }

}
