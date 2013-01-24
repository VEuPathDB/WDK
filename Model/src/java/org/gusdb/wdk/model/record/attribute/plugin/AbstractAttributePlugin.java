package org.gusdb.wdk.model.record.attribute.plugin;

import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;

import javax.sql.DataSource;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.LinkAttributeField;
import org.gusdb.wdk.model.record.attribute.PrimaryKeyAttributeField;
import org.gusdb.wdk.model.record.attribute.PrimaryKeyAttributeValue;
import org.gusdb.wdk.model.record.attribute.TextAttributeField;
import org.gusdb.wdk.model.user.Step;
import org.json.JSONException;

public abstract class AbstractAttributePlugin implements AttributePlugin {

    protected static final String ATTRIBUTE_COLUMN = "wdk_attribute";

    private String name;
    private String display;
    private String description;
    private String view;

    protected WdkModel wdkModel;
    protected Map<String, String> properties;
    protected AttributeField attributeField;
    private Step step;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplay() {
        return (display == null) ? name : display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the view
     */
    public String getView() {
        return this.view;
    }

    /**
     * @param view the view to set
     */
    public void setView(String view) {
        this.view = view;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public void setAttributeField(AttributeField attribute) {
        this.attributeField = attribute;
    }

    public void setStep(Step step) {
        this.step = step;
        this.wdkModel = step.getUser().getWdkModel();
    }

    /**
     * @return the current step
     */
    protected Step getStep() {
        return step;
    }

    /**
     * @return the combined attribute sql and id sql. the returned columns
     *         include all primary key columns (they can be found in the
     *         PrimaryKeyAttributeField of the RecordClass), as well as a column
     *         for the associated attribute, the name of the column is defined
     *         as AbstractAttributePlugin.ATTRIBUTE_COLUMN.
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     * @throws WdkUserException
     * @throws SQLException
     * @throws JSONException
     */
    protected String getAttributeSql() throws WdkModelException,
            NoSuchAlgorithmException, WdkUserException, SQLException,
            JSONException {
        // format the display of the attribute in sql
        Map<String, String> queries = new LinkedHashMap<String, String>();
        String column = formatColumn(attributeField, queries);

        RecordClass recordClass = step.getQuestion().getRecordClass();
        String[] pkColumns = recordClass.getPrimaryKeyAttributeField()
                .getColumnRefs();
        AnswerValue answerValue = step.getAnswerValue();
        String idSql = answerValue.getIdSql();

        // construct the select clause
        StringBuilder sql = new StringBuilder("SELECT ");
        for (String pkColumn : pkColumns) {
            sql.append("idq.").append(pkColumn).append(", ");
        }
        sql.append(column).append(" AS ").append(ATTRIBUTE_COLUMN);

        // construct the from clause
        sql.append(" FROM (" + idSql + ") idq");
        for (String queryName : queries.keySet()) {
            String sqlId = queries.get(queryName);
            SqlQuery query = (SqlQuery) wdkModel.resolveReference(queryName);
            String attrSql = answerValue.getAttributeSql(query);
            sql.append(", (" + attrSql + ") " + sqlId);
        }

        // construct the where clause
        boolean first = true;
        for (String sqlId : queries.values()) {
            for (String pkColumn : pkColumns) {
                if (first) {
                    sql.append(" WHERE ");
                    first = false;
                } else sql.append(" AND ");
                sql.append("idq." + pkColumn + " = " + sqlId + "." + pkColumn);
            }
        }

        return sql.toString();
    }

    /**
     * @return the values of the associated attribute. the key of the map is the
     *         primary key of a Orecord instance.
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     * @throws WdkUserException
     * @throws SQLException
     * @throws JSONException
     */
    protected Map<PrimaryKeyAttributeValue, Object> getAttributeValues()
            throws WdkModelException, NoSuchAlgorithmException,
            WdkUserException, SQLException, JSONException {
        Map<PrimaryKeyAttributeValue, Object> values = new LinkedHashMap<PrimaryKeyAttributeValue, Object>();
        RecordClass recordClass = step.getQuestion().getRecordClass();
        PrimaryKeyAttributeField pkField = recordClass
                .getPrimaryKeyAttributeField();
        String[] pkColumns = pkField.getColumnRefs();
        String sql = getAttributeSql();
        DataSource dataSource = wdkModel.getQueryPlatform().getDataSource();
        ResultSet resultSet = null;
        try {
            resultSet = SqlUtils.executeQuery(wdkModel, dataSource, sql,
                    "wdk-attribute-plugin-combined", 5000);
            while (resultSet.next()) {
                Map<String, Object> pkValues = new LinkedHashMap<String, Object>();
                for (String pkColumn : pkColumns) {
                    pkValues.put(pkColumn, resultSet.getObject(pkColumn));
                }
                PrimaryKeyAttributeValue pkValue = new PrimaryKeyAttributeValue(
                        pkField, pkValues);
                Object value = resultSet.getObject(ATTRIBUTE_COLUMN);
                values.put(pkValue, value);
            }
        }
        finally {
            SqlUtils.closeResultSet(resultSet);
        }
        return values;
    }

    private String formatColumn(AttributeField attribute,
            Map<String, String> queries) throws WdkModelException {
        // if column attribute can be formatted directly.
        if (attribute instanceof ColumnAttributeField) {
            Column column = ((ColumnAttributeField) attribute).getColumn();
            String queryName = column.getQuery().getFullName();
            String sqlId = queries.get(queryName);
            if (sqlId == null) {
                sqlId = "aq" + queries.size();
                queries.put(queryName, sqlId);
            }
            return sqlId + "." + column.getName();
        }

        // get the content of the attribute
        String content;
        if (attribute instanceof LinkAttributeField) {
            content = ((LinkAttributeField) attribute).getDisplayText();
        } else if (attribute instanceof PrimaryKeyAttributeField) {
            content = ((PrimaryKeyAttributeField) attribute).getText();
        } else if (attribute instanceof TextAttributeField) {
            content = ((TextAttributeField) attribute).getText();
        } else {
            throw new WdkModelException("Attribute type not supported: "
                    + attribute.getName());
        }
        // escape the quotes
        content = content.trim().replaceAll("'", "''");

        // replace each attribute in the content
        StringBuilder builder = new StringBuilder("'");
        int pos = 0;
        Map<String, AttributeField> fields = step.getQuestion()
                .getAttributeFieldMap();
        Matcher matcher = AttributeField.MACRO_PATTERN.matcher(content);
        while (matcher.find()) {
            String fieldName = matcher.group(1);
            AttributeField field = fields.get(fieldName);
            String fieldContent = formatColumn(field, queries);

            if (matcher.start() > pos)
                builder.append(content.substring(pos, matcher.start()));
            builder.append("' || " + fieldContent + " || '");
            pos = matcher.end();
        }
        if (pos < content.length() - 1) builder.append(content.substring(pos));
        return builder.append("'").toString();
    }
}
