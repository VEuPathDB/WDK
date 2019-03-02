package org.gusdb.wdk.service.request.strategy;

import static org.gusdb.fgputil.json.JsonUtil.getBooleanOrDefault;
import static org.gusdb.fgputil.json.JsonUtil.getStringOrDefault;
import static org.gusdb.wdk.model.user.StepContainer.withId;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.Step.StepBuilder;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.json.JSONObject;

public class StrategyRequest {

  private String _name;
  private String _savedName;
  private String _description;
  private boolean _isSaved;
  private boolean _isPublic;
  private long _rootStepId;
  private Collection<StepBuilder> _steps;

  /**
   * Strategy Request JSON as follows:
   * {
   *   name: strategy name
   *   savedName: saved strategy name (optional)
   *   description: strategy description (optional - default empty string)
   *   isSaved: whether strategy should be saved (optional - default false)
   *   isPublic: whether strategy should be public (optional - default false)
   *   root: {
   *     id: 1234,
   *     primaryInput: {
   *       id: 2345,
   *       primaryInput: { id: 3456 },
   *       secondaryInput: { id: 4567 }
   *     },
   *     secondaryInput: {
   *       id: 5678,
   *       primaryInput: { id: 6789 }
   *     }
   *   }
   * }
   *
   * @param name
   * @param savedName
   * @param description
   * @param isSaved
   * @param isPublic
   * @param stepTree
   */
  public StrategyRequest(
      String name,
      String savedName,
      String description,
      boolean isSaved,
      boolean isPublic,
      long rootStepId,
      Collection<StepBuilder> steps) {
    _name = name;
    _savedName = savedName;
    _description = description;
    _isSaved = isSaved;
    _isPublic = isPublic;
    _rootStepId = rootStepId;
    _steps = steps;
  }

  public static StrategyRequest createFromJson(Optional<Strategy> existingStrategy,
      JSONObject json, StepFactory stepFactory)
      throws WdkModelException, DataValidationException {
    String name = json.getString(JsonKeys.NAME);
    String savedName = getStringOrDefault(json, JsonKeys.SAVED_NAME, "");
    String description = getStringOrDefault(json, JsonKeys.DESCRIPTION, "");
    boolean isSaved = getBooleanOrDefault(json, JsonKeys.IS_SAVED, false);
    boolean isPublic = getBooleanOrDefault(json, JsonKeys.IS_PUBLIC, false);

    TwoTuple<Long, Collection<StepBuilder>> treeInput =
        treeToSteps(existingStrategy, json.getJSONObject(JsonKeys.ROOT_STEP), stepFactory);
    return new StrategyRequest(name, savedName, description, isSaved,
        isPublic, treeInput.getFirst(), treeInput.getSecond());
  }

  public String getName() {
    return _name;
  }

  public String getSavedName() {
    return _savedName;
  }

  public String getDescription() {
    return _description;
  }

  public boolean isSaved() {
    return _isSaved;
  }

  public boolean isPublic() {
    return _isPublic;
  }

  public long getRootStepId() {
    return _rootStepId;
  }

  public Collection<StepBuilder> getSteps() {
    return _steps;
  }

  public static TwoTuple<Long, Collection<StepBuilder>> treeToSteps(
    Optional<Strategy> existingStrategy,
    JSONObject input,
    StepFactory factory
  ) throws WdkModelException, DataValidationException {

    final Queue<StepBuilder> allBuilders = new LinkedList<>();
    final Queue<JSONObject> next = new LinkedList<>();

    next.add(input);

    while(!next.isEmpty()) {

      final JSONObject cur = next.poll();
      final long stepId = cur.getLong(JsonKeys.ID);

      // try to find step in the existing strategy if present
      Optional<Step> stepOpt = existingStrategy
        .flatMap(strat -> strat.findFirstStep(withId(stepId)));

      // if not there, look up in DB
      Step step = stepOpt.isPresent() ? stepOpt.get() :
        factory.getStepById(stepId, ValidationLevel.NONE)
          .orElseThrow(() -> new DataValidationException(stepId + " is not a valid step ID."));

      // check that the step either already lives in the existing strategy or is unattached
      Optional<Long> existingStrategyId = existingStrategy.map(Strategy::getStrategyId);
      if (step.getStrategyId().isPresent() &&
          existingStrategyId.isPresent() &&
          !step.getStrategyId().get().equals(existingStrategyId.get())) {
        throw new DataValidationException("Step " + step.getStepId() +
          " belongs to strategy " + step.getStrategyId() +
          " so cannot be assigned to strategy " + existingStrategyId);
      }

      final StepBuilder builder = Step.builder(step).removeStrategy();

      if (cur.has(JsonKeys.PRIMARY_INPUT_STEP)) {
        final JSONObject prim = cur.getJSONObject(JsonKeys.PRIMARY_INPUT_STEP);
        builder.getAnswerSpec()
          .setParamValue(
            step.getPrimaryInputStepParamName()
              .orElseThrow(() -> new DataValidationException(
                  "Step " + stepId + " does not allow a primary input step.")),
            prim.getString(JsonKeys.ID)
          );
        next.add(prim);
      }

      if (cur.has(JsonKeys.SECONDARY_INPUT_STEP)) {
        final JSONObject sec = cur.getJSONObject(JsonKeys.SECONDARY_INPUT_STEP);
        builder.getAnswerSpec()
          .setParamValue(
            step.getSecondaryInputStepParamName()
              .orElseThrow(() -> new DataValidationException(
                "Step " + stepId + " does not allow a secondary input step.")),
            sec.getString(JsonKeys.ID)
          );
        next.add(sec);
      }

      allBuilders.add(builder);
    }

    return new TwoTuple<>(allBuilders.peek().getStepId(), allBuilders);
  }
}
