package org.gusdb.wdk.model.test.sanity;

import java.util.ArrayList;
import java.util.List;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.test.sanity.SanityTester.ElementTest;
import org.gusdb.wdk.model.test.sanity.SanityTester.Statistics;
import org.gusdb.wdk.model.test.sanity.SanityTester.TestBuilder;
import org.gusdb.wdk.model.user.User;

public class TopDownTestBuilder implements TestBuilder {

  public static class QueryStatistics extends Statistics {

    @Override
    public void processResult(ElementTest test, TestResult result) { }

    @Override
    public String getSummaryLine(TestFilter testFilter) {
      return "No tests run";
    }

    @Override
    public boolean isFailedOverall() {
      return true;
    }
  }

  @Override
  public Statistics getNewStatisticsObj() {
    return new QueryStatistics();
  }

  @Override
  public List<ElementTest> buildTestSequence(WdkModel wdkModel, User user, boolean skipWebSvcQueries)
      throws WdkModelException {
    return new ArrayList<>();
  }

}
