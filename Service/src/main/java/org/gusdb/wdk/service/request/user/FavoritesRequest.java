package org.gusdb.wdk.service.request.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FavoritesRequest {
	
  @SuppressWarnings("unused")
  private static Logger LOG = Logger.getLogger(FavoritesRequest.class);
  
  private final List<Long> _favoriteIds;
  private final RecordClass _recordClass;
  private final Map<String,Object> _pkValues;
  private final String _note;
  private final String _group;
  
  public FavoritesRequest(List<Long> favoriteIds) {
    _favoriteIds = favoriteIds;
    _recordClass = null;
    _pkValues = null;
    _note = null;
	_group = null;
  }

  public FavoritesRequest(RecordClass recordClass, Map<String,Object> pkValues,
		  String note, String group) {  
	_recordClass = recordClass;
	_pkValues = pkValues;
	_note = note;
	_group = group;
	_favoriteIds = null;
  }
  
  /**
   * Creates a list of multiple favorite ids
   * Input Format:
   * 
   * [Long, Long, ...]
   * 
   * @param jsonArray
   * @return
   */
  public static FavoritesRequest getFavoriteIdsFromJson(JSONArray jsonArray) {
    List<Long> favoriteIds = new ArrayList<>();  
	for(int i = 0; i < jsonArray.length(); i++) {
      favoriteIds.add(jsonArray.getLong(i));
	}
	return new FavoritesRequest(favoriteIds);
  }
  
  /**
   * Input Format:
   * 
   * {
   *  recordClassName: String,
   *  note: String (optional),
   *  group: String (optional),
   *  id: [
   *    {name : record_id1_name, value : record_id1_value},
   *    {name : record_id2_name: value " record_id2_value},
   *    ...
   *  ]  
   * }
   * 
   * @param json
   * @param model
   * @return
   * @throws RequestMisformatException
   */
  public static FavoritesRequest createFromJson(JSONObject json, WdkModel wdkModel) throws RequestMisformatException {
    try {	
      RecordClass recordClass = null;
      Map<String, Object> pkValues = new LinkedHashMap<String, Object>();
      String recordClassName = json.getString("recordClassName");
      recordClass = wdkModel.getRecordClass(recordClassName);
      List<String> pkColumns = Arrays.asList(recordClass.getPrimaryKeyDefinition().getColumnRefs());
      JSONArray array = json.getJSONArray("id");
      for(int i = 0; i < array.length(); i++) {
        String name = array.getJSONObject(i).getString("name");
        if(!pkColumns.contains(name)) {
          throw new JSONException("Request contains an unknown primary key id " + name);
        }
        pkValues.put(name, array.getJSONObject(i).getString("value"));
      }
      String note = json.has("note") ? json.getString("note") : null;
      String group = json.has("group") ? json.getString("group") : null;
      return new FavoritesRequest(recordClass, pkValues, note, group);
    }
    catch (WdkModelException | JSONException e) {
      String detailMessage = e.getMessage() != null ? e.getMessage() : "No additional information.";
      throw new RequestMisformatException(detailMessage, e);
    }
  }
  
  /**
   * Creates a request with note and group only.  The favorite is identified by a favorite id
   * which appears in the url.
   * Input Format:
   * 
   * {
   *  note: String,
   *  group: String
   * }
   * 
   * @param json
   * @param model
   * @return
   * @throws RequestMisformatException
   */
  public static FavoritesRequest createNoteAndGroupFromJson(JSONObject json, WdkModel wdkModel) throws RequestMisformatException {
    try {
	  String note = json.getString("note");
	  String group = json.getString("group");
	  return new FavoritesRequest(null, null, note, group);
	}
	catch (JSONException e) {
      String detailMessage = e.getMessage() != null ? e.getMessage() : "No additional information.";
      throw new RequestMisformatException(detailMessage, e);
    }
  }

  public RecordClass getRecordClass() {
	return _recordClass;
  }

  public Map<String, Object> getPkValues() {
	return _pkValues;
  }
  
  public String getNote() {
	return _note;
  }
  
  public String getGroup() {
    return _group;
  }

  public List<Long> getFavoriteIds() {
	return _favoriteIds;
  }
  
  
  
}
