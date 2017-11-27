package org.gusdb.wdk.service.formatter;

import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.user.Step;
import org.json.JSONException;
import org.json.JSONObject;

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

  public static JSONObject getStepJson(Step step) throws WdkModelException {
    try {
      return new JSONObject()
        .put(Keys.ID, step.getStepId())
        .put(Keys.DISPLAY_NAME, step.getDisplayName())
        .put(Keys.SHORT_DISPLAY_NAME, step.getShortDisplayName())
        .put(Keys.CUSTOM_NAME, step.getCustomName())
        .put(Keys.BASE_CUSTOM_NAME, step.getBaseCustomName())
        .put(Keys.IS_COLLAPSIBLE, step.isCollapsible())
        .put(Keys.COLLAPSED_NAME, step.getCollapsedName())
        .put(Keys.DESCRIPTION, step.getDescription())
        .put(Keys.OWNER_ID, step.getUser().getUserId())
        .put(Keys.STRATEGY_ID, JsonUtil.convertNulls(step.getStrategyId()))
        .put(Keys.ESTIMATED_SIZE, step.getEstimateSize())
        .put(Keys.HAS_COMPLETE_STEP_ANALYSES, step.getHasCompleteAnalyses())
        .put(Keys.RECORD_CLASS_NAME, step.getType())
        .put(Keys.IS_ANSWER_SPEC_COMPLETE, step.isAnswerSpecComplete())
        // FIXME: call AnswerSpecFactory.createFromStep() and pass to formatter;
        //    to do so, must extract JSON formatting from Step and other classes
        .put(Keys.ANSWER_SPEC, createAnswerSpec(step))
        .put(Keys.IS_VALID, step.isValid())
        .put(Keys.CREATED_TIME, step.getCreatedTime())
        .put(Keys.LAST_RUN_TIME, step.getLastRunTime());
    }
    catch (JSONException e) {
      throw new WdkModelException("Unable to convert Step to service JSON", e);
    }
  }

  // FIXME: this method should convert AnswerSpec -> JSONObject
  private static JSONObject createAnswerSpec(Step step) {
    JSONObject json = new JSONObject()
      .put(Keys.QUESTION_NAME, step.getQuestionName())
      .put(Keys.PARAMETERS, step.getParamsJSON())
      .put(Keys.FILTERS, JsonUtil.getOrEmptyArray(step.getFilterOptionsJSON()))
      .put(Keys.VIEW_FILTERS, JsonUtil.getOrEmptyArray(step.getViewFilterOptionsJSON()))
      .put(Keys.WDK_WEIGHT, step.getAssignedWeight());
    AnswerFilterInstance legacyFilter = step.getFilter();
    if (legacyFilter != null) {
      json.put(Keys.LEGACY_FILTER_NAME, legacyFilter.getName());
    }
    return json;
  }
}
