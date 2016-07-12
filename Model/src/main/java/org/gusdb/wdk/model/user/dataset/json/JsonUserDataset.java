package org.gusdb.wdk.model.user.dataset.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetDependency;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.UserDatasetMeta;
import org.gusdb.wdk.model.user.dataset.UserDatasetShare;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUserDataset implements UserDataset {

  private static final String META = "meta";
  private static final String TYPE = "type";
  private static final String OWNER = "owner";
  private static final String SHARES  = "shares";
  private static final String CREATED  = "created";
  private static final String MODIFIED  = "modified";
  private static final String UPLOADED  = "uploaded";
  private static final String DEPENDENCIES  = "dependencies";
  private static final String SIZE  = "size";
  
  private JsonUserDatasetMeta meta;
  private String type;
  private Integer ownerId;
  private Date created;
  private Date modified;
  private Date uploaded;
  private Map<Integer, JsonUserDatasetShare> shares = new HashMap<Integer, JsonUserDatasetShare>();
  private Map<String, UserDatasetFile> dataFiles = new HashMap<String, UserDatasetFile>();
  private Set<JsonUserDatasetDependency> dependencies;
  private JSONObject jsonObject;
  
  /**
   * Construct from jsonObject, eg, when info is provided from larger json file
   * @param jsonObject
   * @throws WdkModelException
   */
  public JsonUserDataset(JSONObject jsonObject, Map<String, UserDatasetFile> dataFiles) throws WdkModelException {
    this.jsonObject = jsonObject;
    unpackJsonObject(jsonObject);
    this.dataFiles = dataFiles;
  }
  
  private void unpackJsonObject(JSONObject jsonObject) throws WdkModelException {
    try {
      this.meta = new JsonUserDatasetMeta(jsonObject.getJSONObject(META));
      this.type = jsonObject.getString(TYPE);
      this.ownerId = jsonObject.getInt(OWNER);
      this.created = new SimpleDateFormat().parse(jsonObject.getString(CREATED));
      this.modified = new SimpleDateFormat().parse(jsonObject.getString(MODIFIED));
      this.uploaded = new SimpleDateFormat().parse(jsonObject.getString(UPLOADED));
      JSONArray dependenciesJson = jsonObject.getJSONArray(DEPENDENCIES);
      for (int i=0; i<dependenciesJson.length(); i++) 
        dependencies.add(new JsonUserDatasetDependency(dependenciesJson.getJSONObject(i)));
      JSONArray sharesJson = jsonObject.getJSONArray(SHARES);
      for (int i=0; i<sharesJson.length(); i++) {
        JsonUserDatasetShare s = new JsonUserDatasetShare(sharesJson.getJSONObject(i));
        shares.put(s.getUserId(), s);     
      }
    } catch (JSONException | ParseException e) {
      throw new WdkModelException(e);
    }
  }
  
  @Override
  public Integer getOwnerId() {
    return ownerId;
  }
  @Override
  public UserDatasetMeta getMeta() {
    return meta;
  }

  @Override
  public String getType() {
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
  public UserDatasetFile getFile(String name) {
    return dataFiles.get(name);
  }

  @Override
  public Collection<UserDatasetShare> getSharedWith() {
    return Collections.unmodifiableCollection(shares.values());
  }

  @Override
  public void shareWith(Integer userId) {
   if (!shares.containsKey(userId))
     shares.put(userId, new JsonUserDatasetShare(userId));
  }

  @Override
  public void unshareWith(Integer userId) {
    if (shares.containsKey(userId))
      shares.remove(userId);
  }

  @Override
  public void unshareWithAllUsers() {
    shares = new HashMap<Integer, JsonUserDatasetShare>();
  }

  @Override
  public Date getCreateDate() {
    return created;
  }

  @Override
  public Date getModifiedDate() {
    return modified;
  }

  @Override
  public Date getUploadedDate() {
    return uploaded;
  }

  @Override
  public Set<UserDatasetDependency> getDependencies() {
    return Collections.unmodifiableSet(dependencies);
  }

  @Override
  public Boolean getIsCompatible() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getIncompatibleReason() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Integer getSize() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Integer getPercentQuota() {
    // TODO Auto-generated method stub
    return null;
  }

  public void updateMeta(JsonUserDatasetMeta meta) {
    this.meta = meta;
  }
}
