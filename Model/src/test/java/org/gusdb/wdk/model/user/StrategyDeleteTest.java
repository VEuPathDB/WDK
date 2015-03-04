package org.gusdb.wdk.model.user;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.junit.Assert;
import org.junit.Test;

public class StrategyDeleteTest extends StrategyOperationTest {

  @Test
  public void testSingle() throws WdkModelException, WdkUserException {
    // create a strategy with single step
    Step step1 = new MockSingleStep(stepFactory, user, MockStep.TYPE_A);
    Strategy strategy = createStrategy(step1);

    // delete a single step, the strategy will be deleted as well
    Map<Integer, Integer> rootMap = strategy.deleteStep(step1);
    Assert.assertEquals(0, rootMap.size());
    Assert.assertTrue(stepFactory.isStrategyDeleted(strategy.getStrategyId()));
    Assert.assertTrue(stepFactory.isStepDeleted(step1.getStepId()));
  }

  @Test
  public void testTransformSingle() throws WdkModelException, WdkUserException {
    // create a strategy with single step
    Step step1 = new MockSingleStep(stepFactory, user, MockStep.TYPE_A);
    Strategy strategy = createStrategy(step1);

    // create a transform
    Step transform = new MockTransformStep(stepFactory, user, step1, MockStep.TYPE_B);
    strategy.insertStepAfter(transform, step1.getStepId());

    // delete the first step, the transform, and the strategy will be deleted as well
    Map<Integer, Integer> rootMap = strategy.deleteStep(step1);
    Assert.assertEquals(0, rootMap.size());
    Assert.assertTrue(stepFactory.isStrategyDeleted(strategy.getStrategyId()));
    Assert.assertTrue(stepFactory.isStepDeleted(step1.getStepId()));
    Assert.assertTrue(stepFactory.isStepDeleted(transform.getStepId()));
  }

  @Test
  public void testTransform() throws WdkModelException, WdkUserException {
    // create a strategy with single step
    Step step1 = new MockSingleStep(stepFactory, user, MockStep.TYPE_A);
    Strategy strategy = createStrategy(step1);

    // create a transform
    Step transform = new MockTransformStep(stepFactory, user, step1, MockStep.TYPE_B);
    strategy.insertStepAfter(transform, step1.getStepId());

    // delete the transform, the strategy will stay, and the first step becomes root again
    Map<Integer, Integer> rootMap = strategy.deleteStep(transform);
    Assert.assertEquals(1, rootMap.size());
    Assert.assertEquals(step1.getStepId(), rootMap.get(transform.getStepId()).intValue());
    Assert.assertFalse(stepFactory.isStrategyDeleted(strategy.getStrategyId()));
    Assert.assertTrue(stepFactory.isStepDeleted(transform.getStepId()));
  }

  @Test
  public void testBooleanFirst() throws WdkModelException, WdkUserException {
    // create a strategy with single step
    Step step1 = new MockSingleStep(stepFactory, user, MockStep.TYPE_A);
    Strategy strategy = createStrategy(step1);

    // create the boolean at step2
    Step step2 = new MockSingleStep(stepFactory, user, MockStep.TYPE_A);
    Step boolean2 = new MockBooleanStep(stepFactory, user, step2, step1, MockStep.TYPE_A);
    strategy.insertStepBefore(boolean2, step1.getStepId());

    // delete the first step
    Map<Integer, Integer> rootMap = strategy.deleteStep(step2);

    // step2 & boolean should be delete, and step1 becomes root again
    Assert.assertEquals(step1.getStepId(), strategy.getLatestStepId());
    Assert.assertEquals(1, rootMap.size());
    Assert.assertEquals(step1.getStepId(), rootMap.get(boolean2.getStepId()).intValue());
    Assert.assertFalse(stepFactory.isStrategyDeleted(strategy.getStrategyId()));
    Assert.assertTrue(stepFactory.isStepDeleted(step2.getStepId()));
    Assert.assertTrue(stepFactory.isStepDeleted(boolean2.getStepId()));
  }

