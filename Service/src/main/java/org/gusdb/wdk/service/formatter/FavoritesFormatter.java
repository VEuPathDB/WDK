package org.gusdb.wdk.service.formatter;

import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.Favorite;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Formats favorite data to JSON.  Each favorite will have the following form:
 * {
 *   primaryKey: [
 *     { name : record_id1_name, value : record_id1_value },
 *     { name : record_id2_name: value " record_id2_value },
 *    ...
 *   ],
 *   id: Long,
 *   displayName: String,
 *   description: String,
 *   group: String,
 *   recordClassName: String (record class full name)
 * }
 * 
 * @author crisl-adm
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
    Map<String, String> pkValues = favorite.getPrimaryKey().getValues();
    JSONArray pkValuesJson = new JSONArray();
    for(String key : pkValues.keySet()) {
      JSONObject pkValueJson = new JSONObject();
      pkValueJson.put(JsonKeys.NAME, key).put(JsonKeys.VALUE, pkValues.get(key));
      pkValuesJson.put(pkValueJson);
    }
    return new JSONObject()
        .put(JsonKeys.ID, favorite.getFavoriteId())
        .put(JsonKeys.PRIMARY_KEY, pkValuesJson)
        .put(JsonKeys.RECORD_CLASS_NAME, favorite.getRecordClass().getFullName())
        .put(JsonKeys.DISPLAY_NAME, favorite.getDisplay())
        .put(JsonKeys.DESCRIPTION, favorite.getNote())
        .put(JsonKeys.GROUP, favorite.getGroup());
  }

  public static JSONObject getCountsJson(int numDeleted, int numUndeleted) {
    return new JSONObject()
        .put(JsonKeys.DELETE, numDeleted)
        .put(JsonKeys.UNDELETE, numUndeleted);
  }

  public static JSONObject getCountJson(int count) throws JSONException {
    return new JSONObject().put(JsonKeys.NUMBER_PROCESSED, count);
  }

}
