package org.gusdb.wdk.service.request.strategy;

import static org.gusdb.fgputil.json.JsonUtil.getBooleanOrDefault;
import static org.gusdb.fgputil.json.JsonUtil.getStringOrDefault;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.formatter.Keys;
import org.gusdb.wdk.service.request.answer.AnswerSpec;
import org.gusdb.wdk.service.request.answer.AnswerSpecFactory;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONException;
import org.json.JSONObject;

public class StepRequest {

  // these props are sent in a step response but are invalid in POST and PATCH
  private static final String[] INVALID_EDIT_PROPS = {
      Keys.ID,
      Keys.DISPLAY_NAME,
      Keys.SHORT_DISPLAY_NAME,
      Keys.BASE_CUSTOM_NAME,
      Keys.DESCRIPTION,
      Keys.OWNER_ID,
      Keys.STRATEGY_ID,
      Keys.ESTIMATED_SIZE,
      Keys.HAS_COMPLETE_STEP_ANALYSES,
      Keys.RECORD_CLASS_NAME
  };

  public static StepRequest newStepFromJson(JSONObject newStep, WdkModelBean modelBean, User user)
      throws RequestMisformatException, DataValidationException {
    try {
      checkForInvalidProps(newStep);
      JSONObject answerSpecJson = newStep.getJSONObject(Keys.ANSWER_SPEC);
      AnswerSpec answerSpec = AnswerSpecFactory.createFromJson(answerSpecJson, modelBean, user);
      String customName = getStringOrDefault(newStep, Keys.CUSTOM_NAME, answerSpec.getQuestion().getName());
      boolean isCollapsible = getBooleanOrDefault(newStep, Keys.IS_COLLAPSIBLE, false);
      String collapsedName = getStringOrDefault(newStep, Keys.COLLAPSED_NAME, customName);
      return new StepRequest(answerSpec, customName, isCollapsible, collapsedName);
    }
    catch (JSONException e) {
      throw new RequestMisformatException("Invalid JSON in step request", e);
    }
  }

  public static StepRequest patchStepFromJson(Step step, JSONObject patchSet, WdkModelBean modelBean, User user)
      throws RequestMisformatException, WdkModelException, DataValidationException {
    try {
      checkForInvalidProps(patchSet);
      AnswerSpec answerSpec = getPatchAnswerSpec(step, patchSet, modelBean, user);
      String customName = getStringOrDefault(patchSet, Keys.CUSTOM_NAME, step.getCustomName());
      boolean isCollapsible = Boolean.getBoolean(getStringOrDefault(patchSet, Keys.IS_COLLAPSIBLE, String.valueOf(step.isCollapsible())));
      String collapsedName = getStringOrDefault(patchSet, Keys.COLLAPSED_NAME, step.getCollapsedName());
      return new StepRequest(answerSpec, customName, isCollapsible, collapsedName);
    }
    catch (JSONException e) {
      throw new RequestMisformatException("Invalid JSON in patch step request", e);
    }
  }
  
  private static AnswerSpec getPatchAnswerSpec(Step step, JSONObject patchSet, WdkModelBean modelBean, User user)
      throws DataValidationException, RequestMisformatException, WdkModelException {
    if (patchSet.has(Keys.ANSWER_SPEC)) {
      JSONObject answerSpecJson = patchSet.getJSONObject(Keys.ANSWER_SPEC);
      AnswerSpec answerSpec = AnswerSpecFactory.createFromJson(answerSpecJson, modelBean, user);
      // valid answer spec but user cannot change question of an existing step
      if (!answerSpec.getQuestion().getFullName().equals(step.getQuestion().getFullName())) {
        throw new DataValidationException("Question of an existing step cannot be changed.");
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
        throw new RequestMisformatException("JSON property " + Keys.ID + " is disallowed. Only the service can assign this value.");
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
