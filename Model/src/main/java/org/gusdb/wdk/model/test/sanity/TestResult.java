package org.gusdb.wdk.model.test.sanity;

public class TestResult {

  public boolean passed = false;
  public String prefix = "***";
  public String status = " FAILED!";
  public long start = System.currentTimeMillis();
  public Long end = start;
  public String returned = "";
  public String expected = "";
  public Exception caughtException = null;

  public void restartTimer() {
    start = System.currentTimeMillis();
  }
  
  public void stopTimer() {
    end = System.currentTimeMillis();
  }
  
  public float getDurationSecs() {
    return (end <= start ? -1L : end - start) / 1000F;
  }

}
