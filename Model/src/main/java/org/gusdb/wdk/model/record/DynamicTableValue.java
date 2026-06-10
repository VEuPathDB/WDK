package org.gusdb.wdk.model.record;

import static org.gusdb.wdk.model.WdkModelException.unwrap;
import static org.gusdb.wdk.model.WdkModelException.wrap;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Timer;
import org.gusdb.fgputil.functional.Either;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.TableFieldProcessQueryResult;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.ProcessQuery;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;

public class DynamicTableValue extends TableValue {

  private static final Logger LOG = Logger.getLogger(DynamicTableValue.class);

  private final Either<SqlQuery, ProcessQuery> _query;
  private final PrimaryKeyValue _primaryKey;
  private final User _user;
  private boolean _rowsLoaded = false;

  public DynamicTableValue(PrimaryKeyValue primaryKey, TableField tableField, User user)
      throws WdkModelException {
    super(tableField);
    _primaryKey = primaryKey;
    _user = user;
    _query = unwrap(() -> tableField.getQuery().mapLeft(qp -> wrap(() ->
        AnswerValue.addPartKeysToAttrOrTableSqlQuery(
            qp.getWrappedQuery(), getTableField().getRecordClass(), primaryKey))));
  }

  private void loadRowsFromQuery() {
    try (ResultList resultList = getResultList(_query, _primaryKey, _user)) {
      int rowCount = 0;
      Timer t = new Timer();
      Integer maxRows = _tableField.getWdkModel().getModelConfig().getMaxTableValueRows();
      while (resultList.next()) {
        LOG.trace("Row " + (++rowCount) + ": fetched in " + t.getElapsedStringAndRestart());
        if (rowCount > maxRows)
          throw new WdkRuntimeException("Table query returned too many (>" + maxRows + ") rows.");
        initializeRow(resultList);
      }
    }
    catch (WdkModelException e) {
      throw new WdkRuntimeException("Unable to load table rows from query for table " + _tableField.getName(), e);
    }
    LOG.debug("Table value rows loaded.");
  }

  // special processing for table values since process queries serving table fields should not be cached
  private ResultList getResultList(Either<SqlQuery, ProcessQuery> query, PrimaryKeyValue primaryKey, User user) throws WdkModelException {
    return query.isLeft()
      // create query instance; TableValue will initialize rows by itself
      ? Query.makeQueryInstance(QueryInstanceSpec.builder()
        .putAll(primaryKey.getValues())
        .buildRunnable(user, query.getLeft(), StepContainer.emptyContainer()))
          .getResults()
      : TableFieldProcessQueryResult.getResultList(user, _tableField, primaryKey.getRawValues());
  }

  @Override
  public Iterator<TableValueRow> iterator() {
    if (!_rowsLoaded) {
      loadRowsFromQuery();
      _rowsLoaded = true;
    }
    return super.iterator();
  }
}
