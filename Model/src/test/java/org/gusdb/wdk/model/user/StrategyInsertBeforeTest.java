package org.gusdb.wdk.model.user;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.junit.Assert;
import org.junit.Test;

public class StrategyInsertBeforeTest extends StrategyOperationTest {

  @Test
  public void testSingleBoolean() throws WdkModelException, WdkUserException {
    // create a strategy with single step
    Step step1 = new MockSingleStep(stepFactory, user, MockStep.TYPE_A);
    Strategy strategy = createStrategy(step1);

    // create the boolean
    Step step2 = new MockSingleStep(stepFactory, user, MockStep.TYPE_A);
    Step booleanStep = new MockBooleanStep(stepFactory, user, step2, step1, MockStep.TYPE_A);

    // insert the boolean after step1
    Map<Integer, Integer> rootMap = strategy.insertStepBefore(booleanStep, step1.getStepId());
    Assert.assertEquals(booleanStep.getStepId(), strategy.getLatestStepId());
    Assert.assertEquals(1, rootMap.size());
    Assert.assertEquals(booleanStep.getStepId(), rootMap.get(step1.getStepId()).intValue());
  }

  @Test
  public void testDoubleTransform() throws WdkModelException, WdkUserException {
    // create a strategy with single step
    Step step1 = new MockSingleStep(stepFactory, user, MockStep.TYPE_A);
    Strategy strategy = createStrategy(step1);

    // create a transform
    Step transform1 = new MockTransformStep(stepFactory, user, step1, MockStep.TYPE_B);
    strategy.insertStepAfter(transform1, step1.getStepId());

    // create a second transform and insert before the first one
    Step transform2 = new MockTransformStep(stepFactory, user, step1, MockStep.TYPE_A);
    Map<Integer, Integer> rootMap = strategy.insertStepBefore(transform2, transform1.getStepId());

    // insert the transform after step1
    Assert.assertEquals(transform1.getStepId(), strategy.getLatestStepId());
    Assert.assertEquals(0, rootMap.size());
  }

  @Test
  public void testDoubleBooleanFirst() throws WdkModelException, WdkUserException {
    // create a strategy with single step
    Step step1 = new MockSingleStep(stepFactory, user, MockStep.TYPE_A);
    Strategy strategy = createStrategy(step1);

    // create the boolean at step2
    Step step2 = new MockSingleStep(stepFactory, user, MockStep.TYPE_A);
    Step boolean2 = new MockBooleanStep(stepFactory, user, step2, step1, MockStep.TYPE_A);
    strategy.insertStepBefore(boolean2, step1.getStepId());

    // create the boolean at step3
    Step step3 = new MockSingleStep(stepFactory, user, MockStep.TYPE_A);
    Step boolean3 = new MockBooleanStep(stepFactory, user, step3, step2, MockStep.TYPE_A);

    // insert the boolean3 after boolean2
    Map<Integer, Integer> rootMap = strategy.insertStepBefore(boolean3, step2.getStepId());
    Assert.assertEquals(boolean2.getStepId(), strategy.getLatestStepId());
    Assert.assertEquals(0, rootMap.size());
  }

  @Test
  public void testDoubleBooleanMiddle() throws WdkModelException, WdkUserException {
    // create a strategy with single step
    Step step1 = new MockSingleStep(stepFactory, user, MockStep.TYPE_A);
    Strategy strategy = createStrategy(step1);

    // create the boolean at step2
    Step step2 = new MockSingleStep(stepFactory, user, MockStep.TYPE_A);
    Step boolean2 = new MockBooleanStep(stepFactory, user, step2, step1, MockStep.TYPE_A);
    strategy.insertStepBefore(boolean2, step1.getStepId());

    // create the boolean at step3
    Step step3 = new MockSingleStep(stepFactory, user, MockStep.TYPE_A);
    Step boolean3 = new MockBooleanStep(stepFactory, user, step2, step3, MockStep.TYPE_A);

    // insert the boolean3 after boolean2
    Map<Integer, Integer> rootMap = strategy.insertStepBefore(boolean3, boolean2.getStepId());
    Assert.assertEquals(boolean2.getStepId(), strategy.getLatestStepId());
    Assert.assertEquals(0, rootMap.size());
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

    // insert the boolean3 before the first step in the nested strategy
    Map<Integer, Integer> rootMap = strategy.insertStepBefore(boolean3, step1.getStepId());
    Assert.assertEquals(boolean2.getStepId(), strategy.getLatestStepId());
    Assert.assertEquals(1, rootMap.size());
    Assert.assertEquals(boolean3.getStepId(), rootMap.get(step1.getStepId()).intValue());
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
