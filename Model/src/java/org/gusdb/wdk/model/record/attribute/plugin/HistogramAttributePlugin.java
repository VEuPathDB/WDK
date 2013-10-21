/**
 * 
 */
package org.gusdb.wdk.model.record.attribute.plugin;

import java.sql.ResultSet;
import java.util.Arrays;
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

  private static final String COLUMN_SUMMARY = "summary";
  private static final String ATTR_DATA = "data";
  private static final String ATTR_HISTOGRAM = "histogram";

  private static final Logger logger = Logger.getLogger(HistogramAttributePlugin.class);

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.AttributePlugin#process()
   */
  @Override
  public Map<String, Object> process(Step step) throws WdkModelException {
    Map<String, Integer> data = loadData(step);

    Map<String, Integer> histogram = computeHistogram(data);

    // compose the result
    Map<String, Object> result = new LinkedHashMap<String, Object>();
    result.put(ATTR_DATA, data);
    result.put(ATTR_HISTOGRAM, histogram);
    return result;
  }

  /**
   * The default function just return sorted data as a histogram.
   * 
   * @param histogram
   * @return
   */
  protected Map<String, Integer> computeHistogram(Map<String, Integer> data) {
    Map<String, Integer> histogram = new LinkedHashMap<>();
    String[] labels = data.keySet().toArray(new String[0]);
    Arrays.sort(labels);
    for (String label : labels) {
      histogram.put(label, data.get(label));
    }
    return histogram;
  }

  private Map<String, Integer> loadData(Step step) throws WdkModelException {
    WdkModel wdkModel = step.getRecordClass().getWdkModel();
    Map<String, Integer> summaries = new LinkedHashMap<String, Integer>();
    ResultSet resultSet = null;
    try {
      String attributeColumn = AbstractAttributePlugin.ATTRIBUTE_COLUMN;
      String attributeSql = getAttributeSql(step);
      String summarySql = composeSql(attributeColumn, attributeSql);
      DataSource dataSource = wdkModel.getAppDb().getDataSource();
      resultSet = SqlUtils.executeQuery(dataSource, summarySql,
          attributeField.getName() + "__attribute-histogram");
      while (resultSet.next()) {
        String column = resultSet.getString(attributeColumn);
        if (column == null)
          column = "";
        int summary = resultSet.getInt(COLUMN_SUMMARY);
        summaries.put(column, summary);
      }
    } catch (Exception ex) {
      logger.error(ex);
      throw new RuntimeException(ex);
    } finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
    }
    return summaries;
  }

  private String composeSql(String attributeColumn, String sql) {
    StringBuilder groupSql = new StringBuilder("SELECT ");
    groupSql.append(attributeColumn + ", count(*) AS " + COLUMN_SUMMARY);
    groupSql.append(" FROM (" + sql + ")  GROUP BY " + attributeColumn);
    groupSql.append(" ORDER BY " + attributeColumn + " ASC");
    return groupSql.toString();
  }
}
