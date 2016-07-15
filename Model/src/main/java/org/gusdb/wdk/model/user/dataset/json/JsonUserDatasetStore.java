package org.gusdb.wdk.model.user.dataset.json;

import java.nio.file.Path;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetCompatibility;
import org.gusdb.wdk.model.user.dataset.UserDatasetStore;
import org.gusdb.wdk.model.user.dataset.UserDatasetType;
import org.gusdb.wdk.model.user.dataset.UserDatasetTypeHandler;
import org.json.JSONObject;

/**
 * An abstract implementation of UserDatasetStore that uses the JSON based
 * objects in this package, as well as java.nio.file.Path (but no other nio classes).
 * Since Path is just a file path, should be compatible with other file systems
 * (such as iRODS).
 * 
 * This is the directory structure:
 * 
 rootPath/
  default_quota
  u12345/              # files in this directory are user readable, but not writeable
    quota              # can put this here, if increased from default
    datasets/
      d34541/
      d67690/
        datafiles/        
           Blah.bigwig    
           blah.profile
        dataset.json
        meta.json      # we have this as a separate file to avoid race conditions with sharing updates
    externalDatasets/
      43425.12592    # reference to dataset in another user.  empty file, name: user_id.dataset_id
      691056.53165 
    removedExternalDatasets/   # holds shares this user no longer wants to see.
      502401.90112

 * @author steve
 *
 */

public abstract class JsonUserDatasetStore implements UserDatasetStore {
  
  protected static final String DATASET_JSON_FILE = "dataset.json";
  protected static final String META_JSON_FILE = "meta.json";
  protected static final String EXTERNAL_DATASETS_DIR = "externalDatasets";
  protected static final String REMOVED_EXTERNAL_DATASETS_DIR = "removedExternalDatasets";
  protected Map<UserDatasetType, UserDatasetTypeHandler> typeHandlersMap;

  @Override
  public void initialize(Map<String, String> configuration, Set<UserDatasetTypeHandler> typeHandlers) throws WdkModelException {
    initialize(configuration);
    for (UserDatasetTypeHandler handler : typeHandlers) typeHandlersMap.put(handler.getUserDatasetType(), handler);
  }
  
  protected abstract void initialize(Map<String, String> configuration) throws WdkModelException;

  @Override
  public abstract Map<Integer, UserDataset> getUserDatasets(Integer userId) throws WdkModelException;

  @Override
  public JsonUserDataset getUserDataset(Integer userId, Integer datasetId) throws WdkModelException {
    Path userDatasetsDir = getUserDatasetsDir(userId, false);
    Path datasetDir = userDatasetsDir.resolve(datasetId.toString());
    return getUserDataset(datasetDir);
  }
  
  protected abstract Path getUserDatasetsDir(Integer userId, boolean createPathIfAbsent) throws WdkModelException;

  protected abstract JsonUserDataset getUserDataset(Path datasetDir) throws WdkModelException;

  /**
   * Check if a dataset is compatible with this application, based on its type and data dependencies.
   * @param userDataset
   * @return
   * @throws WdkModelException 
   */
  @Override
  public UserDatasetCompatibility getCompatibility(UserDataset userDataset) throws WdkModelException {
    UserDatasetType type = userDataset.getType();
    if (!typeHandlersMap.containsKey(type)) 
      return new UserDatasetCompatibility(false, "Type " + type + " is not supported.");
    return typeHandlersMap.get(type).getCompatibility(userDataset);
  }

  @Override
  public void updateMetaFromJson(Integer userId, Integer datasetId, JSONObject metaJson) throws WdkModelException {
    JsonUserDatasetMeta metaObj = new JsonUserDatasetMeta(metaJson);  // validate the input json
    Path metaJsonFile = getUserDatasetsDir(userId, false).resolve(datasetId.toString()).resolve(META_JSON_FILE);
    writeFileAtomic(metaJsonFile, metaObj.getJsonObject().toString());
  }
  
  protected void writeJsonUserDataset(JsonUserDataset dataset) throws WdkModelException {
    Path datasetJsonFile = getUserDatasetsDir(dataset.getOwnerId(), false).resolve(dataset.getUserDatasetId().toString()).resolve(DATASET_JSON_FILE);
    writeFileAtomic(datasetJsonFile, dataset.getDatasetJsonObject().toString());
  }
  
  protected abstract void writeFileAtomic(Path file, String contents) throws WdkModelException;
  
  @Override
  public void shareUserDatasets(Integer ownerUserId, Set<Integer> datasetIds, Set<Integer> recipientUserIds) throws WdkModelException {
    Set<Integer[]> externalDatasetLinks = new HashSet<Integer[]>();
    for (Integer datasetId : datasetIds) {
      JsonUserDataset dataset = getUserDataset(ownerUserId, datasetId);
      for (Integer recipientUserId : recipientUserIds) {
        dataset.shareWith(recipientUserId);
        Integer[] linkInfo = {ownerUserId, datasetId};
        externalDatasetLinks.add(linkInfo);
      }
      writeJsonUserDataset(dataset);  // write this before the links
      for (Integer[] linkInfo : externalDatasetLinks) 
        writeExternalDatasetLink(ownerUserId, linkInfo[0], linkInfo[1]);
    }    
  }
  
  /**
   * Get a file name to use as a link from a recipient to a shared dataset in the owner's space.
   * @param ownerUserId
   * @param datasetId
   * @return
   */
  protected String getExternalDatasetFileName(Integer ownerUserId, Integer datasetId) {
    return ownerUserId + "." + datasetId;
  }

  protected abstract void writeExternalDatasetLink(Integer ownerUserId, Integer datasetId, Integer recipientUserId) throws WdkModelException;

  @Override
  public abstract void deleteUserDataset(Integer userId, Integer datasetId) throws WdkModelException;

  @Override
  public abstract void deleteExternalUserDataset(Integer ownerUserId, Integer datasetId, Integer recipientUserId) throws WdkModelException;

  @Override
  public abstract Date getModificationTime(Integer userId) throws WdkModelException;

  @Override
  public abstract Integer getQuota(Integer userId) throws WdkModelException;
}
