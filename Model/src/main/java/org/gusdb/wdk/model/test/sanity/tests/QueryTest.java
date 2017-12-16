package org.gusdb.wdk.model.test.sanity.tests;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.query.param.values.ValidStableValuesFactory;
import org.gusdb.wdk.model.query.param.values.ValidStableValuesFactory.CompleteValidStableValues;
import org.gusdb.wdk.model.query.param.values.WriteableStableValues;
import org.gusdb.wdk.model.test.sanity.RangeCountTestUtil;
import org.gusdb.wdk.model.test.sanity.SanityTester.ElementTest;
import org.gusdb.wdk.model.test.sanity.SanityTester.Statistics;
import org.gusdb.wdk.model.test.sanity.TestResult;
import org.gusdb.wdk.model.user.User;

public class QueryTest implements ElementTest {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(QueryTest.class);

  private final User _user;
  private final Query _query;
  private final QuerySet _querySet;
  private final ParamValuesSet _paramValuesSet;
  
  protected QueryTest(User user, QuerySet querySet, Query query, ParamValuesSet paramValuesSet) {
    _user = user;
    _query = query;
    _querySet = querySet;
    _paramValuesSet = paramValuesSet;
  }

  @Override
  public String getTestName() {
    return getTestName(_querySet, _query);
  }

  public static String getTestName(QuerySet querySet, Query query) {
    return querySet.getQueryTypeEnum() + " QUERY " + query.getFullName();
  }

  @Override
  public String getCommand() {
    String paramString = getParamString();
    return "wdkQuery -model " + _query.getWdkModel().getProjectId() + " -query " +
        _query.getFullName() + (paramString.isEmpty() ? "" : " ") + paramString;
  }

  protected String getParamString() {
    return "";
  }

  protected int getMinRows() {
    return getParamValuesSet().getMinRows();
  }

  protected int getMaxRows() {
    return getParamValuesSet().getMaxRows();
  }

  protected QuerySet getQuerySet() {
    return _querySet;
  }

  protected ParamValuesSet getParamValuesSet() {
    return _paramValuesSet;
  }

  protected boolean isFailureOnCountMismatch() {
    return false;
  }

  @Override
  public final TestResult test(Statistics stats) throws Exception {
    int sanityMin = getMinRows();
    int sanityMax = getMaxRows();
    TestResult result = new TestResult(this);
    result.setExpected("Expect [" + sanityMin + " - " + sanityMax + "] rows" +
        ((sanityMin != 1 || sanityMax != ParamValuesSet.MAXROWS) ? "" : " (default)"));
    int count = runQuery(_user, _query, _paramValuesSet, result);
    result.setReturned(count + " rows returned");
    RangeCountTestUtil.applyCountAssessment(count, sanityMin, sanityMax,
        isFailureOnCountMismatch(), result);
    return result;
  }

  protected static CompleteValidStableValues getValidatedParams(User user, Query query,
      Map<String, String> paramValues) throws WdkUserException, WdkModelException {
    return ValidStableValuesFactory.createFromCompleteValues(user,
            new WriteableStableValues(query, paramValues), true);
  }

  protected static CompleteValidStableValues getValidatedEmptyParams(User user, Query query)
      throws WdkUserException, WdkModelException {
    return getValidatedParams(user, query, new HashMap<>());
  }

  /**
   * Tests query
   * 
   * @param user user to run query as
   * @param query query to run
   * @param paramValuesSet param values to test
   * @param result result to populate
   * @return number of rows returned
   * @throws Exception if something goes wrong
   */
  protected int runQuery(User user, Query query, ParamValuesSet paramValuesSet,
      TestResult result) throws Exception {
    int count = 0;
    QueryInstance<?> instance = query.makeInstance(user, getValidatedParams(user, query, paramValuesSet.getParamValues()));
    try (ResultList resultList = instance.getResults()) {
      while (resultList.next()) {
        count++;
      }
      return count;
    }
  }
}
