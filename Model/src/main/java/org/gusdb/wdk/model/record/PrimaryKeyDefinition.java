package org.gusdb.wdk.model.record;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.record.attribute.PkColumnAttributeField;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;

/**
 * <p>
 * Defines the {@link Column}s that can be used to uniquely
 * identify a {@link RecordInstance}.
 * </p>
 * <p>
 * A primary key is a combination of one or more {@link Column}s; if the
 * columns are needed as separate attributes, corresponding
 * {@link PkColumnAttributeField}s can to be defined inside the {@link RecordClass}.
 * If they are not defined, they will be generated.
 * </p>
 * <p>
 * Due to the limitation of the basket/dataset tables, we can only support a
 * limited number of columns in a primary key. the number is defined
 * {@link Utilities#MAX_PK_COLUMN_COUNT}.
 * </p>
 * 
 * @author jerric
 */
public class PrimaryKeyDefinition extends WdkModelBase {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(PrimaryKeyDefinition.class);

  private RecordClass _recordClass;

  /**
   * if an alias query ref is defined, the ids will be passed though this alias
   * query to get the new ids whenever a recordInstance is created.
   */
  private String _aliasQueryRef = null;
  private String _aliasPluginClassName = null;

  /**
   * the reference to a query that returns a list of alias ids of the given gene id
   */
  private Query _aliasQuery = null;
  private PrimaryKeyAliasPlugin _aliasPlugin = null;

  private List<WdkModelText> _columnRefList = new ArrayList<WdkModelText>();
  private Set<String> _columnRefSet = new LinkedHashSet<String>();

  public void setRecordClass(RecordClass recordClass) {
    _recordClass = recordClass;
  }

  public RecordClass getRecordClass() {
    return _recordClass;
  }

  public void addColumnRef(WdkModelText columnRef) {
    _columnRefList.add(columnRef);
  }

  public String[] getColumnRefs() {
    return _columnRefSet.toArray(new String[_columnRefSet.size()]);
  }

  public boolean hasColumn(String columnName) {
    return _columnRefSet.contains(columnName);
  }

  /**
   * @param aliasQueryRef
   */
  public void setAliasQueryRef(String aliasQueryRef) {
    _aliasQueryRef = aliasQueryRef;
  }

  /**
   * @param aliasPluginClassName
   */
  public void setAliasPluginClassName(String aliasPluginClassName) {
    _aliasPluginClassName = aliasPluginClassName;
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    // exclude columnRefs
    for (WdkModelText columnRef : _columnRefList) {
      if (columnRef.include(projectId)) {
        columnRef.excludeResources(projectId);
        String columnName = columnRef.getText();

        if (_columnRefSet.contains(columnName)) {
          throw new WdkModelException("The columnRef " + columnRef
              + " is duplicated in primaryKetAttribute in " + "recordClass "
              + _recordClass.getFullName());
        }
        else {
          _columnRefSet.add(columnName);
        }
      }
    }
    _columnRefList = null;

    if (_columnRefSet.size() == 0)
      throw new WdkModelException("No primary key column defined in "
          + "recordClass " + _recordClass.getFullName());
    if (_columnRefSet.size() > Utilities.MAX_PK_COLUMN_COUNT)
      throw new WdkModelException("You can specify up to "
          + Utilities.MAX_PK_COLUMN_COUNT + " primary key "
          + "columns in recordClass " + _recordClass.getFullName());
  }

  @Override
  public void resolveReferences(WdkModel model) throws WdkModelException {

    // resolve the alias query
    resolveAliasQuery(model);

    // resolve the alias plugin
    try {
      if (_aliasPluginClassName != null && _aliasPlugin == null) {
        Class<? extends PrimaryKeyAliasPlugin> pluginClass = Class.forName(_aliasPluginClassName).asSubclass(
            PrimaryKeyAliasPlugin.class);
        _aliasPlugin = pluginClass.newInstance();
      }
    }
    catch (Exception e) {
      throw new WdkModelException("Failed instantiating aliasPlugin for class " + _aliasPluginClassName, e);
    }
  }

