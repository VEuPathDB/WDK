package org.gusdb.wdk.model.answer.factory;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.dbms.SqlResultList;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.SqlQueryInstance;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.DynamicRecordInstance;
import org.gusdb.wdk.model.record.PrimaryKeyDefinition;
import org.gusdb.wdk.model.record.PrimaryKeyValue;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.TableValue;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeValue;

public class DynamicRecordInstanceList extends LinkedHashMap<PrimaryKeyValue, DynamicRecordInstance> {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(DynamicRecordInstanceList.class);

  private final AnswerValue _answerValue;
  private final QueryInstance<?> _idsQueryInstance;

  public DynamicRecordInstanceList(AnswerValue answerValue) throws WdkModelException, WdkUserException {
    _answerValue = answerValue;
    _idsQueryInstance = _answerValue.getIdsQueryInstance();
    initPageRecordInstances();
  }

  /**
   * Initialize the page's record instances, setting each one's PK value.
   */
  private void initPageRecordInstances() throws WdkModelException, WdkUserException {
    try {
      Question question = _answerValue.getAnswerSpec().getQuestion();
      String sql = _answerValue.getPagedIdSql();
      WdkModel wdkModel = question.getWdkModel();
      DatabaseInstance platform = wdkModel.getAppDb();
      DataSource dataSource = platform.getDataSource();
      ResultSet resultSet;
      try {
        resultSet = SqlUtils.executeQuery(dataSource, sql, _idsQueryInstance.getQuery().getFullName() + "__id-paged");
      }
      catch (SQLException e) {
        throw new WdkModelException(e);
      }
      try (ResultList resultList = new SqlResultList(resultSet)) {
        RecordClass recordClass = question.getRecordClass();
        String[] pkColumns = recordClass.getPrimaryKeyDefinition().getColumnRefs();
        while (resultList.next()) {
          // get primary key. the primary key is supposed to be translated to
          // the current ones from the id query, and no more translation
          // needed.
          //
          // If this assumption is false, then we need to join the alias query
          // into the paged id query as well.
          Map<String, Object> pkValues = new LinkedHashMap<String, Object>();
          for (String column : pkColumns) {
            Object value = resultList.get(column);
            pkValues.put(column, value);
          }
          DynamicRecordInstance record = new DynamicRecordInstance(_answerValue.getUser(), question, this, pkValues);
          put(record.getPrimaryKey(), record);
        }
      }

      // check if the number of records is expected
      int expected = _answerValue.getPageSize();

      if (expected != size()) {
        String message = "Expected to find " + expected + " records in paged id query result." + NL +
            "ResultSize: " + _answerValue.getResultSizeFactory().getResultSize() + NL +
            "Start: " + _answerValue.getStartIndex() + ", End: " + _answerValue.getEndIndex() + NL +
            "Expected: " + expected + ", Actual: " + size() + NL +
            "Paged ID SQL:" + NL + sql + NL;
        throw new WdkModelException(message);
      }
    }
    catch (WdkModelException | WdkUserException ex) {
      LOG.error("Unable to initialize record instances", ex);
      throw ex;
    }
  }

