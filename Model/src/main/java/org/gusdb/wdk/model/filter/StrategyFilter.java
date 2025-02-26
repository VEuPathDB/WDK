package org.gusdb.wdk.model.filter;

import java.util.List;
import java.util.stream.Collectors;

import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.spec.SimpleAnswerSpec;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpecBuilder.FillStrategy;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.Strategy;
import org.json.JSONArray;
import org.json.JSONObject;

public class StrategyFilter extends StepFilter {

  public static final String FILTER_NAME = "WdkStrategyFilter";

  public static final String KEY_STRATEGY = "strategy";

  public static StepFilterDefinition getDefinition() {
    StepFilterDefinition definition = new StepFilterDefinition();
    definition.setName(FILTER_NAME);
    definition.setDisplay("Existing Strategy");
    definition.setDescription("Filter the results by other strategies of the same type.");
    definition.setImplementation(StrategyFilter.class.getName());
    definition.setView("/wdk/jsp/results/strategyFilter.jsp");
    return definition;
  }

  @Override
  public String getKey() {
    return FILTER_NAME;
  }

  @Override
  public JSONObject getSummaryJson(AnswerValue answer, String idSql) throws WdkModelException {
    String rcName = answer.getQuestion().getRecordClass().getFullName();
    List<Strategy> strategies = new StepFactory(answer.getRequestingUser())
        .getStrategies(answer.getRequestingUser().getUserId(), ValidationLevel.RUNNABLE, FillStrategy.FILL_PARAM_IF_MISSING)
        .values()
        .stream()
        .filter(strategy -> strategy.isValid() && strategy.getRecordClass().get().getFullName().equals(rcName))
        .collect(Collectors.toList());
    return getStrategiesJson(strategies);
  }

  private JSONObject getStrategiesJson(List<Strategy> strategies) {
    JSONArray arr = new JSONArray();
    for (Strategy strat : strategies) {
      arr.put(new JSONObject()
        .put("id", strat.getStrategyId())
        .put("name", strat.getName())
        .put("size", strat.getEstimatedSize())
        .put("recordClass", strat.getRecordClass().get().getFullName()));
    }
    return new JSONObject().put("strategies", arr);
  }

  /**
   * the options contains the id of the strategy chosen as the filter
   * 
   * @throws WdkModelException
   * 
   * @see org.gusdb.wdk.model.filter.Filter#getSql
   */
  @Override
  public String getSql(AnswerValue answer, String idSql, JSONObject jsValue) throws WdkModelException {
    Strategy strategy = getStrategy(answer, jsValue);
    RunnableObj<Step> step = strategy.getRootStep().getRunnable()
        .getOrThrow(st -> new WdkModelException("Strategy specified must have a runnable root step."));
    AnswerValue rootAnswer = AnswerValueFactory.makeAnswer(Step.getRunnableAnswerSpec(step));

    // make sure both answers are of the same type.
    RecordClass recordClass = answer.getQuestion().getRecordClass();
    String rootName = rootAnswer.getQuestion().getRecordClass().getFullName();
    if (!recordClass.getFullName().equals(rootName))
      throw new WdkModelException("You cannot filter the result with a strategy of a different type.");
    
    String filterSql = rootAnswer.getIdSql();
    return new StringBuilder("SELECT idQ.* FROM ")
      .append(" (" + idSql + ") idQ, (" + filterSql + ") filterQ WHERE ")
      .append(recordClass.getPrimaryKeyDefinition().createJoinClause("idQ", "filterQ"))
      .toString();
  }

  @Override
  public String getDisplayValue(AnswerValue answerValue, JSONObject jsValue) throws WdkModelException {
    Strategy strategy = getStrategy(answerValue, jsValue);
    return strategy.getName();
  }

  private Strategy getStrategy(AnswerValue answer, JSONObject jsValue) throws WdkModelException {
    int strategyId = jsValue.getInt(KEY_STRATEGY);
    return new StepFactory(answer.getRequestingUser()).getStrategyById(strategyId, ValidationLevel.SEMANTIC, FillStrategy.FILL_PARAM_IF_MISSING)
        .orElseThrow(() -> new WdkModelException("Passed ID (" + strategyId + ") does not correspond to a strategy."));
  }

  @Override
  public void setDefaultValue(JSONObject defaultValue) {
    throw new UnsupportedOperationException("Not supported until the defaultValueEquals() method is fully implemented");
  }

  /**
   * Not fully implemented yet.
   */
  @Override
  public boolean defaultValueEquals(SimpleAnswerSpec answerSpec, JSONObject value)  throws WdkModelException {
    return false;
  }

  @Override
  public ValidationBundle validate(Question question, JSONObject value, ValidationLevel validationLevel) {
    // TODO: make sure incoming strategy has a runnable root step
    return ValidationBundle.builder(ValidationLevel.SEMANTIC).build();
  }
}
