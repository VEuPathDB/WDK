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
 * A data container representing a User Dataset.  It can construct from and
 * serialize to JSONObject.
 *
 * @author steve
 */
public class
JsonUserDataset implements UserDataset {

  public static final String TYPE = "type";
  public static final String OWNER = "owner";
  public static final String SHARES  = "shares";
  public static final String CREATED  = "created";
  public static final String DEPENDENCIES  = "dependencies";
  public static final String SIZE  = "size";
  public static final String PROJECTS  = "projects";
  public static final String NAME  = "name";
  public static final String DATA_FILES  = "dataFiles";

  private final Map<Long, JsonUserDatasetShare> sharesMap = new HashMap<>();
  private final Map<String, UserDatasetFile> dataFiles = new HashMap<>();
  private final Set<UserDatasetDependency> dependencies = new HashSet<>();
  private final Set<String> projects = new HashSet<>();
  private final long userDatasetId;
  private final JSONObject datasetJsonObject;

  private JsonUserDatasetMeta meta;
  private UserDatasetType type;
  private long ownerId;
  private long created;
  private int size;
  private JSONObject metaJsonObject;

  /**
   * Construct from jsonObject, eg, when info is provided from larger json file
   */
  public JsonUserDataset(
    long userDatasetId,
    JSONObject datasetJsonObject,
    JSONObject metaJsonObject,
    Path dataFilesDir,
    JsonUserDatasetSession session
  ) throws WdkModelException {
    this.userDatasetId = userDatasetId;
    this.datasetJsonObject = datasetJsonObject;
    this.metaJsonObject = metaJsonObject;

    unpackJson(datasetJsonObject, metaJsonObject, dataFilesDir, session);
  }

  // TODO: consider active validation of the JSONObject
  private void unpackJson(
    JSONObject datasetJsonObj,
    JSONObject metaJsonObj,
    Path dataFilesDir,
    JsonUserDatasetSession session
  ) throws WdkModelException {
    try {
      this.meta = new JsonUserDatasetMeta(metaJsonObj);
      this.type = JsonUserDatasetTypeFactory.getUserDatasetType(datasetJsonObj.getJSONObject(TYPE));
      this.ownerId = datasetJsonObj.getLong(OWNER);
      this.size = datasetJsonObj.getInt(SIZE);
      this.created = datasetJsonObj.getLong(CREATED);

      var dependenciesJson = datasetJsonObj.getJSONArray(DEPENDENCIES);
      for (int i=0; i<dependenciesJson.length(); i++)
        dependencies.add(new JsonUserDatasetDependency(dependenciesJson.getJSONObject(i)));

      var projectsJson = datasetJsonObj.getJSONArray(PROJECTS);
      for (int i=0; i<projectsJson.length(); i++)
        projects.add(projectsJson.getString(i));

      var dataFilesJson = datasetJsonObj.getJSONArray(DATA_FILES);
      for (int i=0; i<dataFilesJson.length(); i++) {
        var dataFileJson = dataFilesJson.getJSONObject(i);
        var name = dataFileJson.getString(NAME);
        var udf  = session.getUserDatasetFile(dataFilesDir.resolve(name), userDatasetId);
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
    return size * 100 / quota;
  }

  @Override
  public Path getMetaJsonFile() {
    return metaJsonFile;
  }

  /**
   * Used for serializing to dataset store
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
   */
  public JSONObject getMetaJsonObject() {
    return metaJsonObject;
  }

  @Override
  public Set<String> getProjects() {
    return Collections.unmodifiableSet(projects);
  }
}