  @Test
  public void testBooleanSecond() throws WdkModelException, WdkUserException {
    // create a strategy with single step
    Step step1 = new MockSingleStep(stepFactory, user, MockStep.TYPE_A);
    Strategy strategy = createStrategy(step1);

    // create the boolean at step2
    Step step2 = new MockSingleStep(stepFactory, user, MockStep.TYPE_A);
    Step boolean2 = new MockBooleanStep(stepFactory, user, step2, step1, MockStep.TYPE_A);
    strategy.insertStepBefore(boolean2, step1.getStepId());

    // delete the first step
    Map<Integer, Integer> rootMap = strategy.deleteStep(step1);

    // step2 & boolean should be delete, and step1 becomes root again
    Assert.assertEquals(step2.getStepId(), strategy.getLatestStepId());
    Assert.assertEquals(1, rootMap.size());
    Assert.assertEquals(step2.getStepId(), rootMap.get(boolean2.getStepId()).intValue());
    Assert.assertFalse(stepFactory.isStrategyDeleted(strategy.getStrategyId()));
    Assert.assertTrue(stepFactory.isStepDeleted(step1.getStepId()));
    Assert.assertTrue(stepFactory.isStepDeleted(boolean2.getStepId()));
  }

  @Test
  public void testBoolean() throws WdkModelException, WdkUserException {
    // create a strategy with single step
    Step step1 = new MockSingleStep(stepFactory, user, MockStep.TYPE_A);
    Strategy strategy = createStrategy(step1);

    // create the boolean at step2
    Step step2 = new MockSingleStep(stepFactory, user, MockStep.TYPE_A);
    Step boolean2 = new MockBooleanStep(stepFactory, user, step2, step1, MockStep.TYPE_A);
    strategy.insertStepBefore(boolean2, step1.getStepId());

    // delete the boolean
    Map<Integer, Integer> rootMap = strategy.deleteStep(boolean2);

    // step2 & boolean should be delete, and step1 becomes root again
    Assert.assertEquals(step2.getStepId(), strategy.getLatestStepId());
    Assert.assertEquals(1, rootMap.size());
    Assert.assertEquals(step2.getStepId(), rootMap.get(boolean2.getStepId()).intValue());
    Assert.assertFalse(stepFactory.isStrategyDeleted(strategy.getStrategyId()));
    Assert.assertTrue(stepFactory.isStepDeleted(step1.getStepId()));
    Assert.assertTrue(stepFactory.isStepDeleted(boolean2.getStepId()));
  }

  @Test
  public void testBooleanNested() throws WdkModelException, WdkUserException {
    // create a strategy with single step
    Step step1 = new MockSingleStep(stepFactory, user, MockStep.TYPE_A);
    Strategy strategy = createStrategy(step1);

    // create the boolean at step2
    Step step2 = new MockSingleStep(stepFactory, user, MockStep.TYPE_A);
    Step boolean2 = new MockBooleanStep(stepFactory, user, step2, step1, MockStep.TYPE_A);
    strategy.insertStepBefore(boolean2, step1.getStepId());

    step1.setCollapsible(true);
    step1.setCollapsedName("Nested");

    // create the boolean at step2 within the nested strategy
    Step step3 = new MockSingleStep(stepFactory, user, MockStep.TYPE_A);
    Step boolean3 = new MockBooleanStep(stepFactory, user, step3, step1, MockStep.TYPE_A);
    strategy.insertStepBefore(boolean3, step1.getStepId());

    // delete step1.
    Map<Integer, Integer> rootMap = strategy.deleteStep(step1);
    Assert.assertEquals(boolean2.getStepId(), strategy.getLatestStepId());
    Assert.assertEquals(1, rootMap.size());
    Assert.assertEquals(step3.getStepId(), rootMap.get(boolean3.getStepId()).intValue());
  }

  @Test(expected = WdkUserException.class)
  public void testSingleIncompatible() throws WdkModelException, WdkUserException {
    // create a strategy with single step
    Step step1 = new MockSingleStep(stepFactory, user, MockStep.TYPE_A);
    Strategy strategy = createStrategy(step1);

    // create an incompatible transform
    Step step2 = new MockSingleStep(stepFactory, user, MockStep.TYPE_A);
    Step transform = new MockTransformStep(stepFactory, user, step2, MockStep.TYPE_A);

    strategy.insertStepBefore(transform, step1.getStepId());
  }

  @Test(expected = WdkUserException.class)
  public void testDoubleIncompatible() throws WdkModelException, WdkUserException {
    // create a strategy with single step
    Step step1 = new MockSingleStep(stepFactory, user, MockStep.TYPE_A);
    Strategy strategy = createStrategy(step1);

    // create a valid transform
    Step transform1 = new MockTransformStep(stepFactory, user, step1, MockStep.TYPE_B);
    strategy.insertStepAfter(transform1, step1.getStepId());

    // create an incompatible transform, the input is ok, but the output won't be compatible with transform1.
    Step transform2 = new MockTransformStep(stepFactory, user, step1, MockStep.TYPE_B);
    strategy.insertStepBefore(transform2, transform1.getStepId());
  }
}
