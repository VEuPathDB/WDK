package org.gusdb.wdk.service.request.strategy;

import static org.gusdb.fgputil.json.JsonUtil.getStringOrDefault;
import static org.gusdb.fgputil.json.JsonUtil.getBooleanOrDefault;

import javax.ws.rs.ForbiddenException;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.Step.StepBuilder;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.StepFactoryHelpers.UserCache;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.model.user.Strategy.StrategyBuilder;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.service.AbstractWdkService;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public class StrategyRequest {

  private String _name;
  private String _savedName;
  private String _description;
  private boolean _isSaved;
  private boolean _isPublic;
  private StepBuilder _rootStep;

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
      StepBuilder rootStep) {
    _name = name;
    _savedName = savedName;
    _description = description;
    _isSaved = isSaved;
    _isPublic = isPublic;
    _rootStep = rootStep;
  }

  public static StrategyRequest createFromJson(JSONObject json, StepFactory stepFactory)
      throws WdkModelException, DataValidationException {
    try {
      String name = json.getString(JsonKeys.NAME);
      String savedName = getStringOrDefault(json, JsonKeys.SAVED_NAME, "");
      String description = getStringOrDefault(json, JsonKeys.DESCRIPTION, "");
      boolean isSaved = getBooleanOrDefault(json, JsonKeys.IS_SAVED, false);
      boolean isPublic = getBooleanOrDefault(json, JsonKeys.IS_PUBLIC, false);

      // RRD 1/11 FIXME notes: this doesn't look quite right to me, instead, should:
      //    1. load the existing strategy
      //    2. load any new steps not in that strategy
      //    3. error if any newly added steps belong to another strat
      //    4. Use strategy and step builders to build a replacement strat based on incoming tree
      //    5. Validate the new strat (build with RUNNABLE)
      //    6. Save the strat
      //    7. Purge strategy ID and answer params from steps that were removed from strat, then save
      //          Can do this easily with StepBuilder.removeStrategy()

      return new StrategyRequest(name, savedName, description, isSaved, isPublic,
          treeToSteps(json.getJSONObject(JsonKeys.ROOT_STEP), stepFactory, ValidationLevel.SEMANTIC).getFirst());
    }
    catch (WdkUserException e) {
      throw new DataValidationException(e);
    }
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

  public StepBuilder getRootStep() {
    return _rootStep;
  }

  public static TwoTuple<StepBuilder, Collection<StepBuilder>> treeToSteps(
    JSONObject input,
    StepFactory fac,
    ValidationLevel chkLvl
  ) throws WdkModelException, WdkUserException, DataValidationException {
    final Queue<StepBuilder> all = new LinkedList<>();
    final Queue<JSONObject> next = new LinkedList<>();

    next.add(input);

    while(!next.isEmpty()) {
      final JSONObject cur = next.poll();
      final Step step = fac.getStepById(cur.getLong(JsonKeys.ID), chkLvl)
        .orElseThrow(WdkUserException::new);
      final StepBuilder bul = Step.builder(step);

      if (cur.has(JsonKeys.PRIMARY_INPUT_STEP)) {
        final JSONObject prim = cur.getJSONObject(JsonKeys.PRIMARY_INPUT_STEP);
        bul.getAnswerSpec()
          .setParamValue(
            step.getPrimaryInputStepParamName()
              .orElseThrow(DataValidationException::new),
            prim.getString(JsonKeys.ID)
          );
        next.add(prim);
      }

      if (cur.has(JsonKeys.SECONDARY_INPUT_STEP)) {
        final JSONObject sec = cur.getJSONObject(JsonKeys.SECONDARY_INPUT_STEP);
        bul.getAnswerSpec()
          .setParamValue(
            step.getSecondaryInputStepParamName()
              .orElseThrow(DataValidationException::new),
            sec.getString(JsonKeys.ID)
          );
        next.add(sec);
      }

      all.add(bul);
    }

    return new TwoTuple<>(all.peek(), all);
  }
}
