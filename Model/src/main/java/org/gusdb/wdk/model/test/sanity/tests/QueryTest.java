package org.gusdb.wdk.model.test.sanity.tests;

import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
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
    return _querySet.getQueryTypeEnum() + " QUERY " + _query.getFullName();
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

  @Override
  public TestResult test(Statistics stats) throws Exception {

    int sanityMin = getMinRows();
    int sanityMax = getMaxRows();

    TestResult result = new TestResult();
    
    try {
      int count = runQuery(_user, _query, _paramValuesSet, result);
      result.passed = (count >= sanityMin && count <= sanityMax);
      result.returned = " It returned " + count + " rows. ";

      if (sanityMin != 1 || sanityMax != ParamValuesSet.MAXROWS) {
        result.expected = "Expected (" + sanityMin + " - " + sanityMax + ") ";
      }

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

  protected int runQuery(User user, Query query, ParamValuesSet paramValuesSet,
      TestResult result) throws Exception {
    int count = 0;
    QueryInstance instance = query.makeInstance(_user,
        paramValuesSet.getParamValues(), true, 0,
        new LinkedHashMap<String, String>());
    ResultList resultList = null;
    try {
      resultList = instance.getResults();
      while (resultList.next()) {
        count++;
      }
      return count;
    }
    finally {
      if (resultList != null) resultList.close();
    }
  }
}
