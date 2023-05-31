package org.gusdb.wdk.service.service.user;

import static java.nio.file.StandardOpenOption.READ;
import static org.gusdb.fgputil.IoUtil.createOpenPermsTempDir;
import static org.gusdb.fgputil.IoUtil.transferStream;
import static org.gusdb.wdk.service.FileRanges.CONTENT_RANGE_HEADER;
import static org.gusdb.wdk.service.FileRanges.parseRangeHeaderValue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.Range;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserCache;
import org.gusdb.wdk.model.user.UserFactory;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetFile;
import org.gusdb.wdk.model.user.dataset.UserDatasetInfo;
import org.gusdb.wdk.model.user.dataset.UserDatasetSession;
import org.gusdb.wdk.model.user.dataset.UserDatasetStore;
import org.gusdb.wdk.service.FileRanges.ByteRangeInformation;
import org.gusdb.wdk.service.UserBundle;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.formatter.UserDatasetFormatter;
import org.gusdb.wdk.service.formatter.UserDatasetsFormatter;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.request.user.UserDatasetShareRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *  TODO: validate
 *    - requested user exists
 *    - sharing:
 *      - target user exists
 *      - shared dataset exists
 *    - external datasets
 *      - original dataset exists, and is still shared
 *
 */
public class UserDatasetService extends UserService {

  private static Logger LOG = Logger.getLogger(UserDatasetService.class);

  public UserDatasetService(@PathParam(USER_ID_PATH_PARAM) String uid) {
    super(uid);
  }

  @GET
  @Path("user-datasets")
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray getAllUserDatasets(@QueryParam("expandDetails") @DefaultValue("false") Boolean expandDatasets) throws WdkModelException {
    LOG.debug("\nservice user-datasets has been called ---gets all user datasets\n");
    return getAllUserDatasetsJson(
        getWdkModel(),
        getPrivateRegisteredUser(),
        new UserDatasetFormatter(expandDatasets));
  }

  public static JSONArray getAllUserDatasetsJson(WdkModel wdkModel, User user, UserDatasetsFormatter formatter) throws WdkModelException {

    UserFactory userFactory = wdkModel.getUserFactory();
    UserDatasetStore dsStore = getUserDatasetStore(wdkModel);
    long userId = user.getUserId();

    try (UserDatasetSession dsSession = dsStore.getSession()) {

      // get all the user datasets this user can see that are installed in this application db.
      Set<Long> installedUserDatasets = wdkModel.getUserDatasetFactory().get().getInstalledUserDatasets(userId);

      // get all datasets owned by this user
      List<UserDatasetInfo> userDatasets = getDatasetInfo(dsSession.getUserDatasets(userId).values(),
          installedUserDatasets, dsStore, dsSession, userFactory, wdkModel, user);

      // get all datasets shared to this user
      List<UserDatasetInfo> sharedDatasets = getDatasetInfo(dsSession.getExternalUserDatasets(userId).values(),
          installedUserDatasets, dsStore, dsSession, userFactory, wdkModel, user);

      // use the formatter to format found user datasets
      return formatter.getUserDatasetsJson(dsSession, userDatasets, sharedDatasets);
    }
  }

  private static List<UserDatasetInfo> getDatasetInfo(final Collection<UserDataset> datasets,
      final Set<Long> installedUserDatasets, final UserDatasetStore dsStore, final UserDatasetSession dsSession,
      final UserFactory userFactory, final WdkModel wdkModel, User user) throws WdkModelException {
    UserCache userCache = new UserCache(userFactory);
    List<UserDatasetInfo> list = datasets.stream()
        .map(dataset -> new UserDatasetInfo(dataset,
            installedUserDatasets.contains(dataset.getUserDatasetId()),
            dsStore, dsSession, userCache, wdkModel))
        .collect(Collectors.toList());
    wdkModel.getUserDatasetFactory().get().addTypeSpecificData(wdkModel, list, user);
    return list;
  }

  @GET
  @Path("user-datasets/{datasetId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUserDataset(@PathParam("datasetId") String datasetIdStr) throws WdkModelException {
    LOG.debug("\nservice user-datasets/datasetId has been called  --gives you one dataset\n");

    WdkModel wdkModel = getWdkModel();
    User user = getPrivateRegisteredUser();
    long userId = user.getUserId();
    long datasetId = parseLongId(datasetIdStr, new NotFoundException("No dataset found with ID " + datasetIdStr));
    UserDatasetStore dsStore = getUserDatasetStore(wdkModel);
    String responseJson;
    try (UserDatasetSession dsSession = dsStore.getSession()) {
      UserDataset userDataset =
        dsSession.getUserDatasetExists(userId, datasetId) ?
        dsSession.getUserDataset(userId, datasetId) :
        dsSession.getExternalUserDataset(userId, datasetId).orElse(null);
      if (userDataset == null) {
        throw new NotFoundException("user-dataset/" + datasetIdStr);
      }
      Set<Long> installedUserDatasets = wdkModel.getUserDatasetFactory().get().getInstalledUserDatasets(userId);
      UserDatasetInfo dsInfo = new UserDatasetInfo(userDataset, installedUserDatasets.contains(datasetId),
        dsStore, dsSession, new UserCache(wdkModel.getUserFactory()), wdkModel);
      dsInfo.loadDetailedTypeSpecificData(user);
      responseJson = UserDatasetFormatter.getUserDatasetJson(dsInfo,
          userDataset.getOwnerId().equals(userId), true).toString();
    }
    return Response.ok(responseJson).build();
  }

