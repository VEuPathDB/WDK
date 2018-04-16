package org.gusdb.wdk.model.user.dataset.json;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.UserDatasetSession;
import org.gusdb.wdk.model.user.dataset.UserDatasetShare;
import org.gusdb.wdk.model.user.dataset.UserDatasetStoreAdaptor;
import org.gusdb.wdk.model.user.dataset.UserDatasetType;
import org.gusdb.wdk.model.user.dataset.UserDatasetTypeHandler;
import org.jfree.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A session for talking to a user dataset store.  Lifespan is expected to be per-request.
 * Provides access to state in the store.  In many cases, caches that state in instance
 * variables to avoid unneeded calls to the store.
 * @author Steve
 *
 */
public class JsonUserDatasetSession implements UserDatasetSession {

  private static final Logger LOG = Logger.getLogger(JsonUserDatasetSession.class);

  protected static final String DATASET_JSON_FILE = "dataset.json";
  protected static final String META_JSON_FILE = "meta.json";
  protected static final String EXTERNAL_DATASETS_DIR = "externalDatasets";
  protected static final String SHARED_WITH_DIR = "sharedWith";
  protected static final String REMOVED_EXTERNAL_DATASETS_DIR = "removedExternalDatasets";
  protected static final String DATAFILES_DIR = "datafiles";

  private static final String NL = System.lineSeparator();
 
  protected Map<UserDatasetType, UserDatasetTypeHandler> typeHandlersMap = new HashMap<UserDatasetType, UserDatasetTypeHandler>();
  private Map<Long, UserDatasetUser> usersMap = new HashMap<Long, UserDatasetUser>();
  private Long defaultQuota;
  protected Path usersRootDir;
  protected UserDatasetStoreAdaptor adaptor;
  private Map<Long, Path> userDirsMap = new HashMap<Long, Path>();

  public JsonUserDatasetSession(UserDatasetStoreAdaptor adaptor, Path usersRootDir) {
    this.adaptor = adaptor;
    this.usersRootDir = usersRootDir;
  }
  
  private UserDatasetUser getUserDatasetUser(Long userId) {
    if (!usersMap.containsKey(userId)) usersMap.put(userId, new UserDatasetUser());
    return usersMap.get(userId);
  }

  public void checkRootDirExists() throws WdkModelException {
    if (!directoryExists(usersRootDir))
      throw new WdkModelException(
          "Provided property 'rootPath' has value '" + usersRootDir + "' which is not an existing directory");
  }

  @Override
  public Long getModificationTime(Long userId) throws WdkModelException {
    Path userDatasetsDir = getUserDatasetsDirIfExists(userId);
    return userDatasetsDir == null? null : adaptor.getModificationTime(userDatasetsDir);
  }

  @Override
  public Map<Long, UserDataset> getUserDatasets(Long userId) throws WdkModelException {
    UserDatasetUser user = getUserDatasetUser(userId);
 
    Path userDatasetsDir = getUserDatasetsDirIfExists(userId);
      
    // iterate through datasets, creating a UD from each   
    if (userDatasetsDir != null) {
      for (Path datasetDir : getDatasetDirs(userId, userDatasetsDir)) {
    	try {  
          UserDataset dataset = getUserDataset(datasetDir);
          user.datasetsMap.put(dataset.getUserDatasetId(), dataset);
    	}
    	catch(WdkModelException wme) {
    	  LOG.error("User dataset at " + datasetDir + " not retrievable.  May be corrupted.");
    	}
      }
    }
    
    return Collections.unmodifiableMap(user.datasetsMap);
  }
  
  private List<Path> getDatasetDirs(Long userId, Path userDatasetsDir) throws WdkModelException {
    UserDatasetUser user = getUserDatasetUser(userId);
    if (user.datasetDirsList == null) {
      user.datasetDirsList = adaptor.getPathsInDir(userDatasetsDir);
    }
    return user.datasetDirsList;
  }

