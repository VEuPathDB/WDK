package org.gusdb.wdk.model.record;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.record.attribute.PkColumnAttributeField;
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
 * If they are not defined, they will be generated from the defined PK columns.
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
   * if an alias query ref is defined, the reference will be resolved into a
   * Query and used to create a QueryBasedPrimaryKeyAliasPlugin, which will be
   * called as if the model defined an alias plugin implementation
   */
  private String _aliasQueryRef;
  private String _aliasPluginClassName;

  // resolved plugin used to look up PKs of valid records given candidate PKs
  private PrimaryKeyAliasPlugin _aliasPlugin;

  private List<WdkModelText> _columnRefList = new ArrayList<>();
  private Set<String> _columnRefSet = new LinkedHashSet<>();

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

  public void setAliasQueryRef(String aliasQueryRef) {
    _aliasQueryRef = aliasQueryRef;
  }

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

    if (_columnRefSet.isEmpty())
      throw new WdkModelException("No primary key column defined in "
          + "recordClass " + _recordClass.getFullName());
    if (_columnRefSet.size() > Utilities.MAX_PK_COLUMN_COUNT)
      throw new WdkModelException("You can specify up to "
          + Utilities.MAX_PK_COLUMN_COUNT + " primary key "
          + "columns in recordClass " + _recordClass.getFullName());
  }

  @Override
  public void resolveReferences(WdkModel model) throws WdkModelException {

    // make sure exactly one of [ aliasQuery, aliasPlugin ] was specified
    if ((_aliasQueryRef == null ? 0 : 1) + (_aliasPluginClassName == null ? 0 : 1) != 1) {
      throw new WdkModelException("Primary key definition of record class '" +
          _recordClass.getFullName() + "' must have exactly one of 'aliasQueryRef' or 'aliasPluginClassName'");
    }

    // if alias query specified, resolve/prepare it and create query based plugin
    if (_aliasQueryRef != null) {
      Query aliasQuery = QueryBasedPrimaryKeyAliasPlugin
          .prepareAliasQuery(_aliasQueryRef, getColumnRefs(), _recordClass);
      _aliasPlugin = new QueryBasedPrimaryKeyAliasPlugin(aliasQuery);
    }

    // otherwise, resolve the configured alias plugin
    else {
      try {
        _aliasPlugin = Class
            .forName(_aliasPluginClassName)
            .asSubclass(PrimaryKeyAliasPlugin.class)
            .getDeclaredConstructor()
            .newInstance();
      }
      catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
          InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
        throw new WdkModelException("Failed instantiating aliasPlugin for class " + _aliasPluginClassName, e);
      }
    }
  }

  public List<Map<String, Object>> lookUpPrimaryKeys(User user, Map<String, Object> pkValues)
      throws RecordNotFoundException, WdkModelException {
    List<Map<String,Object>> primaryKeys = _aliasPlugin.getPrimaryKey(user, pkValues);
    if (primaryKeys.isEmpty()) {
      throw new RecordNotFoundException("No " + _recordClass.getDisplayName() + " record found for the primary key values: " + displayPkValues(pkValues));
    }
    return primaryKeys;
  }

  public PrimaryKeyValue getPrimaryKeyFromResultSet(ResultSet resultSet) throws WdkModelException {
    try {
      Map<String, Object> pkValues = new LinkedHashMap<String, Object>();
      for (String column : getColumnRefs()) {
        pkValues.put(column, resultSet.getObject(column));
      }
      return new PrimaryKeyValue(this, pkValues);
    }
    catch (SQLException e) {
      throw new WdkModelException("Unable to read expected primary key value from result set.", e);
    }
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

  public String createJoinClause(String subQuery1Name, String subQuery2Name) {
    return Arrays.stream(getColumnRefs())
        .map(colName -> " " + subQuery1Name + "." + colName + " = " + subQuery2Name + "." + colName + " ")
        .collect(Collectors.joining(" AND "));
  }
}
