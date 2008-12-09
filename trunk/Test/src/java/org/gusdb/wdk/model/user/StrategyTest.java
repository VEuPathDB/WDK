/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.gusdb.wdk.model.UnitTestHelper;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xingao
 * 
 */
public class StrategyTest {

    private User user;

    public StrategyTest() throws Exception {
        user = UnitTestHelper.getRegisteredUser();
    }

    @Test
    public void testCreateStrategy() throws Exception {
        int strategyCount = user.getStrategyCount();

        Step step = UnitTestHelper.createNormalStep(user);
        Strategy strategy = user.createStrategy(step, false);

        Assert.assertEquals("strategy count", strategyCount + 1,
                user.getStrategyCount());

        Step root = strategy.getLatestStep();
        StepTest.compareStep(step, root);
    }

    @Test
    public void testCreateStrategyWithName() throws Exception {
        String strategyName = "My Strategy";

        Step step = UnitTestHelper.createNormalStep(user);
        Strategy strategy = user.createStrategy(step, strategyName, false);

        Assert.assertEquals("strategy name", strategyName, strategy.getName());
    }

    @Test
    public void testGetStrategy() throws Exception {
        Step step = UnitTestHelper.createNormalStep(user);
        Strategy strategy = user.createStrategy(step, false);

        Strategy loadedStrategy = user.getStrategy(strategy.getDisplayId());
        compareStrategy(strategy, loadedStrategy);
    }

    @Test
    public void testGetStrategies() throws Exception {
        Step step = UnitTestHelper.createNormalStep(user);
        Strategy strategy = user.createStrategy(step, false);
        Strategy[] strategies = user.getStrategies();

        boolean hasStrategy = false;
        for (Strategy loadedStrategy : strategies) {
            if (strategy.getInternalId() == loadedStrategy.getInternalId()) {
                compareStrategy(strategy, loadedStrategy);

                hasStrategy = true;
                break;
            }
        }
        Assert.assertTrue("strategy not found", hasStrategy);
    }

    @Test
    public void testDeleteStrategy() throws Exception {
        Step step = UnitTestHelper.createNormalStep(user);
        Strategy strategy = user.createStrategy(step, false);

        int count = user.getStrategyCount();
        user.deleteStrategy(strategy.getDisplayId());

        Assert.assertEquals("strategy count", count - 1,
                user.getStrategyCount());

        // get a delete strategy, should raise a WdkUserException
        try {
            user.getStrategy(strategy.getDisplayId());
            Assert.assertTrue("strategy not deleted", false);
        } catch (WdkUserException ex) {
            // do nothing, expected.
        }

    }

    @Test
    public void testDeleteStrategies() throws Exception {
        Step step = UnitTestHelper.createNormalStep(user);
        user.createStrategy(step, false);

        user.deleteStrategies();

        Assert.assertEquals("strategy count", 0, user.getStrategyCount());
    }

    private void compareStrategy(Strategy expected, Strategy actual)
            throws NoSuchAlgorithmException, WdkModelException, JSONException,
            WdkUserException, SQLException {
        Assert.assertEquals("internal strategy id", expected.getInternalId(),
                actual.getInternalId());
        Assert.assertEquals("strategy id", expected.getDisplayId(),
                actual.getDisplayId());
        Assert.assertEquals("strategy length", expected.getLength(),
                actual.getLength());
        Assert.assertEquals("strategy name", expected.getName(),
                actual.getName());
        Assert.assertEquals("strategy type", expected.getType(),
                actual.getType());

        // compare the steps
        Assert.assertEquals("steps count", expected.getAllSteps().length,
                actual.getAllSteps().length);
        StepTest.compareStep(expected.getLatestStep(), actual.getLatestStep());
    }
}
