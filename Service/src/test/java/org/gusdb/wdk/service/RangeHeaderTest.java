package org.gusdb.wdk.service;

import org.gusdb.wdk.service.FileRanges.ByteRangeInformation;
import org.junit.Assert;
import org.junit.Test;

public class RangeHeaderTest {

  private static final String[] TEST_CASES = {
    "bytes=2651586560-2652372991"
  };

  @Test
  public void testRanges() {
    for (String testCase : TEST_CASES) {
      ByteRangeInformation range = FileRanges.parseRangeHeaderValue(testCase);
      Assert.assertTrue(range.isRangeHeaderSubmitted());
      Assert.assertEquals(2651586560L, range.getDesiredRange().getBegin().longValue());
      Assert.assertEquals(2652372991L, range.getDesiredRange().getEnd().longValue());
      System.out.println(range);
    }
  }
}