  /**
   * Service to stream out a binary datafile from IRODS
   */
  @GET
  @Path("user-datasets/{datasetId}/user-datafiles/{datafileName}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response getBinaryDatafile(
    @PathParam("datasetId")    String datasetIdStr,
    @PathParam("datafileName") String datafileName,
    @HeaderParam("Range")      String fileRange
  ) throws WdkModelException {
    LOG.debug("\nservice user-datasets/datasetId/user-datafiles/filename has been called\n");

    long userId = getPrivateRegisteredUser().getUserId();
    long datasetId = parseLongId(datasetIdStr, new NotFoundException("No dataset found with ID " + datasetIdStr));
    UserDatasetStore dsStore = getUserDatasetStore(getWdkModel());
    try (UserDatasetSession dsSession = dsStore.getSession()) {
      final UserDatasetFile file = dsSession.getUserDatasetExists(userId, datasetId)
        ? dsSession.getUserDataset(userId, datasetId).getFile(dsSession, datafileName)
        : dsSession.getExternalUserDatafile(userId, datasetId, datafileName)
          .orElse(null);

      if (file == null)
        return Response.status(Status.NOT_FOUND).build();

      ByteRangeInformation rangeInfo = parseRangeHeaderValue(fileRange);
      return rangeInfo.isRangeHeaderSubmitted()
        ? getDatafileRange(dsSession, file, rangeInfo.getDesiredRange())
        : getFullDatafile(dsSession, file);
    } catch (IOException e) {
      throw new WdkModelException(e);
    }
  }

  @PUT
  @Path("user-datasets/{datasetId}/meta")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateMetaInfo(@PathParam("datasetId") String datasetIdStr, String body) throws WdkModelException {
    LOG.debug("\nservice user-datasets/datasetId/meta has been called\n");

    long userId = getPrivateRegisteredUser().getUserId();
    long datasetId = parseLongId(datasetIdStr, new NotFoundException("No dataset found with ID " + datasetIdStr));
    UserDatasetStore dsStore = getUserDatasetStore(getWdkModel());
    try (UserDatasetSession dsSession = dsStore.getSession()) {
      if (!dsSession.getUserDatasetExists(userId, datasetId)) throw new NotFoundException("user-dataset/" + datasetIdStr);
      JSONObject metaJson = new JSONObject(body);
      dsSession.updateMetaFromJson(userId, datasetId, metaJson);
      return Response.noContent().build();
    }
    catch (JSONException e) {
      throw new BadRequestException(e);
    }
  }

  /**
   * This service allows a WDK user to share/unshare owned datasets with other
   * WDK users.
   * <p>
   * The JSON object accepted by the service should have the following form:
   * <pre>
   *    {
   *      "add": {
   *        "dataset_id1": [ "user_id1", "user_id2" ]
   *        "dataset_id2": [ "user_id2" ]
   *      },
   *      "delete" {
   *        "dataset_id3": [ "user_id1", "user_id3" ]
   *      }
   *    }
   * </pre>
   */
  @PATCH
  @Path("user-dataset-sharing")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONObject manageShares(String body) throws WdkModelException, DataValidationException {
    long userId = getPrivateRegisteredUser().getUserId();
    JSONObject jsonObj = new JSONObject(body);
    UserDatasetStore dsStore = getUserDatasetStore(getWdkModel());
    try (UserDatasetSession dsSession = dsStore.getSession()) {
      Set<Long> ownedDatasetIds = dsSession.getUserDatasets(userId).keySet();
      UserDatasetShareRequest request = UserDatasetShareRequest.createFromJson(jsonObj, getWdkModel(), ownedDatasetIds);
      Map<String, Map<Long, Set<Long>>> userDatasetShareMap = request.getUserDatasetShareMap();
      for (String key : userDatasetShareMap.keySet()) {
        // Find datasets to share
        if ("add".equals(key)) {
          Set<Long> targetDatasetIds = userDatasetShareMap.get(key).keySet();
          for (Long targetDatasetId : targetDatasetIds) {
            Set<Long> targetUserIds = userDatasetShareMap.get(key).get(targetDatasetId);
            validateTargetUserIds(targetUserIds);
            // Since each dataset may be shared with different users, we share datasets one by one
            dsSession.shareUserDataset(userId, targetDatasetId, targetUserIds);
          }
        }
        // Find datasets to unshare
        if ("delete".equals(key)) {
          Set<Long> targetDatasetIds = userDatasetShareMap.get(key).keySet();
          for (Long targetDatasetId : targetDatasetIds) {
            Set<Long> targetUserIds = userDatasetShareMap.get(key).get(targetDatasetId);
            validateTargetUserIds(targetUserIds);
            // Since each dataset may unshared with different users, we unshare datasets one by one.
            dsSession.unshareUserDataset(userId, targetDatasetId, targetUserIds);
          }
        }
      }
      return UserDatasetFormatter.getUserDatasetSharesJson(
          getWdkModel().getUserFactory(), userDatasetShareMap);
    }
    catch(JSONException | RequestMisformatException e) {
      throw new BadRequestException(e);
    }
  }

