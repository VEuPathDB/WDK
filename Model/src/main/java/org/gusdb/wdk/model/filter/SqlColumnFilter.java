package org.gusdb.wdk.model.filter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeField;
import org.json.JSONObject;

public abstract class SqlColumnFilter extends ColumnFilter {

	protected static final String COLUMN_PROPERTY = "property";
	protected static final String COLUMN_COUNT = "count";
	
	public SqlColumnFilter(String name, ColumnAttributeField attribute) {
		super(name, attribute);
	}

	/**
	 * 
	 * @param inputSql SQL that provides a set of rows to filter, including: filter, primary key and dynamic columns
	 * @param jsValue The parameter values.   This method should validate the JSON, and throw a WdkModelException if malformed, or a WdkUserException of illegal values.
	 * @return Sql that wraps the input, filtering the rows.
	 * @throws WdkModelException
	 * @throws WdkUserException
	 */
	public abstract String getFilterSql(String inputSql, JSONObject jsValue)
			throws WdkModelException, WdkUserException;
	
	/**
	 * 
	 * @param inputSql SQL that provides a set of rows to filter, including: filter, primary key and dynamic columns
	 * @return Sql that wraps the input, providing a summary with at least these two columns:  "property" (varchar) and "count" (number), where count is the number of things found for the item named in property
	 * @throws WdkModelException
	 * @throws WdkUserException
	 */
	public abstract String getSummarySql (String inputSql)
			throws WdkModelException, WdkUserException;
	
	@Override
	public abstract String getDisplayValue(AnswerValue answer, JSONObject jsValue)
			throws WdkModelException, WdkUserException;

	@Override
	public FilterSummary getSummary(AnswerValue answer, String idSql)
			throws WdkModelException, WdkUserException {
	    String attributeSql = getAttributeSql(answer, idSql);

	    Map<String, Integer> counts = new LinkedHashMap<>();
	    // group by the query and get a count
	    
	    String sql = getSummarySql(attributeSql);
	    
	    ResultSet resultSet = null;
	    DataSource dataSource = answer.getQuestion().getWdkModel().getAppDb().getDataSource();
	    try {
	      resultSet = SqlUtils.executeQuery(dataSource, sql, getKey() + "-summary");
	      while (resultSet.next()) {
	        String value = resultSet.getString(COLUMN_PROPERTY);
	        int count = resultSet.getInt(COLUMN_COUNT);
	        counts.put(value, count);
	      }
	    }
	    catch (SQLException ex) {
	      throw new WdkModelException(ex);
	    }
	    finally {
	      SqlUtils.closeResultSetAndStatement(resultSet);
	    }

	    return new ListColumnFilterSummary(counts);

	}

	@Override
	public String getSql(AnswerValue answer, String idSql, JSONObject jsValue)
			throws WdkModelException, WdkUserException {
		
		String attributeSql = getAttributeSql(answer, idSql);

		StringBuilder sql = new StringBuilder("SELECT idq.* ");

		// need to join with idsql here to get extra (dynamic) columns from idq
		String[] pkColumns = answer.getQuestion().getRecordClass().getPrimaryKeyAttributeField().getColumnRefs();
		sql.append(" FROM (" + idSql + ") idq, (" + attributeSql + ") aq ");
		for (int i = 0; i < pkColumns.length; i++) {
			sql.append((i == 0) ? " WHERE " : " AND ");
			sql.append(" idq." + pkColumns[i] + " = aq." + pkColumns[i]);
		}
		
		String filterSql = getFilterSql(sql.toString(), jsValue);
		
		StringBuilder finalSql = new StringBuilder("SELECT idq2.* from (" + idSql + ") idq2, (" + filterSql + ") filter ");
		for (int i = 0; i < pkColumns.length; i++) {
			finalSql.append((i == 0) ? " WHERE " : " AND ");
			finalSql.append(" idq2." + pkColumns[i] + " = filter." + pkColumns[i]);
		}

		return finalSql.toString();

	}

}
