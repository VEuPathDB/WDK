package org.gusdb.wdk.model.test.sanity.tests;

import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.SqlQueryInstance;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.test.sanity.TestResult;
import org.gusdb.wdk.model.user.User;

public class TableTotalQueryTest extends QueryTest {

  public TableTotalQueryTest(User user, QuerySet querySet, Query query, ParamValuesSet paramValuesSet) {
    super(user, querySet, query, paramValuesSet);
  }

  @Override
  protected int runQuery(User user, Query query, ParamValuesSet paramValuesSet,
      TestResult result) throws Exception {

    Map<String, String> params = new LinkedHashMap<String, String>();
    // put user id into the param
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
