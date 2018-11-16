package org.gusdb.wdk.model.record;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.user.StepContainer;

/**
 * For now only supports numeric property (count)
 */
public class SqlQueryResultPropertyPlugin implements ResultProperty {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(SqlQueryResultPropertyPlugin.class);

  private final static String WDK_ID_SQL_PARAM = "WDK_ID_SQL";
  private final static String PROPERTY_COLUMN = "propertyValue";

  Query _query;
  String _propertyName;

  public SqlQueryResultPropertyPlugin(Query query, String propertyName) throws WdkModelException {
    this._query = query;
    this._propertyName = propertyName;
    // logger.debug("*******Result Property: " + propertyName + " and Query: " + query.toString() + " and SQL:
    // \n" + ((SqlQuery)query).getSql() );
    validateQuery(query);
  }

  @Override
  public Integer getPropertyValue(AnswerValue answerValue, String propertyName)
      throws WdkModelException, WdkUserException {
    String recordClassName = answerValue.getAnswerSpec().getQuestion().getRecordClass().getFullName();
    if (!propertyName.equals(_propertyName))
      throw new WdkModelException("Accessing result property plugin for record class '" +
          recordClassName + "' with illegal property name '" + propertyName +
          "'.  The allowed property name is '" + this._propertyName + "'");

    QueryInstance<?> queryInstance = Query.makeQueryInstance(QueryInstanceSpec.builder()
        .put(WDK_ID_SQL_PARAM, answerValue.getIdSql())
        .buildRunnable(answerValue.getUser(), _query, StepContainer.emptyContainer()));
    try (ResultList results = queryInstance.getResults()) {
      results.next();
      Integer count = ((BigDecimal) results.get(PROPERTY_COLUMN)).intValue();
      if (results.next())
        throw new WdkModelException("Record class '" + recordClassName +
            "' has an SqlResultPropertyPlugin whose SQL returns more than one row.");
      return count;
    }
  }

  private void validateQuery(Query query) throws WdkModelException {

    // must have only one parameter, and return only one column, the result size
    Param[] params = query.getParams();
    if (params.length != 1 || params[0].getFullName().equals(WDK_ID_SQL_PARAM))
      throw new WdkModelException("ResultSizeQuery '" + query.getFullName() +
          "' must have exactly one paramter, with name '" + WDK_ID_SQL_PARAM + "'");

    Map<String, Column> columnMap = query.getColumnMap();
    if (columnMap.size() != 1 || !columnMap.containsKey(PROPERTY_COLUMN))
      throw new WdkModelException("ResultSizeQuery '" + query.getFullName() +
          "' must have exactly one column, with name '" + PROPERTY_COLUMN + "'");
  }
}
