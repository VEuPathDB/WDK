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

    registeredUser.getSession().mergeUser(guest);
  }

  @Test
  public void testMergeUserWithSimpleStep() throws Exception {
    StepFactory stepFactory = UnitTestHelper.getModel().getStepFactory();
    User guest = UnitTestHelper.getGuest();
    UnitTestHelper.createNormalStep(guest);

    User registeredUser = UnitTestHelper.getRegisteredUser();
    int count = stepFactory.getStepCount(guest) + stepFactory.getStepCount(registeredUser);

    registeredUser.getSession().mergeUser(guest);
    Assert.assertEquals(count, stepFactory.getStepCount(registeredUser));
  }

  @Test
  public void testMergeUserWithCombinedStep() throws Exception {
    StepFactory stepFactory = UnitTestHelper.getModel().getStepFactory();
    User guest = UnitTestHelper.getGuest();
    Step step1 = UnitTestHelper.createNormalStep(guest);
    Step step2 = UnitTestHelper.createNormalStep(guest);
    StepUtilities.createBooleanStep(guest, step1.getStrategyId(), step1, step2, "OR", null);

    User registeredUser = UnitTestHelper.getRegisteredUser();
    int count = stepFactory.getStepCount(guest) + stepFactory.getStepCount(registeredUser);

    registeredUser.getSession().mergeUser(guest);
    Assert.assertEquals(count, stepFactory.getStepCount(registeredUser));
  }

  @Test
  public void testMergeUserWithSimpleUnsavedStrategy() throws Exception {
    StepFactory stepFactory = UnitTestHelper.getModel().getStepFactory();
    User guest = UnitTestHelper.getGuest();
    Step step = UnitTestHelper.createNormalStep(guest);
    StepUtilities.createStrategy(step, false);

    User registeredUser = UnitTestHelper.getRegisteredUser();
    int count = stepFactory.getStrategyCount(guest) + stepFactory.getStrategyCount(registeredUser);
    int unsavedCount = countStrategies(StepUtilities.getUnsavedStrategiesByCategory(registeredUser));

    registeredUser.getSession().mergeUser(guest);

    int newCount = countStrategies(StepUtilities.getUnsavedStrategiesByCategory(registeredUser));
    Assert.assertEquals(count, stepFactory.getStrategyCount(registeredUser));
    Assert.assertEquals(unsavedCount + 1, newCount);
  }

  @Test
  public void testMergeUserWithMultipleStrategies() throws Exception {
    StepFactory stepFactory = UnitTestHelper.getModel().getStepFactory();
    User guest = UnitTestHelper.getGuest();
    StepUtilities.createStrategy(UnitTestHelper.createNormalStep(guest), false);
    StepUtilities.createStrategy(UnitTestHelper.createNormalStep(guest), false);

    User registeredUser = UnitTestHelper.getRegisteredUser();
    int count = stepFactory.getStrategyCount(guest) + stepFactory.getStrategyCount(registeredUser);

    registeredUser.getSession().mergeUser(guest);

    Assert.assertEquals(count, stepFactory.getStrategyCount(registeredUser));
  }

  @Test
  public void testMergeUserWithComplexStrategies() throws Exception {
    StepFactory stepFactory = UnitTestHelper.getModel().getStepFactory();
    User guest = UnitTestHelper.getGuest();
    Step step1 = UnitTestHelper.createNormalStep(guest);
    Strategy strategy = StepUtilities.createStrategy(step1, false);

    Step step2 = UnitTestHelper.createNormalStep(guest);
    Step boolean2 = StepUtilities.createBooleanStep(guest, step1.getStrategyId(), step1, step2, "OR", null);
    strategy.insertStepAfter(boolean2, step1.getStepId());

    Step step3 = UnitTestHelper.createNormalStep(guest);
    Step boolean3 = StepUtilities.createBooleanStep(guest, boolean2.getStrategyId(), boolean2, step3, "OR", null);
    strategy.insertStepAfter(boolean3, boolean2.getStepId());
    strategy.update(true);

    User registeredUser = UnitTestHelper.getRegisteredUser();
    int count = stepFactory.getStrategyCount(guest) + stepFactory.getStrategyCount(registeredUser);

    registeredUser.getSession().mergeUser(guest);

    Assert.assertEquals(count, stepFactory.getStrategyCount(registeredUser));
  }

  @Test
  public void testMergeUserWithStrategiesOfSameName() throws Exception {
    StepFactory stepFactory = UnitTestHelper.getModel().getStepFactory();
    String existName = "Name" + UnitTestHelper.getRandom().nextInt();
    User guest = UnitTestHelper.getGuest();
    Step step = UnitTestHelper.createNormalStep(guest);
    StepUtilities.createStrategy(step, false, existName);

    User registeredUser = UnitTestHelper.getRegisteredUser();
    Step newStep = UnitTestHelper.createNormalStep(registeredUser);
    Strategy expected = StepUtilities.createStrategy(newStep, false, existName);

    int count = stepFactory.getStrategyCount(guest) + stepFactory.getStrategyCount(registeredUser);

    registeredUser.getSession().mergeUser(guest);

    Assert.assertEquals(count, stepFactory.getStrategyCount(registeredUser));

    Strategy actual = StepUtilities.getStrategy(registeredUser, expected.getStrategyId());
    StrategyTest.compareStrategy(expected, actual);
  }

  @Test
  public void testMergeUserWithSavedStrategiesOfSameName() throws Exception {
    StepFactory stepFactory = UnitTestHelper.getModel().getStepFactory();
    String existName = "Name" + UnitTestHelper.getRandom().nextInt();
    User guest = UnitTestHelper.getGuest();
    Step step = UnitTestHelper.createNormalStep(guest);
    StepUtilities.createStrategy(step, false, existName);

    User registeredUser = UnitTestHelper.getRegisteredUser();
    Step newStep = UnitTestHelper.createNormalStep(registeredUser);
    Strategy expected = StepUtilities.createStrategy(newStep, true, existName);

    int count = stepFactory.getStrategyCount(guest) + stepFactory.getStrategyCount(registeredUser);

    registeredUser.getSession().mergeUser(guest);

    Assert.assertEquals(count, stepFactory.getStrategyCount(registeredUser));

    Strategy actual = StepUtilities.getStrategy(registeredUser, expected.getStrategyId());
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
