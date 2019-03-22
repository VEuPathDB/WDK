package org.gusdb.wdk.model.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.filter.FilterOptionList;
import org.gusdb.wdk.model.query.BooleanOperator;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordClassSet;

public class StepUtilities {

  private static Logger logger = Logger.getLogger(StepUtilities.class);

  public static Step createStep(User user, Long strategyId, Question question, Map<String, String> paramValues,
      String filterName, boolean deleted, boolean validate, int assignedWeight) throws WdkModelException {
    AnswerFilterInstance filter = null;
    RecordClass recordClass = question.getRecordClass();
    if (filterName != null) {
      filter = recordClass.getFilterInstance(filterName);
    }
    else
      filter = recordClass.getDefaultFilter();
    return createStep(user, strategyId, question, paramValues, filter, deleted, validate, assignedWeight);
  }

  public static Step createStep(User user, Long strategyId, Question question, Map<String, String> paramValues,
      AnswerFilterInstance filter, boolean deleted, boolean validate, int assignedWeight)
      throws WdkModelException {
    return createStep(user, strategyId, question, paramValues, filter, deleted, validate,
        assignedWeight, null);
  }

  public static Step createStep(User user, Long strategyId, Question question, Map<String, String> paramValues,
      AnswerFilterInstance filter, boolean deleted, boolean validate,
      int assignedWeight, FilterOptionList filterOptions) throws WdkModelException {
    Step step = user.getWdkModel().getStepFactory().createStep(user, strategyId, question, paramValues,
        filter, deleted, validate, assignedWeight, filterOptions);
    return step;
  }

  public static Strategy createStrategy(Step step, boolean saved) throws WdkModelException, WdkUserException {
    return createStrategy(step, null, null, saved, null, false, false);
  }

  public static Strategy createStrategy(Step step, boolean saved, boolean hidden) throws WdkModelException,
      WdkUserException {
    return createStrategy(step, null, null, saved, null, hidden, false);
  }

  // Transitional method...how to handle savedName properly?
  // Probably by expecting it if a name is given?
  public static Strategy createStrategy(Step step, String name, boolean saved) throws WdkModelException,
      WdkUserException {
    return createStrategy(step, name, null, saved, null, false, false);
  }

  public static Strategy createStrategy(Step step, String name, String savedName, boolean saved, String description,
      boolean hidden, boolean isPublic) throws WdkModelException, WdkUserException {
    User user = step.getUser();
    Strategy strategy = user.getWdkModel().getStepFactory().createStrategy(
        user, step, name, savedName, saved, description, hidden, isPublic);

    // set the view to this one
    ActiveStrategyFactory activeStrats = user.getSession().getActiveStrategyFactory();
    String strategyKey = Long.toString(strategy.getStrategyId());
    activeStrats.openActiveStrategy(strategyKey);
    if (strategy.isValid()) {
      activeStrats.setViewStrategyKey(strategyKey);
      activeStrats.setViewStepId(step.getStepId());
    }
    return strategy;
  }

  public static Map<Long, Step> getStepsMap(User user) throws WdkModelException {
    logger.debug("loading steps...");
    Map<Long, Step> invalidSteps = new LinkedHashMap<>();
    Map<Long, Step> allSteps = user.getWdkModel().getStepFactory().loadSteps(user, invalidSteps);
    return allSteps;
  }

  public static Map<Long, Strategy> getStrategiesMap(User user) throws WdkModelException {
    logger.debug("loading strategies...");
    Map<Long, Strategy> invalidStrategies = new LinkedHashMap<>();
    Map<Long, Strategy> strategies = user.getWdkModel().getStepFactory().loadStrategies(user, invalidStrategies);
    return strategies;
  }

  public static Map<String, List<Step>> getStepsByCategory(User user) throws WdkModelException {
    Map<Long, Step> steps = getStepsMap(user);
    Map<String, List<Step>> category = new LinkedHashMap<String, List<Step>>();
    for (Step step : steps.values()) {
      // not include the histories marked as 'deleted'
      if (step.isDeleted())
        continue;

      String type = step.getRecordClass().getFullName();
      List<Step> list;
      if (category.containsKey(type)) {
        list = category.get(type);
      }
      else {
        list = new ArrayList<Step>();
        category.put(type, list);
      }
      list.add(step);
    }
    return category;
  }

