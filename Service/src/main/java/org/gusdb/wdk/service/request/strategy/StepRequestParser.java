package org.gusdb.wdk.service.request.strategy;

import static org.gusdb.fgputil.json.JsonUtil.getBooleanOrDefault;
import static org.gusdb.fgputil.json.JsonUtil.getJsonObjectOrDefault;
import static org.gusdb.fgputil.json.JsonUtil.getStringOrDefault;

import org.gusdb.fgputil.validation.ValidObjectFactory.SemanticallyValid;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.Step.StepBuilder;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserCache;
import org.gusdb.wdk.service.request.answer.AnswerSpecServiceFormat;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONException;
import org.json.JSONObject;

public class StepRequestParser {

  public static class NewStepRequest {

    private final SemanticallyValid<AnswerSpec> _answerSpec;
    private final String _customName;
    private final boolean _isExpanded;
    private final String _expandedName;
    private final JSONObject _displayPrefs;

    public NewStepRequest(SemanticallyValid<AnswerSpec> answerSpec,
        String customName, boolean isExpanded, String expandedName,
        JSONObject displayPrefs) {
      _answerSpec = answerSpec;
      _customName = customName;
      _isExpanded = isExpanded;
      _expandedName = expandedName;
      _displayPrefs = displayPrefs;
    }

    public SemanticallyValid<AnswerSpec> getAnswerSpec() {
      return _answerSpec;
    }

    public String getCustomName() {
      return _customName;
    }

    public boolean isExpanded() {
      return _isExpanded;
    }

    public String getExpandedName() {
      return _expandedName;
    }

    public JSONObject getDisplayPrefs() {
      return _displayPrefs;
    }
  }

  public static NewStepRequest newStepFromJson(JSONObject stepJson, WdkModel wdkModel, User user)
      throws RequestMisformatException, DataValidationException, WdkModelException {
    try {
      String questionName = stepJson.getString(JsonKeys.SEARCH_NAME);
      Question question = wdkModel.getQuestionByName(questionName)
          .orElseThrow(() -> new DataValidationException(
              questionName + " is not a valid search name."));
      SemanticallyValid<AnswerSpec> validSpec = parseAnswerSpec(question,
          stepJson.getJSONObject(JsonKeys.SEARCH_CONFIG), wdkModel,
          user, StepContainer.emptyContainer());
      AnswerSpec spec = validSpec.get();

      // Since this method is intended for new steps, the step can not yet be
      // part of a strategy and so any answer params it has should be null (empty string).
      for (AnswerParam param : spec.getQuestion().getQuery().getAnswerParams()) {
        if (!AnswerParam.NULL_VALUE.equals(spec.getQueryInstanceSpec().get(param.getName()))) {
          throw new DataValidationException("Answer Params in new steps must have the null value (empty string).");
        }
      }

      String customName = getStringOrDefault(stepJson, JsonKeys.CUSTOM_NAME, spec.getQuestion().getName());
      boolean isExpanded = getBooleanOrDefault(stepJson, JsonKeys.IS_EXPANDED, false);
      String expandedName = getStringOrDefault(stepJson, JsonKeys.EXPANDED_NAME, null);
      JSONObject displayPrefs = getJsonObjectOrDefault(stepJson, JsonKeys.DISPLAY_PREFS, new JSONObject());

      return new NewStepRequest(validSpec, customName, isExpanded, expandedName, displayPrefs);
    }
    catch (JSONException e) {
      throw new RequestMisformatException("Invalid JSON in step request", e);
    }
  }

  private static SemanticallyValid<AnswerSpec> parseAnswerSpec(Question question, JSONObject answerSpecJson, WdkModel wdkModel, User user, StepContainer container)
      throws JSONException, RequestMisformatException, DataValidationException, WdkModelException {
    return AnswerSpecServiceFormat
        .parse(question, answerSpecJson, wdkModel)
        .build(user, container, ValidationLevel.SEMANTIC)
        .getSemanticallyValid()
        .getOrThrow(spec ->
          // incoming answer spec not semantically valid
          new DataValidationException(spec.getValidationBundle()));
  }

  public static Step updateStepMeta(Step step, JSONObject patchSet)
      throws WdkModelException, RequestMisformatException {
    try {
      StepBuilder newStep = Step.builder(step);

      if (patchSet.has(JsonKeys.CUSTOM_NAME))
        newStep.setCustomName(patchSet.getString(JsonKeys.CUSTOM_NAME));
      if (patchSet.has(JsonKeys.IS_EXPANDED))
        newStep.setExpanded(patchSet.getBoolean(JsonKeys.IS_EXPANDED));
      if (patchSet.has(JsonKeys.EXPANDED_NAME))
        newStep.setExpandedName(patchSet.getString(JsonKeys.EXPANDED_NAME));
      if (patchSet.has(JsonKeys.DISPLAY_PREFERENCES))
        newStep.setDisplayPrefs(patchSet.getJSONObject(JsonKeys.DISPLAY_PREFERENCES));

      return newStep.build(new UserCache(step.getUser()),
          step.getValidationBundle().getLevel(), step.getStrategy());
    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.getMessage());
    }
  }

  public static SemanticallyValid<AnswerSpec> getReplacementAnswerSpec(
      Step existingStep, JSONObject answerSpecJson, WdkModel wdkModel, User user)
      throws DataValidationException, RequestMisformatException, JSONException, WdkModelException {

    SemanticallyValid<AnswerSpec> validSpec = parseAnswerSpec(
        existingStep.getAnswerSpec().getQuestion(),
        answerSpecJson, wdkModel, user, existingStep.getContainer());
    AnswerSpec answerSpec = validSpec.get();

    // user cannot change question of an existing step (since # and type of answer params may change);
    //   we could check for validity of # and type of answer params in the future; no use case now
    // NOTE: since answer spec JSON no longer contains question, this is a non-issue, but keeping
    //       code in case anyone thinks it's a good idea to allow a question change in the future.
    if (!answerSpec.getQuestion().getFullName().equals(
        existingStep.getAnswerSpec().getQuestion().getFullName())) {
      throw new DataValidationException("Question of an existing step cannot be changed.");
    }

    // make sure user has not tried to modify answer params
    assertAnswerParamsUnmodified(existingStep, answerSpec);

    return validSpec;
  }

  public static void assertAnswerParamsUnmodified(Step existingStep, AnswerSpec answerSpec) throws DataValidationException {
    // ensure answer param values are not modified; the strategy service handles
    //   modification of the strategy tree (i.e. answer param values)
    QueryInstanceSpec updateParams = answerSpec.getQueryInstanceSpec();
    QueryInstanceSpec currentParams = existingStep.getAnswerSpec().getQueryInstanceSpec();
    DataValidationException illegalAnswerParamException =
        new DataValidationException("Changes to answer param values are not allowed.");
    for (Param param : answerSpec.getQuestion().getQuery().getAnswerParams()) {
      // incoming answer params must match existing values
      if (!updateParams.get(param.getName()).equals(currentParams.get(param.getName()))) {
        throw illegalAnswerParamException;
      }
    }
  }
}
