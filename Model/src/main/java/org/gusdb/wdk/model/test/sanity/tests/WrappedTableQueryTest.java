package org.gusdb.wdk.model.test.sanity.tests;

import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.test.sanity.TestResult;
import org.gusdb.wdk.model.user.User;

public class WrappedTableQueryTest extends TableQueryTest {

  public WrappedTableQueryTest(User user, QuerySet querySet, Query query, ParamValuesSet paramValuesSet) {
    super(user, querySet, query, paramValuesSet);
  }

  @Override
  protected int runQuery(User user, Query query, ParamValuesSet paramValuesSet,
      TestResult result) throws Exception {
    query = RecordClass.prepareQuery(query.getWdkModel(), query,
        paramValuesSet.getParamNames());
    result.restartTimer();
    return super.runQuery(user, query, paramValuesSet, result);
  }

}
