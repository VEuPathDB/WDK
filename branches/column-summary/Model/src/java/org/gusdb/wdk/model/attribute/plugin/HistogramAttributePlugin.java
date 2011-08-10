/**
 * 
 */
package org.gusdb.wdk.model.attribute.plugin;

import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.gusdb.wdk.model.AbstractAttributePlugin;
import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.SqlQuery;
import org.json.JSONException;

/**
 * @author jerric
 * 
 */
public class HistogramAttributePlugin extends AbstractAttributePlugin implements
        AttributePlugin {

    private static final String PROP_MAX_BAR_LENGTH = "max-bar-length";
    private static final String COLUMN_SUMMARY = "summary";
    private static final String ATTR_PLUGIN = "plugin";
    private static final String ATTR_SUMMARY = "summary";
    private static final String ATTR_HISTOGRAM = "histogram";

    private static final float DEFAULT_MAX_BAR_LENGTH = 600;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.AttributePlugin#process(org.gusdb.wdk.model.AnswerValue
     * )
     */
    public Map<String, Object> process(AnswerValue answerValue)
            throws NoSuchAlgorithmException, WdkModelException,
            WdkUserException, SQLException, JSONException {
        Map<String, Integer> summaries = new LinkedHashMap<String, Integer>();
        WdkModel wdkModel = answerValue.getQuestion().getWdkModel();
        String columnName = attribute.getColumn().getName();
        DataSource dataSource = wdkModel.getQueryPlatform().getDataSource();
        String sql = composeSql(answerValue);
        ResultSet resultSet = null;
        try {
            resultSet = SqlUtils.executeQuery(wdkModel, dataSource, sql,
                    "wdk-attribute-histogram-" + attribute.getName());
            while (resultSet.next()) {
                String column = resultSet.getString(columnName);
                int summary = resultSet.getInt(COLUMN_SUMMARY);
                summaries.put(column, summary);
            }
        } finally {
            SqlUtils.closeResultSet(resultSet);
        }
        Map<String, Integer> histogram = scaleHistogram(summaries);

        // compose the result
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put(ATTR_SUMMARY, summaries);
        result.put(ATTR_HISTOGRAM, histogram);
        result.put(ATTR_PLUGIN, this);
        return result;
    }

    private String composeSql(AnswerValue answerValue)
            throws NoSuchAlgorithmException, WdkModelException,
            WdkUserException, SQLException, JSONException {
        WdkModel wdkModel = answerValue.getQuestion().getWdkModel();
        Column column = attribute.getColumn();
        String columnName = column.getName();
        String queryName = column.getQuery().getFullName();
        SqlQuery query = (SqlQuery) wdkModel.resolveReference(queryName);
        RecordClass recordClass = answerValue.getQuestion().getRecordClass();

        // compose the sql to get data
        String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();
        String idSql = answerValue.getIdSql();
        String attrSql = query.getSql();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT " + columnName + ", count(*) AS " + COLUMN_SUMMARY);
        sql.append(" FROM (" + idSql + ") idq, (" + attrSql + ") aq ");
        for (int i = 0; i < pkColumns.length; i++) {
            sql.append((i == 0) ? " WHERE " : " AND ");
            sql.append("idq." + pkColumns[i] + " = aq." + pkColumns[i]);
        }
        sql.append(" GROUP BY " + columnName);
        sql.append(" ORDER BY " + columnName + " ASC");
        return sql.toString();
    }

    private Map<String, Integer> scaleHistogram(Map<String, Integer> histogram) {
        // get the max bar length
        float maxBarLength = DEFAULT_MAX_BAR_LENGTH;
        String strMax = properties.get(PROP_MAX_BAR_LENGTH);
        if (strMax != null && strMax.length() != 0)
            maxBarLength = Float.valueOf(strMax);

        // find the max length
        int maxLength = 0;
        for (int length : histogram.values()) {
            if (maxLength < length) maxLength = length;
        }

        float scale = maxBarLength / maxLength;
        Map<String, Integer> scaled = new LinkedHashMap<String, Integer>();
        for(String column : histogram.keySet()) {
            int length = Math.max(1, Math.round(histogram.get(column) * scale));
            scaled.put(column, length);
        }
        return scaled;
    }
}