  public static Strategy[] getInvalidStrategies(User user) throws WdkModelException {

    Map<Long, Strategy> strategies = new LinkedHashMap<>();
    user.getWdkModel().getStepFactory().loadStrategies(user, strategies);

    Strategy[] array = new Strategy[strategies.size()];
    strategies.values().toArray(array);
    return array;
  }

  public static Strategy[] getStrategies(User user) throws WdkModelException {
    Map<Long, Strategy> map = getStrategiesMap(user);
    Strategy[] array = new Strategy[map.size()];
    map.values().toArray(array);
    return array;
  }

  public static Map<String, List<Strategy>> getStrategiesByCategory(User user) throws WdkModelException {
    Map<Long, Strategy> strategies = getStrategiesMap(user);
    return formatStrategiesByRecordClass(strategies.values(), user.getWdkModel());
  }

  public static Map<String, List<Strategy>> getUnsavedStrategiesByCategory(User user) throws WdkModelException {
    WdkModel wdkModel = user.getWdkModel();
    List<Strategy> strategies = wdkModel.getStepFactory().loadStrategies(user, false, false);
    return formatStrategiesByRecordClass(strategies, wdkModel);
  }

  public static Map<String, List<Strategy>> getSavedStrategiesByCategory(User user) throws WdkModelException {
    WdkModel wdkModel = user.getWdkModel();
    List<Strategy> strategies = wdkModel.getStepFactory().loadStrategies(user, true, false);
    return formatStrategiesByRecordClass(strategies, wdkModel);
  }

  public static Map<String, List<Strategy>> getRecentStrategiesByCategory(User user) throws WdkModelException {
    WdkModel wdkModel = user.getWdkModel();
    List<Strategy> strategies = wdkModel.getStepFactory().loadStrategies(user, false, true);
    return formatStrategiesByRecordClass(strategies, wdkModel);
  }

  public static Map<String, List<Strategy>> getActiveStrategiesByCategory(User user) throws WdkModelException,
      WdkUserException {
    Strategy[] strategies = user.getSession().getActiveStrategies();
    List<Strategy> list = new ArrayList<Strategy>();
    for (Strategy strategy : strategies)
      list.add(strategy);
    return formatStrategiesByRecordClass(list, user.getWdkModel());
  }

  private static Map<String, List<Strategy>> formatStrategiesByRecordClass(Collection<Strategy> strategies, WdkModel wdkModel)
      throws WdkModelException {
    Map<String, List<Strategy>> category = new LinkedHashMap<String, List<Strategy>>();
    for (RecordClassSet rcSet : wdkModel.getAllRecordClassSets()) {
      for (RecordClass recordClass : rcSet.getRecordClasses()) {
        String type = recordClass.getFullName();
        category.put(type, new ArrayList<Strategy>());
      }
    }
    for (Strategy strategy : strategies) {
      String rcName = strategy.getRecordClass().getFullName();
      List<Strategy> list;
      if (category.containsKey(rcName)) {
        list = category.get(rcName);
      }
      else {
        list = new ArrayList<Strategy>();
        category.put(rcName, list);
      }
      category.get(rcName).add(strategy);
    }
    return category;
  }

  public static Map<Long, Step> getStepsMap(User user, String rcName) throws WdkModelException {
    Map<Long, Step> steps = getStepsMap(user);
    Map<Long, Step> selected = new LinkedHashMap<>();
    for (long stepDisplayId : steps.keySet()) {
      Step step = steps.get(stepDisplayId);
      if (rcName.equalsIgnoreCase(step.getRecordClass().getFullName()))
        selected.put(stepDisplayId, step);
    }
    return selected;
  }

  public static Step[] getSteps(User user, String rcName) throws WdkModelException {
    Map<Long, Step> map = getStepsMap(user, rcName);
    Step[] array = new Step[map.size()];
    map.values().toArray(array);
    return array;
  }

