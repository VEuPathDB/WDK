package org.gusdb.wdk.model.report.reporter;

import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.report.AbstractAttributeReporter;
import org.json.JSONObject;

public class HistogramAttributeReporter extends AbstractAttributeReporter {

  public HistogramAttributeReporter(AnswerValue answerValue) {
    super(answerValue);
  }

  public static final String PROP_TYPE = "type";

  public static final String TYPE_CATEGORY = "category";
  public static final String TYPE_INT = "int";
  public static final String TYPE_FLOAT = "float";

  private static final String COLUMN_COUNT = "count";
  private static final String ATTR_DATA = "data";
  private static final String ATTR_TYPE = "type";
  private static final String ATTR_LABEL = "attrLabel";
  private static final String ATTR_RECORD_COUNT_LABEL = "recordCountLabel";

  private static final Logger logger = Logger.getLogger(HistogramAttributeReporter.class);

  @Override
  protected JSONObject getJsonResult(AnswerValue answerValue) throws WdkModelException {

    // load the data.
    Map<String, Integer> data = loadData(answerValue);
 
    // compose the result
    JSONObject result = new JSONObject();
    result.put(ATTR_DATA, data);

    // add labels
    result.put(ATTR_LABEL, _attributeField.getDisplayName());
    result.put(ATTR_RECORD_COUNT_LABEL, "# of " +
        answerValue.getAnswerSpec().getQuestion().getRecordClass().getDisplayNamePlural());

    // determine the type
    String type = getType(data);
    result.put(ATTR_TYPE, type);

    return result;
  }
  

  private String getType(Map<String, Integer> data) {
    String type = _properties.get(PROP_TYPE);
    if (type != null) { // return specified type
      if (type.equals(TYPE_CATEGORY) || type.equals(TYPE_INT)
          || type.equals(TYPE_FLOAT))
        return type;
    }

    // infer type by the content in label
    try {
      type = TYPE_INT;
      for (String label : data.keySet()) {
        // check if it's a number
        Float.valueOf(label);

        // check if it's a float
        if (label.indexOf(".") >= 0) type = TYPE_FLOAT;
      }
      return type;
    } catch(Exception ex) {
      return TYPE_CATEGORY;
    }
  }

  private Map<String, Integer> loadData(AnswerValue answerValue) {
    WdkModel wdkModel = answerValue.getAnswerSpec().getQuestion().getRecordClass().getWdkModel();
    Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
    ResultSet resultSet = null;
    try {
      String attributeColumn = ATTRIBUTE_COLUMN;
      String attributeSql = getAttributeSql(answerValue);
      String groupSql = composeSql(attributeColumn, attributeSql);
      DataSource dataSource = wdkModel.getAppDb().getDataSource();
      resultSet = SqlUtils.executeQuery(dataSource, groupSql,
          getAttributeField().getName() + "__attribute-histogram");
      while (resultSet.next()) {
        String column = resultSet.getString(attributeColumn);
        // Skip null columns. We used to return an empty string, but this messes
        // up the type inference done in getType(), causing an integer histogram
        // type to be set to a category histogram type.
        if (column == null) continue;
        int count = resultSet.getInt(COLUMN_COUNT);
        counts.put(column, count);
      }
    } catch (Exception ex) {
      logger.error(ex);
      throw new RuntimeException(ex);
    } finally {
      SqlUtils.closeResultSetAndStatement(resultSet, null);
    }
    return counts;
  }

  private String composeSql(String attributeColumn, String sql) {
    StringBuilder groupSql = new StringBuilder("SELECT ");
    groupSql.append(attributeColumn + ", count(*) AS " + COLUMN_COUNT);
    groupSql.append(" FROM (" + sql + ") attrs GROUP BY " + attributeColumn);
    groupSql.append(" ORDER BY " + attributeColumn + " ASC");
    return groupSql.toString();
  }

}
