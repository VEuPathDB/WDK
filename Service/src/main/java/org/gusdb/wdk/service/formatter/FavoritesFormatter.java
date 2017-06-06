package org.gusdb.wdk.service.formatter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.Favorite;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Formats favorites data.  Favorites JSON will have the following form:
 * {
 *   id: String (primary key),
 *   display: String,
 *   note: String,
 *   group: String,
 *   recordClassName: String (record class full name)
 *   
 * @author crisl-adm
 *
 */
public class FavoritesFormatter {
	
  public static JSONObject getFavoritesJson(Map<RecordClass, List<Favorite>> favorites) throws WdkModelException {
	JSONObject favoritesJson = new JSONObject();  
    try {
      for(RecordClass recordClass : favorites.keySet()) {
    	List<Favorite> favoritesList = favorites.get(recordClass);
    	JSONArray favoritesArray = new JSONArray();
    	favoritesList.forEach((favorite) -> favoritesArray.put(getFavoriteJson(favorite)));
        favoritesJson.put(recordClass.getFullName(), favoritesArray);
      }
      return favoritesJson;
    }
    catch(JSONException e) {
      throw new WdkModelException("Unable to convert Favorites to service JSON", e);
    }
  }

  protected static JSONObject getFavoriteJson(Favorite favorite) throws JSONException {
	  JSONObject favoriteJSON = new JSONObject();
	  favorite.getPrimaryKey().getValues().forEach((key, value) -> favoriteJSON.put(key, value)); 
      return favoriteJSON
	        .put(Keys.DISPLAY, favorite.getDisplay())
	        .put(Keys.NOTE, favorite.getNote())
	        .put(Keys.GROUP, favorite.getGroup())
	        .put(Keys.RECORD_CLASS_NAME, favorite.getRecordClass().getName());
  }
  
  public static JSONObject getNumberProcessedJson(int numProcessed) {
    return new JSONObject().put(Keys.FAV_NUMBER_PROCESSED, numProcessed);
  }
  
  public static JSONObject getGroupsJson(String[] groups) {
	JSONArray groupsArray = new JSONArray();
	Arrays.asList(groups).forEach((group) -> groupsArray.put(group)); 
    return new JSONObject().put(Keys.GROUPS, groupsArray);
  }
	
}
