package org.gusdb.wdk.model.user.dataset.json;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

import static org.gusdb.fgputil.FormatUtil.NL;

/**
 * A session for talking to a user dataset store.  Lifespan is expected to be
 * per-request. Provides access to state in the store.  In many cases, caches
 * that state in instance variables to avoid unneeded calls to the store.
 *
 * @author Steve
 */
public class JsonUserDatasetSession implements UserDatasetSession {

  private static final Logger LOG = Logger.getLogger(JsonUserDatasetSession.class);

  private static final String DATASET_JSON_FILE = "dataset.json";
  private static final String META_JSON_FILE = "meta.json";
  private static final String EXTERNAL_DATASETS_DIR = "externalDatasets";
  private static final String SHARED_WITH_DIR = "sharedWith";
  private static final String REMOVED_EXTERNAL_DATASETS_DIR = "removedExternalDatasets";
  private static final String DATASETS_DIR = "datasets";
  private static final String DATAFILES_DIR = "datafiles";

  protected UserDatasetStoreAdaptor adaptor;
  private Map<UserDatasetType, UserDatasetTypeHandler> typeHandlersMap = new HashMap<>();
  private Path usersRootDir;
  private Map<Long, UserDatasetUser> usersMap = new HashMap<>();
  private Long defaultQuota;
  private Map<Long, Path> userDirsMap = new HashMap<>();

  public JsonUserDatasetSession(UserDatasetStoreAdaptor adaptor, Path usersRootDir) {
    this.adaptor = adaptor;
    this.usersRootDir = usersRootDir;
  }

  public void checkRootDirExists() throws WdkModelException {
    if (!directoryExists(usersRootDir))
      throw new WdkModelException("Provided property 'rootPath' has value '"
        + usersRootDir + "' which is not an existing directory");
  }

  @Override
  public Long getModificationTime(Long userId) throws WdkModelException {
    Optional<Path> userDatasetsDir = getUserDatasetsDir(userId);
    return userDatasetsDir.isPresent()
      ? adaptor.getModificationTime(userDatasetsDir.get())
      : null;
  }

  @Override
  public Map<Long, UserDataset> getUserDatasets(Long userId) throws WdkModelException {
    UserDatasetUser user = getUserDatasetUser(userId);
    Path userDatasetsDir = getUserDatasetsDirIfExists(userId);

    // iterate through datasets, creating a UD from each
    if (userDatasetsDir != null) {
      final List<Path> tmp = getDatasetDirs(userId, userDatasetsDir);
      for (Path datasetDir : tmp) {
        try {
          UserDataset dataset = getUserDataset(datasetDir);
          user.datasetsMap.put(dataset.getUserDatasetId(), dataset);
        } catch (WdkModelException wme) {
        }
      }
    }

    return Collections.unmodifiableMap(user.datasetsMap);
  }

  @Override
  public Set<UserDatasetShare> getSharedWith(Long ownerUserId, Long datasetId) throws WdkModelException {
    UserDatasetUser user = getUserDatasetUser(ownerUserId);
    if (user.sharedWithUsers.get(datasetId) == null) {

      Path sharedWithDir = getSharedWithDir(ownerUserId, datasetId);

      List<Path> sharedWithPaths = adaptor.getPathsInDir(sharedWithDir);
      Set<UserDatasetShare> sharedWithItems = new HashSet<>();
      for (Path sharedWithPath : sharedWithPaths) {
        String userIdString = sharedWithPath.getFileName().toString();
        Long timestamp = adaptor.getModificationTime(sharedWithPath);
        sharedWithItems.add(new JsonUserDatasetShare(new Long(userIdString), timestamp));
      }
      user.sharedWithUsers.put(datasetId, sharedWithItems);
    }
    return Collections.unmodifiableSet(user.sharedWithUsers.get(datasetId));
  }

  @Override
  public Optional<UserDataset> getExternalUserDataset(long userId, long dsId)
  throws WdkModelException {
    return Optional.ofNullable(getExternalDatasetsMap(
      getUserDatasetUser(userId)).externalDatasetsMap.get(dsId));
  }

