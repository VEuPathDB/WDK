package org.gusdb.wdk.service.formatter;

import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.service.request.answer.AnswerSpecServiceFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

/**
 * Formats WDK Step objects.  Step JSON will have the following form:
 * {
 *   id: Number,
 *   displayName: String,
 *   shortDisplayName: String,
 *   customName: String,
 *   baseCustomName: String,
 *   isCollapsible: Boolean,
 *   collapsedName: String,
 *   description: String,
 *   ownerId: Number (user ID of owner),
 *   strategyId: Number (ID of containing strategy),
 *   estimatedSize: Number (last known number of results),
 *   hasCompleteStepAnalyses: Boolean,
 *   recordClass: String (RecordClass full name),
 *   answerSpecComplete: boolean (false for unattached steps)
 *   answerSpec: see AnswerRequest (input and output formats are the same)
 * }
 *
 * @author rdoherty
 */
public class StepFormatter {

  private static JSONObject getStepJson(Step step) throws WdkModelException {
    try {
      return new JSONObject()
        .put(JsonKeys.ID, step.getStepId())
        .put(JsonKeys.DISPLAY_NAME, step.getDisplayName())
        .put(JsonKeys.SHORT_DISPLAY_NAME, step.getShortDisplayName())
        .put(JsonKeys.CUSTOM_NAME, step.getCustomName())
        .put(JsonKeys.BASE_CUSTOM_NAME, step.getBaseCustomName())
        .put(JsonKeys.IS_COLLAPSIBLE, step.isCollapsible())
        .put(JsonKeys.COLLAPSED_NAME, step.getCollapsedName())
        .put(JsonKeys.DESCRIPTION, step.getDescription())
        .put(JsonKeys.OWNER_ID, step.getUser().getUserId())
        .put(JsonKeys.STRATEGY_ID, JsonUtil.convertNulls(step.getStrategyId()))
        .put(JsonKeys.HAS_COMPLETE_STEP_ANALYSES, step.getHasCompleteAnalyses())
        .put(JsonKeys.RECORD_CLASS_NAME, step.getType())
        .put(JsonKeys.IS_ANSWER_SPEC_COMPLETE, step.isAnswerSpecComplete())

        // FIXME: call AnswerSpecFactory.createFromStep() and pass to formatter;
        //    to do so, must extract JSON formatting from Step and other classes
        .put(JsonKeys.SEARCH_CONFIG, AnswerSpecServiceFormat.format(step.getAnswerSpec()))
        .put(JsonKeys.IS_VALID, step.isValid())
        .put(JsonKeys.INVALID_REASONS, new JSONArray(step.getValidationBundle().getAllErrors()))
        .put(JsonKeys.CREATED_TIME, step.getCreatedTime())
        .put(JsonKeys.LAST_RUN_TIME, step.getLastRunTime());
    }
    catch (JSONException e) {
      throw new WdkModelException("Unable to convert Step to service JSON", e);
    }
  }

  public static JSONObject getStepJsonWithResultSize(Step step) throws WdkModelException {
    return getStepJson(step)
        .put(JsonKeys.ESTIMATED_SIZE, step.getResultSize());
  }

  public static JSONObject getStepJsonWithEstimatedSize(Step step) throws WdkModelException {
    return getStepJson(step)
        .put(JsonKeys.ESTIMATED_SIZE, step.getEstimatedSize());
  }

  public static JSONObject formatAsStepTree(Step step) {
    final JSONObject out = new JSONObject()
      .put(JsonKeys.ID, step.getStepId());

    Optional.ofNullable(step.getSecondaryInputStep())
      .ifPresent(s -> out.put(JsonKeys.SECONDARY_INPUT_STEP, formatAsStepTree(s)));
    Optional.ofNullable(step.getPrimaryInputStep())
        .ifPresent(s -> out.put(JsonKeys.PRIMARY_INPUT_STEP, formatAsStepTree(s)));

    return out;
  }
}
