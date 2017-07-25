package org.gusdb.wdk.service.service.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.Favorite;
import org.gusdb.wdk.model.user.FavoriteFactory;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.UserBundle;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.formatter.FavoritesFormatter;
import org.gusdb.wdk.service.request.user.FavoritesRequest;
import org.json.JSONObject;

public class FavoritesService extends UserService {

  private static final String NOT_LOGGED_IN = "The user is not logged in.";
  private static final String DELETE_ACTION = "delete";
  private static final String UNDELETE_ACTION = "undelete";
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
    UserBundle userBundle = getUserBundle(Access.PRIVATE);
    User user = userBundle.getTargetUser();
    if (user.isGuest()) {
      throw new ForbiddenException(NOT_LOGGED_IN);
    }
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
      UserBundle userBundle = getUserBundle(Access.PRIVATE);
      User user = userBundle.getTargetUser();
      if (user.isGuest()) {
        throw new ForbiddenException(NOT_LOGGED_IN);
      }
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
  
  //updates an existing favorite found by favorite id with another (i.e. edits note and/or group)
  /**
   * Updates an existing favorite found by its favorite id (if belonging to the given user) with a
   * new favorite.  The body need only contain note and group (both required but can be empty strings). 
   * @param favoriteId
   * @param body
   * @return - a 204 response in the event of a successful edit.
   * @throws WdkModelException
   */
  @PUT
  @Path("favorites/{favoriteId}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response editFavoriteByFavoriteId(@PathParam(FAVORITE_ID_PATH_PARAM) Long favoriteId, String body) throws WdkModelException {
    UserBundle userBundle = getUserBundle(Access.PRIVATE);
    User user = userBundle.getTargetUser();
    if (user.isGuest()) {
      throw new ForbiddenException(NOT_LOGGED_IN);
    }
    JSONObject json = new JSONObject(body);
    try {
      FavoritesRequest favoritesRequest = FavoritesRequest.createNoteAndGroupFromJson(json, getWdkModel());
      FavoriteFactory factory = getWdkModel().getFavoriteFactory();
      factory.editFavorite(user, favoriteId, favoritesRequest.getNote(), favoritesRequest.getGroup());
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
    UserBundle userBundle = getUserBundle(Access.PRIVATE);
    User user = userBundle.getTargetUser();
    if (user.isGuest()) {
      throw new ForbiddenException(NOT_LOGGED_IN);
    }
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
    UserBundle userBundle = getUserBundle(Access.PRIVATE);
    User user = userBundle.getTargetUser();
    if (user.isGuest()) {
      throw new ForbiddenException(NOT_LOGGED_IN);
    }
    JSONObject json = new JSONObject(body);
    FavoritesRequest favoritesRequest = FavoritesRequest.getFavoriteActionMapFromJson(json);
    Map<String,List<Long>> favoriteActionMap = favoritesRequest.getFavoriteActionMap();
    for(String action : favoriteActionMap.keySet()) {
      if(DELETE_ACTION.equals(action)) {
        getWdkModel().getFavoriteFactory().removeFromFavorite(user, favoriteActionMap.get(action));
      }
      if(UNDELETE_ACTION.equals(action)) {
        getWdkModel().getFavoriteFactory().undeleteFavorites(user, favoriteActionMap.get(action));
      }
    }
    return Response.noContent().build();
  }
  
  /**
   * Delete all of the user's favorites
   * @return - no response for successful execution
   * @throws WdkModelException
   */
  @DELETE
  @Path("favorites")
  public Response deleteAllFavorites() throws WdkModelException {
    UserBundle userBundle = getUserBundle(Access.PRIVATE);
    User user = userBundle.getTargetUser();
    if (user.isGuest()) {
      throw new ForbiddenException(NOT_LOGGED_IN);
    }
    getWdkModel().getFavoriteFactory().deleteAllFavorites(user);
    return Response.noContent().build();  
  }

  /**
   * Creates a new favorite for the given user.  If the favorite has previously existed but was
   * deleted, the original is undeleted.
   * @param body
   * @return
   * @throws WdkModelException
   */
  @POST
  @Path("favorites")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response addToFavorites(String body) throws WdkModelException {
    UserBundle userBundle = getUserBundle(Access.PRIVATE);
    User user = userBundle.getTargetUser();
    if (user.isGuest()) {
      throw new ForbiddenException(NOT_LOGGED_IN);
    }
    JSONObject json = new JSONObject(body);
    try {
      FavoritesRequest favoritesRequest = FavoritesRequest.createFromJson(json, getWdkModel());
      FavoriteFactory factory = getWdkModel().getFavoriteFactory();
      factory.addToFavorites(user, favoritesRequest.getRecordClass(), favoritesRequest.getPkValues());
      return Response.noContent().build();
    }
    catch(WdkUserException e) {
      throw new BadRequestException(e);
    }
  }
  
  // GET by POST
  @POST
  @Path("favorites/query")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFavoriteByProperties(String body) throws WdkModelException {
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
      if(favorite == null) {
      	return Response.ok("[]").build();
      }
      return Response.ok(FavoritesFormatter.getFavoriteJson(favorite).toString()).build();
    }
    catch(WdkUserException e) {
      throw new BadRequestException(e);
    }
  }
  


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
  
}
