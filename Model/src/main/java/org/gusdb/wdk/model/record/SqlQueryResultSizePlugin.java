package org.gusdb.wdk.model.record;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.*;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.user.StepContainer;

public class SqlQueryResultSizePlugin implements ResultSize {

  private final static String WDK_ID_SQL_PARAM = "WDK_ID_SQL";
  private final static String COUNT_COLUMN = "count";

  SqlQuery _query;

  public SqlQueryResultSizePlugin(SqlQuery query) throws WdkModelException {
    _query = query;
    validateQuery(query);
  }

  @Override
  public Integer getResultSize(AnswerValue answerValue) throws WdkModelException {
    return getResultSize(answerValue, answerValue.getIdSql());
  }

  @Override
  public Integer getResultSize(AnswerValue answerValue, String idSql) throws WdkModelException {

    SqlQueryInstance queryInstance = (SqlQueryInstance) SqlQuery.makeQueryInstance(QueryInstanceSpec.builder()
        .put(WDK_ID_SQL_PARAM, idSql)
        .buildRunnable(answerValue.getRequestingUser(), _query, StepContainer.emptyContainer()));
    try (ResultList results = queryInstance.getUncachedResultsSubstitutePartitionKeys(answerValue)) {
      results.next();
      Integer count = ((Number) results.get(COUNT_COLUMN)).intValue();
      RecordClass recordClass = answerValue.getQuestion().getRecordClass();
      if (results.next())
        throw new WdkModelException("Record class '" + recordClass.getName() +
            "' has an SqlResultSizePlugin whose SQL returns more than one row.");
      return count;
    }
  }

  private void validateQuery(Query query) throws WdkModelException {

    // must have only one parameter, and return only one column, the result size
    Param[] params = query.getParams();
    if (params.length != 1 || !params[0].getName().equals(WDK_ID_SQL_PARAM))
      throw new WdkModelException("ResultSizeQuery '" + query.getFullName() +
          "' must have exactly one paramter, with name '" + WDK_ID_SQL_PARAM + "'");

    Map<String, Column> columnMap = query.getColumnMap();

    if (columnMap.size() != 1 || !columnMap.containsKey(COUNT_COLUMN))
      throw new WdkModelException("ResultSizeQuery '" + query.getFullName() +
          "' must have exactly one column, with name '" + COUNT_COLUMN + "'");
  }
}
