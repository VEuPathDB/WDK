package org.gusdb.wdk.service.request.user;

import static org.gusdb.wdk.service.formatter.Keys.ADD;
import static org.gusdb.wdk.service.formatter.Keys.REMOVE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.PrimaryKeyValue;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.service.request.RecordRequest;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BasketRequests {

  private static final Logger LOG = Logger.getLogger(BasketRequests.class);

  public static class BasketActions {

    private static final List<String> ACTION_TYPES = Arrays.asList(ADD, REMOVE);

    private Map<String,List<PrimaryKeyValue>> _basketActionMap;

    public BasketActions(Map<String,List<PrimaryKeyValue>> basketActionMap) {
      _basketActionMap = basketActionMap;
      // clear out IDs that appear in both "delete" and "undelete"
      cleanData(_basketActionMap.get(ADD), _basketActionMap.get(REMOVE));
    }

    private void cleanData(List<PrimaryKeyValue> toAdd, List<PrimaryKeyValue> toRemove) {
      for (int j, i = 0; i < toRemove.size(); i++) {
        PrimaryKeyValue id = toRemove.get(i);
        if ((j = toAdd.indexOf(id)) != -1) {
          // found ID in both lists; remove from both
          toAdd.remove(j);
          toRemove.remove(i);
          i--; // recheck at the current index
        }
      }
    }

    public List<PrimaryKeyValue> getRecordsToAdd() {
      return _basketActionMap.get(ADD);
    }

    public List<PrimaryKeyValue> getRecordsToRemove() {
      return _basketActionMap.get(REMOVE);
    }
  }

  /**
   * Creates set of actions, each associated with a list of basket records
   * Input Format:
   * {
   *   create: [PrimaryKey, PrimaryKey, ...],
   *   delete: [PrimaryKey, PrimaryKey, ...]
   * }
   * Where PrimaryKey is [ { name: String, value: String } ].
   * 
   * @param json input object
   * @return parsed actions to perform on IDs
   * @throws JSONException 
   * @throws WdkModelException 
   * @throws DataValidationException 
   */
  public static BasketActions parseBasketActionsJson(JSONObject json, RecordClass expectedRecordClass)
      throws WdkModelException, JSONException, DataValidationException {
    List<Object> unrecognizedActions = new ArrayList<>();
    Map<String, List<PrimaryKeyValue>> basketActionMap = new HashMap<>();
    for (String actionType : JsonUtil.getKeys(json)) {
      if (BasketActions.ACTION_TYPES.contains(actionType)) {
        List<PrimaryKeyValue> basketPks = new ArrayList<>();
        JSONArray jsonArray = json.getJSONArray(actionType);
        for (int i = 0; i < jsonArray.length(); i++) {
          PrimaryKeyValue pk = RecordRequest.parsePrimaryKey(jsonArray.getJSONArray(i), expectedRecordClass);
          basketPks.add(pk);
        }
        basketActionMap.put(actionType, basketPks);
      }
      else {
        unrecognizedActions.add(actionType);
      }
    }
    if(!unrecognizedActions.isEmpty()) {
      String unrecognized = FormatUtil.join(unrecognizedActions.toArray(), ",");
      LOG.warn("Basket service will ignore the following unrecognized actions: " + unrecognized);
    }
    return new BasketActions(basketActionMap);
  }
}
