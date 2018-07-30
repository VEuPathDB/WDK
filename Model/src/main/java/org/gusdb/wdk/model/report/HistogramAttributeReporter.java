package org.gusdb.wdk.model.report;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
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
  private static final String ATTR_MIN = "min";
  private static final String ATTR_MAX = "max";
  private static final String ATTR_AVG = "avg";
  private static final String ATTR_BIN_SIZE = "binSize";
  private static final String ATTR_BIN_COUNT = "binCount";
  private static final String ATTR_MAX_BIN_COUNT = "maxBinCount";
  private static final String ATTR_LABEL = "attrLabel";
  private static final String ATTR_RECORD_COUNT_LABEL = "recordCountLabel";

  private static final Integer DEFAULT_BIN_COUNT = 10;
  private static final Integer MAX_BIN_COUNT = 100;

  private static final Logger logger = Logger.getLogger(HistogramAttributeReporter.class);

  /*
   * (non-Javadoc)
   *
   * @see org.gusdb.wdk.model.AttributePlugin#process()
   */
  @Override
  protected JSONObject getJsonResult(AnswerValue answerValue) throws WdkModelException {

    // load the data.
    Map<String, Integer> data = loadData(answerValue);
    JSONObject jsonData = new JSONObject();
    for (String key : data.keySet()) jsonData.put(key, data.get(key));
 
    // compose the result
    JSONObject result = new JSONObject();
    result.put(ATTR_DATA, data);

    // add labels
    result.put(ATTR_LABEL, _attributeField.getDisplayName());
    result.put(ATTR_RECORD_COUNT_LABEL, "# of " + answerValue.getQuestion().getRecordClass().getDisplayNamePlural());

    // determine the type
    String type = getType(data);
    result.put(ATTR_TYPE, type);

    // look for min and max
    Number[] range = getRange(data, type);
    result.put(ATTR_MIN, range[0]);
    result.put(ATTR_MAX, range[1]);
    result.put(ATTR_AVG, getAverage(data, type));

    // get bin size and count
    Integer count = getBinCount(type, range);
    result.put(ATTR_BIN_COUNT, count);
    result.put(ATTR_BIN_SIZE, getBinSize(type, range, count));

    result.put(ATTR_MAX_BIN_COUNT, getMaxBinCount(type, range));

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

  private Integer getBinCount(String type, Number[] range) {
    // if min & max is the same, return binSize as 1.
    float min = Float.valueOf(range[0].toString());
    float max = Float.valueOf(range[1].toString());
    if (min == max) return 1;

    return type.equals(TYPE_FLOAT) ? DEFAULT_BIN_COUNT
        : Math.min(DEFAULT_BIN_COUNT, range[1].intValue());
  }

  private Integer getMaxBinCount(String type, Number[] range) {
    if (type.equals(TYPE_CATEGORY)) {
      return (int)range[1];
    }
    if (type.equals(TYPE_INT)) {
      return Math.min(MAX_BIN_COUNT, (int)range[1]);
    }
    return MAX_BIN_COUNT;
  }

  private Double getAverage(Map<String, Integer> data, String type) {
    if (type.equals(TYPE_CATEGORY)) return null;

    Double average = data.entrySet()
      .stream()
      .flatMap(entry -> {
        Double[] uniqData = new Double[entry.getValue()];
        Arrays.fill(uniqData, Double.valueOf(entry.getKey()));
        return Arrays.stream(uniqData);
      })
      .collect(Collectors.averagingDouble(d -> d));

    return Double.parseDouble(String.format("%1.2f", average));
  }

  private Number getBinSize(String type, Number[] range, Integer numBins) {
    if (type.equals(TYPE_CATEGORY)) return 1;

    // if min & max is the same, return binSize as 1.
    float min = Float.valueOf(range[0].toString());
    float max = Float.valueOf(range[1].toString());
    if (min == max) return 1;

    if (type.equals(TYPE_FLOAT)) { // float type, continuous range
      return Math.round(max / numBins * 100) / (float)100;
    } else { // int or category, distinct range
      return Math.max(1, (int)(Math.ceil((max - min + 1) / numBins)));
    }
  }

  private Number[] getRange(Map<String, Integer> data, String type) {
    // empty data
    if (data.size() == 0) return new Number[] { 0, 0 };

    // non numeric
    if (type.equals(TYPE_CATEGORY)) return new Number[] { 0, data.size() - 1 };

    // numeric label, get the min & max of the labels.
    float min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
    for (String label : data.keySet()) {
      float value = Float.valueOf(label);
      if (min > value) min = value;
      if (max < value) max = value;
    }
    if (type.equals(TYPE_INT)) {
      return new Number[] { (int)min, (int)max };
    } else {
      return new Number[] { min, max };
    }
  }

  private Map<String, Integer> loadData(AnswerValue answerValue) {
    WdkModel wdkModel = answerValue.getQuestion().getRecordClass().getWdkModel();
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

  /*
  @Override
  protected JSONObject getJsonResult(AnswerValue answerValue) throws WdkModelException {
    Map<String, Object> result = process(answerValue);
    
    JSONObject jsonResult = new JSONObject();
    
    JSONObject jsonData = new JSONObject();
    @SuppressWarnings("unchecked")
    Map<String, Integer> data = (Map<String, Integer>) result.get(ATTR_DATA);
    for (String key : data.keySet()) jsonData.put(key, data.get(key));
    
    jsonResult.put(ATTR_DATA, jsonData);
    jsonResult.put(ATTR_TYPE, result.get(ATTR_TYPE));
    jsonResult.put(ATTR_MIN, result.get(ATTR_MIN));
    jsonResult.put(ATTR_MAX, result.get(ATTR_MAX));
    jsonResult.put(ATTR_AVG, result.get(ATTR_AVG));
    jsonResult.put(ATTR_BIN_COUNT, result.get(ATTR_BIN_COUNT));
    jsonResult.put(ATTR_BIN_SIZE, result.get(ATTR_BIN_SIZE));
    jsonResult.put(ATTR_MAX_BIN_COUNT, result.get(ATTR_MAX_BIN_COUNT));
    
    return jsonResult;
  }
  */
}
