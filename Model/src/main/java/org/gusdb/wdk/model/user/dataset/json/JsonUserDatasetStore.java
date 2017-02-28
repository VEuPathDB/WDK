package org.gusdb.wdk.model.user.dataset.json;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.UserDatasetShare;
import org.gusdb.wdk.model.user.dataset.UserDatasetStore;
import org.gusdb.wdk.model.user.dataset.UserDatasetStoreAdaptor;
import org.gusdb.wdk.model.user.dataset.UserDatasetType;
import org.gusdb.wdk.model.user.dataset.UserDatasetTypeHandler;
import org.json.JSONException;
import org.json.JSONObject;
//import org.apache.log4j.Logger;


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
        /sharedWith
          54145        # a an empty file, named for the user that is shared with
          90912
    externalDatasets/
      43425.12592    # reference to dataset in another user.  empty file, name: user_id.dataset_id
      691056.53165 
    removedExternalDatasets/   # holds shares this user no longer wants to see.
      502401.90112

 * @author steve
 *
 */

public abstract class JsonUserDatasetStore implements UserDatasetStore {
  //private static final Logger LOG = Logger.getLogger(JsonUserDatasetStore.class);

  
  protected static final String DATASET_JSON_FILE = "dataset.json";
  protected static final String META_JSON_FILE = "meta.json";
  protected static final String EXTERNAL_DATASETS_DIR = "externalDatasets";
  protected static final String SHARED_WITH_DIR = "sharedWith";
  protected static final String REMOVED_EXTERNAL_DATASETS_DIR = "removedExternalDatasets";
  protected Map<UserDatasetType, UserDatasetTypeHandler> typeHandlersMap = new HashMap<UserDatasetType, UserDatasetTypeHandler>();
  
  private static final String NL = System.lineSeparator();

  protected Path usersRootDir;
  private UserDatasetStoreAdaptor adaptor;
  protected String id;
  
  public JsonUserDatasetStore(UserDatasetStoreAdaptor adaptor) {
    this.adaptor = adaptor;
  }
  
  @Override
  public void initialize(Map<String, String> configuration, Map<UserDatasetType, UserDatasetTypeHandler> typeHandlers) throws WdkModelException {
    String pathName = configuration.get("rootPath");
    if (pathName == null)
      throw new WdkModelException("Required configuration 'rootPath' not found.");
    usersRootDir = Paths.get(pathName);
    
    typeHandlersMap = typeHandlers;
  }
  
  public void checkRootDirExists() throws WdkModelException {
    if (!directoryExists(usersRootDir))
      throw new WdkModelException(
          "Provided property 'rootPath' has value '" + usersRootDir + "' which is not an existing directory");
  }

  @Override
  public Long getModificationTime(Integer userId) throws WdkModelException {
    return adaptor.getModificationTime(getUserDatasetsDir(userId));
  }

  @Override
  public Map<Integer, UserDataset> getUserDatasets(Integer userId) throws WdkModelException {

    Path userDatasetsDir = getUserDatasetsDir(userId);

    // iterate through datasets, creating a UD from each
    Map<Integer, UserDataset> datasetsMap = new HashMap<Integer, UserDataset>();
    fillDatasetsMap(userDatasetsDir, datasetsMap);
    return Collections.unmodifiableMap(datasetsMap);
  }
  
  @Override
  public Set<UserDatasetShare> getSharedWith(Integer ownerUserId, Integer datasetId) throws WdkModelException {

    Set<UserDatasetShare> sharedWithUsers = new HashSet<UserDatasetShare>();

    Path sharedWithDir = getSharedWithDir(ownerUserId, datasetId);
    
    List<Path> sharedWithPaths = adaptor.getPathsInDir(sharedWithDir);
    for (Path sharedWithPath : sharedWithPaths) {
      String userIdString = sharedWithPath.getFileName().toString();
      Long timestamp = adaptor.getModificationTime(sharedWithPath);
      sharedWithUsers.add(new JsonUserDatasetShare(new Integer(userIdString), timestamp));
    }
    return Collections.unmodifiableSet(sharedWithUsers);
  }
  
  private Path getSharedWithDir(Integer userId, Integer datasetId) throws WdkModelException {
    Path userDatasetDir = getUserDatasetDir(userId, datasetId);
    Path sharedWithDir = userDatasetDir.resolve(SHARED_WITH_DIR);
    if (!adaptor.fileExists(sharedWithDir)) {
      adaptor.createDirectory(sharedWithDir);
    }
    return sharedWithDir;
  }
  
