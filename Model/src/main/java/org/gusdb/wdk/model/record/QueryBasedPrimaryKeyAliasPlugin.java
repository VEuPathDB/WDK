package org.gusdb.wdk.model.record;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;

/**
 * Implements a PrimaryKeyAliasPLugin that uses an alias query
 * to look up records by primary key value.
 *
 * @author rdoherty
 */
public class QueryBasedPrimaryKeyAliasPlugin implements PrimaryKeyAliasPlugin {

  private static final String ALIAS_OLD_KEY_COLUMN_PREFIX = "old_";

  private Query _aliasQuery;

  public QueryBasedPrimaryKeyAliasPlugin(Query aliasQuery) {
    _aliasQuery = aliasQuery;
  }

  @Override
  public List<Map<String, Object>> getPrimaryKey(User user, Map<String, Object> inputPkValues)
      throws WdkModelException, RecordNotFoundException {

    List<Map<String, Object>> records = new ArrayList<Map<String, Object>>();

    // get alias from the alias query
    Map<String, String> oldValues = new LinkedHashMap<String, String>();
    for (String param : inputPkValues.keySet()) {
      String oldParam = ALIAS_OLD_KEY_COLUMN_PREFIX + param;
      String value = Utilities.parseValue(inputPkValues.get(param));
      oldValues.put(oldParam, value);
    }

    QueryInstance<?> instance = Query.makeQueryInstance(QueryInstanceSpec.builder()
        .putAll(oldValues).buildRunnable(user, _aliasQuery, StepContainer.emptyContainer()));

    try (ResultList resultList = instance.getResults()) {
      while (resultList.next()) {
        Map<String, Object> newValue = new LinkedHashMap<String, Object>();
        for (String param : inputPkValues.keySet()) {
          newValue.put(param, resultList.get(param));
        }
        records.add(newValue);
      }
    }

    return records;
  }

  /**
   * Resolve the alias query, and verify the needed columns. A alias query
   * should return all columns in the primary key, and it should also
   * return another set of columns that starts with ALIAS_OLD_KEY_COLUMN_PREFIX
   * prepended to the column names in the primary key.
   *
   * The resolved query is then wrapped to select a particular primary key
   * from the bulk query, using PK cols as params to the resulting query,
   * which is returned.
   *
   * @param aliasQueryRef query ref
   * @param pkColumnNames column names for this PK
   * @param recordClass record class this plugin belongs to
   * @throws WdkModelException
   */
  public static Query prepareAliasQuery(
      String aliasQueryRef,
      String[] pkColumnNames,
      RecordClass recordClass
  ) throws WdkModelException {

    WdkModel wdkModel = recordClass.getWdkModel();
    SqlQuery query = (SqlQuery)wdkModel.resolveReference(aliasQueryRef);

    recordClass.validateBulkQuery(query);

    Map<String, Column> columnMap = query.getColumnMap();
    // make sure the attribute query also returns old primary key columns
    for (String column : pkColumnNames) {
      column = ALIAS_OLD_KEY_COLUMN_PREFIX + column;
      if (!columnMap.containsKey(column))
        throw new WdkModelException("The alias query " + query.getFullName() +
            " of " + recordClass.getFullName() + " does not return the " +
            "required old primary key column " + column);
    }

    // the alias query should also return columns for old primary key
    // columns, with a prefix "old_".
    String[] paramNames = new String[pkColumnNames.length];
    for (int i = 0; i < pkColumnNames.length; i++) {
      paramNames[i] = ALIAS_OLD_KEY_COLUMN_PREFIX + pkColumnNames[i];
    }

    return RecordClass.prepareQuery(wdkModel, query, paramNames);
  }

}
