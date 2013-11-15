/**
 * 
 */
package org.gusdb.wdk.model.record.attribute.plugin;

import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.Step;

/**
 * @author jerric
 * 
 */
public class HistogramAttributePlugin extends AbstractAttributePlugin implements
    AttributePlugin {
  public static final String PROP_TYPE = "type";

  public static final String TYPE_CATEGORY = "category";
  public static final String TYPE_INT = "int";
  public static final String TYPE_FLOAT = "float";

  private static final String COLUMN_COUNT = "count";
  private static final String ATTR_DATA = "histogramData";
  private static final String ATTR_TYPE = "histogramType";
  private static final String ATTR_MIN = "histogramMin";
  private static final String ATTR_MAX = "histogramMax";
  private static final String ATTR_BIN_SIZE = "histogramBinSize";

  private static final Logger logger = Logger.getLogger(HistogramAttributePlugin.class);

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.AttributePlugin#process()
   */
  @Override
  public Map<String, Object> process(Step step) throws WdkModelException {
    // load the data.
    Map<String, Integer> data = loadData(step);

    // compose the result
    Map<String, Object> result = new LinkedHashMap<String, Object>();
    result.put(ATTR_DATA, data);

    // determine the type
    String type = getType(data);
    result.put(ATTR_TYPE, type);

    // look for min and max
    Number[] range = getRange(data, type);
    result.put(ATTR_MIN, range[0]);
    result.put(ATTR_MAX, range[1]);

    // get bin size
    result.put(ATTR_BIN_SIZE, getBinSize(data, type, range));

    return result;
  }

  private String getType(Map<String, Integer> data) {
    String type = properties.get(PROP_TYPE);
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

  private Number getBinSize(Map<String, Integer> data, String type, Number[] range) {
    // if min & max is the same, return binSize as 1.
    float min = Float.valueOf(range[0].toString());
    float max = Float.valueOf(range[1].toString()); 
    if (min == max) return 1;

    if (type.equals(TYPE_FLOAT)) { // float type, continuous range
      return (max - min) / 10D;
    } else { // int or category, distinct range
      return Math.max(1, (int)(Math.ceil((max - min + 1) / 10D)));
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

  private Map<String, Integer> loadData(Step step) throws WdkModelException {
    WdkModel wdkModel = step.getRecordClass().getWdkModel();
    Map<String, Integer> counts = new LinkedHashMap<>();
    ResultSet resultSet = null;
    try {
      String attributeColumn = AbstractAttributePlugin.ATTRIBUTE_COLUMN;
      String attributeSql = getAttributeSql(step);
      String groupSql = composeSql(attributeColumn, attributeSql);
      DataSource dataSource = wdkModel.getAppDb().getDataSource();
      resultSet = SqlUtils.executeQuery(dataSource, groupSql,
          attributeField.getName() + "__attribute-histogram");
      while (resultSet.next()) {
        String column = resultSet.getString(attributeColumn);
        if (column == null)
          column = "";
        int count = resultSet.getInt(COLUMN_COUNT);
        counts.put(column, count);
      }
    } catch (Exception ex) {
      logger.error(ex);
      throw new RuntimeException(ex);
    } finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
    }
    return counts;
  }

  private String composeSql(String attributeColumn, String sql) {
    StringBuilder groupSql = new StringBuilder("SELECT ");
    groupSql.append(attributeColumn + ", count(*) AS " + COLUMN_COUNT);
    groupSql.append(" FROM (" + sql + ")  GROUP BY " + attributeColumn);
    groupSql.append(" ORDER BY " + attributeColumn + " ASC");
    return groupSql.toString();
  }
}
