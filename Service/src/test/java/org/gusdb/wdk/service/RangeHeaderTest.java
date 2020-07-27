package org.gusdb.wdk.service;

import org.gusdb.fgputil.Range;
import org.junit.Test;

public class RangeHeaderTest {

  private static final String[] TEST_CASES = {
    "bytes=2651586560-2652372991"
  };

  @Test
  public void testRanges() {
    for (String testCase : TEST_CASES) {
      Range<Long> range = FileRanges.parseRangeHeaderValue(testCase);
      System.out.println(range);
    }
  }
}
