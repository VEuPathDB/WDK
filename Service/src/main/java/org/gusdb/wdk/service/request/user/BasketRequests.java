package org.gusdb.wdk.service.request.user;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.PrimaryKeyDefinition;
import org.gusdb.wdk.model.record.PrimaryKeyValue;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.service.request.RecordRequest;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.json.JSONArray;
import org.json.JSONObject;

import static org.gusdb.wdk.core.api.JsonKeys.ADD;
import static org.gusdb.wdk.core.api.JsonKeys.ADD_FROM_STEP_ID;
import static org.gusdb.wdk.core.api.JsonKeys.REMOVE;

public class BasketRequests {

  public static class BasketActions extends PatchMap<PrimaryKeyValue> {

    private static final List<String> ACTION_TYPES = Arrays.asList(ADD, REMOVE, ADD_FROM_STEP_ID);

    /**
     * Creates set of actions, each associated with a list of basket records
     * Input Format:
     * {
     *   add: [PrimaryKey, PrimaryKey, ...],
     *   remove: [PrimaryKey, PrimaryKey, ...],
     *   createFromStepId: Number
     * }
     * Where PrimaryKey is [ { name: String, value: String } ].
     *
     * @param json input object
     * @return parsed actions to perform on IDs
     * @throws WdkModelException
     * @throws DataValidationException
     */
    public BasketActions(JSONObject json, RecordClass recordClass, StepFactory stepFactory)
        throws DataValidationException, WdkModelException, WdkUserException {
      super(ACTION_TYPES);
      List<PrimaryKeyValue> recordsToAdd = parseIdJsonArray(json, ADD, recordClass);
      List<PrimaryKeyValue> recordsToRemove = parseIdJsonArray(json, REMOVE, recordClass);
      List<PrimaryKeyValue> stepIdsToAdd = parseStepIds(json, ADD_FROM_STEP_ID, stepFactory);
      recordsToAdd.addAll(stepIdsToAdd);
      put(ADD, recordsToAdd);
      put(REMOVE, recordsToRemove);
      removeSharedIds(ADD, REMOVE);
    }

    public BasketActions(List<PrimaryKeyValue> recordsToAdd, List<PrimaryKeyValue> recordsToRemove) {
      super(ACTION_TYPES);
      put(ADD, recordsToAdd);
      put(REMOVE, recordsToRemove);
      removeSharedIds(ADD, REMOVE);
    }

    public List<PrimaryKeyValue> getRecordsToAdd() {
      return get(ADD);
    }

    public List<PrimaryKeyValue> getRecordsToRemove() {
      return get(REMOVE);
    }

    private List<PrimaryKeyValue> parseIdJsonArray(JSONObject json, String key, RecordClass recordClass) throws DataValidationException, WdkModelException {
      if (!json.has(key)) return new LinkedList<>();
      JSONArray idJsonArray = json.getJSONArray(key);
      List<PrimaryKeyValue> pkValues = new LinkedList<>();
      for (int i = 0; i < idJsonArray.length(); i++) {
        pkValues.add(RecordRequest.parsePrimaryKey(idJsonArray.getJSONArray(i), recordClass));
      }
      return pkValues;
    }

    private List<PrimaryKeyValue> parseStepIds(JSONObject json, String key, StepFactory stepFactory) throws WdkModelException, WdkUserException {
      if (!json.has(ADD_FROM_STEP_ID)) return new LinkedList<>();
      Long stepId = json.getLong(ADD_FROM_STEP_ID);
      Step step = stepFactory.getStepById(stepId).orElseThrow(() -> new WdkUserException("Could not load step with step id " + stepId));
      PrimaryKeyDefinition pkDef = step.getRecordClass().getPrimaryKeyDefinition();
      return step.getAnswerValue().getAllIds()
          .stream()
          .map(ids -> {
            Map<String, Object> pkValues = new HashMap<>();
            for (int i = 0; i < pkDef.getColumnRefs().length; i++) {
              pkValues.put(pkDef.getColumnRefs()[i], ids[i]);
            }
            try {
              return new PrimaryKeyValue(pkDef, pkValues);
            } catch (Exception e) {
              throw (e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e));
            }
          })
          .collect(Collectors.toList());
    }
  }

}