  public static Step[] getSteps(User user) throws WdkModelException {
    Map<Long, Step> map = getStepsMap(user);
    Step[] array = new Step[map.size()];
    map.values().toArray(array);
    return array;
  }

  public static Step[] getInvalidSteps(User user) throws WdkModelException {
    Map<Long, Step> steps = new LinkedHashMap<>();
    user.getWdkModel().getStepFactory().loadSteps(user, steps);

    Step[] array = new Step[steps.size()];
    steps.values().toArray(array);
    return array;
  }

  public static Map<Long, Strategy> getStrategiesMap(User user, String rcName) throws WdkModelException {
    Map<Long, Strategy> strategies = getStrategiesMap(user);
    Map<Long, Strategy> selected = new LinkedHashMap<>();
    for (long strategyId : strategies.keySet()) {
      Strategy strategy = strategies.get(strategyId);
      if (rcName.equalsIgnoreCase(strategy.getRecordClass().getFullName()))
        selected.put(strategyId, strategy);
    }
    return selected;
  }

  public static Strategy[] getStrategies(User user, String dataType) throws WdkModelException {
    Map<Long, Strategy> map = getStrategiesMap(user, dataType);
    Strategy[] array = new Strategy[map.size()];
    map.values().toArray(array);
    return array;
  }

  public static Step getStep(User user, long stepID) throws WdkModelException {
    return user.getWdkModel().getStepFactory().loadStep(user, stepID);
  }

  public static Strategy getStrategy(User user, long strategyId) throws WdkModelException, WdkUserException {
    return getStrategy(user, strategyId, true);
  }

  public static Strategy getStrategy(User user, long strategyId, boolean allowDeleted)
      throws WdkModelException, WdkUserException {
    return user.getWdkModel().getStepFactory().loadStrategy(user, strategyId, allowDeleted);
  }

  public static void deleteSteps(User user) throws WdkModelException {
    user.getWdkModel().getStepFactory().deleteSteps(user, false);
  }

  public static void deleteStrategy(User user, long strategyId) throws WdkModelException {
    ActiveStrategyFactory activeStrategyFactory = user.getSession().getActiveStrategyFactory();
    String strategyKey = Long.toString(strategyId);
    int order = activeStrategyFactory.getOrder(strategyKey);
    if (order > 0)
      activeStrategyFactory.closeActiveStrategy(strategyKey);
    user.getWdkModel().getStepFactory().deleteStrategy(strategyId);
  }

  public static void deleteStrategies(User user) throws WdkModelException {
    deleteStrategies(user, false);
  }

  public static void deleteStrategies(User user, boolean allProjects) throws WdkModelException {
    user.getSession().getActiveStrategyFactory().clear();
    user.getWdkModel().getStepFactory().deleteStrategies(user, allProjects);
  }

