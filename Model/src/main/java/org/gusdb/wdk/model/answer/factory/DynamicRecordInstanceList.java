package org.gusdb.wdk.model.answer.factory;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
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

  public DynamicRecordInstanceList(AnswerValue answerValue) throws WdkModelException {
    _answerValue = answerValue;
    _idsQueryInstance = _answerValue.getIdsQueryInstance();
    initPageRecordInstances();
  }

  /**
   * Initialize the page's record instances, setting each one's PK value.
   */
  private void initPageRecordInstances() throws WdkModelException {
    try {
      Question question = _answerValue.getAnswerSpec().getQuestion();

      DataSource dataSource = question.getWdkModel().getAppDb().getDataSource();
      String sql = _answerValue.getPagedIdSql();
      String sqlName = _idsQueryInstance.getQuery().getFullName() + "__id-paged";

      new SQLRunner(dataSource, sql, sqlName).executeQuery(rs -> {
        try {
          @SuppressWarnings("resource")
          ResultList resultList = new SqlResultList(rs).setResponsibleForClosingResultSet(false);
          String[] pkColumns = question.getRecordClass().getPrimaryKeyDefinition().getColumnRefs();
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
          return null;
        }
        catch (WdkModelException | WdkUserException e) {
          throw new SQLRunnerException(e);
        }
      });

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
    catch (SQLRunnerException ex) {
      LOG.error("Unable to initialize record instances", ex);
      throw WdkModelException.translateFrom(ex);
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
    Query rawAttrQuery = (Query) wdkModel.resolveReference(attributeQuery.getFullName());

    LOG.debug("filling attribute values from answer " + rawAttrQuery.getFullName());
    for (Column column : rawAttrQuery.getColumns()) {
      LOG.trace("column: '" + column.getName() + "'");
    }

    // get and run the paged attribute query sql
    DataSource dataSource = wdkModel.getAppDb().getDataSource();
    String sql = _answerValue.getAnswerAttributeSql(rawAttrQuery, false);
    String sqlName = rawAttrQuery.getFullName() + "__attr-paged";

    try {
      int count = new SQLRunner(dataSource, sql, sqlName).executeQuery(rs -> {
        try {
          // wrap result set in result list; closing handled by SQLRunner
          @SuppressWarnings("resource")
          ResultList resultList = new SqlResultList(rs).setResponsibleForClosingResultSet(false);

          // fill in the column attributes
          PrimaryKeyDefinition pkDef = question.getRecordClass().getPrimaryKeyDefinition();
          Map<String, AttributeField> fields = question.getAttributeFieldMap();

          int counter = 0;
          while (resultList.next()) {
            PrimaryKeyValue primaryKey = new PrimaryKeyValue(pkDef, resultList);
            DynamicRecordInstance record = get(primaryKey);

            if (record == null) {
              StringBuffer error = new StringBuffer();
              error.append("Paged attribute query [");
              error.append(rawAttrQuery.getFullName());
              error.append("] returns rows that doesn't match the paged ");
              error.append("records. (");
              error.append(primaryKey.getValuesAsString());
              error.append(").\nPaged Attribute SQL:\n").append(sql);
              error.append("\n").append("Paged ID SQL:\n").append(_answerValue.getPagedIdSql());
              throw new WdkModelException(error.toString());
            }

            // fill in the column attributes
            for (String columnName : rawAttrQuery.getColumnMap().keySet()) {
              AttributeField field = fields.get(columnName);
              if (field != null && (field instanceof QueryColumnAttributeField)) {
                // valid attribute field, fill it in
                Object objValue = resultList.get(columnName);
                QueryColumnAttributeValue value = new QueryColumnAttributeValue((QueryColumnAttributeField) field, objValue);
                record.addAttributeValue(value);
              }
            }
            counter++;
          }

          return counter;
        }
        catch (WdkModelException e) {
          throw new SQLRunnerException(e);
        }
      });

      if (count != size()) {
        String uncachedIdSql = "";
        if (_answerValue.getIdsQueryInstance() instanceof SqlQueryInstance) {
          uncachedIdSql = "Uncached ID SQL: " + ((SqlQueryInstance)_answerValue.getIdsQueryInstance()).getUncachedSql();
        }
        String message = "The integrated attribute query '" + rawAttrQuery.getFullName() +
            "' doesn't return the same number of records as ID SQL for the current page.  Check that the ID " +
            "query returns no nulls or duplicates, and that the attribute-query join does not change the row count." + NL +
            "ResultSize: " + _answerValue.getResultSizeFactory().getResultSize() + NL +
            "Start: " + _answerValue.getStartIndex() + ", End: " + _answerValue.getEndIndex() + NL +
            "Expected (page size): " + size() + ", Actual (returned from attribute query): " + count + NL + NL +
            "Paged attribute SQL:" + NL + sql + NL +
            "Question: " + _answerValue.getAnswerSpec().getQuestion().getFullName() + NL + NL + uncachedIdSql;

        throw new WdkModelException(message);
      }
      LOG.debug("Attribute query [" + rawAttrQuery.getFullName() + "] integrated.");
    }
    catch (SQLRunnerException e) {
      LOG.error("Error executing attribute query using SQL \"" + sql + "\"", e);
      throw WdkModelException.translateFrom(e);
    }
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
      PrimaryKeyValue primaryKey = new PrimaryKeyValue(pkDef, resultList);
      DynamicRecordInstance record = get(primaryKey);

      if (record == null) {
        StringBuffer error = new StringBuffer();
        error.append("Paged table query [" + tableQuery.getFullName());
        error.append("] returned rows that doesn't match the paged ");
        error.append("records. (");
        error.append(primaryKey.getValuesAsString());
        error.append(").\nPaged table SQL:\n" + _answerValue.getAnswerTableSql(tableQuery));
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