  /**
   * Integrate into the page's RecordInstances the attribute values from a particular attributes query. The
   * attributes query result includes only rows for this page.
   * 
   * The query is obtained from Column, and the query should not be modified.
   * 
   * @throws WdkUserException
   */
  public void integrateAttributesQuery(Query attributeQuery) throws WdkModelException, WdkUserException {
    LOG.debug("Integrating attributes query " + attributeQuery.getFullName());

    Question question = _answerValue.getAnswerSpec().getQuestion();
    WdkModel wdkModel = question.getWdkModel();

    // has to get a clean copy of the attribute query, without pk params appended
    attributeQuery = (Query) wdkModel.resolveReference(attributeQuery.getFullName());

    LOG.debug("filling attribute values from answer " + attributeQuery.getFullName());
    for (Column column : attributeQuery.getColumns()) {
      LOG.trace("column: '" + column.getName() + "'");
    }

    String sql = _answerValue.getPagedAttributeSql(attributeQuery, false);
    int count = 0;

    // get and run the paged attribute query sql
    DatabaseInstance platform = wdkModel.getAppDb();
    DataSource dataSource = platform.getDataSource();

    ResultList resultList = null;
    try {
      resultList = new SqlResultList(SqlUtils.executeQuery(dataSource, sql, attributeQuery.getFullName() +
          "__attr-paged"));

      // fill in the column attributes
      PrimaryKeyDefinition pkDef = question.getRecordClass().getPrimaryKeyDefinition();
      Map<String, AttributeField> fields = question.getAttributeFieldMap();

      while (resultList.next()) {
        PrimaryKeyValue primaryKey = pkDef.getPrimaryKeyFromResultList(resultList);
        DynamicRecordInstance record = get(primaryKey);

        if (record == null) {
          StringBuffer error = new StringBuffer();
          error.append("Paged attribute query [");
          error.append(attributeQuery.getFullName());
          error.append("] returns rows that doesn't match the paged ");
          error.append("records. (");
          error.append(primaryKey.getValuesAsString());
          error.append(").\nPaged Attribute SQL:\n").append(sql);
          error.append("\n").append("Paged ID SQL:\n").append(_answerValue.getPagedIdSql());
          throw new WdkModelException(error.toString());
        }

        // fill in the column attributes
        for (String columnName : attributeQuery.getColumnMap().keySet()) {
          AttributeField field = fields.get(columnName);
          if (field != null && (field instanceof QueryColumnAttributeField)) {
            // valid attribute field, fill it in
            Object objValue = resultList.get(columnName);
            QueryColumnAttributeValue value = new QueryColumnAttributeValue((QueryColumnAttributeField) field, objValue);
            record.addAttributeValue(value);
          }
        }
        count++;
      }
    }
    catch (SQLException e) {
      LOG.error("Error executing attribute query using SQL \"" + sql + "\"", e);
      throw new WdkModelException(e);
    }
    finally {
      if (resultList != null)
        resultList.close();
    }

    if (count != size()) {
      String uncachedIdSql = "";
      if (_answerValue.getIdsQueryInstance() instanceof SqlQueryInstance) {
        uncachedIdSql = "Uncached ID SQL: " + ((SqlQueryInstance)_answerValue.getIdsQueryInstance()).getUncachedSql();
      }
      String message = "The integrated attribute query '" + attributeQuery.getFullName() +
          "' doesn't return the same number of records as ID SQL for the current page.  Check that the ID " +
          "query returns no nulls or duplicates, and that the attribute-query join does not change the row count." + NL +
          "ResultSize: " + _answerValue.getResultSizeFactory().getResultSize() + NL +
          "Start: " + _answerValue.getStartIndex() + ", End: " + _answerValue.getEndIndex() + NL +
          "Expected (page size): " + size() + ", Actual (returned from attribute query): " + count + NL + NL +
          "Paged attribute SQL:" + NL + sql + NL +
          "Question: " + _answerValue.getAnswerSpec().getQuestion().getFullName() + NL + NL + uncachedIdSql;
          
      throw new WdkModelException(message);
    }
    LOG.debug("Attribute query [" + attributeQuery.getFullName() + "] integrated.");
  }

  public void integrateTableQuery(TableField tableField) throws WdkModelException, WdkUserException {

    ResultList resultList = _answerValue.getTableFieldResultList(tableField);

    // initialize table values
    for (DynamicRecordInstance record : values()) {
      TableValue tableValue = new TableValue(tableField);
      record.addTableValue(tableValue);
    }

    // make table values
    PrimaryKeyDefinition pkDef = _answerValue.getAnswerSpec().getQuestion().getRecordClass().getPrimaryKeyDefinition();
    Query tableQuery = tableField.getWrappedQuery();

    while (resultList.next()) {
      PrimaryKeyValue primaryKey = pkDef.getPrimaryKeyFromResultList(resultList);
      DynamicRecordInstance record = get(primaryKey);

      if (record == null) {
        StringBuffer error = new StringBuffer();
        error.append("Paged table query [" + tableQuery.getFullName());
        error.append("] returned rows that doesn't match the paged ");
        error.append("records. (");
        error.append(primaryKey.getValuesAsString());
        error.append(").\nPaged table SQL:\n" + _answerValue.getPagedTableSql(tableQuery));
        error.append("\n" + "Paged ID SQL:\n" + _answerValue.getPagedIdSql());
        throw new WdkModelException(error.toString());
      }

      TableValue tableValue = record.getTableValue(tableField.getName());
      // initialize a row in table value
      tableValue.initializeRow(resultList);
    }
    LOG.debug("Table query [" + tableQuery + "] integrated.");
  }

}
