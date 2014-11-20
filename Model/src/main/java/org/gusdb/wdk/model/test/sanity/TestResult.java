package org.gusdb.wdk.model.test.sanity;

import static org.gusdb.fgputil.FormatUtil.NL;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.test.sanity.SanityTester.ElementTest;

public class TestResult {

  private static final String BANNER_LINE_TOP = "vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv";
  private static final String BANNER_LINE_BOT = "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^";

  private ElementTest _test;
  private int _index = -1; // no index specified
  private long _start = System.currentTimeMillis();
  private Long _end = _start;
  private boolean _passed = false;
  private String _expected = "";
  private String _returned = "";
  private Exception _caughtException = null;

  public TestResult(ElementTest test) {
    _test = test;
  }

  public ElementTest getTest() {
    return _test;
  }

  public void restartTimer() {
    _end = _start = System.currentTimeMillis();
  }

  public void stopTimer() {
    _end = System.currentTimeMillis();
  }

  public float getDurationSecs() {
    return (_end <= _start ? 0 : _end - _start) / 1000F;
  }

  public String getResultString() {
    StringBuilder sb = new StringBuilder();
    if (!isPassed()) sb.append(BANNER_LINE_TOP).append(NL);
    sb.append(getShortResultString() + NL + "  [ " + _test.getCommand() + " ]" + NL);
    if (getCaughtException() != null) {
      sb.append(FormatUtil.getStackTrace(getCaughtException()));
    }
    if (!isPassed()) sb.append(BANNER_LINE_BOT).append(NL);
    return sb.toString();
  }

  public String getShortResultString() {
    return "[test: " + getIndex() + "] " + getStatus(_passed) + " " +
        _test.getTestName() + " | " +
        getDurationSecs() + " secs" + " | " +
        getExpected() + " | " +
        getReturned() +
        (getCaughtException() == null ? "" : " | " +
            "Threw " + getCaughtException().getClass().getSimpleName());
  }
  
  private static String getStatus(boolean passed) {
    return passed ? "   PASSED   " : "***FAILED***";
  }

  public void setPassed(boolean passed)       { _passed = passed; }
  public void setIndex(int index)             { _index = index; }
  public void setExpected(String expected)    { _expected = expected; }
  public void setReturned(String returned)    { _returned = returned; }
  public void setCaughtException(Exception e) { _caughtException = e; }

  public boolean isPassed()              { return _passed; }
  public int getIndex()                  { return _index; }
  private String getExpected()           { return _expected; }
  private String getReturned()           { return _returned; }
  private Exception getCaughtException() { return _caughtException; }

}
