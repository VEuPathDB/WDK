package org.gusdb.wdk.service.request.user;

import java.util.ArrayList;
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
  private final List<Map<String,Object>> _ids;
  private final String _note;
  private final String _group;
    
  public FavoritesRequest(RecordClass recordClass, List<Map<String,Object>> ids,
		  String note, String group) {
	_recordClass = recordClass;
	_ids = ids;
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
   *  data: [{project_id: String, source_id: String}]
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
      String[] pkColumns = recordClass.getPrimaryKeyDefinition().getColumnRefs();
      JSONArray array = json.getJSONArray("data");
      List<Map<String, Object>> ids = new ArrayList<Map<String, Object>>();
      for (int i = 0; i < array.length(); i++) {
        JSONObject object = array.getJSONObject(i);
        Map<String, Object> pkValues = new LinkedHashMap<String, Object>();
        for (String column : pkColumns) {
          pkValues.put(column, object.getString(column));
        }
        ids.add(pkValues);
      }
      return new FavoritesRequest(recordClass, ids, note, group);
    }
    catch (WdkModelException | JSONException e) {
      String detailMessage = e.getMessage() != null ? e.getMessage() : "No additional information.";
      throw new RequestMisformatException(detailMessage, e);
    }
  }

  public RecordClass getRecordClass() {
	return _recordClass;
  }

  public List<Map<String, Object>> getIds() {
	return _ids;
  }
  
  public String getNote() {
	return _note;
  }
  
  public String getGroup() {
    return _group;
  }
  
}
