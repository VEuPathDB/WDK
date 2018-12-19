package org.gusdb.wdk.service.request.strategy;

import static org.gusdb.fgputil.FormatUtil.NL;
import static org.gusdb.fgputil.FormatUtil.join;
import static org.gusdb.fgputil.json.JsonUtil.getBooleanOrDefault;
import static org.gusdb.fgputil.json.JsonUtil.getStringOrDefault;

import java.util.Optional;

import org.gusdb.fgputil.validation.ValidObjectFactory.SemanticallyValid;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.request.answer.AnswerSpecServiceFormat;
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

  public static StepRequest newStepFromJson(JSONObject stepJson, WdkModel wdkModel, User user)
      throws RequestMisformatException, DataValidationException, WdkModelException {
    try {
      checkForInvalidProps(stepJson);
      SemanticallyValid<AnswerSpec> validSpec = parseAnswerSpec(
          stepJson, wdkModel, user, StepContainer.emptyContainer());
      AnswerSpec spec = validSpec.getObject();

      // Since this method is intended for new steps, the step can not yet be
      // part of a strategy and so any answer params it has should be null (empty string).
      for (AnswerParam param : spec.getQuestion().getQuery().getAnswerParams()) {
        if (!spec.getQueryInstanceSpec().get(param.getName()).equals(AnswerParam.NULL_VALUE)) {
          throw new DataValidationException("Answer Params in new steps must have the null value (empty string).");
        }
      }

      String customName = getStringOrDefault(stepJson, JsonKeys.CUSTOM_NAME, spec.getQuestion().getName());
      boolean isCollapsible = getBooleanOrDefault(stepJson, JsonKeys.IS_COLLAPSIBLE, false);
      String collapsedName = getStringOrDefault(stepJson, JsonKeys.COLLAPSED_NAME, customName);

      return new StepRequest(Optional.of(validSpec), customName, isCollapsible, collapsedName);
    }
    catch (JSONException e) {
      throw new RequestMisformatException("Invalid JSON in step request", e);
    }
  }

  private static SemanticallyValid<AnswerSpec> parseAnswerSpec(JSONObject stepJson, WdkModel wdkModel, User user, StepContainer container)
      throws JSONException, RequestMisformatException, DataValidationException, WdkModelException {
    return AnswerSpecServiceFormat
        .parse(stepJson.getJSONObject(JsonKeys.ANSWER_SPEC), wdkModel)
        .build(user, container, ValidationLevel.SEMANTIC)
        .getSemanticallyValid()
        .getOrThrow(spec ->
          // incoming answer spec not semantically valid
          new DataValidationException("Invalid answer spec: " + join(spec.getValidationBundle().getAllErrors(), NL)));
  }

  public static StepRequest patchStepFromJson(Step step, JSONObject patchSet, WdkModel wdkModel, User user)
      throws RequestMisformatException, DataValidationException, WdkModelException {
    try {
      checkForInvalidProps(patchSet);
      Optional<SemanticallyValid<AnswerSpec>> answerSpec = getPatchAnswerSpec(step, patchSet, wdkModel, user);
      String customName = getStringOrDefault(patchSet, JsonKeys.CUSTOM_NAME, step.getCustomName());
      boolean isCollapsible = getBooleanOrDefault(patchSet, JsonKeys.IS_COLLAPSIBLE, step.isCollapsible());
      String collapsedName = getStringOrDefault(patchSet, JsonKeys.COLLAPSED_NAME, step.getCollapsedName());
      return new StepRequest(answerSpec, customName, isCollapsible, collapsedName);
    }
    catch (JSONException e) {
      throw new RequestMisformatException("Invalid JSON in patch step request", e);
    }
  }
  
  private static Optional<SemanticallyValid<AnswerSpec>> getPatchAnswerSpec(Step step, JSONObject patchSet, WdkModel wdkModel, User user)
      throws DataValidationException, RequestMisformatException, JSONException, WdkModelException {
    if (patchSet.has(JsonKeys.ANSWER_SPEC)) {
      SemanticallyValid<AnswerSpec> validSpec = parseAnswerSpec(patchSet, wdkModel, user, step.getContainer());
      AnswerSpec answerSpec = validSpec.getObject();

      // user cannot change question of an existing step (since # of answer params may change)
      if (!answerSpec.getQuestion().getFullName().equals(
          step.getAnswerSpec().getQuestion().getFullName())) {
        throw new DataValidationException("Question of an existing step cannot be changed.");
      }
      QueryInstanceSpec updateParams = answerSpec.getQueryInstanceSpec();
      QueryInstanceSpec currentParams = step.getAnswerSpec().getQueryInstanceSpec();
      DataValidationException illegalAnswerParamException =
          new DataValidationException("Changes to the answer param values are not allowed.");
      for (Param param : answerSpec.getQuestion().getQuery().getAnswerParams()) {
        String updateParamValue = updateParams.get(param.getName());
        if (step.getStrategy() == null) {
          // incoming answer params must be "null" = empty strings
          if (!updateParamValue.equals(AnswerParam.NULL_VALUE)) {
            throw illegalAnswerParamException;
          }
        }
        else {
          // incoming answer params must match existing values
          if (!updateParamValue.equals(currentParams.get(param.getName()))) {
            throw illegalAnswerParamException;
          }
        }
      }
      return Optional.of(validSpec);
    }
    else {
      // patch set did not include new answer spec; use spec from existing step
      return Optional.empty();
    }
  }

  private static void checkForInvalidProps(JSONObject stepJson) throws RequestMisformatException {
    for (String badProp : INVALID_EDIT_PROPS) {
      if (stepJson.has(badProp)) {
        throw new RequestMisformatException("JSON property " + badProp + " is disallowed. Only the service can assign this value.");
      }
    }
  }

  private final Optional<SemanticallyValid<AnswerSpec>> _answerSpec;
  private final String _customName;
  private final boolean _isCollapsible;
  private final String _collapsedName;

  public StepRequest(Optional<SemanticallyValid<AnswerSpec>> answerSpec, String customName, boolean isCollapsible, String collapsedName) {
    _answerSpec = answerSpec;
    _customName = customName;
    _isCollapsible = isCollapsible;
    _collapsedName = collapsedName;
  }

  public Optional<SemanticallyValid<AnswerSpec>> getAnswerSpec() {
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
