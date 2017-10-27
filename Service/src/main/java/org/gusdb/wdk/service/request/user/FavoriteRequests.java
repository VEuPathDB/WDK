package org.gusdb.wdk.service.request.user;

import static org.gusdb.fgputil.json.JsonUtil.getStringOrDefault;
import static org.gusdb.wdk.service.formatter.Keys.DELETE;
import static org.gusdb.wdk.service.formatter.Keys.UNDELETE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.PrimaryKeyValue;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordIdentity;
import org.gusdb.wdk.model.user.FavoriteFactory.NoteAndGroup;
import org.gusdb.wdk.service.formatter.Keys;
import org.gusdb.wdk.service.request.RecordRequest;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FavoriteRequests {

  private static Logger LOG = Logger.getLogger(FavoriteRequests.class);

  private FavoriteRequests() {}

  private static class NoteAndGroupImpl extends TwoTuple<String,String> implements NoteAndGroup {
    public NoteAndGroupImpl(String note, String group) {
      super(note, group);
    }
    @Override public String getNote() { return getFirst(); }
    @Override public String getGroup() { return getSecond(); }
  }

  public static class FavoriteEdit extends TwoTuple<RecordIdentity,NoteAndGroup> {
    public FavoriteEdit(RecordIdentity identity, NoteAndGroup noteAndGroup) {
      super(identity, noteAndGroup);
    }
    public RecordIdentity getIdentity() { return getFirst(); }
    public NoteAndGroup getNoteAndGroup() { return getSecond(); }
  }

  public static class FavoriteActions {

    private static final List<String> ACTION_TYPES = Arrays.asList(DELETE, UNDELETE);

    private Map<String,List<Long>> _favoriteActionMap;

    public FavoriteActions(Map<String,List<Long>> favoriteActionMap) {
      _favoriteActionMap = favoriteActionMap;
      // add empty ID lists if not supplied in request
      if (!_favoriteActionMap.containsKey(DELETE)) _favoriteActionMap.put(DELETE, Collections.EMPTY_LIST);
      if (!_favoriteActionMap.containsKey(UNDELETE)) _favoriteActionMap.put(UNDELETE, Collections.EMPTY_LIST);
      // clear out IDs that appear in both "delete" and "undelete"
      cleanData(_favoriteActionMap.get(DELETE), _favoriteActionMap.get(UNDELETE));
    }

    private void cleanData(List<Long> toDelete, List<Long> toUndelete) {
      for (int j, i = 0; i < toDelete.size(); i++) {
        Long id = toDelete.get(i);
        if ((j = toUndelete.indexOf(id)) != -1) {
          // found ID in both lists; remove from both
          toUndelete.remove(j);
          toDelete.remove(i);
          i--; // recheck at the current index
        }
      }
    }

    public List<Long> getIdsToDelete() {
      return _favoriteActionMap.get(DELETE);
    }

    public List<Long> getIdsToUndelete() {
      return _favoriteActionMap.get(UNDELETE);
    }
  }

  /**
   * Creates set of actions, each associated with a list of favorite ids
   * Input Format:
   * {
   *   delete: [Long, Long, ...],
   *   undelete: [Long, Long, ...]
   * }
   * 
   * @param json input object
   * @return parsed actions to perform on IDs
   */
  public static FavoriteActions parseFavoriteActionsJson(JSONObject json) {
    List<Object> unrecognizedActions = new ArrayList<>();
    Map<String, List<Long>> favoriteActionMap = new HashMap<>();
    for (String actionType : JsonUtil.getKeys(json)) {
      if (FavoriteActions.ACTION_TYPES.contains(actionType)) {
        List<Long> favoriteIds = new ArrayList<>();
        JSONArray jsonArray = json.getJSONArray(actionType);
        for (int i = 0; i < jsonArray.length(); i++) {
          Long favoriteId = jsonArray.getLong(i);	
          favoriteIds.add(favoriteId);
        }
        favoriteActionMap.put(actionType, favoriteIds);
      }
      else {
        unrecognizedActions.add(actionType);
      }
    }
    if(!unrecognizedActions.isEmpty()) {
      String unrecognized = FormatUtil.join(unrecognizedActions.toArray(), ",");
      LOG.warn("Favorites service will ignore the following unrecognized actions: " + unrecognized);
    }
    return new FavoriteActions(favoriteActionMap);
  }

  /**
   * Input Format:
   * 
   * {
   *  recordClassName: String,
   *  description: String (optional),
   *  group: String (optional),
   *  primaryKey: [
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
   * @throws DataValidationException 
   */
  public static FavoriteEdit createFromJson(JSONObject json, WdkModel wdkModel) throws RequestMisformatException, DataValidationException {
    try {
      String recordClassName = json.getString(Keys.RECORD_CLASS_NAME);
      RecordClass recordClass = wdkModel.getRecordClass(recordClassName);
      JSONArray pkArray = json.getJSONArray(Keys.PRIMARY_KEY);
      PrimaryKeyValue primaryKey = RecordRequest.parsePrimaryKey(pkArray, recordClass);
      String note = getStringOrDefault(json, Keys.DESCRIPTION, null);
      String group = getStringOrDefault(json, Keys.GROUP, null);
      return new FavoriteEdit(
          new RecordIdentity(recordClass, primaryKey),
          new NoteAndGroupImpl(note, group));
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
   *  description: String,
   *  group: String
   * }
   * 
   * @param json
   * @return
   * @throws RequestMisformatException
   * @throws DataValidationException 
   */
  public static NoteAndGroup createNoteAndGroupFromJson(JSONObject json)
      throws RequestMisformatException, DataValidationException {
    try {
      String note = getValidatedInputString(json, Keys.DESCRIPTION, 200);
      String group = getValidatedInputString(json, Keys.GROUP, 50);
      return new NoteAndGroupImpl(note, group);
    }
    catch (JSONException e) {
      String detailMessage = e.getMessage() != null ? e.getMessage() : "No additional information.";
      throw new RequestMisformatException(detailMessage, e);
    }
  }

  private static String getValidatedInputString(JSONObject container, String key, int maxLength)
      throws DataValidationException {
    String value = container.getString(key);
    if (FormatUtil.getUtf8EncodedBytes(value).length > maxLength) {
      throw new DataValidationException(
          "Value for property '" + key + "' cannot exceed " + maxLength + " UTF-8 characters.");
    }
    return value;
  }
}
