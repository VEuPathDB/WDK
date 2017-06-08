package org.gusdb.wdk.service.service.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.Favorite;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.UserBundle;
import org.gusdb.wdk.service.annotation.PATCH;
import org.gusdb.wdk.service.formatter.FavoritesFormatter;
import org.gusdb.wdk.service.request.user.FavoritesRequest;
import org.json.JSONObject;

public class FavoritesService extends UserService {
	
  private static final String NOT_LOGGED_IN = "The user is not logged in.";

  @SuppressWarnings("unused")
  private static Logger LOG = Logger.getLogger(FavoritesService.class);	

  public FavoritesService(@PathParam(USER_ID_PATH_PARAM) String uid) {
    super(uid);
  }
  

  //gets a full list of favorites
  @GET
  @Path("favorites")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFavorites() throws WdkModelException {
    UserBundle userBundle = getUserBundle(Access.PRIVATE);
	User user = userBundle.getTargetUser();
    if (user.isGuest()) {
      throw new ForbiddenException(NOT_LOGGED_IN);
    }
	Map<RecordClass, List<Favorite>> favorites = user.getFavorites();
    return Response.ok(FavoritesFormatter.getFavoritesJson(favorites).toString()).build();   
  }

  // creates a new favorite
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
      List<Map<String,Object>> ids = new ArrayList<Map<String,Object>>();
      ids.add(favoritesRequest.getPkValues());
      user.addToFavorite(favoritesRequest.getRecordClass(), ids);
      if(favoritesRequest.getNote() != null) {
        user.setFavoriteNotes(favoritesRequest.getRecordClass(), ids, favoritesRequest.getNote());
      }
      if(favoritesRequest.getGroup() == null) {
        user.setFavoriteGroups(favoritesRequest.getRecordClass(), ids, favoritesRequest.getGroup());
      }  
      return Response.noContent().build();
    }
    catch(WdkUserException e) {
      throw new BadRequestException(e);
    }
  }

  // deletes all of a user's favorites
  @DELETE
  @Path("favorites")
  public Response clearFavorites() throws WdkModelException {
    UserBundle userBundle = getUserBundle(Access.PRIVATE);
    User user = userBundle.getTargetUser();
    if (user.isGuest()) {
      throw new ForbiddenException(NOT_LOGGED_IN);
    }
    user.clearFavorite();
    return Response.noContent().build();  
  }

  // GET by POST: returns a single favorite (IDs + note + group)
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
      FavoritesRequest favoritesRequest = FavoritesRequest.createFromJson(json, getWdkModel());
      Favorite favorite = user.getFavorite(favoritesRequest.getRecordClass(), favoritesRequest.getPkValues());
      return Response.ok(FavoritesFormatter.getFavoriteJson(favorite).toString()).build();
    }
    catch(WdkUserException e) {
      throw new BadRequestException(e);
    }
  }

  // replaces an existing favorite with another (i.e. edits note and/or group)
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
      user.addToFavorite(favoritesRequest.getRecordClass(), ids);
      if(favoritesRequest.getNote() != null) {
        user.setFavoriteNotes(favoritesRequest.getRecordClass(), ids, favoritesRequest.getNote());
      }
      if(favoritesRequest.getGroup() == null) {
        user.setFavoriteGroups(favoritesRequest.getRecordClass(), ids, favoritesRequest.getGroup());
      }  
      return Response.noContent().build();
    }
    catch(WdkUserException e) {
      throw new BadRequestException(e);
    }   
  }

  // deletes a single favorite
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
      user.removeFromFavorite(favoritesRequest.getRecordClass(), ids);
      return Response.noContent().build();
    }
    catch(WdkUserException e) {
      throw new BadRequestException(e);
    }
  }  
}
