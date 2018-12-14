package org.gusdb.wdk.model.user;

import org.gusdb.wdk.model.UnitTestHelper;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.BooleanOperator;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xingao
 * 
 */
public class StepTest {

  public static void compareStep(Step expectedStep, Step actualStep) throws WdkModelException {
    Assert.assertEquals("step id", expectedStep.getStepId(), actualStep.getStepId());
    Assert.assertEquals("valid", expectedStep.isValid(), actualStep.isValid());
    Assert.assertEquals("custom name", expectedStep.getCustomName(), actualStep.getCustomName());
    Assert.assertEquals("answer checksum", expectedStep.getAnswerValue().getChecksum(),
        actualStep.getAnswerValue().getChecksum());
  }

  private User user;
  private StepFactory stepFactory;

  public StepTest() throws Exception {
    this.user = UnitTestHelper.getRegisteredUser();
    this.stepFactory = user.getWdkModel().getStepFactory();
  }

  @Test
  public void testCreateNormalStep() throws Exception {
    int stepCount = stepFactory.getStepCount(user);

    Step step = UnitTestHelper.createNormalStep(user);

    Assert.assertEquals("step count", stepCount + 1, stepFactory.getStepCount(user));

    Assert.assertTrue("stepId should be positive", step.getStepId() > 0);
    Assert.assertFalse("Step shouldn't be deleted", step.isDeleted());
    Assert.assertFalse("This is not combined", step.isCombined());
    Assert.assertFalse("This is not transform", step.isTransform());
    Assert.assertTrue("get result size", step.getAnswerValue().getResultSizeFactory().getResultSize() > 0);
  }

  @Test
  public void testCreateBooleanStep() throws Exception {
    Step leftOperand = UnitTestHelper.createNormalStep(user);
    Step rightOperand = UnitTestHelper.createNormalStep(user);

    int leftSize = leftOperand.getResultSize();
    int rightSize = rightOperand.getResultSize();

    Step step = StepUtilities.createBooleanStep(user, leftOperand.getStrategyId(), leftOperand, rightOperand,
        BooleanOperator.UNION, null);
    int size = step.getResultSize();
    Assert.assertTrue("result is boolean", step.isCombined());
    Assert.assertTrue("total size no smaller than left", size >= leftSize);
    Assert.assertTrue("total size no smaller than right", size >= rightSize);
    Assert.assertTrue("total size no bigger than combined", size <= leftSize + rightSize);
  }

  @Test
  public void testLoadSteps() throws Exception {
    // create a step
    Step step = UnitTestHelper.createNormalStep(user);

    Step[] steps = StepUtilities.getSteps(user);
    Assert.assertTrue("should have steps", steps.length > 0);

    boolean hasStep = false;
    for (Step loadedStep : steps) {
      if (loadedStep.getStepId() == step.getStepId()) {
        compareStep(step, loadedStep);

        hasStep = true;
        break;
      }
    }
    Assert.assertTrue("step not found", hasStep);
  }

  @Test
  public void testLoadStep() throws Exception {
    // create a step
    Step step = UnitTestHelper.createNormalStep(user);

    Step loadedStep = StepUtilities.getStep(user, step.getStepId());

    compareStep(step, loadedStep);
  }

  @Test
  public void testDeleteStep() throws Exception {
    // create a step
    Step step = UnitTestHelper.createNormalStep(user);

    int stepCount = stepFactory.getStepCount(user);
    stepFactory.deleteStep(step.getStepId());

    Assert.assertEquals("step count ", stepCount - 1, stepFactory.getStepCount(user));

    // now check if the step is really deleted; a WdkUserException should be
    // thrown
    try {
      StepUtilities.getStep(user, step.getStepId());
      Assert.assertTrue("step is not deleted", false);
    }
    catch (WdkModelException ex) {
      // do nothing, expected
    }
  }

  @Test
  public void testDeleteSteps() throws Exception {
    // create a step
    UnitTestHelper.createNormalStep(user);

    StepUtilities.deleteSteps(user);

    // the step list should be empty now
    int stepCount = stepFactory.getStepCount(user);
    Assert.assertEquals("step count", 0, stepCount);
  }
}