  @Override
  public Map<Integer, UserDataset> getExternalUserDatasets(Integer userId) throws WdkModelException {

    Map<Integer, UserDataset> extDsMap = new HashMap<Integer, UserDataset>();
    Map<Integer, Map<Integer, UserDataset>> otherUsersCache = new HashMap<Integer, Map<Integer, UserDataset>>();

    Path externalDatasetsDir = getUserDir(userId).resolve(EXTERNAL_DATASETS_DIR);
    if (!adaptor.isDirectory(externalDatasetsDir)) return extDsMap;
    
    List<Path> externalLinkPaths = adaptor.getPathsInDir(externalDatasetsDir);
    for (Path externalLinkPath : externalLinkPaths) {
      ExternalDatasetLink link = new ExternalDatasetLink(externalLinkPath.getFileName());
      if (!checkUserDirExists(link.externalUserId)) throw new WdkModelException("User ID in external dataset link " + externalLinkPath.getFileName() + " is not a user in the datasets store");

      UserDataset originalDataset = followExternalDatasetLink(userId, link, otherUsersCache);

      if (originalDataset != null) extDsMap.put(originalDataset.getUserDatasetId(), originalDataset);
    }
    return Collections.unmodifiableMap(extDsMap);
  }
  
  /**
   * Get the user dataset pointed to by an external link
   * @param userId
   * @param ownerUserId
   * @param userDatasetId
   * @return null if this user doesn't have such an external link, or, if the linked dataset doesn't exist or isn't shared.
   * @throws WdkModelException
   * TODO: this method might not be needed here (or in the super class).
 
  @Override
  public UserDataset getExternalUserDataset(Integer userId, Integer ownerUserId, Integer userDatasetId) throws WdkModelException {

    Map<Integer, Map<Integer, UserDataset>> otherUsersCache = new HashMap<Integer, Map<Integer, UserDataset>>();

    Path externalDatasetsDir = getUserDir(userId).resolve(EXTERNAL_DATASETS_DIR);
    if (!adaptor.isDirectory(externalDatasetsDir)) return null;
    
    ExternalDatasetLink link = new ExternalDatasetLink(ownerUserId, userDatasetId);
    
    return followExternalDatasetLink(userId, link, otherUsersCache);
  }
  */
  
  /**
   * Find the user dataset pointed to by an external link
   * @param userId
   * @param link
   * @param otherUsersCache
   * @return null if not found
   * @throws WdkModelException
   */
  private UserDataset followExternalDatasetLink(Integer userId, ExternalDatasetLink link, Map<Integer, Map<Integer, UserDataset>> otherUsersCache) throws WdkModelException {
    if (!checkUserDirExists(link.externalUserId)) return null;
    
    // get the datasets belonging to the external user.  (use cache to avoid re-querying repeated user IDs)
    Map<Integer, UserDataset> datasetsOfExternalUser;
    
    if (otherUsersCache.containsKey(link.externalUserId)) {
      datasetsOfExternalUser = otherUsersCache.get(link.externalUserId);
    } else {
      datasetsOfExternalUser = getUserDatasets(link.externalUserId);
      otherUsersCache.put(link.externalUserId, datasetsOfExternalUser);
    }

    // if the external dataset does exist in the other user,
    // and if it is in fact shared with our user, then add it to the
    // map of found external datasets
    // TODO: report if we can't follow the link, because owner removed or unshared it
    if (datasetsOfExternalUser.containsKey(link.datasetId)) {
      
      // find the original dataset, and grab its declared shares
      UserDataset originalDataset = datasetsOfExternalUser.get(link.datasetId);
      Set<UserDatasetShare> shares = getSharedWith(link.externalUserId, link.datasetId);

      // see if our user is among the declared shares
      boolean found = false;
      for (UserDatasetShare share : shares) {
        if (share.getUserId().equals(userId)) {
          found = true;
          break;
        }
      }
      if (found) return originalDataset;
    }
    return null;
  }
  
  class ExternalDatasetLink {
    Integer externalUserId;
    Integer datasetId;
    
    
    ExternalDatasetLink(Integer  externalUserId, Integer datasetId) {
      this.externalUserId = externalUserId;
      this.datasetId = datasetId;
    }
    
    ExternalDatasetLink(Path externalLinkPath) throws WdkModelException {
      String linkName = externalLinkPath.getFileName().toString();
      String[] words = linkName.split(Pattern.quote("."));
      if (words.length != 2)
        throw new WdkModelException("Illegal external dataset link: " + linkName);
      try {
        externalUserId = new Integer(words[0]);
        datasetId = new Integer(words[1]);
      }
      catch (NumberFormatException e) {
        throw new WdkModelException("Illegal external dataset link: " + linkName);
      }  
    }
    
