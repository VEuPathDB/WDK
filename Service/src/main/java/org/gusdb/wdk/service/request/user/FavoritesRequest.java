package org.gusdb.wdk.service.request.user;

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
  
  private final RecordClass _recordClass;
  private final Map<String,Object> _pkValues;
  private final String _note;
  private final String _group;
    
  public FavoritesRequest(RecordClass recordClass, Map<String,Object> pkValues,
		  String note, String group) {
	_recordClass = recordClass;
	_pkValues = pkValues;
	_note = note;
	_group = group;
  }
  
  /**
   * * Input Format:
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
      String recordClassName = json.getString("recordClassName");
      String note = json.has("note") ? json.getString("note") : null;
      String group = json.has("group") ? json.getString("group") : null;
      RecordClass recordClass = wdkModel.getRecordClass(recordClassName);
      List<String> pkColumns = Arrays.asList(recordClass.getPrimaryKeyDefinition().getColumnRefs());
      JSONArray array = json.getJSONArray("id");
      Map<String, Object> pkValues = new LinkedHashMap<String, Object>();
      for(int i = 0; i < array.length(); i++) {
        String name = array.getJSONObject(i).getString("name");
        if(!pkColumns.contains(name)) {
          throw new JSONException("Request contains an unknown primary key id " + name);
        }
        pkValues.put(name, array.getJSONObject(i).getString("value"));
      }
      return new FavoritesRequest(recordClass, pkValues, note, group);
    }
    catch (WdkModelException | JSONException e) {
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
  
}
