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

        Strategy loadedStrategy = user.getStrategy(strategy.getStrategyId());
        compareStrategy(strategy, loadedStrategy);
    }

    @Test
    public void testGetStrategies() {

    }

    @Test
    public void testDeleteStrategy() {

    }

    @Test
    public void testDeleteStrategies() {

    }

    private void compareStrategy(Strategy expected, Strategy actual)
            throws NoSuchAlgorithmException, WdkModelException, JSONException,
            WdkUserException, SQLException {
        Assert.assertEquals("internal strategy id", expected.getInternalId(),
                actual.getInternalId());
        Assert.assertEquals("strategy id", expected.getStrategyId(),
                actual.getStrategyId());
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
