package org.gusdb.wdk.model.filter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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

  public static final int MAX_DISPLAY_LENGTH = 30;

  private static final String COLUMN_COUNT = "counts";
  private static final String KEY_VALUES = "values";

  public static ColumnFilterDefinition getDefinition() {
    ColumnFilterDefinition definition = new ColumnFilterDefinition();
    definition.setName(FILTER_NAME);
    definition.setImplementation(ListColumnFilter.class.getName());
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
      while (resultSet.next()) {
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
    StringBuilder sql = new StringBuilder("SELECT idq.* ");

    // need to join with idsql here to get extra (dynamic) columns from idq
    String[] pkColumns = answer.getQuestion().getRecordClass().getPrimaryKeyAttributeField().getColumnRefs();
    sql.append(" FROM (" + idSql + ") idq, (" + attributeSql + ") aq ");
    for (int i = 0; i < pkColumns.length; i++) {
      sql.append((i == 0) ? " WHERE " : " AND ");
      sql.append(" idq." + pkColumns[i] + " = aq." + pkColumns[i]);
    }
    sql.append(" AND " + columnName + " IN (");

    // put the filter values as join conditions
    List<String> values = getValues(jsValue);
    for (int i = 0; i < values.size(); i++) {
      if (i > 0)
        sql.append(", ");
      // escape quotes
      String value = "'" + values.get(i).replaceAll("'", "''") + "'";
      sql.append(value);
    }
    sql.append(")");
    return sql.toString();
  }

  @Override
  public String getDisplayValue(AnswerValue answer, JSONObject jsValue) {
    List<String> values = getValues(jsValue);
    StringBuilder buffer = new StringBuilder();
    for (String value : values) {
      if (buffer.length() + value.length() > MAX_DISPLAY_LENGTH) {
        buffer.append("...");
        break;
      }
      if (buffer.length() > 0)
        buffer.append(", ");
      buffer.append(value);
    }
    return buffer.toString();
  }

  private List<String> getValues(JSONObject jsValue) {
    List<String> values = new ArrayList<>();
    Object objValues = jsValue.get(KEY_VALUES);
    if (objValues instanceof JSONArray) { // multi values
      JSONArray jsValues = (JSONArray) objValues;
      for (int i = 0; i < jsValues.length(); i++) {
        values.add(jsValues.getString(i));
      }
    }
    else { // single value
      values.add(objValues.toString());
    }
    return values;
  }
}
