package org.gusdb.wdk.model.test.sanity.tests;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.SqlQueryInstance;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.test.sanity.TestResult;
import org.gusdb.wdk.model.user.User;

public class AttributeQueryTest extends QueryTest {

  private static Map<QuerySet, Integer> _attributeQueryRowMap = new HashMap<>();

  public AttributeQueryTest(User user, QuerySet querySet, Query query, ParamValuesSet paramValuesSet)
      throws WdkModelException {
    super(user, querySet, query, paramValuesSet);
    // if attribute query set, then get min/max row count values using query set and cache
    if (!_attributeQueryRowMap.containsKey(querySet)) {
      _attributeQueryRowMap.put(querySet, getAttributeQueryRowCount(querySet));
    }
  }

  @Override
  protected int getMinRows() {
    return _attributeQueryRowMap.get(getQuerySet());
  }

  @Override
  protected int getMaxRows() {
    return _attributeQueryRowMap.get(getQuerySet());
  }

  @Override
  protected boolean isFailureOnCountMismatch() {
    return true;
  }

  @Override
  protected int runQuery(User user, Query query, ParamValuesSet paramValuesSet, TestResult result) throws WdkModelException, WdkUserException, SQLException {
    int count = testAttributeQueryCount(user, query, paramValuesSet);
    result.restartTimer();
    testAttributeQueryTime(user, query, paramValuesSet, count);
    return count;
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

  private static int testAttributeQueryCount(User user, Query query, ParamValuesSet paramValuesSet)
      throws SQLException, WdkModelException, WdkUserException {

    Map<String, String> params = new LinkedHashMap<String, String>();

    SqlQueryInstance instance = (SqlQueryInstance) query.makeInstance(user,
        params, true, 0, new LinkedHashMap<String, String>());

    String sql = "select count (*) from" +
        "(select distinct * from (" + instance.getUncachedSql() + "))";

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

  private static void testAttributeQueryTime(User user, Query query,
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
}