  @Override
  public Set<UserDatasetShare> getSharedWith(Long ownerUserId, Long datasetId) throws WdkModelException {
    UserDatasetUser user = getUserDatasetUser(ownerUserId);
    if (user.sharedWithUsers.get(datasetId) == null) {
      
  
      Path sharedWithDir = getSharedWithDir(ownerUserId, datasetId);
  
      List<Path> sharedWithPaths = adaptor.getPathsInDir(sharedWithDir);
      Set<UserDatasetShare> sharedWithItems = new HashSet<UserDatasetShare>();
      for (Path sharedWithPath : sharedWithPaths) {
        String userIdString = sharedWithPath.getFileName().toString();
        Long timestamp = adaptor.getModificationTime(sharedWithPath);
        sharedWithItems.add(new JsonUserDatasetShare(new Long(userIdString), timestamp));
      }
      user.sharedWithUsers.put(datasetId, sharedWithItems);
    }
    return Collections.unmodifiableSet(user.sharedWithUsers.get(datasetId));
  }

  /**
   * Locates the path to the sharedWith directory for the given user and dataset.  If no such
   * path is found under the dataset directory, the directory is created.
   * @param userId - The user to whom the dataset belongs
   * @param datasetId - The subject dataset
   * @return - The path to the sharedWith directory under the dataset's directory
   * @throws WdkModelException
   */
  private Path getSharedWithDir(Long userId, Long datasetId) throws WdkModelException {
    UserDatasetUser user = getUserDatasetUser(userId);

    Path userDatasetDir = getUserDatasetDir(userId, datasetId);
    
    // if stmt breaks un/sharing of multiple UDs
    //if (user.sharedWithDir == null) {
      user.sharedWithDir = userDatasetDir.resolve(SHARED_WITH_DIR);
      //TODO should we handle this upon setting up the dataset rather than on the fly?
      if (!adaptor.fileExists(user.sharedWithDir)) {
        adaptor.createDirectory(user.sharedWithDir);
      }
    //}
    return user.sharedWithDir;
  }

