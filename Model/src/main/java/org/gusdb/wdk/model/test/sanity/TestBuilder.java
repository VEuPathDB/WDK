package org.gusdb.wdk.model.test.sanity;

import java.util.List;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.QuerySet.QueryType;
import org.gusdb.wdk.model.query.param.ParamValuesSet;
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

  protected void addQueryTest(List<ElementTest> tests, QuerySet querySet, QueryType queryType,
      Query query, User user, boolean skipWebSvcQueries) throws WdkModelException {
    if (!SanityTester.isTestable(query, skipWebSvcQueries)) return;
    LOG.debug("   Building tests for Query " + query.getName() + " using " + query.getParamValuesSets().size() + " ParamValuesSets");
    int numParamValuesSets = query.getNumParamValuesSets();
    try {
      for (ParamValuesSet paramValuesSet : query.getParamValuesSets()) {
        switch (queryType) {
          case VOCAB:
            tests.add(new VocabQueryTest(user, querySet, query, paramValuesSet));
            break;
          case ATTRIBUTE:
            tests.add(new AttributeQueryTest(user, querySet, query, paramValuesSet));
            break;
          case TABLE:
            tests.add(new TableQueryTest(user, querySet, query, paramValuesSet));
            // perform additional test for table queries
            tests.add(new TableTotalQueryTest(user, querySet, query, paramValuesSet));
          default:
            // TABLE_TOTAL should never be a QuerySet's query type; it exists only for sanity tests.
            // All other query types are not sanity testable
            LOG.debug("QuerySet " + querySet.getName() + " with type " +
                queryType + " is not sanity testable.  Skipping...");
        }
      }
    }
    catch (Exception e) {
      // error while generating param values sets
      LOG.error("Unable to generate paramValuesSets for query " + query.getName(), e);
      // to keep the index correct, add already failed tests for each of the param values sets we expected
      for (int i = 0; i < numParamValuesSets; i++) {
        tests.add(new UncreateableTest(querySet, query, e));
      }
    }
  }
}
