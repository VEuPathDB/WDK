package org.gusdb.wdk.service.request.strategy;

import static org.gusdb.fgputil.json.JsonUtil.getBooleanOrDefault;
import static org.gusdb.fgputil.json.JsonUtil.getStringOrDefault;

import java.util.Map;
import java.util.Map.Entry;

import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.request.answer.AnswerSpecFactory;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONException;
import org.json.JSONObject;

public class StepRequest {

  // these props are sent in a step response but are invalid in POST and PATCH
  private static final String[] INVALID_EDIT_PROPS = {
      JsonKeys.ID,
      JsonKeys.DISPLAY_NAME,
      JsonKeys.SHORT_DISPLAY_NAME,
      JsonKeys.BASE_CUSTOM_NAME,
      JsonKeys.DESCRIPTION,
      JsonKeys.OWNER_ID,
      JsonKeys.STRATEGY_ID,
      JsonKeys.ESTIMATED_SIZE,
      JsonKeys.HAS_COMPLETE_STEP_ANALYSES,
      JsonKeys.RECORD_CLASS_NAME
  };

  public static StepRequest newStepFromJson(JSONObject newStep, WdkModel model, User user)
      throws RequestMisformatException, DataValidationException {
    try {
      checkForInvalidProps(newStep);
      JSONObject answerSpecJson = newStep.getJSONObject(JsonKeys.ANSWER_SPEC);
      // Since this method is intended for new steps, the step can not yet be part of a strategy and so
      // any answer params it has should be null.  So allowIncompleteSpec param is true.
      AnswerSpec answerSpec = AnswerSpecFactory.createFromJson(answerSpecJson, model, user, true);
      String customName = getStringOrDefault(newStep, JsonKeys.CUSTOM_NAME, answerSpec.getQuestion().getName());
      boolean isCollapsible = getBooleanOrDefault(newStep, JsonKeys.IS_COLLAPSIBLE, false);
      String collapsedName = getStringOrDefault(newStep, JsonKeys.COLLAPSED_NAME, customName);
      // DB field length for collapsedName is 200
      collapsedName = collapsedName == null ? collapsedName : collapsedName.substring(0, Math.min(collapsedName.length(),200));
      return new StepRequest(answerSpec, customName, isCollapsible, collapsedName);
    }
    catch (JSONException e) {
      throw new RequestMisformatException("Invalid JSON in step request", e);
    }
  }

  public static StepRequest patchStepFromJson(Step step, JSONObject patchSet, WdkModel model, User user)
      throws RequestMisformatException, WdkModelException, DataValidationException {
    try {
      checkForInvalidProps(patchSet);
      AnswerSpec answerSpec = getPatchAnswerSpec(step, patchSet, model, user);
      String customName = getStringOrDefault(patchSet, JsonKeys.CUSTOM_NAME, step.getCustomName());
      boolean isCollapsible = Boolean.getBoolean(getStringOrDefault(patchSet, JsonKeys.IS_COLLAPSIBLE, String.valueOf(step.isCollapsible())));
      String collapsedName = getStringOrDefault(patchSet, JsonKeys.COLLAPSED_NAME, step.getCollapsedName());
      // DB field length for collapsedName is 200
      collapsedName = collapsedName == null ? collapsedName : collapsedName.substring(0, 200);
      return new StepRequest(answerSpec, customName, isCollapsible, collapsedName);
    }
    catch (JSONException e) {
      throw new RequestMisformatException("Invalid JSON in patch step request", e);
    }
  }
  
  private static AnswerSpec getPatchAnswerSpec(Step step, JSONObject patchSet, WdkModel model, User user)
      throws DataValidationException, RequestMisformatException, WdkModelException {
    if (patchSet.has(JsonKeys.ANSWER_SPEC)) {
      JSONObject answerSpecJson = patchSet.getJSONObject(JsonKeys.ANSWER_SPEC);
      boolean expectIncompleteSpec = step.getStrategyId() == null;
      AnswerSpec answerSpec = AnswerSpecFactory.createFromJson(answerSpecJson, model, user, expectIncompleteSpec);
      // user cannot change question of an existing step
      if (!answerSpec.getQuestion().getFullName().equals(step.getQuestion().getFullName())) {
        throw new DataValidationException("Question of an existing step cannot be changed.");
      }
      Map<String, Param> updateParamMap = answerSpec.getQuestion().getParamMap();
      Map<String, org.gusdb.wdk.model.answer.spec.ParamValue> updateParamValueMap = answerSpec.getParamValues();
      Map<String, String> currentParamValueMap = step.getParamValues();
      for (Entry<String,Param> entry : updateParamMap.entrySet()) {
        if (entry.getValue() instanceof AnswerParam) {
          Object updateParamValue = updateParamValueMap.get(entry.getKey()).getObjectValue();
          Object currentParamValue = currentParamValueMap.get(entry.getKey());
          if (updateParamValue == null && currentParamValue != null ||
              updateParamValue != null && !updateParamValue.equals(currentParamValue)) {
            throw new DataValidationException("Changes to the answer param values are not allowed.");
          }
        }
      }
      return answerSpec;
    }
    else {
      // patch set did not include new answer spec; use spec from existing step
      return AnswerSpecFactory.createFromStep(step);
    }
  }

  private static void checkForInvalidProps(JSONObject stepJson) throws RequestMisformatException {
    for (String badProp : INVALID_EDIT_PROPS) {
      if (stepJson.has(badProp)) {
        throw new RequestMisformatException("JSON property " + JsonKeys.ID + " is disallowed. Only the service can assign this value.");
      }
    }
  }

  private final AnswerSpec _answerSpec;
  private final String _customName;
  private final boolean _isCollapsible;
  private final String _collapsedName;

  public StepRequest(AnswerSpec answerSpec, String customName, boolean isCollapsible, String collapsedName) {
    _answerSpec = answerSpec;
    _customName = customName;
    _isCollapsible = isCollapsible;
    _collapsedName = collapsedName;
  }

  public AnswerSpec getAnswerSpec() {
    return _answerSpec;
  }

  public String getCustomName() {
    return _customName;
  }

  public boolean isCollapsible() {
    return _isCollapsible;
  }

  public String getCollapsedName() {
    return _collapsedName;
  }
}
