package org.gusdb.wdk.model.query.param;

import junit.framework.Assert;

import org.junit.Test;

public class TimestampParamTest {

  private final TimestampParam param;

  public TimestampParamTest() {
    param = new TimestampParam();
  }

  @Test
  public void testInterval() {
    testInterval(1);
    testInterval(10);
    testInterval(30);
  }

  private void testInterval(int interval) {
    param.setInterval(interval);

    try {
      // wait till the beginning of a second
      while (true) {
        long fraction = System.currentTimeMillis() % 1000;
        if (fraction < 250)
          break;
        Thread.sleep(1000 - fraction);
      }

      String value = param.getDefault();
      // duration is half of the interval, to make sure the first check returns the same value, but the second
      // check returns a different value.
      long duration = interval * 1000 / 2;
      Thread.sleep(duration);
      Assert.assertEquals(value, param.getDefault());

      Thread.sleep(duration);
      Assert.assertFalse(param.getDefault().equals(value));
    }
    catch (InterruptedException ex) { // do nothing
    }
  }

}
