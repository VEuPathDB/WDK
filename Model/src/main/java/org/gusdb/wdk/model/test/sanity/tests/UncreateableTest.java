package org.gusdb.wdk.model.test.sanity.tests;

import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.test.sanity.SanityTester.ElementTest;
import org.gusdb.wdk.model.test.sanity.SanityTester.Statistics;
import org.gusdb.wdk.model.test.sanity.TestResult;

public class UncreateableTest implements ElementTest {

  private interface StatUpdater {
    public void updateFailureStat(Statistics stats);
  }

  private final String _testName;
  private final Exception _creationException;
  private final StatUpdater _statUpdater;

  public UncreateableTest(Question question, Exception creationException) {
    _testName = QuestionTest.getTestName(question);
    _creationException = creationException;
    _statUpdater = new StatUpdater() {
      @Override public void updateFailureStat(Statistics stats) {
        stats.questionsFailed++;
      }};
  }

  public UncreateableTest(QuerySet querySet, Query query, Exception creationException) {
    _testName = QueryTest.getTestName(querySet, query);
    _creationException = creationException;
    _statUpdater = new StatUpdater() {
      @Override public void updateFailureStat(Statistics stats) {
        stats.queriesFailed++;
      }};
  }

  @Override
  public String getTestName() {
    return _testName;
  }

  @Override
  public TestResult test(Statistics stats) throws Exception {
    _statUpdater.updateFailureStat(stats);
    TestResult result = new TestResult(this);
    result.setPassed(false);
    result.setReturned("Could not be created");
    result.setCaughtException(_creationException);
    result.stopTimer();
    return result;
  }
  
  @Override
  public String getCommand() {
    return "";
  }

}
