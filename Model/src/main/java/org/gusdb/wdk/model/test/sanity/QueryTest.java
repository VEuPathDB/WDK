package org.gusdb.wdk.model.test.sanity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.SqlQueryInstance;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.test.sanity.SanityTester.ElementTest;
import org.gusdb.wdk.model.test.sanity.SanityTester.Statistics;
import org.gusdb.wdk.model.user.User;

public class QueryTest implements ElementTest {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(QueryTest.class);

  private static Map<QuerySet, Integer> _attributeQueryRowMap = new HashMap<>();

  private final User _user;
  private final Query _query;
  private final QuerySet _querySet;
  private final String _queryType;
  private final ParamValuesSet _paramValuesSet;

  public QueryTest(User user, QuerySet querySet, Query query, ParamValuesSet paramValuesSet) throws WdkModelException {
    _user = user;
    _query = query;
    _querySet = querySet;
    _queryType = _querySet.getQueryType();
    _paramValuesSet = paramValuesSet;
    // if attribute query set, then get min/max row count values using query set and cache
    if (querySet.getQueryType().equals(QuerySet.TYPE_ATTRIBUTE) &&
        !_attributeQueryRowMap.containsKey(querySet)) {
      _attributeQueryRowMap.put(querySet, getAttributeQueryRowCount(querySet));
    }
  }

  private static int getAttributeQueryRowCount(QuerySet querySet)
      throws WdkModelException {
    // discover number of entities expected in each attribute query
    String testRowCountSql = querySet.getTestRowCountSql();
    if (testRowCountSql == null) return -1;
    ResultSet rs = null;
    try {
      WdkModel model = querySet.getWdkModel();
      DatabaseInstance appdb = model.getAppDb();
      DataSource ds = appdb.getDataSource();
      rs = SqlUtils.executeQuery(ds, testRowCountSql, querySet.getName() + "__sanity-test-row-count");
      if (rs.next())
        return rs.getInt(1);
      else
        throw new WdkModelException("Count query '" + testRowCountSql + "' returned zero rows.");
    }
    catch (SQLException e) {
      throw new WdkModelException("Could not get expected counts for query set", e);
    }
    finally {
      SqlUtils.closeResultSetAndStatement(rs);
    }
  }

  @Override
  public String getTestName() {
    return _queryType.toUpperCase() + " QUERY" +
        (_paramValuesSet == null ? "_TOTAL " : " ") +
        _query.getFullName();
  }

  @Override
  public String getCommand() {
    String params = (_queryType.equals(QuerySet.TYPE_TABLE) ?
        " -params " + _paramValuesSet.getCmdLineString() : "");
    return "wdkQuery -model " + _query.getWdkModel().getProjectId() +
        " -query " + _query.getFullName() + params;
  }

  @Override
  public TestResult test(Statistics stats) throws Exception {

    int minRows = -1;
    int maxRows = -1;
    if (_queryType.equals(QuerySet.TYPE_ATTRIBUTE)) {
      minRows = maxRows = _attributeQueryRowMap.get(_querySet);
    }
    else {
      minRows = _paramValuesSet.getMinRows();
      maxRows = _paramValuesSet.getMaxRows();
    }

    String queryType = (_paramValuesSet == null ? _queryType + "_TOTAL" : _queryType);
    TestResult result = new TestResult();
    
    int sanityMin = minRows;
    int sanityMax = maxRows;
    int count = 0;

    try {
      if (queryType.equals(QuerySet.TYPE_ATTRIBUTE)) {
        count = testAttributeQuery_Count(_user, _query, _paramValuesSet);
        result.restartTimer();
        testAttributeQuery_Time(_user, _query, _paramValuesSet, count);
      }
      else if (queryType.equals(QuerySet.TYPE_TABLE + "_TOTAL")) {
        count = testTableQuery_TotalTime(_user, _query);
      }
      else {
        Query query = _query;
        if (queryType.equals(QuerySet.TYPE_TABLE)) {
          query = RecordClass.prepareQuery(query.getWdkModel(), query,
              _paramValuesSet.getParamNames());
        }
        
        result.restartTimer();
        count = testNonAttributeQuery(_querySet, query, _paramValuesSet);
      }

      result.passed = (count >= sanityMin && count <= sanityMax);
      result.returned = " It returned " + count + " rows. ";

      if (sanityMin != 1 || sanityMax != ParamValuesSet.MAXROWS)
        result.expected = "Expected (" + sanityMin + " - " + sanityMax + ") ";

      return result;
    }
    finally {
      result.stopTimer();
      if (result.passed) {
        stats.queriesPassed++;
      }
      else {
        stats.queriesFailed++;
      }
    }
  }

