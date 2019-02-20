package org.gusdb.wdk.service.service.user;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.json.JsonIterators;
import org.gusdb.fgputil.json.JsonType;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.RecordIdentity;
import org.gusdb.wdk.model.user.Favorite;
import org.gusdb.wdk.model.user.FavoriteFactory;
import org.gusdb.wdk.model.user.FavoriteFactory.NoteAndGroup;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.formatter.FavoritesFormatter;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.request.user.FavoriteRequests;
import org.gusdb.wdk.service.request.user.FavoriteRequests.FavoriteActions;
import org.gusdb.wdk.service.request.user.FavoriteRequests.FavoriteEdit;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Provides the following service endpoints (all behind /user/{id}):
 *
 * GET    /favorites              returns list of all favorites for the user
 * POST   /favorites              creates a new favorite (or returns existing if present or even deleted)
 * PATCH  /favorites              allows deletion and undeletion of multiple favorites in one request
 * DELETE /favorites              deletes all a user's favorites
 * GET    /favorites/{favoriteId} gets a favorite by ID
 * PUT    /favorites/{favoriteId} updates a favorite by ID
 * DELETE /favorites/{favoriteId} deletes a favorite by ID
 * POST   /favorites/query        queries favorite status (presence) of multiple records at one time
 *
 * @author crisl
 */
public class FavoritesService extends UserService {

  private static final String FAVORITE_ID_PATH_PARAM = "favoriteId";

  @SuppressWarnings("unused")
  private static Logger LOG = Logger.getLogger(FavoritesService.class);

  public FavoritesService(@PathParam(USER_ID_PATH_PARAM) String uid) {
    super(uid);
  }

  /**
   * Gets all the favorite belonging to the given user
   * @return - a json array representation of the list of favorites, which could be empty.
   * @throws WdkModelException
   */
  @GET
  @Path("favorites")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFavorites() throws WdkModelException {
    User user = getPrivateRegisteredUser();
    List<Favorite> favorites = getWdkModel().getFavoriteFactory().getAllFavorites(user);
    return Response.ok(FavoritesFormatter.getFavoritesJson(favorites).toString()).build();
  }


  /**
   * Remove multiple favorites using a json array of favorite ids in the body of the request
   *
   * @param body - json array of favorite ids
   * @return no response for successful execution
   * @throws WdkModelException
   * @throws DataValidationException
   */
  @PATCH
  @Path("favorites")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response batchDeleteFavoritesByFavoriteIds(String body) throws WdkModelException, DataValidationException {
    User user = getPrivateRegisteredUser();
    FavoriteFactory factory = getWdkModel().getFavoriteFactory();
    JSONObject json = new JSONObject(body);
    FavoriteActions actions = new FavoriteActions(json);
    int numDeleted = factory.deleteFavorites(user, actions.getIdsToDelete());
    int numUndeleted = factory.undeleteFavorites(user, actions.getIdsToUndelete());
    return Response.ok(FavoritesFormatter.getCountsJson(numDeleted, numUndeleted).toString()).build();
  }


  /**
   * Creates a new favorite for the given user.  If a favorite already exists for this record, it is returned.
   * If a favorite previously existed but was deleted, the original is undeleted and returned.
   *
   * @param body
   * @return
   * @throws WdkModelException
   * @throws DataValidationException
   */
  @POST
  @Path("favorites")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response addToFavorites(String body) throws WdkModelException, DataValidationException {
    User user = getPrivateRegisteredUser();
    JSONObject json = new JSONObject(body);
    try {
      FavoriteEdit newFavorite = FavoriteRequests.createFromJson(json, getWdkModel());
      RecordIdentity identity = newFavorite.getIdentity();
      FavoriteFactory factory = getWdkModel().getFavoriteFactory();
      Favorite favorite;
      if ((favorite = factory.getFavorite(user, identity.getRecordClass(), identity.getPrimaryKey().getRawValues())) == null) {
        favorite = factory.addToFavorites(user, identity.getRecordClass(), identity.getPrimaryKey().getRawValues());
      }
      return Response.ok(new JSONObject().put(JsonKeys.ID, favorite.getFavoriteId())).build();
    }
    catch(WdkUserException e) {
      throw new BadRequestException(e);
    }
  }

