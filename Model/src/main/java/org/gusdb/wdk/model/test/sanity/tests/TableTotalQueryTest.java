package org.gusdb.wdk.model.test.sanity.tests;

import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.SqlQueryInstance;
import org.gusdb.wdk.model.query.param.ParamStableValues;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.query.param.ValidatedParamStableValues;
import org.gusdb.wdk.model.test.sanity.TestResult;
import org.gusdb.wdk.model.user.User;

public class TableTotalQueryTest extends QueryTest {

  public TableTotalQueryTest(User user, QuerySet querySet, Query query, ParamValuesSet paramValuesSet) {
    super(user, querySet, query, paramValuesSet);
  }

  //TODO - CWL Verify
  @Override
  protected int runQuery(User user, Query query, ParamValuesSet paramValuesSet,
      TestResult result) throws Exception {

    Map<String, String> params = new LinkedHashMap<String, String>();

    ValidatedParamStableValues validatedParamStableValues =
        ValidatedParamStableValues.createFromCompleteValues(user, new ParamStableValues(query, params));
    SqlQueryInstance instance = (SqlQueryInstance) query.makeInstance(user,
        validatedParamStableValues, true, 0, new LinkedHashMap<String, String>());

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