    String getFileName() {
      return "" + externalUserId + "." + datasetId;
    }
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
    Path userDatasetsDir = getUserDatasetsDir(userId);
    Path datasetDir = userDatasetsDir.resolve(datasetId.toString());
    return getUserDataset(datasetDir);
  }
  
  @Override
  public boolean getUserDatasetExists(Integer userId, Integer datasetId) throws WdkModelException {
    Path userDatasetsDir = getUserDatasetsDir(userId);
    if (!directoryExists(userDatasetsDir)) return false;
    Path datasetDir = userDatasetsDir.resolve(datasetId.toString());
    return directoryExists(datasetDir);
  }
  
  private Path getUserDatasetDir(Integer userId, Integer datasetId) throws WdkModelException {
    Path userDatasetsDir = getUserDatasetsDir(userId);
    return userDatasetsDir.resolve(datasetId.toString());
  }
  
  /**
   * Put all data files found in the provided directory into the map (from name to file)
   * @param dataFilesDir
   * @param dataFilesMap
   * @throws WdkModelException
   */
  protected void putDataFilesIntoMap(Path dataFilesDir, Map<String, UserDatasetFile> dataFilesMap, Integer userDatasetId) throws WdkModelException {
    for (Path path : adaptor.getPathsInDir(dataFilesDir))
      dataFilesMap.put(path.getFileName().toString(), getUserDatasetFile(path, userDatasetId));
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
    
    JSONObject datasetJson = parseJsonFile(datasetDir.resolve("dataset.json"));
    JSONObject metaJson = parseJsonFile(datasetDir.resolve("meta.json"));

    Path datafilesDir = datasetDir.resolve("datafiles");
    if (!directoryExists(datafilesDir))
      throw new WdkModelException("Can't find datafiles directory " + datafilesDir);
 
    Map<String, UserDatasetFile> dataFiles = new HashMap<String, UserDatasetFile>();

    for (Path dataFilePath : adaptor.getPathsInDir(datafilesDir)) {
      UserDatasetFile udf = getUserDatasetFile(dataFilePath, datasetId);
      dataFiles.put(udf.getFileName(), udf);
    }

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
  
  @Override
  public UserDatasetTypeHandler getTypeHandler(UserDatasetType type) {
    return typeHandlersMap.get(type);
  }
  
  @Override
  public void updateMetaFromJson(Integer userId, Integer datasetId, JSONObject metaJson) throws WdkModelException {
    JsonUserDatasetMeta metaObj = new JsonUserDatasetMeta(metaJson);  // validate the input json
    Path metaJsonFile = getUserDatasetsDir(userId).resolve(datasetId.toString()).resolve(META_JSON_FILE);
    writeFileAtomic(metaJsonFile, metaObj.getJsonObject().toString(), false);
  }
  
  protected void writeJsonUserDataset(JsonUserDataset dataset) throws WdkModelException {
    Path datasetJsonFile = getUserDatasetsDir(dataset.getOwnerId()).resolve(dataset.getUserDatasetId().toString()).resolve(DATASET_JSON_FILE);
    writeFileAtomic(datasetJsonFile, dataset.getDatasetJsonObject().toString(), false);
  }
  
  @Override
  public void shareUserDataset(Integer ownerUserId, Integer datasetId, Set<Integer> recipientUserIds) throws WdkModelException {
	Set<Integer[]> externalDatasetLinks = new HashSet<Integer[]>();
	JsonUserDataset dataset = getUserDataset(ownerUserId, datasetId);
    for (Integer recipientUserId : recipientUserIds) {
      if (recipientUserId.equals(ownerUserId)) continue;  // don't think this is worth throwing an error on
      writeShareFile(ownerUserId, datasetId, recipientUserId);
      Integer[] linkInfo = {datasetId, recipientUserId};
      externalDatasetLinks.add(linkInfo);
    }
    writeJsonUserDataset(dataset);  // write this before the links
    for (Integer[] linkInfo : externalDatasetLinks) {
      writeExternalDatasetLink(ownerUserId, linkInfo[0], linkInfo[1]);
    }    
  }

  @Override
  public void shareUserDatasets(Integer ownerUserId, Set<Integer> datasetIds, Set<Integer> recipientUserIds) throws WdkModelException {
    Set<Integer[]> externalDatasetLinks = new HashSet<Integer[]>();
    for (Integer datasetId : datasetIds) {
      JsonUserDataset dataset = getUserDataset(ownerUserId, datasetId);
      for (Integer recipientUserId : recipientUserIds) {
        if (recipientUserId.equals(ownerUserId)) continue;  // don't think this is worth throwing an error on
        writeShareFile(ownerUserId, datasetId, recipientUserId);
        Integer[] linkInfo = {datasetId, recipientUserId};
        externalDatasetLinks.add(linkInfo);
      }
      writeJsonUserDataset(dataset);  // write this before the links
      for (Integer[] linkInfo : externalDatasetLinks) 
        writeExternalDatasetLink(ownerUserId, linkInfo[0], linkInfo[1]);
    }    
  }
  
  @Override
  public void unshareUserDataset(Integer ownerUserId, Integer datasetId, Integer recipientUserId) throws WdkModelException {
    Path sharedWithDir = getSharedWithDir(ownerUserId, datasetId);
    Path sharedWithFile = sharedWithDir.resolve(recipientUserId.toString());
    if (adaptor.fileExists(sharedWithFile)) adaptor.deleteFileOrDirectory(sharedWithFile);
    removeExternalDatasetLink(ownerUserId, datasetId, recipientUserId);
  }
  
  @Override
  public void unshareUserDataset(Integer ownerUserId, Integer datasetId, Set<Integer> recipientUserIds) throws WdkModelException {
    for (Integer recipientUserId : recipientUserIds) {
	  unshareUserDataset(ownerUserId, datasetId, recipientUserId);
    }  
  }
  
  @Override
  public void unshareUserDataset(Integer ownerUserId, Integer datasetId) throws WdkModelException {
    Path sharedWithDir = getSharedWithDir(ownerUserId, datasetId);
    for (Path shareFilePath : adaptor.getPathsInDir(sharedWithDir))
      adaptor.deleteFileOrDirectory(shareFilePath);
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
   * Write a file in a dataset dir, indicating that this dataset is shared with another user.
   * @param ownerUserId
   * @param datasetId
   * @param recipientUserId
   * @throws WdkModelException
   */
  protected void writeShareFile(Integer ownerUserId, Integer datasetId, Integer recipientUserId) throws WdkModelException {

    Path sharedWithDir = getSharedWithDir(ownerUserId, datasetId);

    Path sharedWithFile = sharedWithDir.resolve(recipientUserId.toString());
    if (!adaptor.fileExists(sharedWithFile)) adaptor.writeEmptyFile(sharedWithFile);
  }
  
  /**
   * Write a file in a user's space, indicating that this user can see another user's dataset.
   * @param ownerUserId
   * @param datasetId
   * @param recipientUserId
   * @throws WdkModelException
   */
  protected void writeExternalDatasetLink(Integer ownerUserId, Integer datasetId, Integer recipientUserId) throws WdkModelException {

    Path recipientUserDir = getUserDir(recipientUserId);
    
    // create externalDatasets dir, if it doesn't exist
    Path recipientExternalDatasetsDir = recipientUserDir.resolve(EXTERNAL_DATASETS_DIR);
    if (!directoryExists(recipientExternalDatasetsDir))
      adaptor.createDirectory(recipientExternalDatasetsDir);
    
    // write link file
    Path externalDatasetFileName = recipientExternalDatasetsDir.resolve(
        getExternalDatasetFileName(ownerUserId, datasetId));
    adaptor.writeEmptyFile(externalDatasetFileName);
  }
  
  /**
   * When the owner unshares a dataset, we need to remove the external dataset link found in the recpient's
   * workspace.
   * @param ownerUserId
   * @param datasetId
   * @param recipientUserId
   * @throws WdkModelException
   */
  protected void removeExternalDatasetLink(Integer ownerUserId, Integer datasetId, Integer recipientUserId) throws WdkModelException {
    Path recipientUserDir = getUserDir(recipientUserId);
    
    // If no external datasets directory exists for this recipient or if the external database link does not
    // exist for this dataset, do nothing.  The client likely made an error.  Should we sent back something?
    Path recipientExternalDatasetsDir = recipientUserDir.resolve(EXTERNAL_DATASETS_DIR);
    if (directoryExists(recipientExternalDatasetsDir)) {
      Path externalDatasetFileName = recipientExternalDatasetsDir.resolve(getExternalDatasetFileName(ownerUserId, datasetId));
      if(fileExists(externalDatasetFileName)) {
        adaptor.deleteFileOrDirectory(externalDatasetFileName);
      }
    }
  }
  
  public boolean directoryExists(Path dir) throws WdkModelException {
    if (adaptor.isDirectory(dir)) return true;
    if (adaptor.fileExists(dir)) throw new WdkModelException("File exists and is not a directory: " + dir);
    return false;
  }
  
  /**
   * Insures that the path given points to an existing file.
   * @param path - path to the potentially existing file
   * @return - true if file exists, false if not
   * @throws WdkModelException - if the path points to a directory.
   */
  public boolean fileExists(Path path) throws WdkModelException {
	if(adaptor.isDirectory(path)) throw new WdkModelException("The path given is not a file, but a directory: " + path);
	return adaptor.fileExists(path);
  }
  
  public void writeFileAtomic(Path file, String contents, boolean errorIfTargetExists) throws WdkModelException {
    if (errorIfTargetExists && adaptor.fileExists(file)) throw new WdkModelException("File already exists: " + file);
    Path tempFile = file.getParent().resolve(file.getFileName().toString() + "." + Long.toString(System.currentTimeMillis()));
    adaptor.writeFile(tempFile, contents, true);
    adaptor.moveFileAtomic(tempFile, file);
  }

  /**
   * Delete a dataset.  But, don't delete externalUserDataset references to it.  The UI
   * will let the recipient user of them know they are dangling.
   */
  @Override
  public void deleteUserDataset(Integer userId, Integer datasetId) throws WdkModelException {
    adaptor.deleteFileOrDirectory(getUserDatasetsDir(userId).resolve(datasetId.toString()));
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
    if (!directoryExists(recipientRemovedDatasetsDir))
      adaptor.createDirectory(recipientRemovedDatasetsDir);
    Path externalDatasetFileName = recipientExternalDatasetsDir.resolve(getExternalDatasetFileName(ownerUserId, datasetId));
    Path moveToFileName = recipientRemovedDatasetsDir.resolve(getExternalDatasetFileName(ownerUserId, datasetId));
    adaptor.moveFileAtomic(externalDatasetFileName, moveToFileName);
  }
  
  @Override
  public void deleteExternalUserDataset(Integer ownerUserId, Integer datasetId, Integer recipientUserId) throws WdkModelException {
    Path recipientExternalDatasetsDir = getUserDir(recipientUserId).resolve(EXTERNAL_DATASETS_DIR);
    Path recipientRemovedDatasetsDir = getUserDir(recipientUserId).resolve(REMOVED_EXTERNAL_DATASETS_DIR);
    deleteExternalUserDataset(recipientExternalDatasetsDir, recipientRemovedDatasetsDir, ownerUserId, datasetId);
  }

  private Path getUserDir(Integer userId) throws WdkModelException {
    Path userDir = usersRootDir.resolve(userId.toString());
    if (!directoryExists(userDir)) {
      adaptor.createDirectory(userDir);
    }
    return userDir;
  }
  
  @Override
  public boolean checkUserDirExists(Integer userId)  throws WdkModelException {
    return directoryExists(usersRootDir.resolve(userId.toString()));
  }

  @Override
  public Integer getQuota(Integer userId) throws WdkModelException {
    Path quotaFile;
    
    Path defaultQuotaFile = usersRootDir.resolve("default_quota");
    Path userQuotaFile = getUserDir(userId).resolve("quota");
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
   * Given a user ID, return a Path to that user's datasets dir.  Create the dir if doesn't exist.
   * @param userId
   * @return
   * @throws WdkModelException
   */
  private Path getUserDatasetsDir(Integer userId) throws WdkModelException {
    Path userDatasetsDir = getUserDir(userId).resolve("datasets");

    if (!directoryExists(userDatasetsDir)) adaptor.createDirectory(userDatasetsDir);
 
    return userDatasetsDir;
  }
  
  @Override
  public boolean checkUserDatasetsDirExists(Integer userId)  throws WdkModelException {
    return directoryExists(usersRootDir.resolve(userId.toString()).resolve("datasets"));
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("UserDatasetStore: " + NL);
    builder.append("  rootPath: " + usersRootDir + NL);
    builder.append("  adaptor: " + adaptor.getClass() + NL);
    for (UserDatasetType type : typeHandlersMap.keySet()) {
      builder.append("  type handler: " + type + " " + typeHandlersMap.get(type).getClass() + NL);
    }
    return builder.toString();
  }

  @Override
  public UserDatasetStoreAdaptor getUserDatasetStoreAdaptor() {
    return adaptor;
  }
  
  @Override
  public String getUserDatasetStoreId() {
	return id;
  }
}
