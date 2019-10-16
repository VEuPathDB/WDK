package org.gusdb.wdk.model.user.dataset.irods;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.TraceLog;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.UserDatasetShare;
import org.gusdb.wdk.model.user.dataset.irods.icat.ICatCollection;
import org.gusdb.wdk.model.user.dataset.irods.icat.ICatDataObject;
import org.gusdb.wdk.model.user.dataset.irods.icat.ICatNode;
import org.gusdb.wdk.model.user.dataset.json.JsonUserDataset;
import org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetSession;
import org.gusdb.wdk.model.user.dataset.json.JsonUserDatasetShare;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class IrodsUserDatasetSession extends JsonUserDatasetSession {

  private static final TraceLog TRACE = new TraceLog(IrodsUserDatasetSession.class);
  private static final Logger LOG = Logger.getLogger(IrodsUserDatasetSession.class);

  /**
   * iCAT metadata keys
   */
  private static final String
    AVU_DATASET_JSON = DATASET_JSON_FILE,
    AVU_META_JSON    = META_JSON_FILE;

  /**
   * Root node for an internal temporary local cache of the iRODS state.
   */
  private ICatCollection iCatMirror;

  /**
   * Paths for which metadata has already been fetched.
   * <p>
   * Used to avoid making unnecessary requests to iCAT
   */
  private final Set<ICatNode> fetchedMeta;

  IrodsUserDatasetSession(Path usersRootDir, Path wdkTempDir) {
    super(new IrodsUserDatasetStoreAdaptor(wdkTempDir), usersRootDir);
    iCatMirror  = new ICatCollection();
    fetchedMeta = new HashSet<>();
  }

  /*⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺*\
  ▏                                                        ▕
  ▏  Method Overrides                                      ▕
  ▏                                                        ▕
  \*⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽*/

  @Override
  public boolean checkUserDirExists(Long userId) throws WdkModelException {
    return TRACE.start(userId).end(loadUserDir(userId, false).isPresent());
  }

  @Override
  public boolean checkUserDatasetsDirExists(Long userId)
  throws WdkModelException {
    TRACE.start(userId);
    loadUserDir(userId, false);
    return TRACE.end(iCatMirror.containsCollection(makeDatasetsDirPath(userId)));
  }

  @Override
  public void close() {
    TRACE.start();
    getIrodsAdaptor().close();
    TRACE.end();
  }

  @Override
  public Long getModificationTime(Long userId) throws WdkModelException {
    TRACE.start(userId);
    loadUserDir(userId, false);
    return TRACE.end(iCatMirror.getCollection(makeDatasetsDirPath(userId))
      .map(ICatNode::getLastModified)
      .orElse(null));
  }

  @Override
  public List<Path> getRecentEvents(
    final String eventsDirectory,
    final long   lastEventId
  ) throws WdkModelException {
    TRACE.start(eventsDirectory, lastEventId);
    // If no prior event has been handled it is assumed that a new db is being
    // spun up and all prior events are needed.
    if (lastEventId < 1) {
      final List<Path> out = loadCollection(Paths.get(eventsDirectory), false)
        .map(col -> col.streamObjectsShallow()
          .sorted(Comparator.comparingLong(ICatNode::getLastModified))
          .map(ICatNode::getPath)
          .collect(Collectors.toList()))
        .orElseGet(Collections::emptyList);

      LOG.info("Number of events to be delivered is " + out.size());
      return TRACE.end(out);
    }

    final String cutoff = "event_" + lastEventId + ".json";

    LOG.info("Event Cutoff ID is " + cutoff);

    final String queryString = "select DATA_NAME where COLL_NAME like '"
      + eventsDirectory + "' AND DATA_NAME > '" + cutoff + "'";

    return TRACE.end(getIrodsAdaptor()
      .executeIrodsQuery(queryString)
      .stream()
      .map(eventFileName -> Paths.get(eventsDirectory, eventFileName))
      .collect(Collectors.toList()));
  }

  @Override
  public Set<UserDatasetShare> getSharedWith(Long ownerId, Long datasetId)
  throws WdkModelException {
    TRACE.start(ownerId, datasetId);
    loadUserDir(ownerId, false);

    final Optional<ICatCollection> optCol = iCatMirror.getCollection(
      makeShareDirPath(ownerId, datasetId));

    return TRACE.end(optCol.map(col -> col.streamObjectsShallow()
      .map(IrodsUserDatasetSession::iCat2DataShare)
      .collect(Collectors.toSet())).orElse(Collections.emptySet()));
  }

  @Override
  public JsonUserDataset getUserDataset(Long userId, Long datasetId)
  throws WdkModelException {
    TRACE.start(userId, datasetId);
    final ICatCollection dsCol = loadCollection(
      makeDatasetPath(userId, datasetId),
      false
    )
      .orElseThrow(Err.datasetNotFound(datasetId, userId));

    loadCollectionMeta(dsCol, false);

    return TRACE.end(collectionToDataset(datasetId, userId, dsCol));
  }

  @Override
  public UserDatasetFile getUserDatasetFile(Path path, long userDatasetId)
  throws WdkModelException {
    TRACE.start(path, userDatasetId);
    final ICatDataObject acdo = loadCollection(path.getParent(), false)
      .flatMap(c -> c.getObject(path.getFileName()))
      .orElse(null);
    return TRACE.end(new IrodsUserDatasetFile(path, acdo, userDatasetId));
  }

  @Override
  public Map<Long, UserDataset> getUserDatasets(Long userId)
  throws WdkModelException {
    TRACE.start(userId);
    // Ensure that we have the given user's directory cached
    loadUserDir(userId, false);

    // Attempt to pull the user's dataset directory from the cache
    final Path dsPath = makeDatasetsDirPath(userId);
    final Optional<ICatCollection> optCol = iCatMirror.getCollection(dsPath);

    // If the user's directory is not present even after attempting to load it
    // then it does not exist, return an empty map
    if (!optCol.isPresent())
      return TRACE.end(Collections.emptyMap());

    final ICatCollection col = optCol.get();

    // Recursively load all available metadata for the user's dataset directory
    loadAllCollectionMeta(col);

    final HashMap<Long, UserDataset> out = new HashMap<>();

    // Using a lazy iterator rather than a stream since we throw exceptions
    final Iterator<ICatCollection> datasets = col.streamCollectionsShallow()
      .iterator();

    // Build the map of user datasets.
    while (datasets.hasNext()) {
      final ICatCollection ds = datasets.next();
      final long dsId = Long.parseLong(ds.getName());
      out.put(dsId, collectionToDataset(dsId, userId, ds));
    }

    return TRACE.end(Collections.unmodifiableMap(out));
  }

  @Override
  public Map<Long, UserDataset> getExternalUserDatasets(Long userId)
  throws WdkModelException {
    TRACE.start(userId);
    loadUserDir(userId, false);

    final Optional<ICatCollection> optCol = iCatMirror.getCollection(
      makeExternalDsDirPath(userId));

    if (!optCol.isPresent())
      return TRACE.end(Collections.emptyMap());

    final Iterator<ExternalDatasetLink> links = optCol.get()
      .streamObjectsShallow()
      .map(ICatNode::getName)
      .map(this::newExtDsLink)
      .iterator();

    if (!links.hasNext())
      return TRACE.end(Collections.emptyMap());

    final Map<Long, UserDataset> out = new HashMap<>();

    // Explanation:
    //   At the time of implementation iCAT did not provide
    //   the ability to run queries containing multiple
    //   wildcards.  This means we cannot pull all the
    //   required data for multiple specific datasets in a
    //   single shot.
    while (links.hasNext()) {
      final ExternalDatasetLink link = links.next();
      final Optional<ICatCollection> extDs = loadShare(link, userId);

      if (!extDs.isPresent())
        continue;

      loadCollectionMeta(extDs.get(), false);
      out.put(link.datasetId, collectionToDataset(link.datasetId, userId, extDs.get()));
    }

    return TRACE.end(out);
  }

  @Override
  public Optional<UserDataset> getExternalUserDataset(long userId, long dsId)
  throws WdkModelException {
    TRACE.start(userId, dsId);
    final Optional<ICatCollection> optDir = getExternalDsCollection(userId, dsId);
    if(optDir.isPresent()) {
      final ICatCollection extDs = optDir.get();
      loadCollectionMeta(extDs, false);
      return TRACE.end(Optional.of(collectionToDataset(dsId, userId, extDs)));
    }
    return TRACE.end(Optional.empty());
  }

  @Override
  public Optional<UserDatasetFile> getExternalUserDatafile(
    final long userId,
    final long dsId,
    final String fileName
  ) throws WdkModelException {
    return TRACE.start(userId, dsId, fileName)
      .end(getExternalDsCollection(userId, dsId)
        .flatMap(c -> c.getObject(resolvePath(DATAFILES_DIR, fileName)))
        .map(o -> new IrodsUserDatasetFile(o.getPath(), o, dsId)));
  }

  @Override
  public boolean getUserDatasetExists(Long userId, Long datasetId)
  throws WdkModelException {
    return TRACE.start(userId, datasetId).end(loadUserDir(userId, false)
      .map(c -> c.containsCollection(resolvePath(DATASETS_DIR, datasetId)))
      .orElse(false));
  }

  /*⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺*\
  ▏                                                        ▕
  ▏  Internal API Methods                                  ▕
  ▏                                                        ▕
  \*⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽*/

  /**
   * Retrieves the {@link ICatCollection} representation of the iRODS collection
   * containing the user dataset belonging to the given dataset id.
   * <p>
   * The dataset must both exist and be shared with the user represented by the
   * given {@code userId} to be returned.
   *
   * @param userId
   *   share target user id
   * @param dsId
   *   external dataset id
   * @return an option containing an {@code ICatCollection} if the following
   *   criteria are met:
   *   <ol>
   *   <li>The given dataset id appears in the given user's external dataset
   *       link directory.
   *   <li>The given dataset id represents a dataset that presently exists in
   *       iRODS.
   *   <li>The dataset represented by the given dataset id has been shared with
   *       the given user.
   *   </ol>
   *
   * @throws WdkModelException
   *   if an error occurs while attempting to load the given user's directory
   *   in iRODS
   * @throws WdkModelException
   *   if the given user has an external dataset link that points to a dataset
   *   that does not exist
   * @throws WdkModelException
   *   if an error occurs while attempting to load the directory for the
   *   external dataset.
   */
  private Optional<ICatCollection> getExternalDsCollection(
    final long userId,
    final long dsId
  ) throws WdkModelException {
    TRACE.start(userId, dsId);
    final Optional<ICatCollection> optDir = loadUserDir(userId, false)
      .flatMap(c -> c.getCollection(resolvePath(EXTERNAL_DATASETS_DIR)));

    // if the user does not have an external datasets directory
    // then stop here
    if (!optDir.isPresent())
      return TRACE.end(Optional.empty());

    // Attempt to locate a file in the user's external datasets directory that
    // references the given dataset id.  If such a file exists, get it's owner
    // id.
    final ExternalDatasetLink link = optDir.get()
      .streamObjectsShallow()
      .map(ICatNode::getName)
      .map(this::newExtDsLink)
      .filter(x -> x.datasetId == dsId)
      .findFirst()
      .orElse(null);

    // if no link with the given dataset id exists, there is no owner to lookup,
    // stop here
    if (link == null)
      return TRACE.end(Optional.empty());

    return TRACE.end(loadShare(link, userId));
  }

  /**
   * Retrieves the {@code ICatCollection} pointed at by the given {@code
   * ExternalDatasetLink} and ensures that it is shared with the given target
   * user id.
   *
   * @param link
   *   link to the external dataset to be loaded
   * @param targetId
   *   target user to which the loaded dataset must be shared
   *
   * @return an option that will contain the requested {@code ICatCollection}
   *   if that collection exists and is shared with the given target user.
   *
   * @throws WdkModelException
   *   if an error occurs while attempting to load the {@code ICatCollection}
   *   from iRODS
   * @throws WdkModelException
   *   if the requested dataset does not exist, as this means iRODS has been
   *   left in an inconsistent state by a previous operation
   */
  private Optional<ICatCollection> loadShare(
    final ExternalDatasetLink link,
    final long targetId
  ) throws WdkModelException {
    TRACE.start(link, targetId);
    final ICatCollection extDs = loadCollection(
      makeDatasetPath(link.externalUserId, link.datasetId),
      false
    )
      .orElseThrow(Err.deadLink(targetId, link));

    return TRACE.end(!extDs.containsObject(resolvePath(SHARED_WITH_DIR, targetId))
      ? Optional.empty()
      : Optional.of(extDs));
  }

  /**
   * @return the backing data store adaptor downcast as {@code
   *   IrodsUserDatasetStoreAdaptor}.
   */
  private IrodsUserDatasetStoreAdaptor getIrodsAdaptor() {
    return TRACE.start().end((IrodsUserDatasetStoreAdaptor) getUserDatasetStoreAdaptor());
  }

  /**
   * Load the contents of the user directory for the given user into the local
   * mirror.
   * <p>
   * If the local mirror already contains an entry for the given user, {@code
   * force} will be used whether or not to overwrite that data.
   *
   * @param userId
   *   id of the user for which the directory contents will be fetched
   * @param force
   *   if the local mirror already contains the directory structure for the
   *   given user, {@code force} decides whether or not the data should be
   *   loaded anyway.
   *   <p>
   *   If {@code force == true} then the existing user directory data in the
   *   local mirror will be replaced with the new data.
   *
   * @throws WdkModelException
   *   see {@link ICatCollection#merge(ICatCollection)}
   */
  private Optional<ICatCollection> loadUserDir(final long userId, final boolean force)
  throws WdkModelException {
    return TRACE.start(userId, force)
      .end(loadCollection(makeUserDirPath(userId), force));
  }

  /**
   * Returns an {@link ICatCollection} located at the given path under the user
   * directory root.
   * <p>
   * This method will attempt to retrieve all contents under the desired path
   * from the temporary local mirror first.  If the path does not exist in the
   * local mirror, or if {@code force} is set to {@code true}, this method will
   * load the given path from iCAT into the local mirror.
   *
   * @param path
   *   path to the collection to load
   * @param force
   *   when set to true will force a pull from iCAT even if this path is already
   *   present in the local mirror, overwriting any data at that path in the
   *   local mirror with the newer result.
   *
   * @return an option containing a tree representation of the iRODS state for
   *   the given path, if that path exists in iRODS
   *
   * @throws WdkModelException
   *   if an error occurs while attempting to load the given path from iCAT.
   */
  private Optional<ICatCollection> loadCollection(
    final Path    path,
    final boolean force
  ) throws WdkModelException {
    TRACE.start(path, force);

    Optional<ICatCollection> optCol = iCatMirror.getCollection(path);

    if (!optCol.isPresent()) {
      optCol = getIrodsAdaptor().readFullPath(path);
    } else if (force) {
      iCatMirror.remove(path);
      optCol = getIrodsAdaptor().readFullPath(path);
    } else {
      return TRACE.end(optCol);
    }

    if (optCol.isPresent())
      iCatMirror.merge(optCol.get());

    return TRACE.end(iCatMirror.getCollection(path));
  }

  /**
   * Loads any custom metadata associated with the given collection
   * from iCAT, then populates the given collection with that metadata.
   * <p>
   * <b>WARNING:</b> Any metadata that previously existed on the input
   *   collection will be overwritten on read from iCAT.
   *
   * @param col
   *   collection for which metadata should be pulled.
   * @param force
   *   if set to true, will cause the metadata to be reloaded even if it has
   *   already been pulled.
   *
   * @throws WdkModelException
   *   if an error occurs while attempting to load metadata for the given
   *   collection from iCAT.
   */
  private void loadCollectionMeta(final ICatCollection col, final boolean force)
  throws WdkModelException {
    TRACE.start(col, force);
    if (!fetchedMeta.contains(col)) {
      getIrodsAdaptor().readMetadataInto(col);
      fetchedMeta.add(col);
    } else if (force) {
      col.clearMetadata();
      getIrodsAdaptor().readMetadataInto(col);
    }
    TRACE.end();
  }

  /**
   * Loads all metadata for the given collection and all available subpaths and
   * populates the given collection tree with that metadata.
   * <p>
   * <b>WARNING:</b> This method will remove all metadata currently stored on
   * the given collection or any of its sub nodes.
   *
   * @param col
   *   collection for which all available metadata should be retrieved
   *
   * @throws WdkModelException
   *   if an error occurs while attempting to fetch metadata from iCAT.
   */
  private void loadAllCollectionMeta(final ICatCollection col)
  throws WdkModelException {
    TRACE.start(col);
    col.clearMetadata();
    final Set<ICatNode> flat = col.streamRecursive()
      .peek(ICatNode::clearMetadata)
      .collect(Collectors.toSet());
    getIrodsAdaptor().readAllMetadataInto(col);
    fetchedMeta.addAll(flat);
    TRACE.end();
  }

  /**
   * Converts an {@link ICatDataObject} into a user data share instance.
   *
   * @param obj
   *   {@code ICatDataObject} from which a {@code UserDatasetShare} instance
   *   should be parsed.
   *
   * @return the parsed {@code UserDatasetShare} instance.
   */
  private static UserDatasetShare iCat2DataShare(final ICatDataObject obj) {
    return TRACE.start(obj)
      .end(new JsonUserDatasetShare(Long.valueOf(obj.getName()),
        Duration.ofSeconds(obj.getLastModified()).toMillis()));
  }

  /**
   * Attempts to read a metadata string from a given {@link ICatNode} and parse
   * it into a {@link JSONObject}.
   *
   * @param node
   *   {@code ICatNode} from which to pull the desired metadata
   * @param key
   *   Key at which the desired metadata JSON is stored on the given node
   *
   * @return the parsed {@code JSONObject instance}
   *
   * @throws WdkModelException
   *   if the given {@code ICatNode} does not have the requested key
   * @throws WdkModelException
   *   if an error occurs while attempting to parse the node's metadata string
   *   as JSON
   */
  private static JSONObject readAndParseJson(
    final ICatNode node,
    final String   key
  ) throws WdkModelException {
    TRACE.start(node, key);
    try {
      return TRACE.end(new JSONObject(node.getMetadata(key)
        .orElseThrow(Err.missingMeta(node, key))));
    } catch (JSONException e) {
      throw Err.malformedMetaJson(node, key, e);
    }
  }

  /**
   * Converts the given {@link ICatCollection} into a {@code JsonUserDataset}
   * by parsing it's attached metadata json from iRODS iCAT.
   *
   * @param dsId
   *   dataset ID
   * @param dsCol
   *   {@code ICatCollection} to be converted into a {@code JsonUserDataset}
   *
   * @return new {@code JsonUserDataset} populated by the input collection's
   *   JSON metadata.
   *
   * @throws WdkModelException
   *   if the collection is missing one or more required JSON metadata values
   * @throws WdkModelException
   *   if an error occurs while attempting to parse a JSON metadata string
   */
  private JsonUserDataset collectionToDataset(
    final long           dsId,
    final long           usId,
    final ICatCollection dsCol
  ) throws WdkModelException {
    TRACE.start(dsId, dsCol);

    // Does this even look look like a real dataset?
    // (do the dataset.json and meta.json files exist?)
    if (!dsCol.contains(AVU_DATASET_JSON) || !dsCol.contains(AVU_META_JSON))
      throw Err.datasetMissingMeta(dsId, usId);

    // Read the contents of the dataset and meta json files
    // from iCAT rather than iRODS to avoid any heavy
    // filesystem calls.
    JSONObject datasetJson = readAndParseJson(dsCol, AVU_DATASET_JSON);
    JSONObject metaJson    = readAndParseJson(dsCol, AVU_META_JSON);

    return TRACE.end(new JsonUserDataset(
      dsId,
      datasetJson.put(
        JsonUserDataset.DATA_FILES,
        dsCol.getCollection(Paths.get(DATAFILES_DIR))
          .map(ICatCollection::streamObjectsShallow)
          .orElseGet(Stream::empty)
          .map(ICatNode::getName)
          .map(s -> new JSONObject().put("name", s))
          .reduce(new JSONArray(), JSONArray::put, (a, b) -> a)
      ),
      metaJson,
      dsCol.getPath().resolve(DATAFILES_DIR),
      this
    ));
  }

  /**
   * Constructs a new {@link ExternalDatasetLink} instance from the given name
   * string, wrapping the potential parse exception in a {@link
   * WdkRuntimeException}.
   *
   * @param name
   *   external dataset link file name
   *
   * @return the constructed {@code ExternalDatasetLink}
   *
   * @throws WdkRuntimeException
   *   if the input file name could not be parsed into an {@code
   *   ExternalDatasetLink}.
   */
  private ExternalDatasetLink newExtDsLink(final String name) {
    TRACE.start(name);
    try { return TRACE.end(new ExternalDatasetLink(name)); }
    catch (WdkModelException e) { throw new WdkRuntimeException(e); }
  }

  /*⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺*\
  ▏                                                        ▕
  ▏  Static Internal Helpers                               ▕
  ▏                                                        ▕
  \*⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽*/

  /**
   * Resolves a {@link Path} instance from the given root name followed by the
   * given objects in input order.
   *
   * @param root
   *   root path element name
   * @param add
   *   elements to append to tha given root name
   *
   * @return a new path starting with the root name followed by all given
   *   additional elements.
   */
  private static Path resolvePath(final String root, final Object... add) {
    return TRACE.start(root, add).end(resolvePath(Paths.get(root), add));
  }

  /**
   * Resolves a {@link Path} instance from the given root path followed by the
   * given objects in input order.
   *
   * @param root
   *   root {@code Path} to append to
   * @param add
   *   elements to append to tha given root path
   *
   * @return the full {@code Path} with all given additional elements appended
   *   to it
   */
  private static Path resolvePath(final Path root, final Object... add) {
    TRACE.start(root, add);
    Path tmp = root;
    for (final Object o : add)
      tmp = tmp.resolve(o.toString());
    return TRACE.end(tmp);
  }

  // TODO: Replace me with a call to StringUtil.lpad once the strategy loading
  //   branch is merged in.
  private static String lPad(final String str, final char with, final int to) {
    TRACE.start(str, with, to);
    final StringBuilder sb = new StringBuilder(to);
    for (int i = to - str.length(); i > 0; i--)
      sb.append(with);
    return TRACE.end(sb.append(str).toString());
  }

  private static final class Err {

    private static final String
      MISSING_META = "Invalid iRODS state. Path %s is missing expected metadata"
      + " %s",
      MALFORMED_META = "Failed to parse JSON metadata for item %s at key %s",
      LINK_TO_DEAD_DS = "User %d has an external dataset link to an unreachable"
        + " dataset (dataset: %d, owner: %d)",
      ILLEGAL_PATH = "Attempted to load the iRODS path \"%s\" which is outside "
        + "of the user directory root.",
      DS_NOT_FOUND = "Dataset %d not found for user %d",
      BROKEN_DS = "Dataset %d for user %d does not contain both a dataset.json"
        + " file and a meta.json file";

    static Supplier<WdkModelException> missingMeta(
      final ICatNode node,
      final String   key
    ) {
      return () -> new WdkModelException(String.format(MISSING_META,
        node.getPath(), key));
    }

    static WdkModelException malformedMetaJson(
      final ICatNode      node,
      final String        key,
      final JSONException e
    ) {
      return new WdkModelException(String.format(MALFORMED_META,
          node.getPath(), key), e);
    }

    static Supplier<WdkModelException> deadLink(
      final long targetId,
      final ExternalDatasetLink link
    ) {
      return () -> new WdkModelException(String.format(LINK_TO_DEAD_DS, targetId, link.datasetId, link.externalUserId));
    }

    static WdkModelException illegalPath(final Path path) {
      return new WdkModelException(String.format(ILLEGAL_PATH, path));
    }

    static Supplier<WdkModelException> datasetNotFound(
      final long datasetId,
      final long userId
    ) {
      return () -> new WdkModelException(String.format(DS_NOT_FOUND,
        datasetId, userId));
    }

    static WdkModelException datasetMissingMeta(
      final long datasetId,
      final long userId
    ) {
      return new WdkModelException(String.format(BROKEN_DS, datasetId, userId));
    }
  }
}
