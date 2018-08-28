package org.gusdb.wdk.model.record;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
import org.gusdb.wdk.model.user.User;

public class DynamicTableValue extends TableValue {

  private static final Logger LOG = Logger.getLogger(DynamicTableValue.class);

  private final QueryInstance<?> _queryInstance;
  private boolean _rowsLoaded = false;

  public DynamicTableValue(PrimaryKeyValue primaryKey, TableField tableField, User user)
      throws WdkModelException, WdkUserException {
    super(tableField);

    // create query instance; TableValue will initialize rows by itself
    _queryInstance = tableField.getWrappedQuery().makeInstance(
        user, primaryKey.getValues(), true, 0, new LinkedHashMap<String, String>());
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
  public Iterator<Map<String, AttributeValue>> iterator() {
    if (!_rowsLoaded) {
      loadRowsFromQuery();
      _rowsLoaded = true;
    }
    return super.iterator();
  }
}
