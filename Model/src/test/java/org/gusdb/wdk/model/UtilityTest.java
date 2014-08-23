/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author xingao
 * 
 */
public class UtilityTest {

    @Test
    public void testToArray() {
        Random random = UnitTestHelper.getRandom();
        String[] expected = new String[] { Integer.toString(random.nextInt()),
                Integer.toString(random.nextInt()),
                Integer.toString(random.nextInt()) };

        // comma separated list
        String input = expected[0] + "," + expected[1] + "," + expected[2];
        String[] actual = Utilities.toArray(input);
        Assert.assertArrayEquals(expected, actual);

        // space separated list
        input = expected[0] + " " + expected[1] + " " + expected[2];
        actual = Utilities.toArray(input);
        Assert.assertArrayEquals(expected, actual);

        // tab separated list
        input = expected[0] + "\t" + expected[1] + "\t" + expected[2];
        actual = Utilities.toArray(input);
        Assert.assertArrayEquals(expected, actual);

        // new line list
        String newline = System.getProperty("line.separator");
        input = expected[0] + newline + expected[1] + newline + expected[2];
        actual = Utilities.toArray(input);
        Assert.assertArrayEquals(expected, actual);

        // comma-space separated list
        input = expected[0] + ", " + expected[1] + ", " + expected[2];
        actual = Utilities.toArray(input);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testFromArray() {
        Random random = UnitTestHelper.getRandom();
        String[] expected = new String[] { Integer.toString(random.nextInt()),
                Integer.toString(random.nextInt()),
                Integer.toString(random.nextInt()),
                Integer.toString(random.nextInt()) };

        String value = Utilities.fromArray(expected);
        String[] actual = Utilities.toArray(value);
        Assert.assertArrayEquals(expected, actual);
    }
}
