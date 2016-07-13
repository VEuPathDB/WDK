package org.gusdb.wdk.model.user.dataset.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetDependency;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.UserDatasetMeta;
import org.gusdb.wdk.model.user.dataset.UserDatasetShare;
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
  private UserDatasetType type;
  private Integer ownerId;
  private Date created;
  private Date modified;
  private Date uploaded;
  private Integer size;
  private Map<Integer, JsonUserDatasetShare> sharesMap = new HashMap<Integer, JsonUserDatasetShare>();
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
  
  // TODO: consider active validation of the JSONObject
  private void unpackJsonObject(JSONObject jsonObject) throws WdkModelException {
    try {
      this.meta = new JsonUserDatasetMeta(jsonObject.getJSONObject(META));
      this.type = JsonUserDatasetTypeFactory.getUserDatasetType(jsonObject.getJSONObject(TYPE));
      this.ownerId = jsonObject.getInt(OWNER);
      this.size = jsonObject.getInt(SIZE);
      this.created = new SimpleDateFormat().parse(jsonObject.getString(CREATED));
      this.modified = new SimpleDateFormat().parse(jsonObject.getString(MODIFIED));
      this.uploaded = new SimpleDateFormat().parse(jsonObject.getString(UPLOADED));
      
      JSONArray dependenciesJson = jsonObject.getJSONArray(DEPENDENCIES);
      for (int i=0; i<dependenciesJson.length(); i++) 
        dependencies.add(new JsonUserDatasetDependency(dependenciesJson.getJSONObject(i)));
      
      // shares is optional on input.  create an empty one if absent.
      if (!jsonObject.has(SHARES)) jsonObject.put(SHARES, new JSONArray());
      JSONArray sharesJson = jsonObject.getJSONArray(SHARES);
      for (int i=0; i<sharesJson.length(); i++) {
        JsonUserDatasetShare s = new JsonUserDatasetShare(sharesJson.getJSONObject(i));
        sharesMap.put(s.getUserId(), s);   
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
  public UserDatasetFile getFile(String name) {
    return dataFiles.get(name);
  }

  @Override
  public Set<UserDatasetShare> getSharedWith() {
    Set<UserDatasetShare> shares = new HashSet<UserDatasetShare>();
    for (JsonUserDatasetShare share : sharesMap.values()) shares.add(share);
    return Collections.unmodifiableSet(shares);
  }

  @Override
  public void shareWith(Integer userId) {
   if (!sharesMap.containsKey(userId)) {
     JsonUserDatasetShare s = new JsonUserDatasetShare(userId);
     sharesMap.put(userId, s);
   }
  }

  @Override
  public void unshareWith(Integer userId) {
    if (sharesMap.containsKey(userId)) {
      sharesMap.remove(userId);
    }
  }

  @Override
  public void unshareWithAllUsers() {
    sharesMap = new HashMap<Integer, JsonUserDatasetShare>();
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

  // TODO: is this the right way to handle this subclassing?
  @Override
  public Set<UserDatasetDependency> getDependencies() {
    Set<UserDatasetDependency> d = new HashSet<UserDatasetDependency>();
    for (JsonUserDatasetDependency dependency : dependencies) d.add(dependency);
    return Collections.unmodifiableSet(d);
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
    return size;
  }

  @Override
  public Integer getPercentQuota(int quota) {
    return new Integer(size * 100 / quota);
  }

  public void updateMeta(JsonUserDatasetMeta meta) {
    this.meta = meta;
  }
  
  public JSONObject getJsonObject() {
    
    // make sure the mutable stuff is up-to-date
    JSONArray sharesJson = new JSONArray();
    for (JsonUserDatasetShare share : sharesMap.values()) sharesJson.put(share.getJsonObject());
    jsonObject.put(SHARES, sharesJson);
    
    jsonObject.put(META, meta.getJsonObject());
    
    return jsonObject;
  }
}
