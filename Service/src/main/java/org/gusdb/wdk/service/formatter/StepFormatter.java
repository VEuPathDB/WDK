package org.gusdb.wdk.service.formatter;

import java.util.Optional;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.fgputil.validation.ValidationBundle;
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
 *   recordClass: String (RecordClass full name),
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
        .put(JsonKeys.RECORD_CLASS_NAME, step.getType())
        .put(JsonKeys.SEARCH_NAME, step.getAnswerSpec().getQuestionName())
        .put(JsonKeys.SEARCH_CONFIG, AnswerSpecServiceFormat.format(step.getAnswerSpec()))
        .put(JsonKeys.VALIDATION, getValidationBundleJson(step.getValidationBundle()))
        .put(JsonKeys.CREATED_TIME, FormatUtil.formatDateTime(step.getCreatedTime()))
        .put(JsonKeys.LAST_RUN_TIME, FormatUtil.formatDateTime(step.getLastRunTime()))
        .put(JsonKeys.DISPLAY_PREFS, JsonUtil.toJSONObject(step.getDisplayPrefs())
            .valueOrElseThrow(e -> new WdkModelException(e)));
    } catch (JSONException e) {
      throw new WdkModelException("Unable to convert Step to service JSON", e);
    }
  }

  private static JSONObject getValidationBundleJson(ValidationBundle validationBundle) {
    boolean isValid = validationBundle.getStatus().isValid();
    JSONObject json = new JSONObject()
        .put(JsonKeys.LEVEL, validationBundle.getLevel())
        .put(JsonKeys.IS_VALID, isValid);
    if (!isValid) {
      json.put(JsonKeys.ERRORS,
        new JSONObject()
          .put(JsonKeys.GENERAL, validationBundle.getUnkeyedErrors())
          .put(JsonKeys.BY_KEY, validationBundle.getKeyedErrors()));
    }
    return json;
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

    Optional.ofNullable(step.getPrimaryInputStep())
      .ifPresent(s -> out.put(JsonKeys.PRIMARY_INPUT_STEP, formatAsStepTree(s)));
    Optional.ofNullable(step.getSecondaryInputStep())
      .ifPresent(s -> out.put(JsonKeys.SECONDARY_INPUT_STEP, formatAsStepTree(s)));

    return out;
  }
}
