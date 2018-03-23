package org.gusdb.wdk.model.user.dataset.json;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetDependency;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.UserDatasetMeta;
import org.gusdb.wdk.model.user.dataset.UserDatasetSession;
import org.gusdb.wdk.model.user.dataset.UserDatasetType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A data container representing a User Dataset.  It can construct from and serialize 
 * to JSONObject.  
 * @author steve
 *
 */
public class JsonUserDataset implements UserDataset {

  private static final String TYPE = "type";
  private static final String OWNER = "owner";
  private static final String SHARES  = "shares";
  private static final String CREATED  = "created";
  private static final String DEPENDENCIES  = "dependencies";
  private static final String SIZE  = "size";
  private static final String PROJECTS  = "projects";
  private static final String NAME  = "name";
  private static final String DATA_FILES  = "dataFiles";
  
  private Long userDatasetId;
  private JsonUserDatasetMeta meta;
  private UserDatasetType type;
  private Long ownerId;
  private Long created;
  private Integer size;
  private Map<Long, JsonUserDatasetShare> sharesMap = new HashMap<>();
  private Map<String, UserDatasetFile> dataFiles = new HashMap<>();
  private Set<UserDatasetDependency> dependencies = new HashSet<>();
  private Set<String> projects = new HashSet<>();
  private JSONObject datasetJsonObject;
  private JSONObject metaJsonObject;
  
  /**
   * Construct from jsonObject, eg, when info is provided from larger json file
   * @param datasetJsonObject
   * @throws WdkModelException
   */
  public JsonUserDataset(Long userDatasetId, JSONObject datasetJsonObject, JSONObject metaJsonObject, Path dataFilesDir, JsonUserDatasetSession session) throws WdkModelException {
    this.userDatasetId = userDatasetId;
    this.datasetJsonObject = datasetJsonObject;
    unpackJson(datasetJsonObject, metaJsonObject, dataFilesDir, session);
  }
  
  // TODO: consider active validation of the JSONObject
  private void unpackJson(JSONObject datasetJsonObj, JSONObject metaJsonObj, Path dataFilesDir, JsonUserDatasetSession session) throws WdkModelException {
    try {
      this.meta = new JsonUserDatasetMeta(metaJsonObj);
      this.type = JsonUserDatasetTypeFactory.getUserDatasetType(datasetJsonObj.getJSONObject(TYPE));
      this.ownerId = datasetJsonObj.getLong(OWNER);
      this.size = datasetJsonObj.getInt(SIZE);
      this.created = datasetJsonObj.getLong(CREATED);
      
      JSONArray dependenciesJson = datasetJsonObj.getJSONArray(DEPENDENCIES);
      for (int i=0; i<dependenciesJson.length(); i++) 
        dependencies.add(new JsonUserDatasetDependency(dependenciesJson.getJSONObject(i)));
          
      JSONArray projectsJson = datasetJsonObj.getJSONArray(PROJECTS);
      for (int i=0; i<projectsJson.length(); i++) 
        projects.add(projectsJson.getString(i));
      
      JSONArray dataFilesJson = datasetJsonObj.getJSONArray(DATA_FILES);
      for (int i=0; i<dataFilesJson.length(); i++) {
        JSONObject dataFileJson = dataFilesJson.getJSONObject(i);
        String name = dataFileJson.getString(NAME);
        Long size = dataFileJson.getLong(SIZE);
        UserDatasetFile udf = session.getUserDatasetFile(dataFilesDir.resolve(name), userDatasetId);
        udf.setSize(size);
        dataFiles.put(name, udf);
      }

    } catch (JSONException e) {
      throw new WdkModelException(e);
    }
  }
  
  @Override
  public Long getUserDatasetId() {
    return userDatasetId;
  }
  
  @Override
  public Long getOwnerId() {
    return ownerId;
  }
  
  @Override
  public UserDatasetMeta getMeta() {
    return meta;
  }
  
  @Override
  public UserDatasetType getType() {
    return type;
  }

  @Override
  public Integer getNumberOfDataFiles() {
    return dataFiles.size();
  }

  @Override
  public Map<String, UserDatasetFile> getFiles() {
    return Collections.unmodifiableMap(dataFiles);
  }

  @Override
  public UserDatasetFile getFile(UserDatasetSession dsSession, String name) {
    return dataFiles.get(name);
  }

  @Override
  public Long getCreatedDate() {
    return created;
  }

  @Override
  public Set<UserDatasetDependency> getDependencies() {
    return Collections.unmodifiableSet(dependencies);
  }

  @Override
  public Integer getSize() {
    return size;
  }

  @Override
  public Integer getPercentQuota(int quota) {
    return new Integer(size * 100 / quota);
  }
  
  /**
   * Used for serializing to dataset store
   * @return
   */
  public JSONObject getDatasetJsonObject() {
    
    // make sure the mutable stuff is up-to-date
    JSONArray sharesJson = new JSONArray();
    for (JsonUserDatasetShare share : sharesMap.values()) sharesJson.put(share.getJsonObject());
    datasetJsonObject.put(SHARES, sharesJson);
    
    return datasetJsonObject;
  }
  
  /**
   * used for serializing to dataset store
   * @return
   */
  public JSONObject getMetaJsonObject() {
    return metaJsonObject;
  }

  @Override
  public Set<String> getProjects() throws WdkModelException {
    return  Collections.unmodifiableSet(projects);
  }
}