  @Override
  public Map<Long, UserDataset> getExternalUserDatasets(Long userId) throws WdkModelException {
    UserDatasetUser user = getUserDatasetUser(userId);
    if (user.externalDatasetsMap == null) {
  
      user.externalDatasetsMap = new HashMap<Long, UserDataset>();
      Map<Long, Map<Long, UserDataset>> otherUsersCache = new HashMap<Long, Map<Long, UserDataset>>();
  
      Path externalDatasetsDir = getUserDir(userId).resolve(EXTERNAL_DATASETS_DIR);
      if (adaptor.isDirectory(externalDatasetsDir)) {  
        List<Path> externalLinkPaths = adaptor.getPathsInDir(externalDatasetsDir);
        for (Path externalLinkPath : externalLinkPaths) {
          ExternalDatasetLink link = new ExternalDatasetLink(externalLinkPath.getFileName());
          if (!checkUserDirExists(link.externalUserId)) throw new WdkModelException("User ID in external dataset link " + externalLinkPath.getFileName() + " is not a user in the datasets store");
    
          UserDataset originalDataset = followExternalDatasetLink(userId, link, otherUsersCache);
    
          if (originalDataset != null) user.externalDatasetsMap.put(originalDataset.getUserDatasetId(), originalDataset);
        }
      }
    }
    return Collections.unmodifiableMap(user.externalDatasetsMap);
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
  private UserDataset followExternalDatasetLink(Long userId, ExternalDatasetLink link, Map<Long, Map<Long, UserDataset>> otherUsersCache) throws WdkModelException {
    if (!checkUserDirExists(link.externalUserId)) return null;

    // get the datasets belonging to the external user.  (use cache to avoid re-querying repeated user IDs)
    Map<Long, UserDataset> datasetsOfExternalUser;

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
    Long externalUserId;
    Long datasetId;

    ExternalDatasetLink(Long  externalUserId, Long datasetId) {
      this.externalUserId = externalUserId;
      this.datasetId = datasetId;
    }

    ExternalDatasetLink(Path externalLinkPath) throws WdkModelException {
      String linkName = externalLinkPath.getFileName().toString();
      String[] words = linkName.split(Pattern.quote("."));
      if (words.length != 2)
        throw new WdkModelException("Illegal external dataset link: " + linkName);
      try {
        externalUserId = new Long(words[0]);
        datasetId = new Long(words[1]);
      }
      catch (NumberFormatException e) {
        throw new WdkModelException("Illegal external dataset link: " + linkName);
      }
    }

    String getFileName() {
      return "" + externalUserId + "." + datasetId;
    }
  }

  @Override
  public JsonUserDataset getUserDataset(Long userId, Long datasetId) throws WdkModelException {
    Path userDatasetsDir = getUserDatasetsDir(userId);
    Path datasetDir = userDatasetsDir.resolve(datasetId.toString());
    return getUserDataset(datasetDir);
  }

  @Override
  public boolean getUserDatasetExists(Long userId, Long datasetId) throws WdkModelException {
    UserDatasetUser user = getUserDatasetUser(userId);
    if (!user.userDatasetExistsMap.containsKey(datasetId)) {
      Path userDatasetsDir = getUserDatasetsDirIfExists(userId);
      if (userDatasetsDir == null) return false;
      Path datasetDir = userDatasetsDir.resolve(datasetId.toString());
      user.userDatasetExistsMap.put(datasetId, directoryExists(datasetDir));
    }
    return user.userDatasetExistsMap.get(datasetId);
  }

  private Path getUserDatasetDir(Long userId, Long datasetId) throws WdkModelException {
    Path userDatasetsDir = getUserDatasetsDir(userId);
    return userDatasetsDir.resolve(datasetId.toString());
  }

  /**
   * Construct a user dataset object, given its location in the store.
   * Note: this method calls the store, but does not cache the values.  Calling code should do so if needed.
   * @param datasetDir
   * @return
   * @throws WdkModelException
   */
  private JsonUserDataset getUserDataset(Path datasetDir) throws WdkModelException {

    Long datasetId;
    try {
      datasetId = Long.parseLong(datasetDir.getFileName().toString());
    }
    catch (NumberFormatException e) {
      throw new WdkModelException("Found file or directory '" + datasetDir.getFileName() +
          "' in user datasets directory " + datasetDir.getParent() + ". It is not a dataset ID");
    }

    JSONObject datasetJson = readAndParseJsonFile(datasetDir.resolve("dataset.json"));
    JSONObject metaJson = readAndParseJsonFile(datasetDir.resolve("meta.json"));

    return new JsonUserDataset(datasetId, datasetJson, metaJson, datasetDir.resolve(DATAFILES_DIR), this);
  }

  /**
   * Read a dataset.json file, and return the JSONObject that it parses to.
   * @param jsonFile
   * @return
   * @throws WdkModelException
   */
  private JSONObject readAndParseJsonFile(Path jsonFile) throws WdkModelException {

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
  public void updateMetaFromJson(Long userId, Long datasetId, JSONObject metaJson) throws WdkModelException {
    JsonUserDatasetMeta metaObj = new JsonUserDatasetMeta(metaJson);  // validate the input json
    Path metaJsonFile = getUserDatasetsDir(userId).resolve(datasetId.toString()).resolve(META_JSON_FILE);
    writeFileAtomic(metaJsonFile, metaObj.getJsonObject().toString(), false);
  }

  @Override
  public void shareUserDataset(Long ownerUserId, Long datasetId, Long recipientUserId) throws WdkModelException {
    if (recipientUserId.equals(ownerUserId)) return;  // don't think this is worth throwing an error on

    // Check both ends of the share
    Path sharedWithPath = getSharedWithFile(ownerUserId, datasetId, recipientUserId);
    Path externalDatasetLink = getExternalDatasetLink(ownerUserId, datasetId, recipientUserId);

    // A dangling share exists for the given dataset between the owner and recipient
    if((sharedWithPath != null && externalDatasetLink == null) || (sharedWithPath == null && externalDatasetLink != null)) {
      throw new WdkModelException("A dangling share exists for dataset " + datasetId
          + " between the owner, " + ownerUserId + ", and the expected recipient, "
          + recipientUserId + "."); 
    }
    // The share is already in place...nothing more to do.
    if(sharedWithPath != null && externalDatasetLink != null) {
      return;
    }
    // Write the external dataset link last because only that put fires an IRODS event.
    writeShareFile(ownerUserId, datasetId, recipientUserId);
    writeExternalDatasetLink(ownerUserId, datasetId, recipientUserId);
  }

  @Override
  public void shareUserDataset(Long ownerUserId, Long datasetId, Set<Long> recipientUserIds) throws WdkModelException {
    for (Long recipientUserId : recipientUserIds) {    	
      shareUserDataset(ownerUserId, datasetId, recipientUserId);
    }
  }

  @Override
  public void unshareUserDataset(Long ownerUserId, Long datasetId, Long recipientUserId) throws WdkModelException {
    if (recipientUserId.equals(ownerUserId)) return;  // don't think this is worth throwing an error on

    // Check both ends of the share
    Path sharedWithPath = getSharedWithFile(ownerUserId, datasetId, recipientUserId);
    Path externalDatasetLink = getExternalDatasetLink(ownerUserId, datasetId, recipientUserId);

    // A dangling share exists for the given dataset between the owner and recipient
    if((sharedWithPath != null && externalDatasetLink == null) || (sharedWithPath == null && externalDatasetLink != null)) {
      throw new WdkModelException("A dangling share exists for dataset " + datasetId
          + " between the owner, " + ownerUserId + ", and the expected recipient, "
          + recipientUserId + "."); 
    }
    // If no share is in place, there is nothing more to do.
    if(sharedWithPath == null && externalDatasetLink == null) {
      return;
    }
    adaptor.deleteFileOrDirectory(sharedWithPath);
    adaptor.deleteFileOrDirectory(externalDatasetLink);
  }

  @Override
  public void unshareUserDataset(Long ownerUserId, Long datasetId, Set<Long> recipientUserIds) throws WdkModelException {
    for (Long recipientUserId : recipientUserIds) {
      unshareUserDataset(ownerUserId, datasetId, recipientUserId);
    }
  }

  @Override
  public void unshareWithAll(Long ownerUserId, Long datasetId) throws WdkModelException {

    // Create a list of recipients of this dataset share
    Set<Long> recipientUserIds = new HashSet<>();
    Path sharedWithDir = getSharedWithDir(ownerUserId, datasetId);
    for (Path shareFilePath : adaptor.getPathsInDir(sharedWithDir)) {
      Long recipientUserId = null;
      try {
        recipientUserId = Long.valueOf(shareFilePath.getFileName().toString());
      }
      // If we find a bad sharedWith file, we just ignore it.
      catch(NumberFormatException nfe) {
        LOG.warn("The recipient id " + shareFilePath.getFileName()
        + " given for dataset share " + datasetId + " is not a valid id.", nfe);
        continue;
      }
      recipientUserIds.add(recipientUserId);
    }
    // Unshare the dataset with that collection of recipients
    unshareUserDataset(ownerUserId, datasetId, recipientUserIds);
  }

  /**
   * Get a file name to use as a link from a recipient to a shared dataset in the owner's space.
   * @param ownerUserId
   * @param datasetId
   * @return
   */
  protected String getExternalDatasetFileName(Long ownerUserId, Long datasetId) {
    return ownerUserId + "." + datasetId;
  }

  /**
   * Write a file in a dataset dir, indicating that this dataset is shared with another user.
   * @param ownerUserId
   * @param datasetId
   * @param recipientUserId
   * @throws WdkModelException
   */
  protected void writeShareFile(Long ownerUserId, Long datasetId, Long recipientUserId) throws WdkModelException {
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
  protected void writeExternalDatasetLink(Long ownerUserId, Long datasetId, Long recipientUserId) throws WdkModelException {

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
   * workspace.  This removal with cause IRODS to fire off a share/revoke event.
   * @param ownerUserId
   * @param datasetId
   * @param recipientUserId
   * @throws WdkModelException
   */
  protected void removeExternalDatasetLink(Long ownerUserId, Long datasetId, Long recipientUserId) throws WdkModelException {
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

  @Override
  public void deleteUserDataset(Long userId, Long datasetId) throws WdkModelException {
    boolean isOwner;
    Path userDatasetsDir = getUserDatasetsDirIfExists(userId);
    if (userDatasetsDir == null) {
      isOwner = false;
    } else {     
      Path datasetDir = userDatasetsDir.resolve(datasetId.toString());
      isOwner = directoryExists(datasetDir);
    }
    
    // User is owner
    if (isOwner) {

      // First remove any shares on this dataset.  This will fire as many IRODS events as
      // there are outstanding shares for this dataset.
      unshareWithAll(userId, datasetId);

      // Then remove the dataset itself.
      adaptor.deleteFileOrDirectory(userDatasetsDir.resolve(datasetId.toString()));
    }
    // User is not the owner - check to see if the user has a share to the dataset
    else {	
      Path externalDatasetDir = getExternalDatasetDir(userId);
      List<Path> externalDatasetLinks = adaptor.getPathsInDir(externalDatasetDir);

      // Look through the user's shares for the dataset id provided
      for(Path externalDatasetLink : externalDatasetLinks) { 
        String[] linkInfo = externalDatasetLink.getFileName().toString().split("\\.");
        if(linkInfo.length == 2) { 	
          try {  
            Long ownerUserId = Long.parseLong(linkInfo[0]);
            Long sharedDatasetId = Long.parseLong(linkInfo[1]);

            // User is the recipient of a share - unshare the dataset on behalf of
            // this user.
            if(sharedDatasetId.equals(datasetId)) {
              unshareUserDataset(ownerUserId, datasetId, userId);
            }
          }
          // Skip over any files not in proper external dataset link format
          catch (NumberFormatException nfe) {
            LOG.warn("The external dataset link  " + externalDatasetLink.getFileName()
              + " is not a valid link.", nfe);
          }
        }
        else {
          LOG.warn("The external dataset link  " + externalDatasetLink.getFileName()
            + " is not a valid link.");
        }
      }
    }
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
  public void deleteExternalUserDataset(Path recipientExternalDatasetsDir, Path recipientRemovedDatasetsDir, Long ownerUserId, Long datasetId) throws WdkModelException {
    if (!directoryExists(recipientRemovedDatasetsDir))
      adaptor.createDirectory(recipientRemovedDatasetsDir);
    Path externalDatasetFileName = recipientExternalDatasetsDir.resolve(getExternalDatasetFileName(ownerUserId, datasetId));
    Path moveToFileName = recipientRemovedDatasetsDir.resolve(getExternalDatasetFileName(ownerUserId, datasetId));
    adaptor.moveFileAtomic(externalDatasetFileName, moveToFileName);
  }

  @Override
  public void deleteExternalUserDataset(Long ownerUserId, Long datasetId, Long recipientUserId) throws WdkModelException {
    Path recipientExternalDatasetsDir = getUserDir(recipientUserId).resolve(EXTERNAL_DATASETS_DIR);
    Path recipientRemovedDatasetsDir = getUserDir(recipientUserId).resolve(REMOVED_EXTERNAL_DATASETS_DIR);
    deleteExternalUserDataset(recipientExternalDatasetsDir, recipientRemovedDatasetsDir, ownerUserId, datasetId);
  }

  private Path getUserDir(Long userId) throws WdkModelException {
    if (!userDirsMap.containsKey(userId)) {
      Path userDir = usersRootDir.resolve(userId.toString());
      if (!directoryExists(userDir)) {
        adaptor.createDirectory(userDir);
      }
      userDirsMap.put(userId, userDir);
    }
    return userDirsMap.get(userId);
  }

  @Override
  public boolean checkUserDirExists(Long userId)  throws WdkModelException {
    return directoryExists(usersRootDir.resolve(userId.toString()));
  }

  @Override
  public Long getQuota(Long userId) throws WdkModelException {
    UserDatasetUser user = getUserDatasetUser(userId);
    if (user.quota == null) {  
      Path userQuotaFile = getUserDir(userId).resolve("quota");
      if (adaptor.fileExists(userQuotaFile)) {
        String line = adaptor.readSingleLineFile(userQuotaFile);
        if (line == null)
          throw new WdkModelException("Empty quota file " + userQuotaFile);
        user.quota = new Long(line.trim());
      } else user.quota = getDefaultQuota();
    }
    return user.quota;
  }
  
  private Long getDefaultQuota() throws WdkModelException {
    if (defaultQuota == null) {
      Path quotaFile = usersRootDir.resolve("default_quota");

      String line = adaptor.readSingleLineFile(quotaFile);
      if (line == null)
        throw new WdkModelException("Empty quota file " + quotaFile);
      defaultQuota = new Long(line.trim());
    }
    return defaultQuota;
  }
  
  @Override
  public Long getDefaultQuota(boolean getFromStore) throws WdkModelException {
    if (getFromStore || defaultQuota == null) {
        Path quotaFile = usersRootDir.resolve("default_quota");
        String line = adaptor.readSingleLineFile(quotaFile);
        if (line == null)
          throw new WdkModelException("Empty quota file " + quotaFile);
        defaultQuota = new Long(line.trim());
      }
      return defaultQuota;
  }
  
  /**
   * Given a user ID, return a Path to that user's datasets dir.  If dir doesn't exist, return NULL.
   * @param userId
   * @return
   * @throws WdkModelException
   */
  private Path getUserDatasetsDirIfExists(Long userId) throws WdkModelException {
    Path userDatasetsDir = getUserDir(userId).resolve("datasets");

    if (!directoryExists(userDatasetsDir)) return null;

    return userDatasetsDir;
  }

  /**
   * Given a user ID, return a Path to that user's datasets dir.  
   * @param userId
   * @return
   * @throws WdkModelException
   */
  private Path getUserDatasetsDir(Long userId) throws WdkModelException {
    Path userDatasetsDir = getUserDatasetsDirIfExists(userId);

    if (userDatasetsDir == null) throw new WdkModelException("User datasets dir does not exist: " + userDatasetsDir);

    return userDatasetsDir;
  }

  @Override
  public boolean checkUserDatasetsDirExists(Long userId)  throws WdkModelException {
    return getUserDatasetsDirIfExists(userId) != null;
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
  public String initializeUserDatasetStoreId() throws WdkModelException {
    return adaptor.findUserDatasetStoreId(usersRootDir);
  }
  
  @Override
  public List<Path> getRecentEvents(String eventsDirectory, Long lastHandledEventId) throws WdkModelException {
    return null;
  }

  /**
   * Locates the path to the external datasets directory for the given user.  If no such
   * path is found under the user's directory, the directory is created.
   * @param userId - The user having potentially external datasets
   * @return - The path to the external datasets directory under the user's directory
   * @throws WdkModelException
   */
  protected Path getExternalDatasetDir(Long userId) throws WdkModelException {
    Path userDir = getUserDir(userId);
    Path externalDatasetDir = userDir.resolve(EXTERNAL_DATASETS_DIR);
    if (!adaptor.fileExists(externalDatasetDir)) {
      adaptor.createDirectory(externalDatasetDir);
    }
    return externalDatasetDir;
  }

  /**
   * Returns the path to the external dataset link identified by the owner, the recipient
   * and the dataset to share/unshare.  Has the side-effect of creating the external
   * dataset link directory if it doesn't already exist.
   * @param ownerUserId - dataset owner
   * @param datasetId - dataset id
   * @param recipientUserId - recipient of the dataset share
   * @return - path of external dataset link 
   * @throws WdkModelException
   */
  protected Path getExternalDatasetLink(Long ownerUserId, Long datasetId, Long recipientUserId) throws WdkModelException {
    Path recipientExternalDatasetsDir = getExternalDatasetDir(recipientUserId);
    Path externalDatasetLink = recipientExternalDatasetsDir.resolve(getExternalDatasetFileName(ownerUserId, datasetId));
    if(fileExists(externalDatasetLink)) {
      return externalDatasetLink;
    }
    return null;
  }

  /**
   * Return the path to the sharedWith file identified by the owner, the recipient
   * and the dataset to share/unshare.  Has the side-effect of creating the sharedWith
   * directory if it doesn't already exist.
   * @param ownerUserId - dataset owner
   * @param datasetId - dataset
   * @param recipientUserId - recipient of the dataset share
   * @return - path to the sharedWith file
   * @throws WdkModelException
   */
  protected Path getSharedWithFile(Long ownerUserId, Long datasetId, Long recipientUserId) throws WdkModelException {
    Path sharedWithDir = getSharedWithDir(ownerUserId, datasetId);
    Path sharedWithFile = sharedWithDir.resolve(recipientUserId.toString());
    if(fileExists(sharedWithFile)) {
      return sharedWithFile;
    }
    return null;
  }

  /**
   * Added because IRODS connections need to be closed.
   */
  @Override
  public void close() {
  }

  @Override
  public UserDatasetFile getUserDatasetFile(Path path, Long userDatasetId) {
    // TODO Auto-generated method stub
    return null;
  }
  
  /**
   * A Class to "memoize" data we get from the store
   * @author Steve
   *
   */
  private class UserDatasetUser {   
    Map<Long, UserDataset> datasetsMap = new HashMap<Long, UserDataset>();
    Map<Long, Set<UserDatasetShare>> sharedWithUsers = new HashMap<Long, Set<UserDatasetShare>>();
    Path sharedWithDir;
    Map<Long, UserDataset> externalDatasetsMap;
    Map<Long, Boolean> userDatasetExistsMap  = new HashMap<Long, Boolean>();
    List<Path> datasetDirsList;
    Long quota;
  }
}

