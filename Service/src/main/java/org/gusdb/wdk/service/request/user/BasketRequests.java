package org.gusdb.wdk.service.request.user;

import static org.gusdb.wdk.service.formatter.Keys.ADD;
import static org.gusdb.wdk.service.formatter.Keys.REMOVE;

import java.util.Arrays;
import java.util.List;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.PrimaryKeyValue;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.service.request.RecordRequest;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.json.JSONObject;

public class BasketRequests {

  @SuppressWarnings("serial")
  public static class BasketActions extends PatchMap<PrimaryKeyValue> {

    private static final List<String> ACTION_TYPES = Arrays.asList(ADD, REMOVE);

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
     * @throws WdkModelException 
     * @throws DataValidationException 
     */
    public BasketActions(JSONObject json, RecordClass recordClass)
        throws DataValidationException, WdkModelException {
      super(json, ACTION_TYPES, idJson -> RecordRequest.parsePrimaryKey(idJson.getJSONArray(), recordClass));
      removeSharedIds(ADD, REMOVE);
    }

    public BasketActions(List<PrimaryKeyValue> recordsToAdd, List<PrimaryKeyValue> recordsToRemove) {
      super(ACTION_TYPES);
      put(ADD, recordsToAdd);
      put(REMOVE, recordsToRemove);
      removeSharedIds(ADD, REMOVE);
    }

    public List<PrimaryKeyValue> getRecordsToAdd() { return get(ADD); }
    public List<PrimaryKeyValue> getRecordsToRemove() { return get(REMOVE); }

  }

}
