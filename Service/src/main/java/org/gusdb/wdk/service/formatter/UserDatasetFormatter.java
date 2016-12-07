package org.gusdb.wdk.service.formatter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunner.ResultSetHandler;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetDependency;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.UserDatasetShare;
import org.gusdb.wdk.model.user.dataset.UserDatasetStore;
import org.gusdb.wdk.model.user.dataset.UserDatasetType;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserDatasetFormatter {

  public static JSONArray getUserDatasetsJson(Map<Integer, UserDataset> userDatasetsMap, Map<Integer, UserDataset> externalUserDatasetsMap, UserDatasetStore userDatasetStore, Set<Integer> installedUserDatasets, DataSource userDbDataSource, String userSchema, boolean expand) throws WdkModelException {
    JSONArray datasetsJson = new JSONArray();
    putDatasetsIntoJsonArray(datasetsJson, userDatasetsMap, userDatasetStore, installedUserDatasets, userDbDataSource, userSchema, false, expand); 
    putDatasetsIntoJsonArray(datasetsJson, externalUserDatasetsMap, userDatasetStore, installedUserDatasets, userDbDataSource, userSchema, true, expand); 
    return datasetsJson;
  }
  
  private static void putDatasetsIntoJsonArray(JSONArray datasetsJson, Map<Integer, UserDataset> userDatasetsMap, UserDatasetStore userDatasetStore, Set<Integer> installedUserDatasets, DataSource userDbDataSource, String userSchema, boolean isExternalDataset, boolean expand) throws WdkModelException {
    for (Integer datasetId : userDatasetsMap.keySet()) {
      if (!expand) datasetsJson.put(datasetId);
      else {
        UserDataset dataset = userDatasetsMap.get(datasetId);
//        UserDatasetCompatibility compat = userDatasetStore.getTypeHandler(dataset.getType()).getCompatibility(dataset, appDbDataSource);
        datasetsJson.put(getUserDatasetJson(dataset, userDatasetStore, installedUserDatasets, userDbDataSource, userSchema, isExternalDataset));
      }
    }
  }
  
  /**
   * Return a JSONObject describing this dataset.  Should not include the contents of data file
   * @return
   * @throws WdkModelException
   * 
   * 
{ id: 12345
  type: {name: "RNA Seq", version: "1.0"},   # our API version
  dependencies: 
     [{resourceIdentifier: "pf3d7_genome_rsrc", 
       resourceVersion: "12/2/2015",
       resourceDisplayName: "pfal genome"}, 
      {resourceIdentifier: "hsap_genome_rsrc", 
       resourceVersion: "2.6", 
       resourceDisplayName: "human genome"}
     ],
  projects: ["PlasmoDB", "ToxoDB"],
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
  sharedWith: [{user: 829424, emailName: "sfischer", time: 13252342341}, {user: 989921, emailName: "dfalke", time: 12332532332}],
  dataFiles: [{name: "blah", size: "10M"}],
  isInstalled: true
}

   */
  public static JSONObject getUserDatasetJson(UserDataset dataset, UserDatasetStore store, Set<Integer> installedUserDatasets, DataSource userDbDataSource, String userSchema, boolean isExternalDataset) throws WdkModelException {
    JSONObject json = new JSONObject();
    JSONObject typeJson = new JSONObject();
    UserDatasetType type = dataset.getType();
    typeJson.put("name", type.getName());
    typeJson.put("version", type.getVersion());
    json.put("id", dataset.getUserDatasetId());
    json.put("type", typeJson);
    json.put("isInstalled", installedUserDatasets.contains(dataset.getUserDatasetId()));

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
    for (String questionName : store.getTypeHandler(type).getRelevantQuestionNames()) questionsJson.put(questionName);
    json.put("questions", questionsJson);
    
    JSONObject metaJson = new JSONObject();
    metaJson.put("name", dataset.getMeta().getName());
    metaJson.put("description", dataset.getMeta().getDescription());
    metaJson.put("summary", dataset.getMeta().getSummary());
    json.put("meta", metaJson);
    json.put("owner", getUserDisplayName( dataset.getOwnerId(),  userSchema,  userDbDataSource)); 
    json.put("ownerUserId", dataset.getOwnerId());
    json.put("size", dataset.getSize());
    json.put("modified", dataset.getModifiedDate());
    json.put("created", dataset.getCreatedDate());
    json.put("uploaded", dataset.getUploadedDate());
    
    // external users don't get to know who else it is shared with
    if (!isExternalDataset) {
      JSONArray sharesJson = new JSONArray();
      for (UserDatasetShare share : store.getSharedWith(dataset.getOwnerId(), dataset.getUserDatasetId())) {
        JSONObject shareJson = new JSONObject();
        shareJson.put("user", share.getUserId());
        shareJson.put("time", share.getTimeShared());
        shareJson.put("userDisplayName", getUserDisplayName(share.getUserId(), userSchema, userDbDataSource));
        sharesJson.put(shareJson);
      }

      json.put("sharedWith", sharesJson);
      int quota = store.getQuota(dataset.getOwnerId());
      DecimalFormat df = new DecimalFormat("#.####");
      json.put("percentQuotaUsed", df.format(dataset.getSize() * 100.0 / quota));
    }
    else {
      json.put("percentQuotaUsed", 0);
    }
    
    JSONArray filesJson = new JSONArray();
    for (UserDatasetFile file : dataset.getFiles().values()) {
      JSONObject fileJson = new JSONObject();
      fileJson.put("name", file.getFileName());
      fileJson.put("size", file.getFileSize());
      filesJson.put(fileJson);
    }
    json.put("datafiles", filesJson);
    
    /* replace this with installation state, when we code that up.
    JSONObject compatJson = new JSONObject();
    compatJson.put("isCompatible", compatibility.isCompatible());
    compatJson.put("notCompatibleReason", compatibility.notCompatibleReason());
    json.put("compatibility", compatJson);
    */
    return json;
  }
  
  // this probably doesn't belong here, but it is not obvious where it does belong
  private static String getUserDisplayName(Integer userId, String userSchema, DataSource userDbDataSource) throws WdkModelException {

    final List<String[]> info = new ArrayList<String[]>();
    ResultSetHandler handler = new ResultSetHandler() {
      @Override
      public void handleResult(ResultSet rs) throws SQLException {
	if (rs.next()) {
	  String[] row = {rs.getString(1), rs.getString(2), rs.getString(3) };
	  info.add(row); // one row will be returned
	}
      }
    };

    String sql = "select email, first_name, last_name from " + userSchema + "users where user_id = ?";
    SQLRunner runner = new SQLRunner(userDbDataSource, sql, "user-email-ud-svc");
    Object[] args = {userId};
    runner.executeQuery(args, handler);
    if (info.isEmpty()) throw new WdkModelException("Can't find user id " + userId + "in user database");
    
    String[] row = info.get(0);
    String [] emailParts = row[0].split("@");
    String emailUserName = emailParts.length == 2? emailParts[0] : row[0]; // if can't parse email, use whole thing.  

    String firstName = row[1].trim();
    firstName = firstName == null? "" : firstName + " ";
    String lastName = row[2].trim();
    lastName = lastName == null? "" : lastName;
    String name = (firstName + lastName).trim();
    if (name.length() == 0) name = emailUserName;
    
    return name;
    
  }
 
}
