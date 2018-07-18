package org.gusdb.wdk.service.formatter;

import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.service.request.answer.AnswerSpecServiceFormat;
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

  public static JSONObject getStepJson(Step step, boolean loadEstimateSize) throws WdkModelException {
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
        .put(Keys.ESTIMATED_SIZE, loadEstimateSize ? step.getCalculatedEstimateSize() : step.getRawEstimateSize())
        .put(Keys.HAS_COMPLETE_STEP_ANALYSES, step.getHasCompleteAnalyses())
        .put(Keys.RECORD_CLASS_NAME, step.getType())
        .put(Keys.IS_ANSWER_SPEC_COMPLETE, step.isAnswerSpecComplete())
        // FIXME: call AnswerSpecFactory.createFromStep() and pass to formatter;
        //    to do so, must extract JSON formatting from Step and other classes
        .put(Keys.ANSWER_SPEC, AnswerSpecServiceFormat.format(step.getAnswerSpec()))
        .put(Keys.IS_VALID, step.isValid())
        .put(Keys.INVALID_REASON, step.getInvalidReason().name())
        .put(Keys.CREATED_TIME, step.getCreatedTime())
        .put(Keys.LAST_RUN_TIME, step.getLastRunTime());
    }
    catch (JSONException e) {
      throw new WdkModelException("Unable to convert Step to service JSON", e);
    }
  }
}
