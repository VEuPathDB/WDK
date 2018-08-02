package org.gusdb.wdk.model.report;

import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;

import javax.sql.DataSource;

import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.factory.AnswerValue;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.record.PrimaryKeyDefinition;
import org.gusdb.wdk.model.record.PrimaryKeyValue;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.DerivedAttributeField;
import org.gusdb.wdk.model.record.attribute.LinkAttributeField;
import org.gusdb.wdk.model.record.attribute.PkColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.TextAttributeField;
import org.json.JSONObject;

public abstract class AbstractAttributeReporter extends AbstractReporter  {

  protected static final String ATTRIBUTE_COLUMN = "wdk_attribute";

  private AttributeField _attributeField;

  protected AbstractAttributeReporter(AnswerValue answerValue) {
    super(answerValue);
  }

  @Override
  protected void write(OutputStream out) throws WdkModelException {
    JSONObject json = getJsonResult(_baseAnswer);
    PrintStream ps = new PrintStream(out);
    ps.print(json.toString());
  }

  abstract protected JSONObject getJsonResult(AnswerValue answerValue) throws WdkModelException;

  @Override
  public Reporter configure(Map<String, String> config) throws WdkUserException, WdkModelException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Reporter configure(JSONObject config) throws WdkUserException, WdkModelException {
    return this;
  }

  @Override
  public void setProperties(ReporterRef reporterRef) throws WdkModelException {
    if (!(reporterRef instanceof AttributeReporterRef)) {
      // this should never happen
      throw new WdkModelException("Reporter ref passed to AbstractAttributeReporter is not an AttributeReporterRef");
    }
    super.setProperties(reporterRef);
    _attributeField = ((AttributeReporterRef)reporterRef).getAttributeField();
  }

  protected AttributeField getAttributeField() {
    return _attributeField;
  }

  protected String getAttributeSql(AnswerValue answerValue) throws WdkModelException {
    WdkModel wdkModel = answerValue.getAnswerSpec().getQuestion().getRecordClass().getWdkModel();

    // format the display of the attribute in sql
    Map<String, String> queries = new LinkedHashMap<String, String>();
    String column = formatColumn(answerValue, _attributeField, queries);

    RecordClass recordClass = answerValue.getAnswerSpec().getQuestion().getRecordClass();
    String[] pkColumns = recordClass.getPrimaryKeyDefinition().getColumnRefs();
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
        } else
          sql.append(" AND ");
        sql.append("idq." + pkColumn + " = " + sqlId + "." + pkColumn);
      }
    }

    return sql.toString();
  }
  
  private String formatColumn(AnswerValue answerValue, AttributeField attribute,
      Map<String, String> queries) throws WdkModelException {

    // if column attribute can be formatted directly (ColumnAttributeField subclasses)
    if (attribute instanceof PkColumnAttributeField) {
      // PK attributes and columns must have the same name
      return "idq." + attribute.getName();
    }
    if (attribute instanceof QueryColumnAttributeField) {
      Column column = ((QueryColumnAttributeField) attribute).getColumn();
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
    } else if (attribute instanceof TextAttributeField) { // includes IdAttributeField
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
    Map<String, AttributeField> fields = answerValue.getQuestion().getAttributeFieldMap();
    Matcher matcher = DerivedAttributeField.MACRO_PATTERN.matcher(content);
    while (matcher.find()) {
      String fieldName = matcher.group(1);
      AttributeField field = fields.get(fieldName);
      String fieldContent = formatColumn(answerValue, field, queries);

      if (matcher.start() > pos)
        builder.append(content.substring(pos, matcher.start()));
      builder.append("' || " + fieldContent + " || '");
      pos = matcher.end();
    }
    if (pos < content.length() - 1)
      builder.append(content.substring(pos));
    return builder.append("'").toString();
  }

  /**
   * @return the values of the associated attribute. the key of the map is the
   *         primary key of a record instance.
   * @throws WdkUserException 
   */
  protected Map<PrimaryKeyValue, Object> getAttributeValues(AnswerValue answerValue)
      throws WdkModelException, SQLException, WdkUserException {
    WdkModel wdkModel = answerValue.getQuestion().getRecordClass().getWdkModel();
    Map<PrimaryKeyValue, Object> values = new LinkedHashMap<>();
    RecordClass recordClass = answerValue.getQuestion().getRecordClass();
    PrimaryKeyDefinition pkDef = recordClass.getPrimaryKeyDefinition();
    String[] pkColumns = pkDef.getColumnRefs();
    String sql = getAttributeSql(answerValue);
    DataSource dataSource = wdkModel.getAppDb().getDataSource();
    ResultSet resultSet = null;
    try {
      resultSet = SqlUtils.executeQuery(dataSource, sql,
          answerValue.getQuestion().getQuery().getFullName()
              + "__attribute-plugin-combined", 5000);
      while (resultSet.next()) {
        Map<String, Object> pkValues = new LinkedHashMap<String, Object>();
        for (String pkColumn : pkColumns) {
          pkValues.put(pkColumn, resultSet.getObject(pkColumn));
        }
        PrimaryKeyValue pkValue = new PrimaryKeyValue(pkDef, pkValues);
        Object value = resultSet.getObject(ATTRIBUTE_COLUMN);
        values.put(pkValue, value);
      }
    } finally {
      SqlUtils.closeResultSetAndStatement(resultSet, null);
    }
    return values;
  }

}
