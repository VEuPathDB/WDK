package org.gusdb.wdk.service.request.user;

import java.util.*;

import org.gusdb.fgputil.json.JsonType;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static java.util.Arrays.stream;

/**
 * A pairing of action to optional list of identifiers to perofm the action on.
 * <p>
 * IDs can be of any type and are generated from JSON with the passed converter.
 * <p>
 * Input JSON must be of the form:
 * <pre>
 * {
 *   action: K,
 *   primaryKeys?: T[]
 * }
 * </pre>
 * <p>
 * If actions are present in input JSON that do not exist in validActions, a
 * DataValidationException is thrown.  If any IDs are not parsable by the
 * value converter, a DataValidationException (or possibly WdkModelException)
 * is thrown.
 * <p>
 * Once parsed, only strings in validActions can be requested;  Unrecognized
 * strings will throw IllegalArgumentException.
 *
 * @author rdoherty
 *
 * @param <K> action key type
 * @param <T> type of IDs parsed by this PatchMap
 */
public abstract class PatchMap<K, T> {

  private final K[] _validActions;

  private K _action;

  private Collection<T> _ids;

  public PatchMap(JSONObject json, K[] validActions)
      throws DataValidationException, WdkModelException, JSONException {
    _validActions = validActions;
    loadIds(json);
  }

  protected PatchMap(K[] validActions) {
    _validActions = validActions;
  }

  /**
   * Gets the action type.
   *
   * @return the action type.
   */
  public K getAction() {
    return _action;
  }

  /**
   * Gets collection of identifier values.
   *
   * @return collection of identifier values
   */
  public Collection<T> getIdentifiers() {
    return _ids;
  }

  /**
   * Set or overwrite the internal action type value.  This value will be
   * checked for validity.
   *
   * @param action new action type
   *
   * @throws DataValidationException thrown if the given action type is invalid.
   */
  protected void setAction(K action) throws DataValidationException {
    if (!stream(_validActions).anyMatch(t -> t.equals(action)))
      throw new DataValidationException("Unrecognized action: " + action);
    _action = action;
  }

  /**
   * Set or overwrite the internal collection of ID values.
   *
   * @param ids new collection of ID values
   */
  protected void setIdentifiers(Collection<T> ids) {
    _ids = ids;
  }

  /**
   * Parse an action type out of the given object.
   *
   * @param obj JSON action value
   *
   * @return parsed value of type {@link K}
   *
   * @throws DataValidationException thrown if the value could not be parsed
   *         into an instance of {@link K}.
   */
  protected abstract K parseAction(Object obj) throws DataValidationException;

  /**
   * Parse the value out of the given {@link JsonType} into a value of type
   * {@link T}.
   *
   * @param obj JSON PrimaryKey value
   *
   * @return Parsed value of type {@link T}.
   *
   * @throws DataValidationException thrown if the value is not valid or could
   *         not create a correct instance of {@link T}
   * @throws WdkModelException thrown if an internal error is encountered while
   *         attempting to parse the given value into an instance of {@link T}
   */
  protected abstract T parsePrimaryKey(JsonType obj)
      throws DataValidationException, WdkModelException;

  /**
   * Parses an input JSON object into a patch map instance.
   *
   * @param json JSON request body
   *
   * @throws DataValidationException Thrown if the input value fails validation
   *         for data correctness.
   * @throws WdkModelException Thrown if an internal error is encountered while
   *         attempting to parse the JSON into usable values.
   */
  private void loadIds(JSONObject json)
      throws DataValidationException, WdkModelException {
    setAction(parseAction(json.get(JsonKeys.ACTION)));

    Collection<T> ids = new ArrayList<>();

    JSONArray jsonArray = json.getJSONArray(JsonKeys.PRIMARY_KEYS);
    for (int i = 0; i < jsonArray.length(); i++)
      ids.add(parsePrimaryKey(new JsonType(jsonArray.get(i))));

    setIdentifiers(ids);
  }
}
