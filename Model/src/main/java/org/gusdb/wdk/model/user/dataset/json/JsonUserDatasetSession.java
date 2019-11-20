package org.gusdb.wdk.model.user.dataset.json;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.TraceLog;
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
  private static final TraceLog TRACE = new TraceLog(JsonUserDatasetSession.class);

  protected static final String DATASET_JSON_FILE = "dataset.json";
  protected static final String META_JSON_FILE = "meta.json";
  protected static final String EXTERNAL_DATASETS_DIR = "externalDatasets";
  protected static final String SHARED_WITH_DIR = "sharedWith";
  protected static final String REMOVED_EXTERNAL_DATASETS_DIR = "removedExternalDatasets";
  protected static final String DATASETS_DIR = "datasets";
  protected static final String DATAFILES_DIR = "datafiles";

  protected UserDatasetStoreAdaptor adaptor;
  protected Path usersRootDir;
  private Map<UserDatasetType, UserDatasetTypeHandler> typeHandlersMap;
  private Map<Long, UserDatasetUser> usersMap;
  private Map<Long, Path> userDirsMap;
  private Long defaultQuota;

  public JsonUserDatasetSession(UserDatasetStoreAdaptor adaptor, Path usersRootDir) {
    TRACE.start(adaptor, usersRootDir);

    this.typeHandlersMap = new HashMap<>();
    this.usersMap = new HashMap<>();
    this.userDirsMap = new HashMap<>();
    this.adaptor = adaptor;
    this.usersRootDir = usersRootDir;

    TRACE.end();
  }

  public void checkRootDirExists() throws WdkModelException {
    TRACE.start();
    if (!directoryExists(usersRootDir))
      throw new WdkModelException("Provided property 'rootPath' has value '"
        + usersRootDir + "' which is not an existing directory");
    TRACE.end();
  }

  @Override
  public Long getModificationTime(Long userId) throws WdkModelException {
    TRACE.start(userId);
    Optional<Path> userDatasetsDir = getUserDatasetsDir(userId);
    return TRACE.end(userDatasetsDir.isPresent()
      ? adaptor.getModificationTime(userDatasetsDir.get())
      : null);
  }

  @Override
  public Map<Long, UserDataset> getUserDatasets(Long userId)
  throws WdkModelException {
    TRACE.start(userId);

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
          // Intentionally empty?
        }
      }
    }

    return TRACE.end(Collections.unmodifiableMap(user.datasetsMap));
  }

  @Override
  public Set<UserDatasetShare> getSharedWith(Long ownerUserId, Long datasetId)
  throws WdkModelException {
    TRACE.start(ownerUserId, datasetId);

    UserDatasetUser user = getUserDatasetUser(ownerUserId);
    if (user.sharedWithUsers.get(datasetId) == null) {

      Path sharedWithDir = getSharedWithDir(ownerUserId, datasetId);

      List<Path> sharedWithPaths = adaptor.getPathsInDir(sharedWithDir);
      Set<UserDatasetShare> sharedWithItems = new HashSet<>();
      for (Path sharedWithPath : sharedWithPaths) {
        String userIdString = sharedWithPath.getFileName().toString();
        Long timestamp = adaptor.getModificationTime(sharedWithPath);
        sharedWithItems.add(new JsonUserDatasetShare(Long.valueOf(userIdString), timestamp));
      }
      user.sharedWithUsers.put(datasetId, sharedWithItems);
    }

    return TRACE.end(Collections.unmodifiableSet(user.sharedWithUsers.get(datasetId)));
  }

  @Override
  public Optional<UserDataset> getExternalUserDataset(long userId, long dsId)
  throws WdkModelException {
    return TRACE.start(userId, dsId).end(Optional.ofNullable(getExternalDatasetsMap(
      getUserDatasetUser(userId)).externalDatasetsMap.get(dsId)));
  }

  @Override
  public Optional<UserDatasetFile> getExternalUserDatafile(
    final long userId,
    final long datasetId,
    final String fileName
  ) throws WdkModelException {
    TRACE.start(userId, datasetId, fileName);
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
      return TRACE.end(userDatasetFile);
    }

    return TRACE.end(Optional.empty());
  }

  @Override
  public Map<Long, UserDataset> getExternalUserDatasets(Long userId)
  throws WdkModelException {
    TRACE.start(userId);
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
    return TRACE.end(Collections.unmodifiableMap(user.externalDatasetsMap));
  }

  // TODO: this is probably doable without 2 adaptor requests
  @Override
  public JsonUserDataset getUserDataset(Long userId, Long datasetId)
  throws WdkModelException {
    TRACE.start(userId, datasetId);
    Path userDatasetsDir = requireUserDatasetsDir(userId);
    Path datasetDir = userDatasetsDir.resolve(datasetId.toString());
    return TRACE.end(getUserDataset(datasetDir));
  }

  @Override
  public boolean getUserDatasetExists(Long userId, Long datasetId)
  throws WdkModelException {
    TRACE.start(userId, datasetId);
    UserDatasetUser user = getUserDatasetUser(userId);

    if (!user.userDatasetExistsMap.containsKey(datasetId))
      user.userDatasetExistsMap.put(datasetId,
        getUserDatasetDir(userId, datasetId).isPresent());

    return TRACE.end(user.userDatasetExistsMap.get(datasetId));
  }

  @Override
  public void updateMetaFromJson(Long userId, Long datasetId, JSONObject metaJson) throws WdkModelException {
    TRACE.start(userId, datasetId, metaJson);
    JsonUserDatasetMeta metaObj = new JsonUserDatasetMeta(metaJson);  // validate the input json
    Path metaJsonFile = requireUserDatasetsDir(userId).resolve(datasetId.toString()).resolve(META_JSON_FILE);
    writeFileAtomic(metaJsonFile, metaObj.getJsonObject().toString(), false);
    TRACE.end();
  }

  @Override
  public void shareUserDataset(Long ownerUserId, Long datasetId, Long recipientUserId) throws WdkModelException {
    TRACE.start(ownerUserId, datasetId, recipientUserId);
    if (recipientUserId.equals(ownerUserId)) {
      TRACE.log("Recipient == owner").end();
      return;  // don't think this is worth throwing an error on
    }

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
      TRACE.log("Already shared").end();
      return;
    }
    // Write the external dataset link last because only that put fires an IRODS event.
    writeShareFile(ownerUserId, datasetId, recipientUserId);
    writeExternalDatasetLink(ownerUserId, datasetId, recipientUserId);
    TRACE.end();
  }

  @Override
  public void shareUserDataset(
    final Long ownerUserId,
    final Long datasetId,
    final Set<Long> recipientUserIds
  ) throws WdkModelException {
    TRACE.start(ownerUserId, datasetId, recipientUserIds);
    for (Long recipientUserId : recipientUserIds)
      shareUserDataset(ownerUserId, datasetId, recipientUserId);
    TRACE.end();
  }

  @Override
  public void unshareUserDataset(Long ownerId, Long datasetId, Long recipientId)
  throws WdkModelException {
    TRACE.start(ownerId, datasetId, recipientId);
    if (recipientId.equals(ownerId)) {
      TRACE.log("Recipient == owner").end();
      return;  // don't think this is worth throwing an error on
    }

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
      TRACE.log("Dataset wasn't shared").end();
      return;
    }
    adaptor.deleteFileOrDirectory(sharedWithPath);
    adaptor.deleteFileOrDirectory(externalDatasetLink);
    TRACE.end();
  }

  @Override
  public void unshareUserDataset(
    final Long ownerUserId,
    final Long datasetId,
    final Set<Long> recipientUserIds
  ) throws WdkModelException {
    TRACE.start(ownerUserId, datasetId, recipientUserIds);
    for (Long recipientUserId : recipientUserIds)
      unshareUserDataset(ownerUserId, datasetId, recipientUserId);
    TRACE.end();
  }

  @Override
  public void unshareWithAll(Long ownerUserId, Long datasetId)
  throws WdkModelException {
    TRACE.start(ownerUserId, datasetId);

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
    TRACE.end();
  }

  @Override
  public void deleteUserDataset(Long userId, Long datasetId)
  throws WdkModelException {
    TRACE.start(userId, datasetId);
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
    TRACE.end();
  }

  @Override
  public void deleteExternalUserDataset(
    final Long ownerUserId,
    final Long datasetId,
    final Long recipientUserId
  ) throws WdkModelException {
    TRACE.start(ownerUserId, datasetId, recipientUserId);
    Path recipientExternalDatasetsDir = getOrCreateUserDir(recipientUserId).resolve(EXTERNAL_DATASETS_DIR);
    Path recipientRemovedDatasetsDir = getOrCreateUserDir(recipientUserId).resolve(REMOVED_EXTERNAL_DATASETS_DIR);
    deleteExternalUserDataset(recipientExternalDatasetsDir, recipientRemovedDatasetsDir, ownerUserId, datasetId);
    TRACE.end();
  }

  @Override
  public boolean checkUserDirExists(Long userId)  throws WdkModelException {
    return TRACE.start(userId).end(getUserDir(userId).isPresent());
  }

  @Override
  public Long getQuota(Long userId) throws WdkModelException {
    TRACE.start(userId);
    UserDatasetUser user = getUserDatasetUser(userId);
    if (user.quota == null) {
      Path userQuotaFile = getOrCreateUserDir(userId).resolve("quota");
      if (adaptor.fileExists(userQuotaFile)) {
        String line = adaptor.readSingleLineFile(userQuotaFile);
        if (line == null)
          throw new WdkModelException("Empty quota file " + userQuotaFile);
        user.quota = Long.valueOf(line.trim());
      } else user.quota = getDefaultQuota();
    }
    return TRACE.end(user.quota);
  }

  @Override
  public Long getDefaultQuota(boolean getFromStore) throws WdkModelException {
    TRACE.start(getFromStore);
    if (getFromStore || defaultQuota == null) {
      Path quotaFile = usersRootDir.resolve("default_quota");
      String line = adaptor.readSingleLineFile(quotaFile);
      if (line == null)
        throw new WdkModelException("Empty quota file " + quotaFile);
      defaultQuota = Long.valueOf(line.trim());
    }
    return TRACE.end(defaultQuota);
  }

  @Override
  public boolean checkUserDatasetsDirExists(Long userId) throws WdkModelException {
    return TRACE.start(userId).end(getUserDatasetsDir(userId).isPresent());
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
    return TRACE.getter(adaptor);
  }

  @Override
  public String initializeUserDatasetStoreId() throws WdkModelException {
    return TRACE.getter(adaptor.findUserDatasetStoreId(usersRootDir));
  }

  @Override
  public List<Path> getRecentEvents(String eventsDirectory, long lastHandledEventId) throws WdkModelException {
    return TRACE.start(eventsDirectory, lastHandledEventId).end(null);
  }

  /**
   * Added because IRODS connections need to be closed.
   */
  @Override
  public void close() {
    TRACE.start().end();
  }

  @Override
  public UserDatasetFile getUserDatasetFile(Path path, long userDatasetId)
  throws WdkModelException {
    // TODO Auto-generated method stub
    return TRACE.start(path, userDatasetId).end(null);
  }

  /*⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺*\
  ▏                                                        ▕
  ▏  Protected Methods                                     ▕
  ▏                                                        ▕
  \*⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽*/

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
    TRACE.start(ownerId, datasetId, recipientId);
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
    TRACE.end();
  }

  protected Path makeUserDirPath(final long userId) {
    return TRACE.start(userId).end(usersRootDir.resolve(String.valueOf(userId)));
  }

  protected Path makeDatasetsDirPath(final long userId) {
    return TRACE.start(userId)
      .end(makeUserDirPath(userId).resolve(DATASETS_DIR));
  }

  protected Path makeDatasetPath(final long userId, final long datasetId) {
    return TRACE.start(userId, datasetId).end(makeDatasetsDirPath(userId)
      .resolve(String.valueOf(datasetId)));
  }

  protected Path makeShareDirPath(final long userId, final long datasetId) {
    return TRACE.start(userId, datasetId).end(makeDatasetPath(userId, datasetId)
      .resolve(SHARED_WITH_DIR));
  }

  protected Path makeSharePath(
    final long ownerId,
    final long datasetId,
    final long targetId
  ) {
    return TRACE.start(ownerId, datasetId, targetId)
      .end(makeShareDirPath(ownerId, datasetId)
        .resolve(String.valueOf(targetId)));
  }

  protected Path makeExternalDsDirPath(final long userId) {
    return TRACE.start(userId)
      .end(makeUserDirPath(userId).resolve(EXTERNAL_DATASETS_DIR));
  }

  /*⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺*\
  ▏                                                        ▕
  ▏  Private Methods                                       ▕
  ▏                                                        ▕
  \*⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽*/

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
    TRACE.start(path);
    final boolean exists = adaptor.fileExists(path);
    if (exists && adaptor.isDirectory(path))
      throw new WdkModelException("The path given is not a file, but a directory: " + path);
    return TRACE.end(exists);
  }

  private void writeFileAtomic(
    final Path file,
    final String contents,
    final boolean errorIfTargetExists
  ) throws WdkModelException {
    TRACE.start(file, contents, errorIfTargetExists);
    if (errorIfTargetExists && adaptor.fileExists(file))
      throw new WdkModelException("File already exists: " + file);
    Path tempFile = file.getParent()
      .resolve(file.getFileName().toString() + "." + System.currentTimeMillis());
    adaptor.writeFile(tempFile, contents, true);
    adaptor.moveFileAtomic(tempFile, file);
    TRACE.end();
  }

  private boolean directoryExists(Path dir) throws WdkModelException {
    TRACE.start(dir);
    if (!adaptor.fileExists(dir))
      return TRACE.end(false);

    if (adaptor.isDirectory(dir))
      return TRACE.end(true);

    throw new WdkModelException("File exists and is not a directory: " + dir);
  }

  /**
   * Delete a link to an external dataset (specified by ownerUserId,datasetId).
   * Move the link from the external datasets dir to the removed external
   * datasets dir
   */
  private void deleteExternalUserDataset(
    final Path recipientExternalDir,
    final Path recipientRemovedDir,
    final Long ownerId,
    final Long dsId
  ) throws WdkModelException {
    TRACE.start(recipientExternalDir, recipientRemovedDir, ownerId, dsId);
    if (!directoryExists(recipientRemovedDir))
      adaptor.createDirectory(recipientRemovedDir);
    Path externalDatasetFileName = recipientExternalDir.resolve(getExternalDatasetFileName(ownerId, dsId));
    Path moveToFileName = recipientRemovedDir.resolve(getExternalDatasetFileName(ownerId, dsId));
    adaptor.moveFileAtomic(externalDatasetFileName, moveToFileName);
    TRACE.end();
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
    TRACE.start(userId);
    Path externalDatasetDir = getOrCreateUserDir(userId).resolve(EXTERNAL_DATASETS_DIR);
    if (!adaptor.fileExists(externalDatasetDir))
      adaptor.createDirectory(externalDatasetDir);
    return TRACE.end(externalDatasetDir);
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
    TRACE.start(ownerId, datasetId, recipientId);
    Path externalDatasetLink = getOrCreateExternalDatasetDir(recipientId)
      .resolve(getExternalDatasetFileName(ownerId, datasetId));

    return TRACE.end(fileExists(externalDatasetLink)
      ? externalDatasetLink
      : null);
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
    TRACE.start(ownerUserId, datasetId, recipientUserId);
    Path sharedWithFile = resolvePath(getSharedWithDir(ownerUserId, datasetId),
      recipientUserId);
    return TRACE.end(fileExists(sharedWithFile) ? sharedWithFile : null);
  }

  /**
   * Get a file name to use as a link from a recipient to a shared dataset in
   * the owner's space.
   */
  private String getExternalDatasetFileName(long ownerUserId, long datasetId) {
    return TRACE.start(ownerUserId, datasetId)
      .end(ownerUserId + "." + datasetId);
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
    TRACE.start(ownerId, datasetId, recipientId);
    Path sharedWithFile = resolvePath(getSharedWithDir(ownerId, datasetId),
      recipientId);
    if (!adaptor.fileExists(sharedWithFile))
      adaptor.writeEmptyFile(sharedWithFile);
    TRACE.end();
  }

  /**
   * Write a file in a user's space, indicating that this user can see another
   * user's dataset.
   */
  private void writeExternalDatasetLink(
    final long ownerId,
    final long datasetId,
    final long recipientId
  ) throws WdkModelException {
    TRACE.start();
    adaptor.writeEmptyFile(resolvePath(
      getOrCreateExternalDatasetDir(recipientId),
      getExternalDatasetFileName(ownerId, datasetId)
    ));
    TRACE.end();
  }

  protected static class ExternalDatasetLink {
    public long externalUserId;
    public long datasetId;

    public ExternalDatasetLink(Path externalLinkPath) throws WdkModelException {
      this(externalLinkPath.getFileName().toString());
    }

    public ExternalDatasetLink(String name) throws WdkModelException {
      String[] words = name.split(Pattern.quote("."));
      if (words.length != 2)
        throw new WdkModelException("Illegal external dataset link: " + name);
      try {
        externalUserId = Long.parseLong(words[0]);
        datasetId      = Long.parseLong(words[1]);
      } catch (NumberFormatException e) {
        throw new WdkModelException("Illegal external dataset link: " + name);
      }
    }

    String getFileName() {
      return "" + externalUserId + "." + datasetId;
    }
  }

  private Long getDefaultQuota() throws WdkModelException {
    TRACE.start();
    if (defaultQuota == null) {
      Path quotaFile = usersRootDir.resolve("default_quota");

      String line = adaptor.readSingleLineFile(quotaFile);
      if (line == null)
        throw new WdkModelException("Empty quota file " + quotaFile);
      defaultQuota = Long.valueOf(line.trim());
    }
    return TRACE.end(defaultQuota);
  }

  /**
   * Given a user ID, return a Path to that user's datasets dir.  If dir doesn't
   * exist, return NULL.
   */
  private Path getUserDatasetsDirIfExists(Long userId) throws WdkModelException {
    TRACE.start(userId);
    Path userDatasetsDir = getOrCreateUserDir(userId).resolve(DATASETS_DIR);
    return TRACE.end(!directoryExists(userDatasetsDir) ? null : userDatasetsDir);
  }

  /**
   * Given a user ID, return a Path to that user's datasets dir.
   */
  private Path requireUserDatasetsDir(long userId) throws WdkModelException {
    return TRACE.start(userId).end(getUserDatasetsDir(userId)
      .orElseThrow(() -> new WdkModelException("User datasets dir does not"
        + "exist for user: " + userId)));
  }

  private UserDatasetUser getUserDatasetUser(Long userId) {
    TRACE.start(userId);
    if (usersMap.containsKey(userId))
      return TRACE.end(usersMap.get(userId));
    final UserDatasetUser user = new UserDatasetUser();
    user.userId = userId;
    usersMap.put(userId, user);
    return TRACE.end(user);
  }

  private List<Path> getDatasetDirs(Long userId, Path userDatasetsDir) throws WdkModelException {
    TRACE.start(userId, userDatasetsDir);
    UserDatasetUser user = getUserDatasetUser(userId);
    if (user.datasetDirsList == null) {
      user.datasetDirsList = adaptor.getPathsInDir(userDatasetsDir);
    }
    return TRACE.end(user.datasetDirsList);
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
    TRACE.start(userId, datasetId);
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
    return TRACE.end(user.sharedWithDirs.get(datasetId));
  }

  /**
   * Find the user dataset pointed to by an external link
   *
   * @return null if not found
   */
  private UserDataset followExternalDatasetLink(
    final Long userId,
    final ExternalDatasetLink link,
    final Map<Long, Map<Long, UserDataset>> otherUsersCache
  ) throws WdkModelException {
    TRACE.start(userId, link, otherUsersCache);
    if (!checkUserDirExists(link.externalUserId)) {
      return TRACE.end(null);
    }

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
      if (found) return TRACE.end(originalDataset);
    }
    return TRACE.end(null);
  }

  /**
   * Construct a user dataset object, given its location in the store. Note:
   * this method calls the store, but does not cache the values.  Calling code
   * should do so if needed.
   */
  private JsonUserDataset getUserDataset(Path datasetDir) throws WdkModelException {
    TRACE.start(datasetDir);
    long datasetId;
    try {
      datasetId = Long.parseLong(datasetDir.getFileName().toString());
    } catch (NumberFormatException e) {
      throw new WdkModelException("Found file or directory '" + datasetDir.getFileName() +
        "' in user datasets directory " + datasetDir.getParent() + ". It is not a dataset ID");
    }

    JSONObject datasetJson = readAndParseJsonFile(datasetDir.resolve(DATASET_JSON_FILE));
    JSONObject metaJson = readAndParseJsonFile(datasetDir.resolve(META_JSON_FILE));

    return TRACE.end(new JsonUserDataset(datasetId, datasetJson, metaJson, datasetDir.resolve(DATAFILES_DIR), this));
  }

  /**
   * Read a dataset.json file, and return the JSONObject that it parses to.
   */
  private JSONObject readAndParseJsonFile(Path jsonFile) throws WdkModelException {
    TRACE.start(jsonFile);
    try {
      return TRACE.end(new JSONObject(adaptor.readFileContents(jsonFile)));
    } catch (JSONException e) {
      throw new WdkModelException("Could not parse " + jsonFile, e);
    }
  }

  private Optional<Path> getUserDatasetDir(long userId, long dsId)
  throws WdkModelException {
    return TRACE.start(userId, dsId)
      .end(optionalDir(makeDatasetPath(userId, dsId)));
  }

  private Optional<Path> getUserDatasetsDir(long userId)
  throws WdkModelException {
    return TRACE.start(userId).end(optionalDir(makeDatasetsDirPath(userId)));
  }

  private Optional<Path> getUserDir(long userId) throws WdkModelException {
    TRACE.start(userId);
    if (userDirsMap.containsKey(userId))
      return TRACE.end(Optional.of(userDirsMap.get(userId)));

    final Optional<Path> out = optionalDir(makeUserDirPath(userId));
    out.ifPresent(p -> userDirsMap.put(userId, p));
    return TRACE.end(out);
  }

  private Optional<Path> optionalDir(Path dir) throws WdkModelException {
    return TRACE.start(dir)
      .end(directoryExists(dir) ? Optional.of(dir) : Optional.empty());
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
  private Path getOrCreateUserDir(long userId) throws WdkModelException {
    TRACE.start(userId);
    if (!userDirsMap.containsKey(userId)) {
      TRACE.log("User dir is not in cache");
      Path userDir = makeUserDirPath(userId);
      if (!directoryExists(userDir)) {
        TRACE.log("User dir "+userDir+" does not exist");
        adaptor.createDirectory(userDir);
      }
      userDirsMap.put(userId, userDir);
    }
    final Path out = userDirsMap.get(userId);
    return TRACE.end(out);
  }

  private UserDatasetUser getExternalDatasetsMap(final UserDatasetUser user)
  throws WdkModelException {
    TRACE.start(user);
    if (user.externalDatasetsMap != null)
      return TRACE.end(user);

    final Path exDir = getOrCreateUserDir(user.userId).resolve(EXTERNAL_DATASETS_DIR);
    user.externalDatasetsMap = new HashMap<>();

    if (!adaptor.isDirectory(exDir))
      return TRACE.end(user);

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

      user.externalDatasetsMap.put(link.datasetId, getUserDataset(
        makeDatasetPath(link.externalUserId, link.datasetId)));
    }

    return TRACE.end(user);
  }

  private boolean isSharedWith(
    final long ownerId,
    final long datasetId,
    final long targetId
  ) throws WdkModelException {
    return TRACE.start(ownerId, datasetId, targetId)
      .end(adaptor.fileExists(resolvePath(usersRootDir, ownerId, DATASETS_DIR,
        datasetId, SHARED_WITH_DIR, targetId)));
  }

  /*⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺*\
  ▏                                                        ▕
  ▏  Private Static Methods                                ▕
  ▏                                                        ▕
  \*⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽*/

  private static Path resolvePath(final Path root, final Object... append) {
    TRACE.start(root, append);
    Path tmp = root;
    for (final Object next : append)
      tmp = tmp.resolve(next.toString());
    return TRACE.end(tmp);
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

