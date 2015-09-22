package org.gusdb.wdk.model.test.sanity;

public class RangeCountTestUtil {

  private static boolean _passCountMismatches = false;

  public static void setPassCountMismatches(boolean passCountMismatches) {
    _passCountMismatches = passCountMismatches;
  }

  /**
   * Performs the following logic to determine whether a test that compares a
   * result count against min and max values should fail, or just throw a
   * warning:
   * 
   * 
   * @param count result count
   * @param min minimum passible value
   * @param max maximum passible value
   * @param failureOnCountMismatch primary indicator of whether to fail; if true,
   * test will fail on mismatch, else test will fail on mismatch if static
   * passCountMismatches value is false
   * @param result result to which logic is applied
   */
  public static void applyCountAssessment(int count, int min, int max,
      boolean failureOnCountMismatch, TestResult result) {

    boolean countMismatch = (count < min || count > max);
    result.setShowMismatchWarning(countMismatch);

    if (failureOnCountMismatch || !_passCountMismatches) {
      // then fail tests for mismatches
      result.setPassed(!countMismatch);
    }
    else {
      // don't fail for mismatch (i.e. always pass)
      result.setPassed(true);
    }
  }
}
