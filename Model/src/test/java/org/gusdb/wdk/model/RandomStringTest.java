package org.gusdb.wdk.model;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class RandomStringTest {

  @Test
  public void doTest() {
    int randomStringLength = 10;
    int numTrials = 1000;

    Set<String> allStrs = new HashSet<>();
    for (int i = 0; i < numTrials; i++) {
      String str = Utilities.randomAlphaNumericString(randomStringLength);
      //System.out.println(i + " " + str);
      Assert.assertFalse(allStrs.contains(str));
      allStrs.add(str);
      Assert.assertEquals(randomStringLength, str.length());
      for (int j = 0; j < str.length(); j++) {
        Assert.assertTrue(Utilities.RANDOM_CHARS.contains(str.substring(j, j + 1)));
      }
    }
  }
}