  @Override
  public Optional<UserDatasetFile> getExternalUserDatafile(
    final long userId,
    final long datasetId,
    final String fileName
  ) throws WdkModelException {
    final List<Path> paths = adaptor.getPathsInDir(resolvePath(
      usersRootDir, userId, EXTERNAL_DATASETS_DIR));

    for (final Path path : paths) {
      final ExternalDatasetLink link = new ExternalDatasetLink(path.getFileName());

      if (link.datasetId != datasetId
        || !isSharedWith(link.externalUserId, datasetId, userId))
        continue;

      final Path filePath = resolvePath(usersRootDir, link.externalUserId,
        DATASETS_DIR, datasetId, DATAFILES_DIR, fileName);

      final Optional<UserDatasetFile> userDatasetFile = Optional.of(
        getUserDatasetFile(filePath, datasetId));
      return userDatasetFile;
    }

    return Optional.empty();
  }

  @Override
  public Map<Long, UserDataset> getExternalUserDatasets(Long userId)
  throws WdkModelException {
    UserDatasetUser user = getUserDatasetUser(userId);
    if (user.externalDatasetsMap == null) {

      user.externalDatasetsMap = new HashMap<>();
      Map<Long, Map<Long, UserDataset>> otherUsersCache = new HashMap<>();

      Path externalDatasetsDir = getOrCreateUserDir(userId).resolve(EXTERNAL_DATASETS_DIR);
      if (adaptor.isDirectory(externalDatasetsDir)) {
        List<Path> externalLinkPaths = adaptor.getPathsInDir(externalDatasetsDir);
        for (Path externalLinkPath : externalLinkPaths) {
          ExternalDatasetLink link = new ExternalDatasetLink(externalLinkPath.getFileName());
          if (!checkUserDirExists(link.externalUserId))
            throw new WdkModelException("User ID in external dataset link "
              + externalLinkPath.getFileName()
              + " is not a user in the datasets store");

          UserDataset originalDataset = followExternalDatasetLink(userId, link, otherUsersCache);

          if (originalDataset != null)
            user.externalDatasetsMap.put(originalDataset.getUserDatasetId(), originalDataset);
        }
      }
    }
    return Collections.unmodifiableMap(user.externalDatasetsMap);
  }

  // TODO: this is probably doable without 2 adaptor requests
  @Override
  public JsonUserDataset getUserDataset(Long userId, Long datasetId) throws WdkModelException {
    Path userDatasetsDir = requireUserDatasetsDir(userId);
    Path datasetDir = userDatasetsDir.resolve(datasetId.toString());
    return getUserDataset(datasetDir);
  }

  @Override
  public boolean getUserDatasetExists(Long userId, Long datasetId) throws WdkModelException {
    LOG.info("S: getUserDatasetExists");
    UserDatasetUser user = getUserDatasetUser(userId);

    if (!user.userDatasetExistsMap.containsKey(datasetId))
      user.userDatasetExistsMap.put(datasetId,
        getUserDatasetDir(userId, datasetId).isPresent());

    LOG.info("E: getUserDatasetExists");
    return user.userDatasetExistsMap.get(datasetId);
  }

  @Override
  public void updateMetaFromJson(Long userId, Long datasetId, JSONObject metaJson) throws WdkModelException {
    JsonUserDatasetMeta metaObj = new JsonUserDatasetMeta(metaJson);  // validate the input json
    Path metaJsonFile = requireUserDatasetsDir(userId).resolve(datasetId.toString()).resolve(META_JSON_FILE);
    writeFileAtomic(metaJsonFile, metaObj.getJsonObject().toString(), false);
  }

  @Override
  public void shareUserDataset(Long ownerUserId, Long datasetId, Long recipientUserId) throws WdkModelException {
    if (recipientUserId.equals(ownerUserId))
      return;  // don't think this is worth throwing an error on

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
    for (Long recipientUserId : recipientUserIds)
      shareUserDataset(ownerUserId, datasetId, recipientUserId);
  }