  @DELETE
  @Path("user-datasets/{datasetId}")
  public Response deleteById(@PathParam("datasetId") String datasetIdStr) throws WdkModelException {
    long userId = getPrivateRegisteredUser().getUserId();
    long datasetId = parseLongId(datasetIdStr, new NotFoundException("No dataset found with ID " + datasetIdStr));
    UserDatasetStore dsStore = getUserDatasetStore(getWdkModel());
    try (UserDatasetSession dsSession = dsStore.getSession()) {
      dsSession.deleteUserDataset(userId, datasetId);
    }
    return Response.noContent().build();
  }

  public static UserDatasetStore getUserDatasetStore(WdkModel wdkModel) {
    return wdkModel.getUserDatasetStore().orElseThrow(
      () -> new NotFoundException("User datasets are not supported by this site."));
  }

  private long parseLongId(String idStr, RuntimeException exception) {
    if (FormatUtil.isInteger(idStr)) {
      return Long.parseLong(idStr);
    }
    throw exception;
  }

  /**
   * Determines whether set of target users is valid.  Any invalid user id
   * throws a Not Found exception.
   *
   * @param targetUserIds
   *   - set of target user ids to check for validity
   */
  private void validateTargetUserIds(Set<Long> targetUserIds) throws WdkModelException {
    for(Long targetUserId : targetUserIds) {
      UserBundle targetUserBundle = UserBundle.createFromTargetId(targetUserId.toString(), getSessionUser(), getWdkModel().getUserFactory(), isSessionUserAdmin());
      if (!targetUserBundle.isValidUserId()) {
        LOG.error("This user dataset share service request contains the following invalid user: " + targetUserId);
        throw new NotFoundException(formatNotFound(UserService.USER_RESOURCE + targetUserBundle.getTargetUserIdString()));
      }
    }
  }

  /**
   * Streams the selected range of bytes from the desired user dataset file from
   * a locally cached copy.
   * <p>
   * If no such copy exists, a copy will be created.
   * <p>
   * If another local copy being created is detected, this will attempt to wait
   * for the other download to complete before streaming the range
   */
  private Response getDatafileRange(
    final UserDatasetSession dsSess,
    final UserDatasetFile dsFile,
    final Range<Long> range // this range should always be inclusive
  ) throws WdkModelException {

    // if begin is omitted, set to 0 (zero offset)
    long begin = range.getBeginOpt().orElse(0L);

    // if end is present, add one for exclusivity; else use file size
    long end = range.getEndOpt().map(endrange -> endrange + 1).orElse(dsFile.getFileSize());

    return Response.status(Status.PARTIAL_CONTENT)
      .entity((StreamingOutput) out -> dsFile.readRangeInto(dsSess, begin, end - begin, out))
      .header(CONTENT_RANGE_HEADER, begin + "-" + (end - 1) + "/" + dsFile.getFileSize())
      .build();
  }

  /**
   * Streams the full contents of the desired user dataset file from either a
   * local cache copy or the external system.
   */
  private Response getFullDatafile(
    final UserDatasetSession dsSess,
    final UserDatasetFile dsFile
  ) throws IOException, WdkModelException {
    LOG.info("getFullDatafile");
    return Response.ok()
      .entity(streamAndDelete(dsFile.getLocalCopy(dsSess,
        createOpenPermsTempDir(getWdkModel().getModelConfig().getWdkTempDir(),
        "irods_"))))
      .build();
  }

  /**
   * Creates a {@code StreamingOutput} instance that will attempt to write all
   * the contents of the given file to the client output stream.
   *
   * @param file
   *   file to write out to the client
   *
   * @return A {@code StreamingOutput} instance
   */
  private StreamingOutput cacheStream(java.nio.file.Path file) {
    return output -> transferStream(output, Files.newInputStream(file, READ));
  }

  /**
   * Creates a {@code StreamingOutput} instance that will attempt to write all
   * the contents of the given file to the client output stream before deleting
   * the given file.
   *
   * @param file
   *   file that will be written to the output stream then deleted
   *
   * @return A {@code StreamingOutput} instance
   */
  private StreamingOutput streamAndDelete(java.nio.file.Path file) {
    return output -> {
      try {
        cacheStream(file).write(output);
      } finally {
        Files.delete(file);
        Files.delete(file.getParent());
      }
    };
  }
}