  /**
   * resolve the alias query, and verify the needed columns. A alias query should return all columns in the
   * primary key, and it should also return another set of columns that starts with
   * ALIAS_OLD_KEY_COLUMN_PREFIX constant, appended by the column names in the primary key.
   * 
   * @param wdkModel
   * @throws WdkModelException
   */
  private void resolveAliasQuery(WdkModel wdkModel) throws WdkModelException {
    if (_aliasQueryRef != null) {
      Query query = (SqlQuery) wdkModel.resolveReference(_aliasQueryRef);

      _recordClass.validateBulkQuery(query);

      Map<String, Column> columnMap = query.getColumnMap();
      // make sure the attribute query also returns old primary key
      // columns
      for (String column : getColumnRefs()) {
        column = Utilities.ALIAS_OLD_KEY_COLUMN_PREFIX + column;
        if (!columnMap.containsKey(column))
          throw new WdkModelException("The attribute query " + query.getFullName() + " of " +
              _recordClass.getFullName() + " does not return the required old primary key " + "column " + column);
      }

      // the alias query should also return columns for old primary key
      // columns, with a prefix "old_".
      String[] pkColumns = getColumnRefs();
      String[] paramNames = new String[pkColumns.length];
      for (int i = 0; i < pkColumns.length; i++) {
        paramNames[i] = Utilities.ALIAS_OLD_KEY_COLUMN_PREFIX + pkColumns[i];
      }

      _aliasQuery = RecordClass.prepareQuery(wdkModel, query, paramNames);
    }
  }

  public List<Map<String, Object>> lookUpPrimaryKeys(User user, Map<String, Object> pkValues)
      throws WdkUserException, WdkModelException {
    List<Map<String,Object>> primaryKeys = new ArrayList<>();
    if (_aliasQuery != null) {
      primaryKeys = getPrimaryKeyFromAliasQuery(user, pkValues);
    }
    else if (_aliasPlugin != null) {
      primaryKeys = getPrimaryKeyFromAliasPlugin(user, pkValues);
    }
    if(primaryKeys.isEmpty()) {
      throw new RecordNotFoundException("No " + _recordClass.getDisplayName() + " record found for the primary key values: " + displayPkValues(pkValues));
    }
    return primaryKeys;
  }

  public PrimaryKeyValue getPrimaryKeyFromResultList(ResultList resultList) throws WdkModelException {
    Map<String, Object> pkValues = new LinkedHashMap<String, Object>();
    for (String column : getColumnRefs()) {
      pkValues.put(column, resultList.get(column));
    }
    return new PrimaryKeyValue(this, pkValues);
  }

  /**
   * Provides a human readable version of the primary key values;
   * @param pkValues
   * @return
   */
  private String displayPkValues(Map<String,Object> pkValues) {
    StringBuilder display = new StringBuilder(); 
    for(String key : pkValues.keySet()) {
      display.append(key + "=" + pkValues.get(key) + " ");
    }
    return display.toString();
  }

  private List<Map<String, Object>> getPrimaryKeyFromAliasPlugin(User user, Map<String, Object> pkValues)
      throws WdkModelException, WdkUserException {
    return _aliasPlugin.getPrimaryKey(user, pkValues);
  }

  private List<Map<String, Object>> getPrimaryKeyFromAliasQuery(User user, Map<String, Object> pkValues)
      throws WdkModelException {

    List<Map<String, Object>> records = new ArrayList<Map<String, Object>>();

    // get alias from the alias query
    Map<String, String> oldValues = new LinkedHashMap<String, String>();
    for (String param : pkValues.keySet()) {
      String oldParam = Utilities.ALIAS_OLD_KEY_COLUMN_PREFIX + param;
      String value = Utilities.parseValue(pkValues.get(param));
      oldValues.put(oldParam, value);
    }

    QueryInstance<?> instance = Query.makeQueryInstance(QueryInstanceSpec.builder()
        .putAll(oldValues).buildRunnable(user, _aliasQuery, StepContainer.emptyContainer()));
    
    try (ResultList resultList = instance.getResults()) {
      while (resultList.next()) {
        Map<String, Object> newValue = new LinkedHashMap<String, Object>();
        for (String param : pkValues.keySet()) {
          newValue.put(param, resultList.get(param));
        }
        records.add(newValue);
      }
      // no alias found, use the original ones
      //if (records.size() == 0)
      //  records.add(pkValues);
    }

    return records;
  }
}
