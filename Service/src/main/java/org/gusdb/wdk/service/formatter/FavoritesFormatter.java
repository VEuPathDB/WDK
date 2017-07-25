package org.gusdb.wdk.service.formatter;

import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.Favorite;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Formats favorites data.  Favorites JSON will have the following form:
 * {
 *  id: [
 *    {name : record_id1_name, value : record_id1_value},
 *    {name : record_id2_name: value " record_id2_value},
 *    ...
 *  ],
 *   favoriteId: Long,
 *   display: String,
 *   note: String,
 *   group: String,
 *   recordClassName: String (record class full name)
 *   
 * @author crisl-adm
 *
 */
public class FavoritesFormatter {
	
  public static JSONArray getFavoritesJson(List<Favorite> favorites) throws WdkModelException {
	JSONArray favoritesJson = new JSONArray();
    try {
      favorites.forEach((favorite) -> favoritesJson.put(getFavoriteJson(favorite)));
      return favoritesJson;
    }
    catch(JSONException e) {
      throw new WdkModelException("Unable to convert Favorites to service JSON", e);
    }
  }

  public static JSONObject getFavoriteJson(Favorite favorite) throws JSONException {
	  JSONObject favoriteJSON = new JSONObject();
	  Map<String, String> pkValues = favorite.getPrimaryKey().getValues();
	  JSONArray pkValuesJson = new JSONArray();
	  for(String key : pkValues.keySet()) {
        JSONObject pkValueJson = new JSONObject();
        pkValueJson.put("name", key).put("value", pkValues.get(key));
        pkValuesJson.put(pkValueJson);
	  }	  
      return favoriteJSON
    		.put(Keys.ID, pkValuesJson)
    		.put(Keys.FAVORITE_ID, favorite.getFavoriteId())
	        .put(Keys.DISPLAY, favorite.getDisplay())
	        .put(Keys.NOTE, favorite.getNote())
	        .put(Keys.GROUP, favorite.getGroup())
	        .put(Keys.RECORD_CLASS_NAME, favorite.getRecordClass().getFullName());
  }
  
  public static JSONObject getCountsJson(Map<String, Integer> countsMap) {
	JSONObject countsJson = new JSONObject();  
    for(String key : countsMap.keySet()) {
      countsJson.put(key, countsMap.get(key));
    }
    return countsJson;
  }
  
  public static JSONObject getCountJson(int count) throws JSONException {
    return new JSONObject().put("count", count);
  }

}
