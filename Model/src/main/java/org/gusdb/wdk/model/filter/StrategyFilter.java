package org.gusdb.wdk.model.filter;

import java.util.Collection;

import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.factory.AnswerValue;
import org.gusdb.wdk.model.answer.spec.SimpleAnswerSpec;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.StepUtilities;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.model.user.User;
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

  /**
   * @throws WdkModelException 
   * @see org.gusdb.wdk.model.filter.Filter#getSummary(org.gusdb.wdk.model.answer.factory.AnswerValue)
   */
  @Override
  public FilterSummary getSummary(AnswerValue answer, String idSql) throws WdkModelException {
    User user = answer.getUser();
    String rcName = answer.getAnswerSpec().getQuestion().getRecordClass().getFullName();
    Collection<Strategy> strategies = StepUtilities.getStrategiesMap(user, rcName).values();
    return new StrategyFilterSummary(strategies);
  }

  /**
   * the options contains the id of the strategy chosen as the filter.
   * @throws WdkModelException 
   * 
   * @see org.gusdb.wdk.model.filter.Filter#getSql(org.gusdb.wdk.model.answer.factory.AnswerValue, java.lang.String, java.lang.String)
   */
  @Override
  public String getSql(AnswerValue answer, String idSql, JSONObject jsValue) throws WdkModelException {
    Strategy strategy = getStrategy(answer, jsValue);
    AnswerValue rootAnswer = strategy.getLatestStep().getAnswerValue();

    // make sure both answers are of the same type.
    RecordClass recordClass = answer.getAnswerSpec().getQuestion().getRecordClass();
    String rootName = rootAnswer.getAnswerSpec().getQuestion().getRecordClass().getFullName();
    if (!recordClass.getFullName().equals(rootName))
      throw new WdkModelException("You cannot filter the result with a strategy of a different type.");
    
    String[] pkColumns = recordClass.getPrimaryKeyDefinition().getColumnRefs();
    String filterSql = rootAnswer.getIdSql();
    StringBuilder buffer = new StringBuilder("SELECT idQ.* FROM ");
    buffer.append(" (" + idSql + ") idQ, (" + filterSql + ") filterQ ");
    for (int i = 0; i < pkColumns.length; i++) {
      buffer.append((i == 0)?" WHERE " : " AND ");
      buffer.append("idQ." + pkColumns[i] + " = filterQ." + pkColumns[i]);
    }
    return buffer.toString();
  }

  @Override
  public String getDisplayValue(AnswerValue answerValue, JSONObject jsValue) throws WdkModelException {
    Strategy strategy = getStrategy(answerValue, jsValue);
    return strategy.getName();
  }

  private Strategy getStrategy(AnswerValue answer, JSONObject jsValue) throws WdkModelException {
    int strategyId = jsValue.getInt(KEY_STRATEGY);
    return answer.getWdkModel().getStepFactory().getStrategyById(strategyId);
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
    // TODO: determine if validation is warranted here
    return ValidationBundle.builder(ValidationLevel.SEMANTIC).build();
  }
}
