package org.gusdb.wdk.service.request.user;

import static org.gusdb.wdk.service.formatter.Keys.DELETE;
import static org.gusdb.wdk.service.formatter.Keys.UNDELETE;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BasketRequests {

  private static final Logger LOG = Logger.getLogger(BasketRequests.class);

  public static class BasketActions {

    private static final List<String> ACTION_TYPES = Arrays.asList(DELETE, UNDELETE);

    private Map<String,List<PrimaryKeyValue>> _basketActionMap;

    public BasketActions(Map<String,List<PrimaryKeyValue>> basketActionMap) {
      _basketActionMap = basketActionMap;
      // clear out IDs that appear in both "delete" and "undelete"
      cleanData(_basketActionMap.get(DELETE), _basketActionMap.get(UNDELETE));
    }

    private void cleanData(List<PrimaryKeyValue> toDelete, List<PrimaryKeyValue> toUndelete) {
      for (int j, i = 0; i < toDelete.size(); i++) {
        PrimaryKeyValue id = toDelete.get(i);
        if ((j = toUndelete.indexOf(id)) != -1) {
          // found ID in both lists; remove from both
          toUndelete.remove(j);
          toDelete.remove(i);
          i--; // recheck at the current index
        }
      }
    }

    public List<PrimaryKeyValue> getRecordsToDelete() {
      return _basketActionMap.get(DELETE);
    }

    public List<PrimaryKeyValue> getRecordsToUndelete() {
      return _basketActionMap.get(UNDELETE);
    }
  }

  /**
   * Creates set of actions, each associated with a list of basket records
   * Input Format:
   * {
   *   delete: [Long, Long, ...],
   *   undelete: [Long, Long, ...]
   * }
   * 
   * @param json input object
   * @return parsed actions to perform on IDs
   * @throws JSONException 
   * @throws WdkModelException 
   */
  public static BasketActions parseBasketActionsJson(JSONObject json, RecordClass expectedRecordClass)
      throws WdkModelException, JSONException {
    List<Object> unrecognizedActions = new ArrayList<>();
    Map<String, List<PrimaryKeyValue>> basketActionMap = new HashMap<>();
    for (String actionType : JsonUtil.getKeys(json)) {
      if (BasketActions.ACTION_TYPES.contains(actionType)) {
        List<PrimaryKeyValue> basketPks = new ArrayList<>();
        JSONArray jsonArray = json.getJSONArray(actionType);
        for (int i = 0; i < jsonArray.length(); i++) {
          PrimaryKeyValue pk = RecordRequest.parsePrimaryKeyNew(jsonArray.getJSONObject(i), expectedRecordClass);
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
