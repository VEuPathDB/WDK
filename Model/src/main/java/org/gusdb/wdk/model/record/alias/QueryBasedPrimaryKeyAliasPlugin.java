package org.gusdb.wdk.model.record.alias;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.dbms.SqlResultList;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.record.PrimaryKeyDefinition;
import org.gusdb.wdk.model.record.PrimaryKeyValue;
import org.gusdb.wdk.model.record.RecordNotFoundException;
import org.gusdb.wdk.model.user.User;

public abstract class QueryBasedPrimaryKeyAliasPlugin implements PrimaryKeyAliasPlugin {

  private static final Logger LOG = Logger.getLogger(QueryBasedPrimaryKeyAliasPlugin.class);

  private static final Pattern DEFAULT_ID_REGEX = Pattern.compile("^[\\w\\-\\|\\.\\:\\(\\)]+$");

  // required 
  protected abstract String getRecordClassFullName();
  protected abstract String getQueryFullName();

  // supply PK col value regex but allow subclasses to override
  protected boolean pkColValuePassesRegex(String name, String value) {
    return DEFAULT_ID_REGEX.matcher(value).find();
  }

  @Override
  public List<Map<String, Object>> getPrimaryKey(User user, Map<String, Object> inputPkValues)
      throws WdkModelException, RecordNotFoundException {

    WdkModel wdkModel = user.getWdkModel();
    DatabaseInstance appDb = wdkModel.getAppDb();

    // find the PK def for this lookup
    PrimaryKeyDefinition pkDef = wdkModel
        .getRecordClassByFullName(getRecordClassFullName())
        .orElseThrow(() -> new WdkModelException("Recordclass " + getRecordClassFullName() + " could not be found in this model."))
        .getPrimaryKeyDefinition();

    PrimaryKeyValue pkValue = Functions.mapException(
        () -> new PrimaryKeyValue(pkDef, inputPkValues),
        e -> new RecordNotFoundException(e.getMessage()));

    LOG.debug("Sequence alias plugin called with pk: " + pkValue.getValuesAsString());

    // check for SQL injection
    for (Entry<String,String> pkCol : pkValue.getValues().entrySet()) {
      if (!pkColValuePassesRegex(pkCol.getKey(), pkCol.getValue())) {
        throw new RecordNotFoundException("Value submitted for column " + pkCol.getKey() + " is not valid.");
      }
    }

    Reference queryRef = new Reference(getQueryFullName());
    Query query = wdkModel.getQuerySet(queryRef.getSetName()).getQuery(queryRef.getElementName());

    if (!(query instanceof SqlQuery)) {
      throw new WdkModelException(queryRef.getTwoPartName() + " is not a valid query. " +
          getClass().getName() + " must specify a SqlQuery reference.");
    }

    String querySql = ((SqlQuery)query).getSql();

    // disallow partition keys macro; we want all partitions and this encourages
    //   model writer to choose an unpartitioned table which is more performant
    if (querySql.contains(SqlQuery.PARTITION_KEYS_MACRO)) {
      throw new WdkModelException("Query '" + queryRef.getTwoPartName() +
          " referenced by " + getClass().getName() + " cannot contain the" +
          " partition keys macro (" + SqlQuery.PARTITION_KEYS_MACRO + ")");
    }

    // Get params of specified query
    Map<String, Param> paramMap = query.getParamMap();
    String sql = switch (paramMap.size()) {

      case 0 ->
        // if zero params, assume attribute query returning all rows with PKs
        "SELECT " + String.join(", ", pkDef.getColumnRefs()) +
        " FROM ( " + querySql + " )" +
        " WHERE " + pkValue.createWhereClause("");

      case 1 ->
        // if one param, assume ID search query with single dataset param;
        paramMap.values().iterator().next().replaceSql(querySql, null,
            // fill in dataset param internal value with a hard-coded select of a single row of PK cols
            "SELECT " + PrimaryKeyDefinition.createSelectClause(pkValue.getValues()) + appDb.getPlatform().getDummyTable());

      default ->
        throw new WdkModelException("Specified query for alias plugin does not contain 0 or 1 parameters.");
    };

    LOG.debug("Supplied full_id passed regex.  Will call the following SQL: " + NL + sql);

    List<Map<String, Object>> foundPks = new SQLRunner(appDb.getDataSource(), sql).executeQuery(rs -> {
      try {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultList result = new SqlResultList(rs);
        while (result.next()) {
          PrimaryKeyValue row = new PrimaryKeyValue(pkDef, result);
          LOG.debug("Found mapped ID: " + row.getValuesAsString());
          rows.add(row.getRawValues());
        }
        return rows;
      }
      catch(WdkModelException e) {
        throw new WdkRuntimeException(e);
      }
    });

    if (foundPks.isEmpty()) {
      throw new RecordNotFoundException("Requested primary key '" + pkValue.getValuesAsString() + "' does not map to any sequence.");
    }

    return foundPks;
    
  }


}
