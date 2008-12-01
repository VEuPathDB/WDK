/**
 * 
 */
package org.gusdb.wdk.model.test.unit;

import org.gusdb.wdk.model.BooleanOperator;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author xingao
 * 
 */
public class StepTest {

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
        Step step = UnitTestHelper.createNormalStep(user);

        Assert.assertTrue("stepId should be positive", step.getDisplayId() > 0);
        Assert.assertFalse("Step shouldn't be deleted", step.isDeleted());
        Assert.assertFalse("This is not combined", step.isCombined());
        Assert.assertFalse("This is not transform", step.isTransform());
    }

    @Test
    public void testCreateBooleanStep() throws Exception {
        Step leftOperand = UnitTestHelper.createNormalStep(user);
        Step rightOperand = UnitTestHelper.createNormalStep(user);

        int leftId = leftOperand.getDisplayId();
        int rightId = rightOperand.getDisplayId();
        int leftSize = leftOperand.getResultSize();
        int rightSize = rightOperand.getResultSize();
        String operator = BooleanOperator.UNION.getOperator(platform);

        String expression = leftId + " " + operator + " " + rightId;

        Step step = user.combineStep(expression);
        int size = step.getResultSize();
        Assert.assertTrue("result is boolean", step.isCombined());
        Assert.assertTrue("total size no smaller than left", size >= leftSize);
        Assert.assertTrue("total size no smaller than right", size >= rightSize);
        Assert.assertTrue("total size no bigger than combined",
                size <= leftSize + rightSize);
    }

    @Test
    public void testCreateComplexBooleanStep() throws Exception {
        Step operand1 = UnitTestHelper.createNormalStep(user);
        Step operand2 = UnitTestHelper.createNormalStep(user);
        Step operand3 = UnitTestHelper.createNormalStep(user);

        int id1 = operand1.getDisplayId();
        int id2 = operand2.getDisplayId();
        int id3 = operand3.getDisplayId();
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
        Step result3 = user.combineStep(id1 + operator + result2.getDisplayId());

        // the result of result1 and result3 should be identical
        Assert.assertEquals("Size equal", resultSize1, result3.getResultSize());
    }
}
