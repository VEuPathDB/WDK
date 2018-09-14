package org.gusdb.wdk.service.request.user;

import static org.gusdb.fgputil.json.JsonUtil.getStringOrDefault;
import static org.gusdb.wdk.service.formatter.JsonKeys.DELETE;
import static org.gusdb.wdk.service.formatter.JsonKeys.UNDELETE;

import java.util.Arrays;
import java.util.List;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.PrimaryKeyValue;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordIdentity;
import org.gusdb.wdk.model.user.FavoriteFactory.NoteAndGroup;
import org.gusdb.wdk.service.formatter.JsonKeys;
import org.gusdb.wdk.service.request.RecordRequest;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FavoriteRequests {

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

  @SuppressWarnings("serial")
  public static class FavoriteActions extends PatchMap<Long> {

    private static final List<String> ACTION_TYPES = Arrays.asList(DELETE, UNDELETE);

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
     * @throws DataValidationException
     * @throws WdkModelException
     */
    public FavoriteActions(JSONObject json) throws DataValidationException, WdkModelException {
      super(json, ACTION_TYPES, idJson -> idJson.getLong());
      removeSharedIds(DELETE, UNDELETE);
    }

    public List<Long> getIdsToDelete() { return get(DELETE); }
    public List<Long> getIdsToUndelete() { return get(UNDELETE); }

  }

  /**
   * Input Format:
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
   * @param wdkModel
   * @return
   * @throws RequestMisformatException
   * @throws DataValidationException 
   */
  public static FavoriteEdit createFromJson(JSONObject json, WdkModel wdkModel)
      throws RequestMisformatException, DataValidationException {
    try {
      String recordClassName = json.getString(JsonKeys.RECORD_CLASS_NAME);
      RecordClass recordClass = wdkModel.getRecordClass(recordClassName);
      JSONArray pkArray = json.getJSONArray(JsonKeys.PRIMARY_KEY);
      PrimaryKeyValue primaryKey = RecordRequest.parsePrimaryKey(pkArray, recordClass);
      String note = getStringOrDefault(json, JsonKeys.DESCRIPTION, null);
      String group = getStringOrDefault(json, JsonKeys.GROUP, null);
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
      String note = getValidatedInputString(json, JsonKeys.DESCRIPTION, 200);
      String group = getValidatedInputString(json, JsonKeys.GROUP, 50);
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
