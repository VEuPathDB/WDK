package org.gusdb.wdk.model.test.sanity.tests;

import java.sql.ResultSet;

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
  protected int runQuery(User user, Query query, ParamValuesSet paramValuesSet, TestResult result) throws Exception {

    SqlQueryInstance instance = (SqlQueryInstance) query.makeInstance(user,
        getValidatedParams(user, query, paramValuesSet.getParamValues()));

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
      SqlUtils.closeResultSetAndStatement(resultSet, null);
    }
  }
}
