package org.gusdb.wdk.model.test.sanity.tests;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.test.sanity.SanityTester.ElementTest;
import org.gusdb.wdk.model.test.sanity.SanityTester.Statistics;
import org.gusdb.wdk.model.test.sanity.TestResult;

public class UncreateableTest implements ElementTest {

  public static final Logger LOG = Logger.getLogger(UncreateableTest.class);
  
  public static enum UncreateableTestType { QUESTION, QUERY }

  private final String _testName;
  private final Exception _creationException;
  private final UncreateableTestType _type;

  public UncreateableTest(Question question, Exception creationException) {
    _testName = QuestionTest.getTestName(question);
    _creationException = creationException;
    _type = UncreateableTestType.QUESTION;
  }

  public UncreateableTest(QuerySet querySet, Query query, Exception creationException) {
    _testName = QueryTest.getTestName(querySet, query);
    _creationException = creationException;
    _type = UncreateableTestType.QUERY;
  }

  public UncreateableTestType getType() {
    return _type;
  }
  
  @Override
  public String getTestName() {
    return _testName;
  }

  @Override
  public TestResult test(Statistics stats) throws Exception {
    LOG.error("Test unable to initialize", _creationException);
    TestResult result = new TestResult(this);
    result.setPassed(false);
    result.setReturned("Could not be created");
    result.setCaughtException(_creationException);
    result.stopTimer();
    stats.processResult(this, result);
    return result;
  }
  
  @Override
  public String getCommand() {
    return "";
  }

}
