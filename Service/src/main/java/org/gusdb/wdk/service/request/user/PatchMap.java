package org.gusdb.wdk.service.request.user;

import static java.util.Arrays.stream;

import java.util.ArrayList;
import java.util.Collection;

import org.gusdb.fgputil.functional.FunctionalInterfaces.FunctionWithException;
import org.gusdb.fgputil.json.JsonIterators;
import org.gusdb.fgputil.json.JsonType;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A pairing of action to optional list of identifiers to perform the action on.
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

  private K _action;

  private Collection<T> _ids;

  public PatchMap(JSONObject json, K[] validActions,
      FunctionWithException<JsonType,K> actionParser,
      FunctionWithException<JsonType,T> idParser)
      throws DataValidationException, WdkModelException, JSONException {
    try {
      _action = validateAction(validActions,
          actionParser.apply(new JsonType(json.get(JsonKeys.ACTION))));
      _ids = new ArrayList<>();
      for (JsonType rawId : JsonIterators.arrayIterable(json.getJSONArray(JsonKeys.PRIMARY_KEYS))) {
        _ids.add(idParser.apply(rawId));
      }
    }
    catch (DataValidationException | WdkModelException | JSONException e) {
      throw e;
    }
    catch (Exception e) {
      throw new WdkModelException("Unable to parse patch request", e);
    }
  }

  public PatchMap(K[] validActions, K action, Collection<T> ids) throws DataValidationException {
    _action = validateAction(validActions, action);
    _ids = ids;
  }

  /**
   * Set or overwrite the internal action type value.  This value will be
   * checked for validity.
   *
   * @param action new action type
   *
   * @throws DataValidationException thrown if the given action type is invalid.
   */
  private K validateAction(K[] validActions, K action) throws DataValidationException {
    if (!stream(validActions).anyMatch(t -> t.equals(action)))
      throw new DataValidationException("Unrecognized action: " + action);
    return action;
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
}
