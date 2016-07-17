package org.gusdb.wdk.model.user.dataset.json;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetCompatibility;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.UserDatasetStore;
import org.gusdb.wdk.model.user.dataset.UserDatasetType;
import org.gusdb.wdk.model.user.dataset.UserDatasetTypeHandler;
import org.json.JSONException;
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

  private Path usersRootDir;
  private JsonUserDatasetStoreAdaptor adaptor;
  
  public JsonUserDatasetStore(JsonUserDatasetStoreAdaptor adaptor) {
    this.adaptor = adaptor;
  }
  
  @Override
  public void initialize(Map<String, String> configuration, Set<UserDatasetTypeHandler> typeHandlers) throws WdkModelException {
    String pathName = configuration.get("rootPath");
    if (pathName == null)
      throw new WdkModelException("Required configuration 'rootPath' not found.");
    usersRootDir = Paths.get(pathName);

    if (adaptor.isDirectory(usersRootDir))
      throw new WdkModelException(
          "Provided property 'rootPath' has value '" + pathName + "' which is not an existing directory");
    
    for (UserDatasetTypeHandler handler : typeHandlers) typeHandlersMap.put(handler.getUserDatasetType(), handler);
  }
  
  public Date getModificationTime(Integer userId) throws WdkModelException {
    return adaptor.getModificationTime(getUserDatasetsDir(userId, false));
  }

  @Override
  public Map<Integer, UserDataset> getUserDatasets(Integer userId) throws WdkModelException {

    Path userDatasetsDir = getUserDatasetsDir(userId, false);

    // iterate through datasets, creating a UD from each
    Map<Integer, UserDataset> datasetsMap = new HashMap<Integer, UserDataset>();
    fillDatasetsMap(userDatasetsDir, datasetsMap);
    return Collections.unmodifiableMap(datasetsMap);
  }
  
  /**
   * Using the provided user datasets dir, fill in the provided map with all datasets in that dir.
   * The map is from dataset ID to dataset
   * @param userDatasetsDir
   * @param datasetsMap
   * @throws WdkModelException
   */
  protected void fillDatasetsMap(Path userDatasetsDir, Map<Integer, UserDataset> datasetsMap)
      throws WdkModelException {
    List<Path> datasetDirs = adaptor.getPathsInDir(userDatasetsDir);
    for (Path datasetDir : datasetDirs) {
      UserDataset dataset = getUserDataset(datasetDir);
      datasetsMap.put(dataset.getUserDatasetId(), dataset);
    }
  }
  
  @Override
  public JsonUserDataset getUserDataset(Integer userId, Integer datasetId) throws WdkModelException {
    Path userDatasetsDir = getUserDatasetsDir(userId, false);
    Path datasetDir = userDatasetsDir.resolve(datasetId.toString());
    return getUserDataset(datasetDir);
  }
  
  /**
   * Put all data files found in the provided directory into the map (from name to file)
   * @param dataFilesDir
   * @param dataFilesMap
   * @throws WdkModelException
   */
  protected void putDataFilesIntoMap(Path dataFilesDir, Map<String, UserDatasetFile> dataFilesMap) throws WdkModelException {
    adaptor.putFilesIntoMap(dataFilesDir, dataFilesMap);
  }
  
  /**
   * Construct a user dataset object, given its location in the store.
   * @param datasetDir
   * @return
   * @throws WdkModelException
   */
  protected JsonUserDataset getUserDataset(Path datasetDir) throws WdkModelException {

    Integer datasetId;
    try {
      datasetId = Integer.parseInt(datasetDir.getFileName().toString());
    }
    catch (NumberFormatException e) {
      throw new WdkModelException("Found file or directory '" + datasetDir.getFileName() +
          "' in user datasets directory " + datasetDir.getParent() + ". It is not a dataset ID");
    }
    
    JSONObject datasetJson = parseJsonFile(datasetDir.resolve("dataset.json")); ;
    JSONObject metaJson = parseJsonFile(datasetDir.resolve("meta.json"));

    Path datafilesDir = datasetDir.resolve("datafiles");
    if (!adaptor.isDirectory(datafilesDir))
      throw new WdkModelException("Can't find datafiles directory " + datafilesDir);

    Map<String, UserDatasetFile> dataFiles = new HashMap<String, UserDatasetFile>();

    return new JsonUserDataset(datasetId, datasetJson, metaJson, dataFiles);
  }
  
  /**
   * Read a dataset.json file, and return the JSONObject that it parses to.
   * @param jsonFile
   * @return
   * @throws WdkModelException
   */
  protected JSONObject parseJsonFile(Path jsonFile) throws WdkModelException {
    
    String contents = adaptor.readFileContents(jsonFile);

    JSONObject json;
    try {
      json = new JSONObject(contents);
    }
    catch (JSONException e) {
      throw new WdkModelException("Could not parse " + jsonFile, e);
    }
    return json;
  }
  
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
    adaptor.writeFileAtomic(metaJsonFile, metaObj.getJsonObject().toString());
  }
  
  protected void writeJsonUserDataset(JsonUserDataset dataset) throws WdkModelException {
    Path datasetJsonFile = getUserDatasetsDir(dataset.getOwnerId(), false).resolve(dataset.getUserDatasetId().toString()).resolve(DATASET_JSON_FILE);
    adaptor.writeFileAtomic(datasetJsonFile, dataset.getDatasetJsonObject().toString());
  }
    
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

  /**
   * Write a file in a user's space, indicating that this user can see another user's dataset.
   * @param ownerUserId
   * @param datasetId
   * @param recipientUserId
   * @throws WdkModelException
   */
  protected void writeExternalDatasetLink(Integer ownerUserId, Integer datasetId, Integer recipientUserId) throws WdkModelException {
    writeExternalDatasetLink(getUserDatasetsDir(recipientUserId, true).resolve(EXTERNAL_DATASETS_DIR), ownerUserId, datasetId);
  }
  
  protected void writeExternalDatasetLink(Path recipientExternalDatasetsDir, Integer ownerUserId, Integer datasetId) throws WdkModelException {
    if (!adaptor.isDirectory(recipientExternalDatasetsDir))
      adaptor.createDirectory(recipientExternalDatasetsDir);
    Path externalDatasetFileName = recipientExternalDatasetsDir.resolve(
        getExternalDatasetFileName(ownerUserId, datasetId));
    adaptor.writeFile(externalDatasetFileName, new String(""));

  }
 
  
  /**
   * Delete a dataset.  But, don't delete externalUserDataset references to it.  The UI
   * will let the recipient user of them know they are dangling.
   */
  @Override
  public void deleteUserDataset(Integer userId, Integer datasetId) throws WdkModelException {
    adaptor.deleteFileOrDirectory(getUserDatasetsDir(userId, false).resolve(datasetId.toString()));
  }
  
  /**
   * Delete a link to an external dataset (specified by ownerUserId,datasetId).  Move the link from
   * the external datasets dir to the removed external datasets dir
   * @param recipientExternalDatasetsDir
   * @param recipientRemovedDatasetsDir
   * @param ownerUserId
   * @param datasetId
   * @throws WdkModelException
   */
  public void deleteExternalUserDataset(Path recipientExternalDatasetsDir, Path recipientRemovedDatasetsDir, Integer ownerUserId, Integer datasetId) throws WdkModelException {
    if (adaptor.isDirectory(recipientRemovedDatasetsDir))
      adaptor.createDirectory(recipientRemovedDatasetsDir);
    Path externalDatasetFileName = recipientExternalDatasetsDir.resolve(
        getExternalDatasetFileName(ownerUserId, datasetId));
    Path moveToFileName = recipientRemovedDatasetsDir.resolve(
        getExternalDatasetFileName(ownerUserId, datasetId));
    adaptor.moveFileAtomic(externalDatasetFileName, moveToFileName);
  }
  
  @Override
  public void deleteExternalUserDataset(Integer ownerUserId, Integer datasetId, Integer recipientUserId) throws WdkModelException {
    Path recipientExternalDatasetsDir = getUserDatasetsDir(recipientUserId, true).resolve(EXTERNAL_DATASETS_DIR);
    Path recipientRemovedDatasetsDir = getUserDatasetsDir(recipientUserId, true).resolve(REMOVED_EXTERNAL_DATASETS_DIR);
    deleteExternalUserDataset(recipientExternalDatasetsDir, recipientRemovedDatasetsDir, ownerUserId, datasetId);
  }

  protected Path getUserDir(Integer userId, boolean createPathIfAbsent) throws WdkModelException {
    Path userDir = usersRootDir.resolve(userId.toString());
    if (!adaptor.isDirectory(userDir)) {
      if (createPathIfAbsent) adaptor.createDirectory(userDir);
      else throw new WdkModelException("Can't find user directory " + userDir);
    }
    return userDir;
  }
  
  @Override
  public Integer getQuota(Integer userId) throws WdkModelException {
    Path quotaFile;
    
    Path defaultQuotaFile = usersRootDir.resolve("default_quota");
    Path userQuotaFile = getUserDir(userId, false).resolve("quota");
    quotaFile = adaptor.fileExists(userQuotaFile)? userQuotaFile : defaultQuotaFile;
    return getQuota(quotaFile);
  }
  
  protected Integer getQuota(Path quotaFile) throws WdkModelException {
    String line = adaptor.readSingleLineFile(quotaFile);
    if (line == null)
      throw new WdkModelException("Empty quota file " + quotaFile);
    return new Integer(line.trim());
  }
      
  /**
   * Given a user ID, return a Path to that user's datasets dir.
   * @param userId
   * @param createPathIfAbsent If the path doesn't exist make it.  If this is false, then err if absent
   * @return
   * @throws WdkModelException
   */
  protected Path getUserDatasetsDir(Integer userId, boolean createPathIfAbsent) throws WdkModelException {
    Path userDatasetsDir = getUserDir(userId, createPathIfAbsent).resolve("datasets");

    if (!adaptor.isDirectory(userDatasetsDir))
      if (createPathIfAbsent)
        adaptor.createDirectory(userDatasetsDir);
      else
        throw new WdkModelException("Can't find user datasets directory " + userDatasetsDir);

    return userDatasetsDir;
  }

}