  @Override
  public void unshareUserDataset(Long ownerId, Long datasetId, Long recipientId)
  throws WdkModelException {
    if (recipientId.equals(ownerId)) return;  // don't think this is worth throwing an error on

    // Check both ends of the share
    Path sharedWithPath = getSharedWithFile(ownerId, datasetId, recipientId);
    Path externalDatasetLink = getExternalDatasetLink(ownerId, datasetId, recipientId);

    // A dangling share exists for the given dataset between the owner and recipient
    if((sharedWithPath != null && externalDatasetLink == null) || (sharedWithPath == null && externalDatasetLink != null)) {
      throw new WdkModelException("A dangling share exists for dataset " + datasetId
        + " between the owner, " + ownerId + ", and the expected recipient, "
        + recipientId + ".");
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
      Long recipientUserId;
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

  @Override
  public void deleteUserDataset(Long userId, Long datasetId) throws WdkModelException {
    Optional<Path> userDatasetsDir = getUserDatasetDir(userId, datasetId);

    // User is owner
    if (userDatasetsDir.isPresent()) {

      // First remove any shares on this dataset.  This will fire as many IRODS
      // events as there are outstanding shares for this dataset.
      unshareWithAll(userId, datasetId);

      // Then remove the dataset itself.
      adaptor.deleteFileOrDirectory(userDatasetsDir.get());
    }
    // User is not the owner - check to see if the user has a share to the dataset
    else {
      Path externalDatasetDir = getOrCreateExternalDatasetDir(userId);
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

  @Override
  public void deleteExternalUserDataset(Long ownerUserId, Long datasetId, Long recipientUserId) throws WdkModelException {
    Path recipientExternalDatasetsDir = getOrCreateUserDir(recipientUserId).resolve(EXTERNAL_DATASETS_DIR);
    Path recipientRemovedDatasetsDir = getOrCreateUserDir(recipientUserId).resolve(REMOVED_EXTERNAL_DATASETS_DIR);
    deleteExternalUserDataset(recipientExternalDatasetsDir, recipientRemovedDatasetsDir, ownerUserId, datasetId);
  }

  @Override
  public boolean checkUserDirExists(Long userId)  throws WdkModelException {
    return getUserDir(userId).isPresent();
  }

  @Override
  public Long getQuota(Long userId) throws WdkModelException {
    UserDatasetUser user = getUserDatasetUser(userId);
    if (user.quota == null) {
      Path userQuotaFile = getOrCreateUserDir(userId).resolve("quota");
      if (adaptor.fileExists(userQuotaFile)) {
        String line = adaptor.readSingleLineFile(userQuotaFile);
        if (line == null)
          throw new WdkModelException("Empty quota file " + userQuotaFile);
        user.quota = new Long(line.trim());
      } else user.quota = getDefaultQuota();
    }
    return user.quota;
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

  @Override
  public boolean checkUserDatasetsDirExists(Long userId) throws WdkModelException {
    return getUserDatasetsDir(userId).isPresent();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("UserDatasetStore: " + NL)
      .append("  rootPath: ").append(usersRootDir).append(NL)
      .append("  adaptor: ").append(adaptor.getClass()).append(NL);

    for (UserDatasetType type : typeHandlersMap.keySet()) {
      builder.append("  type handler: ").append(type).append(" ")
        .append(typeHandlersMap.get(type).getClass()).append(NL);
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
   * Insures that the path given points to an existing file.
   *
   * @param path
   *   path to the potentially existing file
   *
   * @return true if file exists, false if not
   *
   * @throws WdkModelException
   *   if the path points to a directory.
   */
  private boolean fileExists(Path path) throws WdkModelException {
    final boolean exists = adaptor.fileExists(path);
    if (exists && adaptor.isDirectory(path))
      throw new WdkModelException("The path given is not a file, but a directory: " + path);
    return exists;
  }

  private void writeFileAtomic(Path file, String contents,
    boolean errorIfTargetExists) throws WdkModelException {
    if (errorIfTargetExists && adaptor.fileExists(file))
      throw new WdkModelException("File already exists: " + file);
    Path tempFile = file.getParent()
      .resolve(file.getFileName().toString() + "." + System.currentTimeMillis());
    adaptor.writeFile(tempFile, contents, true);
    adaptor.moveFileAtomic(tempFile, file);
  }

  private boolean directoryExists(Path dir) throws WdkModelException {
    if (!adaptor.fileExists(dir))
      return false;

    if (adaptor.isDirectory(dir))
      return true;

    throw new WdkModelException("File exists and is not a directory: " + dir);
  }

  /**
   * Delete a link to an external dataset (specified by ownerUserId,datasetId).
   * Move the link from the external datasets dir to the removed external
   * datasets dir
   */
  private void deleteExternalUserDataset(Path recipientExternalDir,
    Path recipientRemovedDir, Long ownerUserId, Long datasetId) throws WdkModelException {
    if (!directoryExists(recipientRemovedDir))
      adaptor.createDirectory(recipientRemovedDir);
    Path externalDatasetFileName = recipientExternalDir.resolve(getExternalDatasetFileName(ownerUserId, datasetId));
    Path moveToFileName = recipientRemovedDir.resolve(getExternalDatasetFileName(ownerUserId, datasetId));
    adaptor.moveFileAtomic(externalDatasetFileName, moveToFileName);
  }

  /**
   * Locates the path to the external datasets directory for the given user.  If
   * no such path is found under the user's directory, the directory is
   * created.
   *
   * @param userId
   *   The user having potentially external datasets
   *
   * @return The path to the external datasets directory under the user's
   *   directory
   */
  private Path getOrCreateExternalDatasetDir(long userId)
  throws WdkModelException {
    Path externalDatasetDir = getOrCreateUserDir(userId).resolve(EXTERNAL_DATASETS_DIR);
    if (!adaptor.fileExists(externalDatasetDir))
      adaptor.createDirectory(externalDatasetDir);
    return externalDatasetDir;
  }

  /**
   * Returns the path to the external dataset link identified by the owner, the
   * recipient and the dataset to share/unshare.  Has the side-effect of
   * creating the external dataset link directory if it doesn't already exist.
   *
   * @param ownerId
   *   dataset owner
   * @param datasetId
   *   dataset id
   * @param recipientId
   *   recipient of the dataset share
   *
   * @return path of external dataset link
   */
  private Path getExternalDatasetLink(long ownerId, long datasetId, long recipientId)
  throws WdkModelException {
    Path externalDatasetLink = getOrCreateExternalDatasetDir(recipientId)
      .resolve(getExternalDatasetFileName(ownerId, datasetId));
    return fileExists(externalDatasetLink) ? externalDatasetLink : null;
  }

  /**
   * Return the path to the sharedWith file identified by the owner, the
   * recipient and the dataset to share/unshare.  Has the side-effect of
   * creating the sharedWith directory if it doesn't already exist.
   *
   * @param ownerUserId
   *   dataset owner
   * @param datasetId
   *   dataset
   * @param recipientUserId
   *   recipient of the dataset share
   *
   * @return - path to the sharedWith file
   */
  private Path getSharedWithFile(
    long ownerUserId,
    long datasetId,
    long recipientUserId
  ) throws WdkModelException {
    Path sharedWithFile = resolvePath(getSharedWithDir(ownerUserId, datasetId),
      recipientUserId);
    return fileExists(sharedWithFile) ? sharedWithFile : null;
  }

  /**
   * Get a file name to use as a link from a recipient to a shared dataset in
   * the owner's space.
   */
  private String getExternalDatasetFileName(long ownerUserId, long datasetId) {
    return ownerUserId + "." + datasetId;
  }

  /**
   * Write a file in a dataset dir, indicating that this dataset is shared with
   * another user.
   * <p>
   * The dataset shared with directory will be created if it did not previously
   * exist.
   */
  private void writeShareFile(long ownerId, long datasetId, long recipientId)
  throws WdkModelException {
    Path sharedWithFile = resolvePath(getSharedWithDir(ownerId, datasetId),
      recipientId);
    if (!adaptor.fileExists(sharedWithFile))
      adaptor.writeEmptyFile(sharedWithFile);
  }

  /**
   * Write a file in a user's space, indicating that this user can see another
   * user's dataset.
   */
  private void writeExternalDatasetLink(long ownerId, long datasetId,
    long recipientId) throws WdkModelException {
    adaptor.writeEmptyFile(resolvePath(
      getOrCreateExternalDatasetDir(recipientId),
      getExternalDatasetFileName(ownerId, datasetId)
    ));
  }

  /**
   * When the owner unshares a dataset, we need to remove the external dataset
   * link found in the recpient's workspace.  This removal with cause IRODS to
   * fire off a share/revoke event.
   */
  protected void removeExternalDatasetLink(
    long ownerId,
    long datasetId,
    long recipientId
  ) throws WdkModelException {
    Path recipientUserDir = getOrCreateUserDir(recipientId);

    // If no external datasets directory exists for this recipient or if the
    // external database link does not exist for this dataset, do nothing.
    // The client likely made an error.  Should we sent back something?
    Path recipientExternalDatasetsDir = recipientUserDir.resolve(EXTERNAL_DATASETS_DIR);
    if (directoryExists(recipientExternalDatasetsDir)) {
      Path externalDatasetFileName = recipientExternalDatasetsDir.resolve(getExternalDatasetFileName(ownerId, datasetId));
      if(fileExists(externalDatasetFileName))
        adaptor.deleteFileOrDirectory(externalDatasetFileName);
    }
  }

  class ExternalDatasetLink {
    long externalUserId;
    long datasetId;

    ExternalDatasetLink(Path externalLinkPath) throws WdkModelException {
      String linkName = externalLinkPath.getFileName().toString();
      String[] words = linkName.split(Pattern.quote("."));
      if (words.length != 2)
        throw new WdkModelException("Illegal external dataset link: " + linkName);
      try {
        externalUserId = Long.parseLong(words[0]);
        datasetId      = Long.parseLong(words[1]);
      } catch (NumberFormatException e) {
        throw new WdkModelException("Illegal external dataset link: " + linkName);
      }
    }

    String getFileName() {
      return "" + externalUserId + "." + datasetId;
    }
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

  /**
   * Given a user ID, return a Path to that user's datasets dir.  If dir doesn't
   * exist, return NULL.
   */
  private Path getUserDatasetsDirIfExists(Long userId) throws WdkModelException {
    Path userDatasetsDir = getOrCreateUserDir(userId).resolve(DATASETS_DIR);

    return !directoryExists(userDatasetsDir) ? null : userDatasetsDir;
  }

  /**
   * Given a user ID, return a Path to that user's datasets dir.
   */
  private Path requireUserDatasetsDir(long userId) throws WdkModelException {
    return getUserDatasetsDir(userId)
      .orElseThrow(() -> new WdkModelException("User datasets dir does not"
        + "exist for user: " + userId));
  }

  private UserDatasetUser getUserDatasetUser(Long userId) {
    if (usersMap.containsKey(userId))
      return usersMap.get(userId);
    final UserDatasetUser user = new UserDatasetUser();
    user.userId = userId;
    usersMap.put(userId, user);
    return user;
  }

  private List<Path> getDatasetDirs(Long userId, Path userDatasetsDir) throws WdkModelException {
    UserDatasetUser user = getUserDatasetUser(userId);
    if (user.datasetDirsList == null) {
      user.datasetDirsList = adaptor.getPathsInDir(userDatasetsDir);
    }
    return user.datasetDirsList;
  }

  /**
   * Locates the path to the sharedWith directory for the given user and
   * dataset.  If no such path is found under the dataset directory, the
   * directory is created.
   *
   * @param userId
   *   The user to whom the dataset belongs
   * @param datasetId
   *   The subject dataset
   *
   * @return The path to the sharedWith directory under the dataset's directory
   */
  private Path getSharedWithDir(long userId, long datasetId) throws WdkModelException {
    UserDatasetUser user = getUserDatasetUser(userId);

    Path userDatasetDir = getUserDatasetDir(userId, datasetId)
      .orElseThrow(() -> new WdkModelException("User " + userId + " does not"
        + " have dataset " + datasetId));

    if (user.sharedWithDirs.get(datasetId) == null) {
      Path sharedWithDir = userDatasetDir.resolve(SHARED_WITH_DIR);
      //TODO should we handle this upon setting up the dataset rather than on the fly?
      if (!adaptor.fileExists(sharedWithDir)) {
        adaptor.createDirectory(sharedWithDir);
      }
      user.sharedWithDirs.put(datasetId, sharedWithDir);
    }
    return user.sharedWithDirs.get(datasetId);
  }

  /**
   * Find the user dataset pointed to by an external link
   *
   * @return null if not found
   */
  private UserDataset followExternalDatasetLink(
    Long userId,
    ExternalDatasetLink link,
    Map<Long, Map<Long, UserDataset>> otherUsersCache
  ) throws WdkModelException {
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

  /**
   * Construct a user dataset object, given its location in the store. Note:
   * this method calls the store, but does not cache the values.  Calling code
   * should do so if needed.
   */
  private JsonUserDataset getUserDataset(Path datasetDir) throws WdkModelException {

    long datasetId;
    try {
      datasetId = Long.parseLong(datasetDir.getFileName().toString());
    } catch (NumberFormatException e) {
      throw new WdkModelException("Found file or directory '" + datasetDir.getFileName() +
        "' in user datasets directory " + datasetDir.getParent() + ". It is not a dataset ID");
    }

    JSONObject datasetJson = readAndParseJsonFile(datasetDir.resolve(DATASET_JSON_FILE));
    JSONObject metaJson = readAndParseJsonFile(datasetDir.resolve(META_JSON_FILE));

    return new JsonUserDataset(datasetId, datasetJson, metaJson, datasetDir.resolve(DATAFILES_DIR), this);
  }

  /**
   * Read a dataset.json file, and return the JSONObject that it parses to.
   */
  private JSONObject readAndParseJsonFile(Path jsonFile) throws WdkModelException {
    try {
      return new JSONObject(adaptor.readFileContents(jsonFile));
    } catch (JSONException e) {
      throw new WdkModelException("Could not parse " + jsonFile, e);
    }
  }

  private Optional<Path> getUserDatasetDir(long userId, long dsId)
  throws WdkModelException {
    return optionalDir(resolvePath(usersRootDir, userId, DATASETS_DIR, dsId));
  }

  private Optional<Path> getUserDatasetsDir(long userId)
  throws WdkModelException {
    return optionalDir(resolvePath(usersRootDir, userId, DATASETS_DIR));
  }

  private Optional<Path> getUserDir(long userId) throws WdkModelException {
    if (userDirsMap.containsKey(userId))
      return Optional.of(userDirsMap.get(userId));

    final Optional<Path> out = optionalDir(resolvePath(usersRootDir, userId));
    out.ifPresent(p -> userDirsMap.put(userId, p));
    return out;
  }

  private Optional<Path> optionalDir(Path dir) throws WdkModelException {
    return directoryExists(dir) ? Optional.of(dir) : Optional.empty();
  }

  /**
   * Returns a path to the user directory in iRODS for the given user, creating
   * that directory if necessary.
   *
   * @param userId
   *   id of the user for whom a path should be returned
   *
   * @return the path to the user directory
   *
   * @throws WdkModelException
   *   thrown if an adaptor error occurs while checking for the existence of or
   *   creating a user directory.
   */
  private Path getOrCreateUserDir(Long userId) throws WdkModelException {
    if (!userDirsMap.containsKey(userId)) {
      Path userDir = usersRootDir.resolve(userId.toString());
      if (!directoryExists(userDir)) {
        adaptor.createDirectory(userDir);
      }
      userDirsMap.put(userId, userDir);
    }
    return userDirsMap.get(userId);
  }

  private UserDatasetUser getExternalDatasetsMap(final UserDatasetUser user)
  throws WdkModelException {
    if (user.externalDatasetsMap != null)
      return user;

    final Path exDir = getOrCreateUserDir(user.userId).resolve(EXTERNAL_DATASETS_DIR);
    user.externalDatasetsMap = new HashMap<>();

    if (!adaptor.isDirectory(exDir))
      return user;

    final Iterator<Path> paths = adaptor.getPathsInDir(exDir)
      .stream()
      .map(Path::getFileName)
      .iterator();

    while (paths.hasNext()) {
      final ExternalDatasetLink link = new ExternalDatasetLink(paths.next());

      if (!isSharedWith(link.externalUserId, link.datasetId, user.userId))
        throw new WdkModelException(String.format(
          "Inconsistent iRODS state.  User %d has external dataset %s which is"
            + "not shared with them.",
          user.userId,
          link
        ));

      user.externalDatasetsMap.put(link.datasetId, getUserDataset(resolvePath(
        usersRootDir, link.externalUserId, DATASETS_DIR, link.datasetId
      )));
    }

    return user;
  }

  private boolean isSharedWith(
    final long ownerId,
    final long datasetId,
    final long targetId
  ) throws WdkModelException {
    return adaptor.fileExists(resolvePath(usersRootDir, ownerId, DATASETS_DIR,
      datasetId, SHARED_WITH_DIR, targetId));
  }

  private static Path resolvePath(final Path root, final Object... append) {
    Path tmp = root;
    for (final Object next : append)
      tmp = tmp.resolve(next.toString());
    return tmp;
  }

  /**
   * A Class to "memoize" data we get from the store
   *
   * @author Steve
   */
  private static class UserDatasetUser {
    long userId;
    Map<Long, UserDataset> datasetsMap = new HashMap<>();
    Map<Long, Set<UserDatasetShare>> sharedWithUsers = new HashMap<>();
    Map<Long, Path> sharedWithDirs = new HashMap<>();
    Map<Long, UserDataset> externalDatasetsMap;
    Map<Long, Boolean> userDatasetExistsMap  = new HashMap<>();
    List<Path> datasetDirsList;
    Long quota;
  }
}

