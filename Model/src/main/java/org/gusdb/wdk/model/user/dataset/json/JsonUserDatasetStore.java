package org.gusdb.wdk.model.user.dataset.json;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
  
  @Override
  public void initialize(Map<String, String> configuration, Set<UserDatasetTypeHandler> typeHandlers) throws WdkModelException {
    String pathName = configuration.get("rootPath");
    if (pathName == null)
      throw new WdkModelException("Required configuration 'rootPath' not found.");
    usersRootDir = Paths.get(pathName);

    if (isDirectory(usersRootDir))
      throw new WdkModelException(
          "Provided property 'rootPath' has value '" + pathName + "' which is not an existing directory");
    
    for (UserDatasetTypeHandler handler : typeHandlers) typeHandlersMap.put(handler.getUserDatasetType(), handler);
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
  protected abstract void fillDatasetsMap(Path userDatasetsDir, Map<Integer, UserDataset> datasetsMap) throws WdkModelException;

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
    putFilesIntoMap(dataFilesDir, dataFilesMap);
  }
  
  protected abstract void putFilesIntoMap(Path dir, Map<String, UserDatasetFile> filesMap) throws WdkModelException;

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
    if (!isDirectory(datafilesDir))
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
    
    String contents = readFileContents(jsonFile);

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
   * Read the contents of the provided file into a string
   * @param file
   * @return
   * @throws WdkModelException
   */
  protected abstract String readFileContents(Path file) throws WdkModelException;

  /**
   * Return true if the provided path is a directory that exists
   * @param dir
   * @return
   * @throws WdkModelException
   */
  protected abstract boolean isDirectory(Path dir) throws WdkModelException;

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
  
  /**
   * Atomically write the provided contents to the provided path
   * @param file
   * @param contents
   * @throws WdkModelException
   */
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
    if (!isDirectory(recipientExternalDatasetsDir))
      createDirectory(recipientExternalDatasetsDir);
    Path externalDatasetFileName = recipientExternalDatasetsDir.resolve(
        getExternalDatasetFileName(ownerUserId, datasetId));
    writeFile(externalDatasetFileName, new String(""));

  }
 
  protected abstract void createDirectory(Path dir) throws WdkModelException;
  
  protected abstract void writeFile(Path file, String contents) throws WdkModelException;
  
  /**
   * Delete a dataset.  But, don't delete externalUserDataset references to it.  The UI
   * will let the recipient user of them know they are dangling.
   */
  @Override
  public void deleteUserDataset(Integer userId, Integer datasetId) throws WdkModelException {
    deleteFileOrDirectory(getUserDatasetsDir(userId, false).resolve(datasetId.toString()));
  }
  
  /**
   * Delete the provided file or dir
   * @param fileOrDir
   * @throws WdkModelException
   */
  protected abstract void deleteFileOrDirectory(Path fileOrDir) throws WdkModelException;

  @Override
  public void deleteExternalUserDataset(Integer ownerUserId, Integer datasetId, Integer recipientUserId) throws WdkModelException {
    Path recipientExternalDatasetsDir = getUserDatasetsDir(recipientUserId, true).resolve(EXTERNAL_DATASETS_DIR);
    Path recipientRemovedDatasetsDir = getUserDatasetsDir(recipientUserId, true).resolve(REMOVED_EXTERNAL_DATASETS_DIR);
    deleteExternalUserDataset(recipientExternalDatasetsDir, recipientRemovedDatasetsDir, ownerUserId, datasetId);
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
    if (isDirectory(recipientRemovedDatasetsDir))
      createDirectory(recipientRemovedDatasetsDir);
    Path externalDatasetFileName = recipientExternalDatasetsDir.resolve(
        getExternalDatasetFileName(ownerUserId, datasetId));
    Path moveToFileName = recipientRemovedDatasetsDir.resolve(
        getExternalDatasetFileName(ownerUserId, datasetId));
    moveFileAtomic(externalDatasetFileName, moveToFileName);
  }
  
  protected abstract void moveFileAtomic(Path from, Path to) throws WdkModelException;

  @Override
  public Date getModificationTime(Integer userId) throws WdkModelException {
    return getModificationTime(getUserDatasetsDir(userId, false));
  }

  /**
   * get the last mod time of a file or dir
   * @param fileOrDir
   * @return
   * @throws WdkModelException
   */
  protected abstract Date getModificationTime(Path fileOrDir) throws WdkModelException;

  protected Path getUserDir(Integer userId, boolean createPathIfAbsent) throws WdkModelException {
    Path userDir = usersRootDir.resolve(userId.toString());
    if (!isDirectory(userDir)) {
      if (createPathIfAbsent) createDirectory(userDir);
      else throw new WdkModelException("Can't find user directory " + userDir);
    }
    return userDir;
  }
  
  @Override
  public Integer getQuota(Integer userId) throws WdkModelException {
    Path quotaFile;
    
    Path defaultQuotaFile = usersRootDir.resolve("default_quota");
    Path userQuotaFile = getUserDir(userId, false).resolve("quota");
    quotaFile = fileExists(userQuotaFile)? userQuotaFile : defaultQuotaFile;
    return getQuota(quotaFile);
  }
  
  protected Integer getQuota(Path quotaFile) throws WdkModelException {
    String line = readSingleLineFile(quotaFile);
    if (line == null)
      throw new WdkModelException("Empty quota file " + quotaFile);
    return new Integer(line.trim());
  }
  
  protected abstract String readSingleLineFile(Path file) throws WdkModelException;
  
  /**
   * return true if the file exists
   * @param file
   * @return
   */
  protected abstract boolean fileExists(Path file);
    
  /**
   * Given a user ID, return a Path to that user's datasets dir.
   * @param userId
   * @param createPathIfAbsent If the path doesn't exist make it.  If this is false, then err if absent
   * @return
   * @throws WdkModelException
   */
  protected Path getUserDatasetsDir(Integer userId, boolean createPathIfAbsent) throws WdkModelException {
    Path userDatasetsDir = getUserDir(userId, createPathIfAbsent).resolve("datasets");

    if (!isDirectory(userDatasetsDir))
      if (createPathIfAbsent)
        createDirectory(userDatasetsDir);
      else
        throw new WdkModelException("Can't find user datasets directory " + userDatasetsDir);

    return userDatasetsDir;
  }

}
