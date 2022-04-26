package org.gusdb.wdk.service.request.user;

import static java.util.Arrays.stream;

import java.util.Collection;
import java.util.Optional;

import javax.ws.rs.NotFoundException;

import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.record.PrimaryKeyValue;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.request.RecordRequest;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.service.AbstractWdkService;
import org.gusdb.wdk.service.service.user.UserService;
import org.json.JSONArray;
import org.json.JSONObject;

public class BasketRequests {

  public enum ActionType {
    ADD("add"),
    ADD_FROM_STEP_ID("addFromStepId"),
    REMOVE("remove"),
    REMOVE_ALL("removeAll");

    public final String value;

    ActionType(String s) {
      value = s;
    }

    public static Optional<ActionType> fromString(String s) {
      return stream(values()).filter(v -> v.value.equals(s)).findFirst();
    }

    @Override
    public String toString() {
      return value;
    }
  }

  public static class BasketActions extends PatchMap<ActionType, PrimaryKeyValue> {

    // will be populated only if action type is addFromStepId
    private Long _stepId;
    private RunnableObj<AnswerSpec> _runnableAnswerSpec;

    /**
     * Creates set of actions, each associated with a list of basket records
     * <p>
     * Input Format:
     * <pre>
     * {
     *   action: "add"|"remove",
     *   primaryKeys?: PrimaryKey[]
     * }
     * OR
     * {
     *   action: "addFromStepId",
     *   stepId: string
     * }
     * OR
     * {
     *   action: "removeAll"
     * }
     * </pre>
     * Where PrimaryKey is <code>[ { name: String, value: String } ]</code>.
     *
     * @param json input object
     * @throws WdkModelException
     * @throws DataValidationException
     */
    public BasketActions(JSONObject json, RecordClass recordClass)
        throws DataValidationException, WdkModelException {
      super(addPrimaryKeysIfAbsent(json), ActionType.values(),
          val -> ActionType.fromString(val.getString()).orElseThrow(DataValidationException::new),
          val -> RecordRequest.parsePrimaryKey(val.getJSONArray(), recordClass));
      if (getAction().equals(ActionType.ADD_FROM_STEP_ID)) {
        _stepId = json.getLong(JsonKeys.STEP_ID);
      }
    }

    private static JSONObject addPrimaryKeysIfAbsent(JSONObject json) {
      return json.has(JsonKeys.PRIMARY_KEYS) ? json :
        json.put(JsonKeys.PRIMARY_KEYS, new JSONArray());
    }

    public BasketActions(ActionType action, Collection<PrimaryKeyValue> records)
        throws DataValidationException {
      super(ActionType.values(), action, records);
    }

    public Long getRequestedStepId() {
      return _stepId;
    }

    public BasketActions setRunnableAnswerSpec(RunnableObj<AnswerSpec> runnableAnswerSpec) {
      _runnableAnswerSpec = runnableAnswerSpec;
      return this;
    }

    public RunnableObj<AnswerSpec> getRunnableAnswerSpec(User user, WdkModel wdkModel) throws NotFoundException, WdkModelException, DataValidationException {
      return _runnableAnswerSpec != null ? _runnableAnswerSpec :
        Step.getRunnableAnswerSpec(wdkModel
          .getStepFactory()
          .getStepByIdAndUserId(_stepId, user.getUserId(), ValidationLevel.RUNNABLE)
          .orElseThrow(() -> new NotFoundException(
            AbstractWdkService.formatNotFound(UserService.STEP_RESOURCE + _stepId)))
          .getRunnable()
          .getOrThrow(step -> new DataValidationException(
            "Step " + _stepId + " is not runnable so its results cannot be added to the basket.")));
    }
  }
}
