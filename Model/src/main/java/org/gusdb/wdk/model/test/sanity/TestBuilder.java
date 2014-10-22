package org.gusdb.wdk.model.test.sanity;

import java.util.List;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.QuerySet.QueryType;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
import org.gusdb.wdk.model.test.ParamValuesFactory;
import org.gusdb.wdk.model.test.ParamValuesFactory.ValuesSetWrapper;
import org.gusdb.wdk.model.test.sanity.SanityTester.ElementTest;
import org.gusdb.wdk.model.test.sanity.SanityTester.Statistics;
import org.gusdb.wdk.model.test.sanity.tests.AttributeQueryTest;
import org.gusdb.wdk.model.test.sanity.tests.TableQueryTest;
import org.gusdb.wdk.model.test.sanity.tests.TableTotalQueryTest;
import org.gusdb.wdk.model.test.sanity.tests.UncreateableTest;
import org.gusdb.wdk.model.test.sanity.tests.VocabQueryTest;
import org.gusdb.wdk.model.user.User;

public abstract class TestBuilder {

  private static final Logger LOG = Logger.getLogger(TestBuilder.class);

  public abstract Statistics getNewStatisticsObj();

  public abstract List<ElementTest> buildTestSequence(WdkModel wdkModel, User user,
      boolean skipWebSvcQueries) throws WdkModelException;

  public static abstract class QueryTestBuilder {

    protected User user;
    protected QuerySet querySet;
    protected Query query;
    private ValuesSetWrapper valuesSetWrapper;

    public abstract ElementTest getTypedQueryTest(ParamValuesSet paramValuesSet) throws WdkModelException;

    public QueryTestBuilder(User user, QuerySet querySet, Query query, ValuesSetWrapper valuesSetWrapper) {
      this.user = user;
      this.querySet = querySet;
      this.query = query;
      this.valuesSetWrapper = valuesSetWrapper;
    }

    public ElementTest getQueryTest() {
      if (valuesSetWrapper.isCreated()) {
        try {
          return getTypedQueryTest(valuesSetWrapper.getValuesSet());
        }
        catch (Exception e) {
          return new UncreateableTest(querySet, query, e);
        }
      }
      else {
        return new UncreateableTest(querySet, query, valuesSetWrapper.getException());
      }
    }
  }

  protected void addQueryTest(List<ElementTest> tests, QuerySet querySet, QueryType queryType,
      Query query, User user, boolean skipWebSvcQueries) {
    if (!SanityTester.isTestable(query, skipWebSvcQueries)) return;
    LOG.debug("   Building tests for Query " + query.getName());
    for (ValuesSetWrapper valuesSetWrapper : ParamValuesFactory.getValuesSetsNoError(user, query)) {
      switch (queryType) {
        case VOCAB:
          tests.add(new QueryTestBuilder(user, querySet, query, valuesSetWrapper) {
            @Override public ElementTest getTypedQueryTest(ParamValuesSet paramValuesSet) throws WdkModelException {
              return new VocabQueryTest(user, querySet, query, paramValuesSet);
            }}.getQueryTest());
          break;
        case ATTRIBUTE:
          tests.add(new QueryTestBuilder(user, querySet, query, valuesSetWrapper) {
            @Override public ElementTest getTypedQueryTest(ParamValuesSet paramValuesSet) throws WdkModelException {
              return new AttributeQueryTest(user, querySet, query, paramValuesSet);
            }}.getQueryTest());
          break;
        case TABLE:
          tests.add(new QueryTestBuilder(user, querySet, query, valuesSetWrapper) {
            @Override public ElementTest getTypedQueryTest(ParamValuesSet paramValuesSet) throws WdkModelException {
              return new TableQueryTest(user, querySet, query, paramValuesSet);
            }}.getQueryTest());
          // perform additional test for table queries
          tests.add(new QueryTestBuilder(user, querySet, query, valuesSetWrapper) {
            @Override public ElementTest getTypedQueryTest(ParamValuesSet paramValuesSet) throws WdkModelException {
              return new TableTotalQueryTest(user, querySet, query, paramValuesSet);
            }}.getQueryTest());
          break;
        default:
          // TABLE_TOTAL should never be a QuerySet's query type; it exists only for sanity tests.
          // All other query types are not sanity testable
          LOG.debug("QuerySet " + querySet.getName() + " with type " +
              queryType + " is not sanity testable.  Skipping...");
      }
    }
  }
}