  private int testNonAttributeQuery(QuerySet querySet, Query query,
      ParamValuesSet paramValuesSet) throws WdkModelException, WdkUserException {

    int count = 0;

    QueryInstance instance = query.makeInstance(_user,
        paramValuesSet.getParamValues(), true, 0,
        new LinkedHashMap<String, String>());
    ResultList rl = null;
    try {
      rl = instance.getResults();
      while (rl.next()) {
        count++;
      }
      return count;
    }
    finally {
      if (rl != null) rl.close();
    }
  }

  private static int testAttributeQuery_Count(User user, Query query, ParamValuesSet paramValuesSet)
      throws SQLException, WdkModelException, WdkUserException {

    // put user id into the param
    Map<String, String> params = new LinkedHashMap<String, String>();

    // since this attribute query is the original copy from model and it doesn't
    // params.put(Utilities.PARAM_USER_ID, Integer.toString(user.getUserId()));

    SqlQueryInstance instance = (SqlQueryInstance) query.makeInstance(user,
        params, true, 0, new LinkedHashMap<String, String>());

    // if (paramValuesSet.getParamValues().size() != 2) {
    // throw new WdkUserException(
    // "missing <defaultTestParamValues> for querySet "
    // + query.getQuerySet().getName());
    // }
    String sql = "select count (*) from (select distinct "
        + paramValuesSet.getNamesAsString() + " from ("
        + instance.getUncachedSql() + ") f1) f2";

    DataSource dataSource = query.getWdkModel().getAppDb().getDataSource();
    ResultSet resultSet = null;
    try {
      resultSet = SqlUtils.executeQuery(dataSource, sql,
        query.getFullName() + "__sanity-test-count");
      if (resultSet.next())
        return resultSet.getInt(1);
      else
        throw new WdkModelException("Count query '" + sql + "' returned zero rows.");
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
    }
  }

  private static void testAttributeQuery_Time(User user, Query query,
      ParamValuesSet paramValuesSet, int count) throws SQLException, WdkModelException, WdkUserException {
    // put user id into the param
    Map<String, String> params = new LinkedHashMap<String, String>();
    // params.put(Utilities.PARAM_USER_ID, Integer.toString(user.getUserId()));

    SqlQueryInstance instance = (SqlQueryInstance) query.makeInstance(user,
        params, true, 0, new LinkedHashMap<String, String>());

    String sql = "select * from (" + instance.getUncachedSql() + ") f "
        + paramValuesSet.getWhereClause();

    DataSource dataSource = query.getWdkModel().getAppDb().getDataSource();
    ResultSet resultSet = null;
    try {
      resultSet = SqlUtils.executeQuery(dataSource, sql,
        query.getFullName() + "__sanity-test-time");
      if (count > 0 && !resultSet.next()) {
        String msg = "no row returned for " + query.getFullName()
            + " using where clause (" + paramValuesSet.getWhereClause() + ")";
        throw new WdkModelException(msg);
      }
      while (resultSet.next()) {} // bring full result over to test speed
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
    }
  }

  private static int testTableQuery_TotalTime(User user, Query query) throws SQLException, WdkModelException, WdkUserException {

    // put user id into the param
    Map<String, String> params = new LinkedHashMap<String, String>();
    // params.put(Utilities.PARAM_USER_ID, Integer.toString(user.getUserId()));

    SqlQueryInstance instance = (SqlQueryInstance) query.makeInstance(user,
        params, true, 0, new LinkedHashMap<String, String>());

    String sql = instance.getUncachedSql();
    DataSource dataSource = query.getWdkModel().getAppDb().getDataSource();
    ResultSet resultSet = null;
    try {
      resultSet = SqlUtils.executeQuery(dataSource, sql,
          query.getFullName() + "__sanity-test-total-time");
      int count = 0;
      while (resultSet.next())
        count++; // bring full result over to test speed
      return count;
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet);
    }
  }
}
