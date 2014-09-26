package org.gusdb.wdk.model.filter;

import java.util.Collection;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.model.user.User;
import org.json.JSONObject;

public class StrategyFilter extends AbstractFilter {

  public static final String FILTER_NAME = "strategy-filter";
  
  public static final String KEY_STRATEGY = "strategy";
  
  public StrategyFilter() {
    super(FILTER_NAME);
    // set default info
    setDisplay("Filter By Strategy");
    setDescription("Filter the results by other strategies of the same type.");
    setView("/wdk/jsp/results/strategyFilter.jsp");
  }
  
  /**
   * @throws WdkModelException 
   * @see org.gusdb.wdk.model.filter.Filter#getSummary(org.gusdb.wdk.model.answer.AnswerValue)
   */
  @Override
  public FilterSummary getSummary(AnswerValue answer, String idSql) throws WdkModelException {
    User user = answer.getUser();
    String rcName = answer.getQuestion().getRecordClass().getFullName();
    Collection<Strategy> strategies = user.getStrategiesMap(rcName).values();
    return new StrategyFilterSummary(strategies);
  }

  /**
   * the options contains the id of the strategy choosen as the filter.
   * @throws WdkUserException 
   * @throws WdkModelException 
   * 
   * @see org.gusdb.wdk.model.filter.Filter#getSql(org.gusdb.wdk.model.answer.AnswerValue, java.lang.String, java.lang.String)
   */
  @Override
  public String getSql(AnswerValue answer, String idSql, JSONObject jsValue) throws WdkModelException, WdkUserException {
    int strategyId = jsValue.getInt(KEY_STRATEGY);
    User user = answer.getUser();
    Strategy strategy = user.getStrategy(strategyId);
    AnswerValue rootAnswer = strategy.getLatestStep().getAnswerValue();

    // make sure both answers are of the same type.
    RecordClass recordClass = answer.getQuestion().getRecordClass();
    String rootName = rootAnswer.getQuestion().getRecordClass().getFullName();
    if (!recordClass.getFullName().equals(rootName))
      throw new WdkUserException("You cannot filter the result with a strategy of a different type.");
    
    String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();
    String filterSql = rootAnswer.getIdSql();
    StringBuilder buffer = new StringBuilder("SELECT idQ.* FROM ");
    buffer.append(" (" + idSql + ") idQ, (" + filterSql + ") filterQ ");
    for (int i = 0; i < pkColumns.length; i++) {
      buffer.append((i == 0)?" WHERE " : " AND ");
      buffer.append("idQ." + pkColumns[i] + " = filterQ." + pkColumns[i]);
    }
    return buffer.toString();
  }

}
