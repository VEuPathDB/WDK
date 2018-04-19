package org.gusdb.wdk.service.request.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.functional.FunctionalInterfaces.FunctionWithException;
import org.gusdb.fgputil.json.JsonType;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Creates a map from action string to a list of IDs.  IDs can be of any type and are generated from
 * JSON with the passed converter.  Input JSON must be of the form:
 * {
 *   "action1": [ id1, id2 ],
 *   "action2": [ id3, id4 ]
 * }
 * 
 * If actions are present in input JSON that do not exist in validActions, a DataValidationException
 * is thrown.  If any IDs are not parsable by the value converter, a DataValidationException (or possibly
 * WdkModelException) is thrown.
 * 
 * Once parsed, only strings in validActions can be requested;  Unrecognized strings will throw
 * IllegalArgumentException.
 * 
 * @author rdoherty
 *
 * @param <T> type of IDs parsed by this PatchMap
 */
@SuppressWarnings("serial")
public class PatchMap<T> extends HashMap<String,List<T>> {

  private final List<String> _validActions;

  public interface ValueConverter<T> extends FunctionWithException<JsonType, T> {
    @Override
    public T apply(JsonType idJson) throws DataValidationException, WdkModelException;
  }

  public PatchMap(JSONObject json, List<String> validActions, ValueConverter<T> valueConverter)
      throws DataValidationException, WdkModelException, JSONException {
    _validActions = validActions;
    loadIds(json, valueConverter);
  }

  // create an empty patch map
  protected PatchMap(List<String> validActions) {
    _validActions = validActions;
  }

  private void loadIds(JSONObject json, ValueConverter<T> valueConverter)
      throws DataValidationException, WdkModelException, JSONException {
    // load IDs for each action and record unrecognized actions
    List<Object> unrecognizedActions = new ArrayList<>();
    for (String actionType : JsonUtil.getKeys(json)) {
      if (_validActions.contains(actionType)) {
        List<T> ids = new ArrayList<>();
        JSONArray jsonArray = json.getJSONArray(actionType);
        for (int i = 0; i < jsonArray.length(); i++) {
          ids.add(valueConverter.apply(new JsonType(jsonArray.get(i))));
        }
        put(actionType, ids);
      }
      else {
        unrecognizedActions.add(actionType);
      }
    }
    if(!unrecognizedActions.isEmpty()) {
      String unrecognized = FormatUtil.join(unrecognizedActions.toArray(), ", ");
      throw new DataValidationException("Unrecognized actions: " + unrecognized);
    }

    // add empty ID lists if not supplied in request
    for (String action : _validActions) {
      if (!containsKey(action)) {
        put(action, Collections.EMPTY_LIST);
      }
    }
  }

  public void removeSharedIds(String action1, String action2) {
    List<T> list1 = get(test(action1));
    List<T> list2 = get(test(action2));
    for (int j, i = 0; i < list1.size(); i++) {
      T id = list1.get(i);
      if ((j = list2.indexOf(id)) != -1) {
        // found ID in both lists; remove from both
        list2.remove(j);
        list1.remove(i);
        i--; // recheck at the current index
      }
    }
  }

  @Override
  public List<T> get(Object action) {
    return super.get(test(action));
  }

  private String test(Object action) {
    if (!(action instanceof String) || !_validActions.contains(action)) {
      throw new IllegalArgumentException("Invalid action list requested: " + action);
    }
    return (String)action;
  }
}
