package org.gusdb.wdk.service.request.user;

import static java.util.Arrays.stream;
import static org.gusdb.fgputil.json.JsonUtil.getStringOrDefault;

import java.util.Optional;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.PrimaryKeyValue;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordIdentity;
import org.gusdb.wdk.model.user.FavoriteFactory.NoteAndGroup;
import org.gusdb.wdk.service.request.RecordRequest;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
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

  public enum ActionType {
    DELETE("delete"),
    DELETE_ALL("deleteAll"),
    UNDELETE("undelete");

    final String value;

    ActionType(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    public static Optional<ActionType> fromString(String s) {
      return stream(values()).filter(v -> v.value.equals(s)).findFirst();
    }
  }

  public static class FavoriteActions extends PatchMap<ActionType, Long> {

    /**
     * Creates set of actions, each associated with a list of favorite IDs
     * <p>
     * Input Format:
     * <pre>
     * {
     *   action: delete|undelete,
     *   primaryKeys?: Long[]
     * }
     * </pre>
     *
     * @param json input object
     * @throws DataValidationException
     * @throws WdkModelException
     */
    public FavoriteActions(JSONObject json) throws DataValidationException, WdkModelException {
      super(json, ActionType.values(),
        val -> ActionType.fromString(val.toString()).orElseThrow(DataValidationException::new),
        val -> val.getLong());
    }
  }

  /**
   * Input Format:
   * <pre>
   * {
   *  recordClassName: String,
   *  description?: String,
   *  group?: String,
   *  primaryKey: [
   *    {name : record_id1_name, value : record_id1_value},
   *    {name : record_id2_name: value " record_id2_value},
   *    ...
   *  ]
   * }
   * </pre>
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

      RecordClass recordClass = wdkModel.getRecordClassByUrlSegment(recordClassName)
        .orElseThrow(() -> new DataValidationException(
          "No record type exists with name '" + recordClassName + "'."));

      PrimaryKeyValue primaryKey = RecordRequest.parsePrimaryKey(
        json.getJSONArray(JsonKeys.PRIMARY_KEY), recordClass);

      return new FavoriteEdit(
        new RecordIdentity(recordClass, primaryKey),
        new NoteAndGroupImpl(
          getStringOrDefault(json, JsonKeys.DESCRIPTION, null),
          getStringOrDefault(json, JsonKeys.GROUP, null)));
    }
    catch (WdkModelException | JSONException e) {
      throw new RequestMisformatException(Optional.ofNullable(e.getMessage())
          .orElse("No additional information."), e);
    }
  }

  /**
   * Creates a request with note and group only.  The favorite is identified by
   * a favorite id which appears in the url.
   * <p>
   * Input Format:
   * <pre>
   * {
   *   description: String,
   *   group: String
   * }
   * </pre>
   *
   * @param json
   * @return
   * @throws RequestMisformatException
   * @throws DataValidationException
   */
  public static NoteAndGroup createNoteAndGroupFromJson(JSONObject json,
      String oldNote, String oldGroup)
      throws RequestMisformatException, DataValidationException {
    String note = oldNote;
    String group = oldGroup;
    try {
      if (json.has(JsonKeys.DESCRIPTION))
        note = getValidatedInputString(json, JsonKeys.DESCRIPTION, 200);

      if (json.has(JsonKeys.GROUP))
        group = getValidatedInputString(json, JsonKeys.GROUP, 50);

      return new NoteAndGroupImpl(note, group);
    }
    catch (JSONException e) {
      throw new RequestMisformatException(Optional.ofNullable(e.getMessage())
          .orElse("No additional information."), e);
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
