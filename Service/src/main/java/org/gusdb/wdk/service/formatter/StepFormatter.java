package org.gusdb.wdk.service.formatter;

import static org.gusdb.wdk.service.formatter.ValidationFormatter.getValidationBundleJson;

import java.util.Optional;
import java.util.Set;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.Named.NamedObject;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.service.request.answer.AnswerSpecServiceFormat;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Formats WDK Step objects.  Step JSON will have the following form:
 * <pre>
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
 *   recordClassName: String (RecordClass full name),
 *   searchName: String (Question's full name),
 *   searchConfig: {
 *     parameters: Object,
 *     filters: Array,
 *     viewFilters: Array,
 *     legacyFilter: String,
 *     wdkWeight: Number
 *   },
 *   validation: {
 *     level: enum[ValidationLevel],
 *     isValid: boolean,
 *     errors: {
 *       general: Array&lt;String&gt;,
 *       byKey: Object
 *     }
 *   },
 *   displayPrefs: {
 *     columnSelection: Array&lt;String&gt;,
 *     sortColumns: {
 *       [String]: enum[SortDirection]
 *     }
 *   }
 * }
 * </pre>
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
        .put(JsonKeys.STRATEGY_ID, JsonUtil.convertNulls(step.getStrategyId().orElse(null)))
        .put(JsonKeys.HAS_COMPLETE_STEP_ANALYSES, step.getHasCompleteAnalyses())
        .put(JsonKeys.RECORD_CLASS_NAME, step.getRecordClass().map(NamedObject::getFullName).orElse(null))
        .put(JsonKeys.SEARCH_NAME, step.getAnswerSpec().getQuestionName())
        .put(JsonKeys.SEARCH_CONFIG, AnswerSpecServiceFormat.format(step.getAnswerSpec()))
        .put(JsonKeys.VALIDATION, getValidationBundleJson(step.getValidationBundle()))
        .put(JsonKeys.CREATED_TIME, FormatUtil.formatDateTime(step.getCreatedTime()))
        .put(JsonKeys.LAST_RUN_TIME, FormatUtil.formatDateTime(step.getLastRunTime()))
        .put(JsonKeys.DISPLAY_PREFERENCES, JsonUtil.toJSONObject(step.getDisplayPrefs())
            .valueOrElseThrow(e -> new WdkModelException(e)));
    } catch (JSONException e) {
      throw new WdkModelException("Unable to convert Step to service JSON", e);
    }
  }

  public static JSONObject getStepJsonWithResultSize(Step step) throws WdkModelException {
    return getStepJson(step)
        .put(JsonKeys.ESTIMATED_SIZE, step.getResultSize());
  }

  public static JSONObject getStepJsonWithEstimatedSize(Step step) throws WdkModelException {
    Integer estSz = step.getEstimatedSize();
    return getStepJson(step)
        .put(JsonKeys.ESTIMATED_SIZE, estSz == -1? null : estSz);
  }

  /**
   * recursively build a step tree (IDs only) based on the provided step
   * @param step include this step and its children in the tree
   * @param accumulatedSteps a set to accumulate Steps included in the tree.  in initial call, should be empty
   * @return
   */
  public static JSONObject formatAsStepTree(Step step, Set<Step> accumulatedSteps) {
    accumulatedSteps.add(step);
    final JSONObject out = new JSONObject()
      .put(JsonKeys.ID, step.getStepId());

    Optional.ofNullable(step.getPrimaryInputStep())
      .ifPresent(s -> out.put(JsonKeys.PRIMARY_INPUT_STEP, formatAsStepTree(s, accumulatedSteps)));
    Optional.ofNullable(step.getSecondaryInputStep())
      .ifPresent(s -> out.put(JsonKeys.SECONDARY_INPUT_STEP, formatAsStepTree(s, accumulatedSteps)));

    return out;
  }
}
