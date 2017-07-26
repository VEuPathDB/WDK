package org.gusdb.wdk.service.service.user;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
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
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.Favorite;
import org.gusdb.wdk.model.user.FavoriteFactory;
import org.gusdb.wdk.model.user.FavoriteFactory.FavoriteIdentity;
import org.gusdb.wdk.model.user.FavoriteFactory.NoteAndGroup;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.formatter.FavoritesFormatter;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.request.user.FavoritesRequest;
import org.gusdb.wdk.service.request.user.FavoritesRequest.FavoriteActions;
import org.gusdb.wdk.service.request.user.FavoritesRequest.FavoriteEdit;
import org.json.JSONArray;
import org.json.JSONObject;

public class FavoritesService extends UserService {

  private static final String NOT_LOGGED_IN = "The user is not logged in.";
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
   */
  @PUT
  @Path("favorites/{favoriteId}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response editFavoriteByFavoriteId(@PathParam(FAVORITE_ID_PATH_PARAM) Long favoriteId, String body) throws WdkModelException {
    User user = getPrivateRegisteredUser();
    JSONObject json = new JSONObject(body);
    try {
      NoteAndGroup noteAndGroup = FavoritesRequest.createNoteAndGroupFromJson(json);
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
    getWdkModel().getFavoriteFactory().removeFromFavorite(user,favoriteIds);
    return Response.noContent().build(); 
  }

  /**
   * Remove multiple favorites using a json array of favorite ids in the body of the request.
   * @param body - json array of favorite ids
   * @return - no response for successful execution
   * @throws WdkModelException
   */
  @PATCH
  @Path("favorites")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response batchDeleteFavoritesByFavoriteIds(String body) throws WdkModelException {
    User user = getPrivateRegisteredUser();
    FavoriteFactory factory = getWdkModel().getFavoriteFactory();
    JSONObject json = new JSONObject(body);
    FavoriteActions actions = FavoritesRequest.getFavoriteActionsJson(json);
    int numDeleted = factory.removeFromFavorite(user, actions.getIdsToDelete());
    int numUndeleted = factory.undeleteFavorites(user, actions.getIdsToUndelete());
    return Response.ok(FavoritesFormatter.getCountsJson(numDeleted, numUndeleted).toString()).build();
  }

  /**
   * Delete all of the user's favorites
   * @return - no response for successful execution
   * @throws WdkModelException
   */
  @DELETE
  @Path("favorites")
  public Response deleteAllFavorites() throws WdkModelException {
    User user = getPrivateRegisteredUser();
    int count = getWdkModel().getFavoriteFactory().deleteAllFavorites(user);
    return Response.ok(FavoritesFormatter.getCountJson(count).toString()).build();
  }

  /**
   * Creates a new favorite for the given user.  If the favorite has previously existed but was
   * deleted, the original is undeleted.
   * @param body
   * @return
   * @throws WdkModelException
   * @throws DataValidationException 
   */
  @POST
  @Path("favorites")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response addToFavorites(String body) throws WdkModelException, DataValidationException {
    User user = getPrivateRegisteredUser();
    JSONObject json = new JSONObject(body);
    try {
      FavoriteEdit newFavorite = FavoritesRequest.createFromJson(json, getWdkModel());
      FavoriteIdentity favSpec = newFavorite.getIdentity();
      FavoriteFactory factory = getWdkModel().getFavoriteFactory();
      factory.addToFavorites(user, favSpec.getRecordClass(), favSpec.getPrimaryKey().getRawValues());
      return Response.noContent().build();
    }
    catch(WdkUserException e) {
      throw new BadRequestException(e);
    }
  }

  /**
   * Input is an array of favorite specs (RC+PK) in the form of:
   * {
   *   recordClass: String,
   *   id: [
   *     {name : record_id1_name, value : record_id1_value},
   *     {name : record_id2_name: value " record_id2_value},
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
    User user = getPrivateRegisteredUser();
    WdkModel model = getWdkModel();
    JSONArray output = new JSONArray();
    for (JsonType favorite : JsonIterators.arrayIterable(new JSONArray(body))) {
      if (!favorite.getType().equals(JsonType.ValueType.OBJECT)) {
        throw new RequestMisformatException("All input array elements must be objects.");
      }
      FavoriteIdentity favId = FavoritesRequest.createFromJson(favorite.getJSONObject(), model).getIdentity();
      Favorite fav = model.getFavoriteFactory().getFavorite(user, favId.getRecordClass(), favId.getPrimaryKey().getRawValues());
      output.put(fav == null ? JSONObject.NULL : fav.getFavoriteId());
    }
    return Response.ok(output.toString()).build();
  }

  private User getPrivateRegisteredUser() throws WdkModelException {
    User user = getUserBundle(Access.PRIVATE).getTargetUser();
    if (user.isGuest()) {
      throw new ForbiddenException(NOT_LOGGED_IN);
    }
    return user;
  }

  /*
  // GET by POST: returns a single favorite (IDs + note + group)
  @Deprecated
  @POST
  @Path("favorites/instance")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFavorite(String body) throws WdkModelException {
    UserBundle userBundle = getUserBundle(Access.PRIVATE);
    User user = userBundle.getTargetUser();
    if (user.isGuest()) {
      throw new ForbiddenException(NOT_LOGGED_IN);
    }
    JSONObject json = new JSONObject(body);
    try {
      WdkModel model = getWdkModel();
      FavoritesRequest favoritesRequest = FavoritesRequest.createFromJson(json, model);
      Favorite favorite = model.getFavoriteFactory().getFavorite(
          user, favoritesRequest.getRecordClass(), favoritesRequest.getPkValues());
      return Response.ok(FavoritesFormatter.getFavoriteJson(favorite).toString()).build();
    }
    catch(WdkUserException e) {
      throw new BadRequestException(e);
    }
  }

  // replaces an existing favorite with another (i.e. edits note and/or group)
  @Deprecated
  @PUT
  @Path("favorites/instance")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response editFavorite(String body) throws WdkModelException {
    UserBundle userBundle = getUserBundle(Access.PRIVATE);
    User user = userBundle.getTargetUser();
    if (user.isGuest()) {
      throw new ForbiddenException(NOT_LOGGED_IN);
    }
    JSONObject json = new JSONObject(body);
    try {
      FavoritesRequest favoritesRequest = FavoritesRequest.createFromJson(json, getWdkModel());
      List<Map<String,Object>> ids = new ArrayList<Map<String,Object>>();
      ids.add(favoritesRequest.getPkValues());
      FavoriteFactory factory = getWdkModel().getFavoriteFactory();
      factory.addToFavorite(user, favoritesRequest.getRecordClass(), ids);
      if(favoritesRequest.getNote() != null) {
        factory.setNotes(user, favoritesRequest.getRecordClass(), ids, favoritesRequest.getNote());
      }
      if(favoritesRequest.getGroup() != null) {
        factory.setGroups(user, favoritesRequest.getRecordClass(), ids, favoritesRequest.getGroup());
      }  
      return Response.noContent().build();
    }
    catch(WdkUserException e) {
      throw new BadRequestException(e);
    }   
  }

  // deletes a single favorite
  @Deprecated
  @DELETE
  @Path("favorites/instance")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response removeFromFavorites(String body) throws WdkModelException {
    UserBundle userBundle = getUserBundle(Access.PRIVATE);
    User user = userBundle.getTargetUser();
    if (user.isGuest()) {
      throw new ForbiddenException(NOT_LOGGED_IN);
    }
    JSONObject json = new JSONObject(body);
    try {
      FavoritesRequest favoritesRequest = FavoritesRequest.createFromJson(json, getWdkModel());
      List<Map<String,Object>> ids = new ArrayList<Map<String,Object>>();
      ids.add(favoritesRequest.getPkValues());
      getWdkModel().getFavoriteFactory().removeFromFavorite(user, favoritesRequest.getRecordClass(), ids);
      return Response.noContent().build();
    }
    catch(WdkUserException e) {
      throw new BadRequestException(e);
    }
  }
  */
}
