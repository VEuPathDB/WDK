package org.gusdb.wdk.model.test.sanity;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.ProcessQuery;
import org.gusdb.wdk.model.query.Query;

/**
 * Main class for running the sanity tests, which is a way to test all Queries
 * and RecordClasses in a WDK model to make sure they work as intended and their
 * results fall within an expected range, even over the course of code base
 * development. See the usage() method for parameter information, and see the
 * gusdb.org wiki page for the structure and content of the sanity test.
 * 
 * Created: Mon August 23 12:00:00 2004 EST
 * 
 * @author David Barkan
 * @version $Revision$ $Date: 2005-08-23 12:31:12 -0400 (Tue, 23 Aug
 *          2005) $Author$
 */
public class SanityTester {

  private static final Logger LOG = Logger.getLogger(SanityTester.class);

  private static final boolean USE_CLASSIC_TEST_SETUP = true;

  public interface ElementTest {
    public String getTestName();
    public String getCommand();
    public TestResult test(Statistics stats) throws Exception;
  }

  public static abstract class Statistics {

    protected float _setupDuration = 0;
    protected int _testsCreated = 0;
    protected int _testsRun = 0;

    public abstract void processResult(ElementTest test, TestResult result);
    public abstract String getSummaryLine(TestFilter testFilter);
    public abstract boolean isFailedOverall();

    public void setSetupDuration(float seconds) { _setupDuration = seconds; }
    public void setNumTestsCreated(int count) { _testsCreated = count; }
    public void incrementTestsRun() { _testsRun++; }

    protected static String fmt(float f) {
      return new DecimalFormat("0.00").format(f);
    }
  }

  private final DatabaseInstance _appDb;
  private final boolean _indexOnly;
  private final boolean _failuresOnly;
  private final TestFilter _testFilter;
  private final Statistics _stats;
  private final List<ElementTest> _tests;

  public SanityTester(WdkModel wdkModel, TestFilter testFilter, boolean failuresOnly,
      boolean indexOnly, boolean skipWebSvcQueries, boolean verbose,
      boolean passCountMismatches) throws WdkModelException {
    _appDb = wdkModel.getAppDb();
    _indexOnly = indexOnly;
    _failuresOnly = failuresOnly;
    _testFilter = testFilter;
    TestResult.setVerbose(verbose);
    RangeCountTestUtil.setPassCountMismatches(passCountMismatches);
    long testStart = System.currentTimeMillis();
    TestBuilder testBuilder = (USE_CLASSIC_TEST_SETUP ?
        new ClassicTestBuilder() : new TopDownTestBuilder());
    _stats = testBuilder.getNewStatisticsObj();
    _tests = testBuilder.buildTestSequence(wdkModel, wdkModel.getSystemUser(), skipWebSvcQueries);
    _stats.setSetupDuration((System.currentTimeMillis() - testStart) / 1000F);
    _stats.setNumTestsCreated(_tests.size());
  }

  public List<TestResult> runTests() {
    List<TestResult> results = new ArrayList<>();
    for (int i = 1; i <= _tests.size(); i++) {
      ElementTest element = _tests.get(i-1);

      if (_testFilter.filterOutTest(i)) continue;

      LOG.info(" [test: " + i + "/" + _tests.size() + "] " + element.getTestName());

      if (_indexOnly) continue;

      // performing test
      TestResult result = new TestResult(element);
      try {
        _stats.incrementTestsRun();
        result = element.test(_stats);
      }
      catch (Exception e) {
        result.setReturned("Exception thrown");
        result.setCaughtException(e);
      }
      finally {
        result.stopTimer();
        _stats.processResult(element, result);
      }

      result.setIndex(i);
      results.add(result);

      // dump test result
      if (!result.isPassed() || !_failuresOnly) {
        LOG.info(NL + result.getResultString());
      }
    }

    // check the connection usage
    if (_appDb.getConnectionsCurrentlyOpen() > 0) {
      LOG.error("Detected connections leaks after processing:");
      LOG.error(_appDb.getUnclosedConnectionInfo());
    }
    
    return results;
  }

  public String getSummaryLine() {
    return _stats.getSummaryLine(_testFilter);
  }

  public boolean isFailedOverall() {
    return _stats.isFailedOverall();
  }

  public static String getRerunLine(List<TestResult> results) {
    List<Integer> failedTestIds = new ArrayList<>();
    for (TestResult result : results) {
      if (!result.isPassed()) failedTestIds.add(result.getIndex());
    }
    return "To re-run failures, use filter string '" +
        TestFilter.getFilterString(failedTestIds) + "'.";
  }

  public static boolean isTestable(OptionallyTestable testable) {
    return !testable.getDoNotTest();
  }

  public static boolean isTestable(Query query, boolean skipWebSvcQueries) {
    return (isTestable(query) && isTestable(query.getQuerySet()) &&
            !(skipWebSvcQueries && query instanceof ProcessQuery));
  }
}
