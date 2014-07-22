/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.UnitTestHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xingao
 * 
 */
public class MergeUserTest {

    @Test
    public void testMergeEmptyUser() throws Exception {
        User guest = UnitTestHelper.getGuest();
        User registeredUser = UnitTestHelper.getRegisteredUser();

        registeredUser.mergeUser(guest);
    }

    @Test
    public void testMergeUserWithSimpleStep() throws Exception {
        User guest = UnitTestHelper.getGuest();
        UnitTestHelper.createNormalStep(guest);

        User registeredUser = UnitTestHelper.getRegisteredUser();
        int count = guest.getStepCount() + registeredUser.getStepCount();

        registeredUser.mergeUser(guest);
        Assert.assertEquals(count, registeredUser.getStepCount());
    }

    @Test
    public void testMergeUserWithCombinedStep() throws Exception {
        User guest = UnitTestHelper.getGuest();
        Step step1 = UnitTestHelper.createNormalStep(guest);
        Step step2 = UnitTestHelper.createNormalStep(guest);
        guest.createBooleanStep(step1, step2, "OR", false, null);

        User registeredUser = UnitTestHelper.getRegisteredUser();
        int count = guest.getStepCount() + registeredUser.getStepCount();

        registeredUser.mergeUser(guest);
        Assert.assertEquals(count, registeredUser.getStepCount());
    }

    @Test
    public void testMergeUserWithSimpleUnsavedStrategy() throws Exception {
        User guest = UnitTestHelper.getGuest();
        Step step = UnitTestHelper.createNormalStep(guest);
        guest.createStrategy(step, false);

        User registeredUser = UnitTestHelper.getRegisteredUser();
        int count = guest.getStrategyCount()
                + registeredUser.getStrategyCount();
        int unsavedCount = countStrategies(registeredUser.getUnsavedStrategiesByCategory());

        registeredUser.mergeUser(guest);

        int newCount = countStrategies(registeredUser.getUnsavedStrategiesByCategory());
        Assert.assertEquals(count, registeredUser.getStrategyCount());
        Assert.assertEquals(unsavedCount + 1, newCount);
    }

    @Test
    public void testMergeUserWithMultipleStrategies() throws Exception {
        User guest = UnitTestHelper.getGuest();
        guest.createStrategy(UnitTestHelper.createNormalStep(guest), false);
        guest.createStrategy(UnitTestHelper.createNormalStep(guest), false);

        User registeredUser = UnitTestHelper.getRegisteredUser();
        int count = guest.getStrategyCount()
                + registeredUser.getStrategyCount();

        registeredUser.mergeUser(guest);

        Assert.assertEquals(count, registeredUser.getStrategyCount());
    }

    @Test
    public void testMergeUserWithComplexStrategies() throws Exception {
        User guest = UnitTestHelper.getGuest();
        Step step1 = UnitTestHelper.createNormalStep(guest);
        Strategy strategy = guest.createStrategy(step1, false);

        Step step2 = UnitTestHelper.createNormalStep(guest);
        Step boolean2 = guest.createBooleanStep(step1, step2, "OR", false, null);
        strategy.addStep(step1.getStepId(), boolean2);

        Step step3 = UnitTestHelper.createNormalStep(guest);
        Step boolean3 = guest.createBooleanStep(boolean2, step3, "OR", false,
                null);
        strategy.addStep(boolean2.getStepId(), boolean3);
        strategy.update(true);

        User registeredUser = UnitTestHelper.getRegisteredUser();
        int count = guest.getStrategyCount()
                + registeredUser.getStrategyCount();

        registeredUser.mergeUser(guest);

        Assert.assertEquals(count, registeredUser.getStrategyCount());
    }

    @Test
    public void testMergeUserWithStrategiesOfSameName() throws Exception {
        String existName = "Name" + UnitTestHelper.getRandom().nextInt();
        User guest = UnitTestHelper.getGuest();
        Step step = UnitTestHelper.createNormalStep(guest);
        guest.createStrategy(step, existName, false);

        User registeredUser = UnitTestHelper.getRegisteredUser();
        Step newStep = UnitTestHelper.createNormalStep(registeredUser);
        Strategy expected = registeredUser.createStrategy(newStep, existName,
                false);

        int count = registeredUser.getStrategyCount()
                + guest.getStrategyCount();

        registeredUser.mergeUser(guest);

        Assert.assertEquals(count, registeredUser.getStrategyCount());

        Strategy actual = registeredUser.getStrategy(expected.getStrategyId());
        StrategyTest.compareStrategy(expected, actual);
    }

    @Test
    public void testMergeUserWithSavedStrategiesOfSameName() throws Exception {
        String existName = "Name" + UnitTestHelper.getRandom().nextInt();
        User guest = UnitTestHelper.getGuest();
        Step step = UnitTestHelper.createNormalStep(guest);
        guest.createStrategy(step, existName, false);

        User registeredUser = UnitTestHelper.getRegisteredUser();
        Step newStep = UnitTestHelper.createNormalStep(registeredUser);
        Strategy expected = registeredUser.createStrategy(newStep, existName,
                true);

        int count = registeredUser.getStrategyCount()
                + guest.getStrategyCount();

        registeredUser.mergeUser(guest);

        Assert.assertEquals(count, registeredUser.getStrategyCount());

        Strategy actual = registeredUser.getStrategy(expected.getStrategyId());
        StrategyTest.compareStrategy(expected, actual);
    }

    private int countStrategies(Map<String, List<Strategy>> strategies) {
        int count = 0;
        for (List<Strategy> list : strategies.values()) {
            count += list.size();
        }
        return count;
    }
}
