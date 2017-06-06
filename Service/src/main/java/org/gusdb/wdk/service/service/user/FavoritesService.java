package org.gusdb.wdk.service.service.user;

import java.util.List;
import java.util.Map;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
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
  
  @GET
  @Path("favorites")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFavorites(String body) throws WdkModelException {
    UserBundle userBundle = getUserBundle(Access.PRIVATE);
	User user = userBundle.getTargetUser();
    if (user.isGuest()) {
      throw new ForbiddenException(NOT_LOGGED_IN);
    }
	Map<RecordClass, List<Favorite>> favorites = user.getFavorites();
    return Response.ok(FavoritesFormatter.getFavoritesJson(favorites).toString()).build();   
  }
  
  @GET
  @Path("favorites/numberProcessed")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFavoritesCount(String body) throws WdkModelException {
    UserBundle userBundle = getUserBundle(Access.PRIVATE);
	User user = userBundle.getTargetUser();
    if (user.isGuest()) {
      throw new ForbiddenException(NOT_LOGGED_IN);
    }
    JSONObject json = new JSONObject(body);
    try {
      FavoritesRequest favoritesRequest = FavoritesRequest.createFromJson(json, getWdkModel());
      int numProcessed = user.getFavoriteCount(favoritesRequest.getIds(), favoritesRequest.getRecordClass());
      return Response.ok(FavoritesFormatter.getNumberProcessedJson(numProcessed).toString()).build();
    }
    catch(WdkUserException e) {
     throw new BadRequestException(e);
    }
  }

  @PATCH
  @Path("favorites/add")
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
      user.addToFavorite(favoritesRequest.getRecordClass(), favoritesRequest.getIds());
      return Response.noContent().build();
    }
    catch(WdkUserException e) {
      throw new BadRequestException(e);
    }
  }
  
  @PATCH
  @Path("favorites/editNote")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response editNote(String body) throws WdkModelException {
    UserBundle userBundle = getUserBundle(Access.PRIVATE);
	User user = userBundle.getTargetUser();
    if (user.isGuest()) {
      throw new ForbiddenException(NOT_LOGGED_IN);
    }
    JSONObject json = new JSONObject(body);
    try {
      FavoritesRequest favoritesRequest = FavoritesRequest.createFromJson(json, getWdkModel());
      String note = favoritesRequest.getNote();
      if(note == null) {
        throw new BadRequestException("An entry for note must exist.");
      }
      user.setFavoriteNotes(favoritesRequest.getRecordClass(), favoritesRequest.getIds(), note);
      return Response.noContent().build();
    }
    catch(WdkUserException e) {
      throw new BadRequestException(e);
    }
  }
  
  @PATCH
  @Path("favorites/editGroup")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response editGroup(String body) throws WdkModelException {
    UserBundle userBundle = getUserBundle(Access.PRIVATE);
	User user = userBundle.getTargetUser();
    if (user.isGuest()) {
      throw new ForbiddenException(NOT_LOGGED_IN);
    }
    JSONObject json = new JSONObject(body);
    try {
      FavoritesRequest favoritesRequest = FavoritesRequest.createFromJson(json, getWdkModel());
      String group = favoritesRequest.getGroup();
      if(group == null) {
        throw new BadRequestException("An entry for group must exist.");
      }
      user.setFavoriteGroups(favoritesRequest.getRecordClass(), favoritesRequest.getIds(), group);
      return Response.noContent().build();
    }
    catch(WdkUserException e) {
      throw new BadRequestException(e);
    }
  }
  
  @DELETE
  @Path("favorites/clear")
  public Response clearFavorites(String body) throws WdkModelException {
    UserBundle userBundle = getUserBundle(Access.PRIVATE);
	User user = userBundle.getTargetUser();
    if (user.isGuest()) {
      throw new ForbiddenException(NOT_LOGGED_IN);
    }
    user.clearFavorite();
    return Response.noContent().build();  
  }
  
  @DELETE
  @Path("favorites/delete")
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
      user.removeFromFavorite(favoritesRequest.getRecordClass(), favoritesRequest.getIds());
      return Response.noContent().build();
    }
    catch(WdkUserException e) {
      throw new BadRequestException(e);
    }
  }
}
