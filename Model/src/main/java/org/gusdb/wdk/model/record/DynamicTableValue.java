package org.gusdb.wdk.model.record;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;

public class DynamicTableValue extends TableValue {

  private static final Logger LOG = Logger.getLogger(DynamicTableValue.class);

  private final QueryInstance<?> _queryInstance;
  private boolean _rowsLoaded = false;

  public DynamicTableValue(PrimaryKeyValue primaryKey, TableField tableField, User user)
      throws WdkModelException {
    super(tableField);

    Query query = tableField.getWrappedQuery();
    if (query instanceof SqlQuery) {
      SqlQuery sqlQuery = new SqlQuery((SqlQuery) query);
      sqlQuery.setSql(sqlQuery.getSql().replaceAll(SqlQuery.PARTITION_KEYS_MACRO, "'pfal3D7'"));
      query = sqlQuery;
    }

    // create query instance; TableValue will initialize rows by itself
    _queryInstance = Query.makeQueryInstance(QueryInstanceSpec.builder()
        .putAll(primaryKey.getValues()).buildRunnable(user, tableField.getWrappedQuery(), StepContainer.emptyContainer()));
  }

  private void loadRowsFromQuery() {
    try (ResultList resultList = _queryInstance.getResults()) {
      while (resultList.next()) {
        initializeRow(resultList);
      }
    }
    catch (WdkModelException e) {
      throw new WdkRuntimeException("Unable to load table rows from query for table " + _tableField.getName(), e);
    }
    LOG.debug("Table value rows loaded.");
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
