package org.gusdb.wdk.model.filter;

import java.util.ArrayList;
import java.util.List;

import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.factory.AnswerValue;
import org.gusdb.wdk.model.answer.spec.SimpleAnswerSpec;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;
import org.json.JSONArray;
import org.json.JSONObject;

public class ListColumnFilter extends SqlColumnFilter {

  public static final String FILTER_NAME = "WdkListColumnFilter";

  public static final int MAX_DISPLAY_LENGTH = 30;

  private static final String KEY_VALUES = "values";

  public static ColumnFilterDefinition getDefinition() {
    ColumnFilterDefinition definition = new ColumnFilterDefinition();
    definition.setName(FILTER_NAME);
    definition.setImplementation(ListColumnFilter.class.getName());
    definition.setDescription("Filter the result by the list of values in the column ");
    definition.setView("/wdk/jsp/results/listColumnFilter.jsp");

    return definition;
  }

  public ListColumnFilter(QueryColumnAttributeField attribute) {
    super(FILTER_NAME, attribute);
  }

  @Override
  public String getSummarySql(String inputSql) throws WdkModelException {
    String columnName = _attribute.getName();

    // group by the query and get a count
    String sql = "SELECT " + columnName + " as " + COLUMN_PROPERTY + ", count(*) AS " + COLUMN_COUNT + " FROM (" + inputSql +
        ") GROUP BY " + columnName;
 
    return sql;
  }

  @Override
  public String getFilterSql(String inputSql, JSONObject jsValue) throws WdkModelException {
    String columnName = _attribute.getName();

    StringBuilder sql = new StringBuilder("select * from (" + inputSql + ") where " + columnName + " in (");

    // put the filter values as join conditions
    List<String> values = getValues(jsValue);
    if (values.size() > 0) {
      for (int i = 0; i < values.size(); i++) {
        if (i > 0)
          sql.append(", ");
        // escape quotes
        String value = "'" + values.get(i).replaceAll("'", "''") + "'";
        sql.append(value);
      }
    } else { // no filter value selected, use default one;
      sql.append("''");
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
    if (values.size() == 0)
      buffer.append("(none selected)");
    return buffer.toString();
  }

  private List<String> getValues(JSONObject jsValue) {
    List<String> values = new ArrayList<>();
    if (jsValue.has(KEY_VALUES)) { 
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
    }
    return values;
  }

  @Override
  public void setDefaultValue(JSONObject defaultValue) {
    throw new UnsupportedOperationException("Not supported until the defaultValueEquals() method is fully implemented");
  }

  /**
   * Not fully implemented yet.
   */
  @Override
  public boolean defaultValueEquals(SimpleAnswerSpec answerSpec, JSONObject value) {
    return false;
  }

  @Override
  public ValidationBundle validate(Question question, JSONObject value, ValidationLevel validationLevel) {
    // TODO: determine if validation is warranted here
    return ValidationBundle.builder(ValidationLevel.SEMANTIC).build();
  }
}
