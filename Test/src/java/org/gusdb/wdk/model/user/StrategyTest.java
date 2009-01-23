/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.gusdb.wdk.model.AnswerFilterInstance;
import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.BooleanOperator;
import org.gusdb.wdk.model.RecordClass;
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

    @Test
    public void testAddStep() throws Exception {
        Step step1 = UnitTestHelper.createNormalStep(user);
        Strategy strategy = user.createStrategy(step1, false);

        Step step2 = UnitTestHelper.createNormalStep(user);
        BooleanOperator operator = BooleanOperator.UNION;

        Step booleanStep = user.createBooleanStep(step1, step2, operator,
                false, null);

        strategy.addStep(booleanStep);
        Step rootStep = strategy.getLatestStep();
        StepTest.compareStep(booleanStep, rootStep);
        StepTest.compareStep(step1, rootStep.getPreviousStep());
        StepTest.compareStep(step2, rootStep.getChildStep());
        StepTest.compareStep(rootStep, rootStep.getPreviousStep().getNextStep());
        StepTest.compareStep(rootStep, rootStep.getChildStep().getParentStep());
    }

    @Test
    public void testChangeBooleanStepFilter() throws Exception {
        Step step1 = UnitTestHelper.createNormalStep(user);
        AnswerValue answerValue = step1.getAnswer().getAnswerValue();
        RecordClass recordClass = answerValue.getQuestion().getRecordClass();
        AnswerFilterInstance[] filters = recordClass.getFilters();
        if (filters.length == 0) return; // no filter exists skip the test

        Strategy strategy = user.createStrategy(step1, false);
        Step step2 = UnitTestHelper.createNormalStep(user);
        BooleanOperator operator = BooleanOperator.UNION;
        AnswerFilterInstance filter = recordClass.getDefaultFilter();
        Step oldStep = user.createBooleanStep(step1, step2, operator, false,
                filter);
        strategy.addStep(oldStep);

        AnswerFilterInstance newFilter = null;
        do {
            int index = UnitTestHelper.getRandom().nextInt(filters.length);
            newFilter = filters[index];
        } while (filter != null && filter.getName().equals(newFilter.getName()));

        Step newStep = oldStep.createStep(newFilter);

        strategy.editOrInsertStep(oldStep.getDisplayId(), newStep);
        StepTest.compareStep(newStep, strategy.getLatestStep());
    }

    @Test
    public void testChangeFirstStepFilter() throws Exception {
        Step oldStep = UnitTestHelper.createNormalStep(user);

        AnswerValue answerValue = oldStep.getAnswer().getAnswerValue();
        RecordClass recordClass = answerValue.getQuestion().getRecordClass();
        AnswerFilterInstance[] filters = recordClass.getFilters();
        if (filters.length == 0) return; // no filter exists skip the test

        Strategy strategy = user.createStrategy(oldStep, false);
        AnswerFilterInstance oldFilter = answerValue.getFilter();
        AnswerFilterInstance newFilter = null;
        do {
            int index = UnitTestHelper.getRandom().nextInt(filters.length);
            newFilter = filters[index];
        } while (oldFilter != null
                && oldFilter.getName().equals(newFilter.getName()));

        Step newStep = oldStep.createStep(newFilter);

        strategy.editOrInsertStep(oldStep.getDisplayId(), newStep);
        StepTest.compareStep(newStep, strategy.getLatestStep());
    }

    @Test
    public void testChangeLeafStepFilter() throws Exception {
        Step step1 = UnitTestHelper.createNormalStep(user);
        Strategy strategy = user.createStrategy(step1, false);

        Step step2 = UnitTestHelper.createNormalStep(user);
        AnswerValue answerValue = step2.getAnswer().getAnswerValue();
        RecordClass recordClass = answerValue.getQuestion().getRecordClass();
        AnswerFilterInstance[] filters = recordClass.getFilters();
        if (filters.length == 0) return; // no filter exists skip the test

        AnswerFilterInstance filter = answerValue.getFilter();
        BooleanOperator operator = BooleanOperator.UNION;
        AnswerFilterInstance booleanFilter = recordClass.getDefaultFilter();
        Step oldBooleanStep = user.createBooleanStep(step1, step2, operator,
                false, booleanFilter);
        strategy.addStep(oldBooleanStep);

        AnswerFilterInstance newFilter = null;
        do {
            int index = UnitTestHelper.getRandom().nextInt(filters.length);
            newFilter = filters[index];
        } while (filter != null && filter.getName().equals(newFilter.getName()));

        step2 = step2.createStep(newFilter);
        Step newBooleanStep = user.createBooleanStep(step1, step2, operator,
                false, booleanFilter);

        strategy.editOrInsertStep(oldBooleanStep.getDisplayId(), newBooleanStep);
        StepTest.compareStep(newBooleanStep, strategy.getLatestStep());
        StepTest.compareStep(step2, strategy.getLatestStep().getChildStep());
    }

    @Test
    public void testChangeMiddleStepFilter() throws Exception {
        Step step1 = UnitTestHelper.createNormalStep(user);
        Strategy strategy = user.createStrategy(step1, false);

        Step step2 = UnitTestHelper.createNormalStep(user);
        AnswerValue answerValue = step2.getAnswer().getAnswerValue();
        RecordClass recordClass = answerValue.getQuestion().getRecordClass();
        AnswerFilterInstance booleanFilter = recordClass.getDefaultFilter();
        AnswerFilterInstance[] filters = recordClass.getFilters();
        if (filters.length == 0) return; // no filter exists skip the test

        // create the second node
        BooleanOperator operator = BooleanOperator.UNION;
        Step middleStep1 = user.createBooleanStep(step1, step2, operator,
                false, booleanFilter);
        strategy.addStep(middleStep1);

        // create the third node
        Step step3 = UnitTestHelper.createNormalStep(user);
        Step middleStep2 = user.createBooleanStep(middleStep1, step3, operator,
                false, booleanFilter);
        strategy.addStep(middleStep2);

        AnswerFilterInstance filter = answerValue.getFilter();
        AnswerFilterInstance newFilter = null;
        do {
            int index = UnitTestHelper.getRandom().nextInt(filters.length);
            newFilter = filters[index];
        } while (filter != null && filter.getName().equals(newFilter.getName()));

        // change the filter of step2
        step2 = step2.createStep(newFilter);
        Step newMiddleStep1 = user.createBooleanStep(step1, step2, operator,
                false, booleanFilter);
        strategy.editOrInsertStep(middleStep1.getDisplayId(), newMiddleStep1);

        Step root = strategy.getLatestStep();
        StepTest.compareStep(newMiddleStep1, root.getPreviousStep());
        StepTest.compareStep(step3, root.getChildStep());
        StepTest.compareStep(step2, root.getPreviousStep().getChildStep());
        StepTest.compareStep(step1, root.getPreviousStep().getPreviousStep());
    }

    @Test
    public void testInsertStep() {
        Assert.assertTrue(false);
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
