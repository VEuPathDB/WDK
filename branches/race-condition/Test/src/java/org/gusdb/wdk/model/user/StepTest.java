/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.gusdb.wdk.model.UnitTestHelper;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.query.BooleanOperator;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xingao
 * 
 */
public class StepTest {

  public static void compareStep(Step expectedStep, Step actualStep)
      throws NoSuchAlgorithmException, WdkUserException, WdkModelException,
      JSONException, SQLException {
    Assert.assertEquals("step id", expectedStep.getStepId(),
        actualStep.getStepId());
    Assert.assertEquals("valid", expectedStep.isValid(), actualStep.isValid());
    Assert.assertEquals("custom name", expectedStep.getCustomName(),
        actualStep.getCustomName());
    Assert.assertEquals("answer checksum",
        expectedStep.getAnswerValue().getChecksum(),
        actualStep.getAnswerValue().getChecksum());
  }

  private WdkModel wdkModel;
  private DBPlatform platform;
  private User user;

  public StepTest() throws Exception {
    wdkModel = UnitTestHelper.getModel();
    platform = wdkModel.getQueryPlatform();
    this.user = UnitTestHelper.getRegisteredUser();
  }

  @Test
  public void testCreateNormalStep() throws Exception {
    int stepCount = user.getStepCount();

    Step step = UnitTestHelper.createNormalStep(user);

    Assert.assertEquals("step count", stepCount + 1, user.getStepCount());

    Assert.assertTrue("stepId should be positive", step.getStepId() > 0);
    Assert.assertFalse("Step shouldn't be deleted", step.isDeleted());
    Assert.assertFalse("This is not combined", step.isCombined());
    Assert.assertFalse("This is not transform", step.isTransform());
    Assert.assertTrue("get result size",
        step.getAnswerValue().getResultSize() > 0);
  }

  @Test
  public void testCreateBooleanStep() throws Exception {
    Step leftOperand = UnitTestHelper.createNormalStep(user);
    Step rightOperand = UnitTestHelper.createNormalStep(user);

    int leftId = leftOperand.getStepId();
    int rightId = rightOperand.getStepId();
    int leftSize = leftOperand.getResultSize();
    int rightSize = rightOperand.getResultSize();
    String operator = BooleanOperator.UNION.getOperator(platform);

    String expression = leftId + " " + operator + " " + rightId;

    Step step = user.combineStep(expression);
    int size = step.getResultSize();
    Assert.assertTrue("result is boolean", step.isCombined());
    Assert.assertTrue("total size no smaller than left", size >= leftSize);
    Assert.assertTrue("total size no smaller than right", size >= rightSize);
    Assert.assertTrue("total size no bigger than combined", size <= leftSize
        + rightSize);
  }

  @Test
  public void testCreateComplexBooleanStep() throws Exception {
    Step operand1 = UnitTestHelper.createNormalStep(user);
    Step operand2 = UnitTestHelper.createNormalStep(user);
    Step operand3 = UnitTestHelper.createNormalStep(user);

    int id1 = operand1.getStepId();
    int id2 = operand2.getStepId();
    int id3 = operand3.getStepId();
    int size1 = operand1.getResultSize();
    int size2 = operand2.getResultSize();
    int size3 = operand3.getResultSize();
    String operator = " " + BooleanOperator.INTERSECT.getOperator(platform)
        + " ";

    String expression = id1 + operator + "(" + id2 + operator + id3 + ")";

    // get a combo result
    Step result1 = user.combineStep(expression);
    int resultSize1 = result1.getResultSize();

    Assert.assertTrue("No bigger than first operand", resultSize1 <= size1);
    Assert.assertTrue("No bigger than second operand", resultSize1 <= size2);
    Assert.assertTrue("No bigger than third operand", resultSize1 <= size3);

    // compose the result step by step
    Step result2 = user.combineStep(id2 + operator + id3);
    Step result3 = user.combineStep(id1 + operator + result2.getStepId());

    // the result of result1 and result3 should be identical
    Assert.assertEquals("Size equal", resultSize1, result3.getResultSize());
  }

  @Test
  public void testLoadSteps() throws Exception {
    // create a step
    Step step = UnitTestHelper.createNormalStep(user);

    Step[] steps = user.getSteps();
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

    Step loadedStep = user.getStep(step.getStepId());

    compareStep(step, loadedStep);
  }

  @Test
  public void testGetInvalidSteps() throws Exception {
    Step[] invalidStep = user.getInvalidSteps();
    for (Step step : invalidStep) {
      Assert.assertNotNull("question name", step.getQuestionName());
      Assert.assertNotNull("params", step.getParamValues());
    }
  }

  @Test
  public void testDeleteStep() throws Exception {
    // create a step
    Step step = UnitTestHelper.createNormalStep(user);

    int stepCount = user.getStepCount();
    user.deleteStep(step.getStepId());

    Assert.assertEquals("step count ", stepCount - 1, user.getStepCount());

    // now check if the step is really deleted; a WdkUserException should be
    // thrown
    try {
      user.getStep(step.getStepId());
      Assert.assertTrue("step is not deleted", false);
    } catch (WdkModelException ex) {
      // do nothing, expected
    }
  }

  @Test
  public void testDeleteSteps() throws Exception {
    // create a step
    UnitTestHelper.createNormalStep(user);

    user.deleteSteps();

    // the step list should be empty now
    int stepCount = user.getStepCount();
    Assert.assertEquals("step count", 0, stepCount);
  }
}