  /**
   * Imports strategy behind strategy key into new strategy owned by this user.
   * The input strategy key is either:
   * <ul>
   *   <li>a strategy signature (generated by a share link)</li>
   *   <li>
   * 
   * @param strategyKey strategy key
   * @return new strategy
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public static Strategy importStrategy(User user, String strategyKey) throws WdkModelException, WdkUserException {
    return importStrategy(user, getStrategyByStrategyKey(user.getWdkModel(), strategyKey), null);
  }

  public static Strategy getStrategyByStrategyKey(WdkModel wdkModel, String strategyKey)
      throws WdkModelException, WdkUserException {
    Strategy oldStrategy;
    String[] parts = strategyKey.split(":");
    if (parts.length == 1) {
      // new strategy export url
      String strategySignature = parts[0];
      oldStrategy = wdkModel.getStepFactory().loadStrategy(strategySignature);
    }
    else {
      // get user from user signature
      User owner = wdkModel.getUserFactory().getUserBySignature(parts[0]);
      // make sure strategy id is an integer
      String strategyIdStr = parts[1];
      if (!FormatUtil.isInteger(strategyIdStr)) {
        throw new WdkUserException("Invalid strategy ID: " + strategyIdStr);
      }
      int strategyId = Integer.parseInt(strategyIdStr);
      oldStrategy = StepUtilities.getStrategy(owner, strategyId, true);
    }
    return oldStrategy;
  }

  public static Strategy importStrategy(User user, Strategy oldStrategy, Map<Long, Long> stepIdsMap)
      throws WdkModelException, WdkUserException {
    Strategy newStrategy = user.getWdkModel().getStepFactory().copyStrategy(user, oldStrategy, stepIdsMap, oldStrategy.getName());
    // highlight the imported strategy
    long rootStepId = newStrategy.getLatestStepId();
    String strategyKey = Long.toString(newStrategy.getStrategyId());
    if (newStrategy.isValid())
      user.getSession().setViewResults(strategyKey, rootStepId, 0);
    return newStrategy;
  }

  public static Step createBooleanStep(User user, long strategyId, Step leftStep, Step rightStep, String booleanOperator,
      String filterName) throws WdkModelException {
    BooleanOperator operator = BooleanOperator.parse(booleanOperator);
    Question question = null;
    try {
      question = leftStep.getQuestion();
    }
    catch (WdkModelException ex) {
      // in case the left step has an invalid question, try the right
      question = rightStep.getQuestion();
    }
    AnswerFilterInstance filter = null;
    RecordClass recordClass = question.getRecordClass();
    if (filterName != null) {
      filter = question.getRecordClass().getFilterInstance(filterName);
    }
    else
      filter = recordClass.getDefaultFilter();
    return createBooleanStep(user, strategyId, leftStep, rightStep, operator, filter);
  }

  public static Step createBooleanStep(User user, long strategyId, Step leftStep, Step rightStep, BooleanOperator operator,
      AnswerFilterInstance filter) throws WdkModelException {
    // make sure the left & right step belongs to the user
    if (leftStep.getUser().getUserId() != user.getUserId())
      throw new WdkModelException("The Left Step [" + leftStep.getStepId() +
          "] doesn't belong to the user #" + user.getUserId());
    if (rightStep.getUser().getUserId() != user.getUserId())
      throw new WdkModelException("The Right Step [" + rightStep.getStepId() +
          "] doesn't belong to the user #" + user.getUserId());

    // verify the record type of the operands
    RecordClass leftRecordClass = leftStep.getQuestion().getRecordClass();
    RecordClass rightRecordClass = rightStep.getQuestion().getRecordClass();
    if (!leftRecordClass.getFullName().equals(rightRecordClass.getFullName()))
      throw new WdkModelException("Boolean operation cannot be applied " +
          "to results of different record types. Left operand is " + "of type " +
          leftRecordClass.getFullName() + ", but the" + " right operand is of type " +
          rightRecordClass.getFullName());

    Question question = user.getWdkModel().getBooleanQuestion(leftRecordClass);
    BooleanQuery booleanQuery = (BooleanQuery) question.getQuery();

    Map<String, String> params = new LinkedHashMap<String, String>();

    String leftName = booleanQuery.getLeftOperandParam().getName();
    String leftKey = Long.toString(leftStep.getStepId());
    params.put(leftName, leftKey);

    String rightName = booleanQuery.getRightOperandParam().getName();
    String rightKey = Long.toString(rightStep.getStepId());
    params.put(rightName, rightKey);

    String operatorString = operator.getBaseOperator();
    params.put(booleanQuery.getOperatorParam().getName(), operatorString);
    //    params.put(booleanQuery.getUseBooleanFilter().getName(), Boolean.toString(useBooleanFilter));

    Step booleanStep = createStep(user, strategyId, question, params, filter, false, false, 0);
    booleanStep.setPreviousStep(leftStep);
    booleanStep.setChildStep(rightStep);
    return booleanStep;
  }

  public static TwoTuple<List<Strategy>, List<String>> getOpenableStrategies(WdkModel wdkModel, List<String> stratKeys) {
    List<Strategy> successfulStrats = new ArrayList<>();
    List<String> failedStratKeys = new ArrayList<>();
    for (String stratKey : stratKeys) {
      try {
        Strategy strat = getStrategyByStrategyKey(wdkModel, stratKey);
        successfulStrats.add(strat);
      }
      catch (Exception e) {
        failedStratKeys.add(stratKey);
      }
    }
    return new TwoTuple<>(successfulStrats, failedStratKeys);
  }
}
