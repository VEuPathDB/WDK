package org.gusdb.wdk.model.record;

import java.util.LinkedHashMap;
import java.util.Map;
import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.param.Param;

/**
 * For now only supports numeric property (count)
 */
public class SqlQueryResultPropertyPlugin implements ResultProperty {
	
  private static final Logger logger = Logger.getLogger(SqlQueryResultPropertyPlugin.class);

	private final static String WDK_ID_SQL_PARAM = "WDK_ID_SQL";
	private final static String PROPERTY_COLUMN = "propertyValue";
	
	Query query;
	String propertyName;

	public SqlQueryResultPropertyPlugin(Query query, String propertyName) throws WdkModelException{
		this.query = query;
		this.propertyName = propertyName;
		validateQuery(query);
	}
	
	@Override
	public Integer getPropertyValue(AnswerValue answerValue, String propertyName)
			throws WdkModelException, WdkUserException {
		RecordClass recordClass = answerValue.getQuestion().getRecordClass();
		logger.debug("Getting property value: in record class: " + recordClass.getFullName() + " and question: " + answerValue.getQuestion().getDisplayName());
		logger.debug(" .... with idSQL: " + answerValue.getIdSql());

		if (!propertyName.equals(this.propertyName)) throw new WdkModelException("Accessing result property plugin for record class '"  + recordClass.getName() + "' with illegal property name '" + propertyName + "'.  The allowed property name is '" + this.propertyName + "'");

		QueryInstance<?> queryInstance = getQueryInstance(answerValue);
		ResultList results = queryInstance.getResults();
		results.next();
		Integer count = ((BigDecimal)results.get(PROPERTY_COLUMN)).intValue();
		if (results.next()) throw new WdkModelException("Record class '"  + recordClass.getName() + "' has an SqlResultPropertyPlugin whose SQL returns more than one row.");
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
	
	private void validateQuery(Query query) throws WdkModelException {

		// must have only one parameter, and return only one column, the result size
		Param[] params = query.getParams();
		if (params.length != 1 || params[0].getFullName().equals(WDK_ID_SQL_PARAM))
			throw new WdkModelException("ResultSizeQuery '" + query.getFullName() + "' must have exactly one paramter, with name '" + WDK_ID_SQL_PARAM + "'");

		Map<String, Column> columnMap = query.getColumnMap();
		
		if (columnMap.size() != 1 || !columnMap.containsKey(PROPERTY_COLUMN))
			throw new WdkModelException("ResultSizeQuery '" + query.getFullName() + "' must have exactly one column, with name '" + PROPERTY_COLUMN + "'");
	}
}

