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
import org.json.JSONArray;
import org.json.JSONObject;

public class ListColumnFilter extends ColumnFilter {

  public static final String FILTER_NAME = "WdkListColumnFilter";

  private static final String COLUMN_COUNT = "counts";
  private static final String KEY_VALUES = "values";

  public static ColumnFilterDefinition getDefinition() {
    ColumnFilterDefinition definition = new ColumnFilterDefinition();
    definition.setName(FILTER_NAME);
    definition.setImplementation(ListColumnFilter.class.getName());
    definition.setDisplay("Filter By ");
    definition.setDescription("Filter the result by the list of values in the column ");
    definition.setView("/wdk/jsp/results/listColumnFilter.jsp");

    return definition;
  }

  public ListColumnFilter(ColumnAttributeField attribute) {
    super(FILTER_NAME, attribute);
  }

  @Override
  public FilterSummary getSummary(AnswerValue answer, String idSql) throws WdkModelException,
      WdkUserException {
    String attributeSql = getAttributeSql(answer, idSql);
    String columnName = attribute.getName();

    Map<String, Integer> counts = new LinkedHashMap<>();
    // group by the query and get a count
    String sql = "SELECT " + columnName + ", count(*) AS " + COLUMN_COUNT + " FROM (" + attributeSql +
        ") GROUP BY " + columnName;
    ResultSet resultSet = null;
    DataSource dataSource = answer.getQuestion().getWdkModel().getAppDb().getDataSource();
    try {
      resultSet = SqlUtils.executeQuery(dataSource, sql, getKey() + "-summary");
      if (resultSet.next()) {
        String value = resultSet.getString(columnName);
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
  public String getSql(AnswerValue answer, String idSql, JSONObject jsValue) throws WdkModelException,
      WdkUserException {
    String attributeSql = getAttributeSql(answer, idSql);
    String columnName = attribute.getName();
    StringBuilder sql = new StringBuilder("SELECT * ");
    sql.append(" FROM (" + attributeSql + ") ");
    sql.append(" WHERE " + columnName + " IN (");

    Object objValues = jsValue.get(KEY_VALUES);
    if (objValues instanceof JSONArray) { // multi values
      JSONArray jsValues = (JSONArray) objValues;
      for (int i = 0; i < jsValues.length(); i++) {
        if (i > 0)
          sql.append(", ");

        String value = jsValues.getString(i);
        // escape quotes
        value = "'" + value.replaceAll("'", "''") + "'";
        sql.append(value);
      }
    }
    else { // single value
      String value = objValues.toString();
      value = "'" + value.replaceAll("'", "''") + "'";
      sql.append(value);
    }
    sql.append(")");
    return sql.toString();
  }

}
