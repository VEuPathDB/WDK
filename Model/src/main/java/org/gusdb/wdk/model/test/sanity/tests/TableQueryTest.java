package org.gusdb.wdk.model.test.sanity.tests;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordClassSet;
import org.gusdb.wdk.model.test.sanity.TestResult;
import org.gusdb.wdk.model.user.User;

public class TableQueryTest extends QueryTest {
  
  public TableQueryTest(User user, QuerySet querySet, Query query, ParamValuesSet paramValuesSet) {
    super(user, querySet, query, paramValuesSet);
  }

  @Override
  protected String getParamString() {
    return "-params " + getParamValuesSet().getCmdLineString();
  }

  @Override
  protected int runQuery(User user, Query query, ParamValuesSet paramValuesSet,
      TestResult result) throws Exception {
    query = RecordClass.prepareQuery(query.getWdkModel(), query, getPkParamNames(query));
    result.restartTimer();
    return super.runQuery(user, query, paramValuesSet, result);
  }

  private String[] getPkParamNames(Query query) throws WdkModelException {
    WdkModel model = query.getWdkModel();
    for (RecordClassSet rcs : model.getAllRecordClassSets()) {
      for (RecordClass rc : rcs.getRecordClasses()) {
        for (String queryFullName : rc.getTableQueries().keySet()) {
          if (query.getFullName().equals(queryFullName)) {
            // found a RecordClass using this table query
            return rc.getPrimaryKeyAttributeField().getColumnRefs();
          }
        }
      }
    }
    throw new WdkModelException("Table query '" + query.getFullName() +
        "' is not referred to by any RecordClass.  This is required so the " +
        "sanity test can discover primary key columns for the RecordClass)");
  }
}