  /**
   * Get the favorite, for favorite id, if it belongs to the given user.
   * @param favoriteId
   * @return a json object representation of the favorite or a 404 if no such favorite is found
   * @throws WdkModelException
   */
  @GET
  @Path("favorites/{favoriteId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFavoriteByFavoriteId(@PathParam(FAVORITE_ID_PATH_PARAM) Long favoriteId) throws WdkModelException {
    try {
      User user = getPrivateRegisteredUser();
      Favorite favorite = getWdkModel().getFavoriteFactory().getFavorite(user, favoriteId);
      if(favorite == null) {
        throw new NotFoundException("No favorite with the id " + favoriteId + " was found for this user.");
      }
      return Response.ok(FavoritesFormatter.getFavoriteJson(favorite).toString()).build();
    }
    catch(NumberFormatException nfe) {
      throw new BadRequestException("The favoriteId, " + favoriteId + " is not a number.");
    }
  }

  /**
   * Updates an existing favorite found by its favorite id (if belonging to the given user) with a
   * new favorite.  The body need only contain note and group (both required but can be empty strings).
   * Note that once created, a user cannot alter the recordClass or primarykey data of a favorite.
   *
   * @param favoriteId
   * @param body
   * @return - a 204 response in the event of a successful edit.
   * @throws WdkModelException
   * @throws DataValidationException
   */
  @PUT
  @Path("favorites/{favoriteId}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response editFavoriteByFavoriteId(@PathParam(FAVORITE_ID_PATH_PARAM) Long favoriteId, String body)
      throws WdkModelException, DataValidationException {
    User user = getPrivateRegisteredUser();
    JSONObject json = new JSONObject(body);
    try {
      NoteAndGroup noteAndGroup = FavoriteRequests.createNoteAndGroupFromJson(json);
      FavoriteFactory factory = getWdkModel().getFavoriteFactory();
      factory.editFavorite(user, favoriteId, noteAndGroup.getNote(), noteAndGroup.getGroup());
      return Response.noContent().build();
    }
    catch(WdkUserException e) {
      throw new BadRequestException(e);
    }
  }

  /**
   * Delete favorite by given favorite id in url (no body)
   * @param favoriteId
   * @return - no response for successful execution
   * @throws WdkModelException
   */
  @DELETE
  @Path("favorites/{favoriteId}")
  public Response deleteFavoriteByFavoriteId(@PathParam(FAVORITE_ID_PATH_PARAM) Long favoriteId) throws WdkModelException {
    User user = getPrivateRegisteredUser();
    List<Long> favoriteIds = new ArrayList<>();
    favoriteIds.add(favoriteId);
    getWdkModel().getFavoriteFactory().deleteFavorites(user,favoriteIds);
    return Response.noContent().build();
  }

  /**
   * Input is an array of favorite specs (RC+PK) in the form of:
   * {
   *   recordClass: String,
   *   id: [
   *     { name: record_id1_name, value: record_id1_value },
   *     { name: record_id2_name, value: record_id2_value },
   *     ...
   *   ]
   * }
   *
   * Output is an integer array of identical size representing the
   * favorite IDs of any favorites found, with null placeholders for
   * favorites not found.  Ordering is the same as the incoming
   * array (i.e. output element at index N is the ID of incoming
   * favorite at index N (or null if not found)).
   *
   * @param body
   * @return
   * @throws WdkModelException
   * @throws RequestMisformatException
   * @throws DataValidationException
   */
  @POST
  @Path("favorites/query")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response queryFavoriteStatus(String body) throws WdkModelException, RequestMisformatException, DataValidationException {
    User user = getUserBundle(Access.PRIVATE).getSessionUser();
    WdkModel model = getWdkModel();
    JSONArray output = new JSONArray();
    for (JsonType favorite : JsonIterators.arrayIterable(new JSONArray(body))) {
      if (!favorite.getType().equals(JsonType.ValueType.OBJECT)) {
        throw new RequestMisformatException("All input array elements must be objects.");
      }
      RecordIdentity favId = FavoriteRequests.createFromJson(favorite.getJSONObject(), model).getIdentity();
      Favorite fav = model.getFavoriteFactory().getFavorite(user, favId.getRecordClass(), favId.getPrimaryKey().getRawValues());
      output.put(fav == null ? JSONObject.NULL : fav.getFavoriteId());
    }
    return Response.ok(output.toString()).build();
  }
}
