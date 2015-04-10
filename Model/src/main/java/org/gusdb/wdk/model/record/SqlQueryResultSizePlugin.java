package org.gusdb.wdk.model.record;

import java.util.LinkedHashMap;
import java.util.Map;
import java.math.BigDecimal;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.param.Param;

public class SqlQueryResultSizePlugin implements ResultSize {
	
	private final static String WDK_ID_SQL_PARAM = "WDK_ID_SQL";
	private final static String COUNT_COLUMN = "count";
	
	Query query;

	public SqlQueryResultSizePlugin(Query query) {
		this.query = query;
	}
	
	@Override
	public Integer getResultSize(AnswerValue answerValue)
			throws WdkModelException, WdkUserException {

		QueryInstance<?> queryInstance = getQueryInstance(answerValue);
		ResultList results = queryInstance.getResults();
		results.next();
		Integer count = ((BigDecimal)results.get(COUNT_COLUMN)).intValue();
		RecordClass recordClass = answerValue.getQuestion().getRecordClass();
		if (results.next()) throw new WdkModelException("Record class '"  + recordClass.getName() + "' has an SqlResultSizePlugin whose SQL returns more than one row.");
		return count;
	}
	
	private QueryInstance<?> getQueryInstance(AnswerValue answerValue) throws WdkModelException, WdkUserException {
	      Map<String, String> params = new LinkedHashMap<String, String>();
	      params.put(WDK_ID_SQL_PARAM, answerValue.getIdSql());
	      QueryInstance<?> queryInstance;
	      try {
	        queryInstance = query.makeInstance(answerValue.getUser(), params, true, 0,
	            new LinkedHashMap<String, String>());
	      }
	      catch (WdkUserException ex) {
	        throw new WdkModelException(ex);
	      }
	      return queryInstance;
	  }
	
	void validateQuery(Query query) throws WdkModelException {

		// must have only one parameter, and return only one column, the result size
		Param[] params = query.getParams();
		if (params.length != 1 || params[0].getFullName().equals(WDK_ID_SQL_PARAM))
			throw new WdkModelException("ResultSizeQuery '" + query.getFullName() + "' must have exactly one paramter, with name '" + WDK_ID_SQL_PARAM + "'");

		Map<String, Column> columnMap = query.getColumnMap();
		
		if (columnMap.size() != 1 || !columnMap.containsKey(COUNT_COLUMN))
			throw new WdkModelException("ResultSizeQuery '" + query.getFullName() + "' must have exactly one column, with name '" + COUNT_COLUMN + "'");
	}
}

