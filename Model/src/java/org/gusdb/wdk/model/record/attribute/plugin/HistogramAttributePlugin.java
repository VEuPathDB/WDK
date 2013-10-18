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

/**
 * @author jerric
 * 
 */
public class HistogramAttributePlugin extends AbstractAttributePlugin implements
    AttributePlugin {

  private static final String COLUMN_SUMMARY = "summary";
  private static final String ATTR_PLUGIN = "plugin";
  private static final String ATTR_SUMMARY = "summary";
  private static final String ATTR_HISTOGRAM = "histogram";

  private static final Logger logger = Logger.getLogger(HistogramAttributePlugin.class);

  private Map<String, Integer> summaries;

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.AttributePlugin#process()
   */
  @Override
  public Map<String, Object> process() {
    loadSummaries();

    Map<String, Double> histogram = scaleHistogram(summaries);

    // compose the result
    Map<String, Object> result = new LinkedHashMap<String, Object>();
    result.put(ATTR_SUMMARY, summaries);
    result.put(ATTR_HISTOGRAM, histogram);
    result.put(ATTR_PLUGIN, this);
    return result;
  }

  @Override
  public String getDownloadContent() {
    loadSummaries();

    StringBuilder builder = new StringBuilder();
    builder.append("<table>");
    builder.append("<tr><th>" + attributeField.getDisplayName()
        + "</th><th>Record Count</th></tr>");
    for (String attribute : summaries.keySet()) {
      int count = summaries.get(attribute);
      builder.append("<tr><td>" + attribute + "</td><td>" + count
          + "</td></tr>");
    }
    builder.append("</table>");
    return builder.toString();
  }

  private void loadSummaries() {
    if (summaries != null)
      return;
    summaries = getSummaries();
  }

  protected Map<String, Integer> getSummaries() {
    Map<String, Integer> summaries = new LinkedHashMap<String, Integer>();
    ResultSet resultSet = null;
    try {
      String attributeColumn = AbstractAttributePlugin.ATTRIBUTE_COLUMN;
      String attributeSql = getAttributeSql();
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

  private Map<String, Double> scaleHistogram(Map<String, Integer> histogram) {
    // find the max length
    int maxLength = 0;
    for (int length : histogram.values()) {
      if (maxLength < length)
        maxLength = length;
    }

    Map<String, Double> scaled = new LinkedHashMap<String, Double>();
    for (String column : histogram.keySet()) {
      double length = histogram.get(column) / (double)maxLength;
      scaled.put(column, length);
    }
    return scaled;
  }
}
