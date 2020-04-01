package org.gusdb.wdk.model.record;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.ImmutableEntry;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.factory.DynamicRecordInstanceList;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeValue;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeValue;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;

/**
 * An instance of RecordClass. A record instance has a unique
 * primaryKeyAttributeValue, and it acts as a container for attribute values and
 * table values.
 * 
 * @author jerric
 * 
 */
public class DynamicRecordInstance extends StaticRecordInstance {

  private static final long serialVersionUID = 1L;

  private static Logger logger = Logger.getLogger(DynamicRecordInstance.class);

  private final User _user;
  private DynamicRecordInstanceList _container;

  public DynamicRecordInstance(User user, RecordClass recordClass, Map<String, Object> pkValues)
      throws WdkModelException, WdkUserException {
    super(recordClass, recordClass, pkValues, true);
    _user = user;
  }

  public DynamicRecordInstance(User user, Question question, DynamicRecordInstanceList container, Map<String, Object> pkValues)
      throws WdkModelException, WdkUserException {
    super(question.getRecordClass(), question, pkValues, false);
    _user = user;
    _container = container;
  }

  @Override
  protected Collection<TableField> getAvailableTableFields() {
    return _recordClass.getTableFieldMap().values();
  }

  private void fillColumnAttributeValues(Query attributeQuery)
      throws WdkModelException, WdkUserException {
    logger.debug("filling column attribute values...");
    if (_container != null) {
      _container.integrateAttributesQuery(attributeQuery);
      return;
    }

    // prepare the attribute query, and make a new one,
    String queryName = attributeQuery.getFullName();

    Query query = _recordClass.getAttributeQuery(queryName);

    logger.debug("filling attribute values from record on query: "
        + query.getFullName());
    for (Column column : query.getColumns()) {
      logger.debug("column: " + column.getName());
    }
    if (query instanceof SqlQuery)
      logger.debug("SQL: \n" + ((SqlQuery) query).getSql());

    Map<String, String> paramValues = _primaryKey.getValues();

    // put user id in the attribute query
    QueryInstance<?> instance = Query.makeQueryInstance(QueryInstanceSpec.builder()
        .putAll(paramValues).buildRunnable(_user, query, StepContainer.emptyContainer()));

    try (ResultList resultList = instance.getResults()) {
      if (!resultList.next()) {
        // throwing exception prevents proper handling in front end...just return?
        _isValidRecord = false;
        throw new WdkModelException("Attribute query " + queryName +
            " doesn't return any row: \n" + instance.getSql());
      }

      Map<String, AttributeField> fields = getAttributeFieldMap();
      for (Column column : query.getColumns()) {
        if (!fields.containsKey(column.getName())) continue;
        AttributeField field = fields.get(column.getName());
        if (!(field instanceof QueryColumnAttributeField)) continue;
        Object objValue = resultList.get(column.getName());
        ColumnAttributeValue value = new QueryColumnAttributeValue(
            (QueryColumnAttributeField) field, objValue);
        addAttributeValue(value);
      }
    }
    logger.debug("column attributes are cached.");
  }

  @Override
  public TableValue getTableValue(String tableName) throws WdkModelException,
      WdkUserException {
    TableField tableField = _recordClass.getTableField(tableName);

    // check if the table value has been cached
    if (_tableValueCache.containsKey(tableName)) {
      //logger.debug("Requested table '" + tableName + "' exists in cache; returning");
      return _tableValueCache.get(tableName);
    }

    // not cached, if it's in the context of an answer, integrate it.
    if (_container != null) {
      logger.debug("Table '" + tableName + "' requested but not present.  AnswerValue present; integrating...");
      _container.integrateTableQuery(tableField);
      logger.debug("Does cache now contain table? " + (_tableValueCache.get(tableName) == null ? "nope!" : "yep!"));
      return _tableValueCache.get(tableName);
    }

    // in the context of record, create the value
    logger.debug("Creating single record table value '" + tableName + "' for PK " + _primaryKey.getValuesAsString());
    TableValue value = new DynamicTableValue(_primaryKey, tableField, _user);
    addTableValue(value);
    return value;
  }

  @Override
  public QueryColumnAttributeValue getQueryColumnAttributeValue(QueryColumnAttributeField field)
      throws WdkModelException, WdkUserException {
    Query query = field.getColumn().getQuery();
    logFill(query);
    fillColumnAttributeValues(query);
    if (!containsKey(field.getName())) {
      // something is wrong here, need further investigation.
      throw new WdkModelException("Attribute query for field " + field.getName() + " exists, but does not " +
          "return a value for that field; needs investigation.");
    }
    return (QueryColumnAttributeValue) get(field.getName());
  }

  private void logFill(Query query) {
    if (logger.isDebugEnabled()) {
      logger.debug("Filling column attribute values from query " + query.getFullName());
      for (Column column : query.getColumns()) {
        logger.trace("column: " + column.getName());
      }
      if (query instanceof SqlQuery) {
        logger.debug("SQL: \n" + ((SqlQuery) query).getSql());
      }
    }
  }

  @Override
  public boolean containsKey(Object key) {
    return _attributeFieldMap.containsKey(key);
  }

  @Override
  public boolean isEmpty() {
    return _attributeFieldMap.isEmpty();
  }

  @Override
  public int size() {
    return _attributeFieldMap.size();
  }

  @Override
  public Set<String> keySet() {
    return _attributeFieldMap.keySet();
  }

  @Override
  public Set<Entry<String,AttributeValue>> entrySet() {
    Set<Entry<String, AttributeValue>> entries = new HashSet<>();
    for (String name : _attributeFieldMap.keySet()) {
      entries.add(new ImmutableEntry<String, AttributeValue>(name, get(name)));
    }
    return entries;
  }

  @Override
  public Collection<AttributeValue> values() {
    List<AttributeValue> values = new ArrayList<AttributeValue>();
    for (String name : _attributeFieldMap.keySet()) {
      values.add(get(name));
    }
    return values;
  }
}
