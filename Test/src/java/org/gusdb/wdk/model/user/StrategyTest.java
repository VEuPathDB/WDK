/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.UnitTestHelper;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.query.BooleanOperator;
import org.gusdb.wdk.model.record.RecordClass;
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
  public void testGetStrategies() throws Exception {
    Step step = UnitTestHelper.createNormalStep(user);
    Strategy strategy = user.createStrategy(step, false);
    Strategy[] strategies = user.getStrategies();

    boolean hasStrategy = false;
    for (Strategy loadedStrategy : strategies) {
      if (strategy.getStrategyId() == loadedStrategy.getStrategyId()) {
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
    user.deleteStrategy(strategy.getStrategyId());

    Assert.assertEquals("strategy count", count - 1, user.getStrategyCount());

    // get a delete strategy, should raise a WdkUserException
    try {
      user.getStrategy(strategy.getStrategyId());
      Assert.assertTrue("strategy not deleted", false);
    } catch (WdkModelException ex) {
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

    Step booleanStep = user.createBooleanStep(step1, step2, operator, false,
        null);

    strategy.addStep(step1.getStepId(), booleanStep);
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
    RecordClass recordClass = step1.getQuestion().getRecordClass();
    AnswerFilterInstance[] filters = recordClass.getFilters();
    if (filters.length == 0)
      return; // no filter exists skip the test

    Strategy strategy = user.createStrategy(step1, false);
    Step step2 = UnitTestHelper.createNormalStep(user);
    BooleanOperator operator = BooleanOperator.UNION;
    AnswerFilterInstance filter = recordClass.getDefaultFilter();
    Step oldStep = user.createBooleanStep(step1, step2, operator, false, filter);
    strategy.addStep(step1.getStepId(), oldStep);

    AnswerFilterInstance newFilter = null;
    do {
      int index = UnitTestHelper.getRandom().nextInt(filters.length);
      newFilter = filters[index];
    } while (filter != null && filter.getName().equals(newFilter.getName()));

    Step newStep = oldStep.createStep(newFilter, 0);

    strategy.editOrInsertStep(oldStep.getStepId(), newStep);
    StepTest.compareStep(newStep, strategy.getLatestStep());
  }

  @Test
  public void testChangeFirstStepFilter() throws Exception {
    Step oldStep = UnitTestHelper.createNormalStep(user);

    RecordClass recordClass = oldStep.getQuestion().getRecordClass();
    AnswerFilterInstance[] filters = recordClass.getFilters();
    if (filters.length == 0)
      return; // no filter exists skip the test

    Strategy strategy = user.createStrategy(oldStep, false);
    AnswerFilterInstance oldFilter = oldStep.getFilter();
    AnswerFilterInstance newFilter = null;
    do {
      int index = UnitTestHelper.getRandom().nextInt(filters.length);
      newFilter = filters[index];
    } while (oldFilter != null
        && oldFilter.getName().equals(newFilter.getName()));

    Step newStep = oldStep.createStep(newFilter, 0);

    strategy.editOrInsertStep(oldStep.getStepId(), newStep);
    StepTest.compareStep(newStep, strategy.getLatestStep());
  }

  @Test
  public void testChangeLeafStepFilter() throws Exception {
    Step step1 = UnitTestHelper.createNormalStep(user);
    Strategy strategy = user.createStrategy(step1, false);

    Step step2 = UnitTestHelper.createNormalStep(user);
    RecordClass recordClass = step2.getQuestion().getRecordClass();
    AnswerFilterInstance[] filters = recordClass.getFilters();
    if (filters.length == 0)
      return; // no filter exists skip the test

    AnswerFilterInstance filter = step2.getFilter();
    BooleanOperator operator = BooleanOperator.UNION;
    AnswerFilterInstance booleanFilter = recordClass.getDefaultFilter();
    Step oldBooleanStep = user.createBooleanStep(step1, step2, operator, false,
        booleanFilter);
    strategy.addStep(step1.getStepId(), oldBooleanStep);

    AnswerFilterInstance newFilter = null;
    do {
      int index = UnitTestHelper.getRandom().nextInt(filters.length);
      newFilter = filters[index];
    } while (filter != null && filter.getName().equals(newFilter.getName()));

    step2 = step2.createStep(newFilter, 0);
    Step newBooleanStep = user.createBooleanStep(step1, step2, operator, false,
        booleanFilter);

    strategy.editOrInsertStep(oldBooleanStep.getStepId(), newBooleanStep);
    StepTest.compareStep(newBooleanStep, strategy.getLatestStep());
    StepTest.compareStep(step2, strategy.getLatestStep().getChildStep());
  }

  @Test
  public void testChangeMiddleStepFilter() throws Exception {
    Step step1 = UnitTestHelper.createNormalStep(user);
    Strategy strategy = user.createStrategy(step1, false);

    Step step2 = UnitTestHelper.createNormalStep(user);
    RecordClass recordClass = step2.getQuestion().getRecordClass();
    AnswerFilterInstance booleanFilter = recordClass.getDefaultFilter();
    AnswerFilterInstance[] filters = recordClass.getFilters();
    if (filters.length == 0)
      return; // no filter exists skip the test

    // create the second node
    BooleanOperator operator = BooleanOperator.UNION;
    Step middleStep1 = user.createBooleanStep(step1, step2, operator, false,
        booleanFilter);
    strategy.addStep(step1.getStepId(), middleStep1);

    // create the third node
    Step step3 = UnitTestHelper.createNormalStep(user);
    Step middleStep2 = user.createBooleanStep(middleStep1, step3, operator,
        false, booleanFilter);
    strategy.addStep(middleStep1.getStepId(), middleStep2);

    AnswerFilterInstance filter = step2.getFilter();
    AnswerFilterInstance newFilter = null;
    do {
      int index = UnitTestHelper.getRandom().nextInt(filters.length);
      newFilter = filters[index];
    } while (filter != null && filter.getName().equals(newFilter.getName()));

    // change the filter of step2
    step2 = step2.createStep(newFilter, 0);
    Step newMiddleStep1 = user.createBooleanStep(step1, step2, operator, false,
        booleanFilter);
    strategy.editOrInsertStep(middleStep1.getStepId(), newMiddleStep1);

    Step root = strategy.getLatestStep();
    StepTest.compareStep(newMiddleStep1, root.getPreviousStep());
    StepTest.compareStep(step3, root.getChildStep());
    StepTest.compareStep(step2, root.getPreviousStep().getChildStep());
    StepTest.compareStep(step1, root.getPreviousStep().getPreviousStep());
  }

  @Test
  public void testLoadSavedStrategy() throws WdkModelException {
    Map<String, List<Strategy>> strategies = user.getSavedStrategiesByCategory();
    for (String category : strategies.keySet()) {
      for (Strategy strategy : strategies.get(category)) {
        Assert.assertEquals(category, strategy.getRecordClass().getFullName());
        Assert.assertTrue(strategy.getIsSaved());
        Assert.assertFalse(strategy.isDeleted());
      }
    }
  }

  @Test
  public void testLoadUnsavedStrategy() throws WdkModelException {
    Map<String, List<Strategy>> strategies = user.getUnsavedStrategiesByCategory();
    for (String category : strategies.keySet()) {
      for (Strategy strategy : strategies.get(category)) {
        Assert.assertEquals(category, strategy.getRecordClass().getFullName());
        Assert.assertFalse(strategy.getIsSaved());
        Assert.assertFalse(strategy.isDeleted());
        System.err.println("#" + strategy.getStrategyId() + ": "
            + strategy.getLastRunTime());
      }
    }
  }

  @Test
  public void testRecentSavedStrategy() throws WdkModelException {
    Calendar calender = Calendar.getInstance();
    calender.add(Calendar.DATE, -1);
    Date threshold = calender.getTime();
    Map<String, List<Strategy>> strategies = user.getRecentStrategiesByCategory();
    for (String category : strategies.keySet()) {
      for (Strategy strategy : strategies.get(category)) {
        Assert.assertEquals(category, strategy.getRecordClass().getFullName());
        Assert.assertFalse(strategy.getIsSaved());
        Assert.assertFalse(strategy.isDeleted());

        Date date = strategy.getLastRunTime();
        Assert.assertTrue(threshold.compareTo(date) <= 0);
      }
    }
  }

  @Test
  public void testInsertStep() throws Exception {
    Step step1 = UnitTestHelper.createNormalStep(user);
    Strategy strategy = user.createStrategy(step1, false);

    Step step2 = UnitTestHelper.createNormalStep(user);
    RecordClass recordClass = step2.getQuestion().getRecordClass();
    AnswerFilterInstance booleanFilter = recordClass.getDefaultFilter();
    AnswerFilterInstance[] filters = recordClass.getFilters();
    if (filters.length == 0)
      return; // no filter exists skip the test

    // create the second node
    BooleanOperator operator = BooleanOperator.UNION;
    Step middleStep1 = user.createBooleanStep(step1, step2, operator, false,
        booleanFilter);
    strategy.addStep(step1.getStepId(), middleStep1);

    // create the third node
    Step step3 = UnitTestHelper.createNormalStep(user);
    Step middleStep2 = user.createBooleanStep(middleStep1, step3, operator,
        false, booleanFilter);
    strategy.addStep(middleStep1.getStepId(), middleStep2);

    Step step4 = UnitTestHelper.createNormalStep(user);
    Step middleStep3 = user.createBooleanStep(middleStep1, step4, operator,
        false, booleanFilter);
    strategy.editOrInsertStep(middleStep1.getStepId(), middleStep3);

    Step root = strategy.getLatestStep();
    StepTest.compareStep(step3, root.getChildStep());
    StepTest.compareStep(step4, root.getPreviousStep().getChildStep());
    StepTest.compareStep(step2,
        root.getPreviousStep().getPreviousStep().getChildStep());
    StepTest.compareStep(
        step1,
        root.getPreviousStep().getPreviousStep().getPreviousStep().getPreviousStep());
  }

  @Test
  public void testImportStrategy() throws Exception {
    Step step1 = UnitTestHelper.createNormalStep(user);
    Strategy strategy = user.createStrategy(step1, false);

    Step step2 = UnitTestHelper.createNormalStep(user);
    RecordClass recordClass = step2.getQuestion().getRecordClass();
    AnswerFilterInstance booleanFilter = recordClass.getDefaultFilter();
    AnswerFilterInstance[] filters = recordClass.getFilters();
    if (filters.length == 0)
      return; // no filter exists skip the test

    // create the second node
    BooleanOperator operator = BooleanOperator.UNION;
    Step middleStep1 = user.createBooleanStep(step1, step2, operator, false,
        booleanFilter);
    strategy.addStep(step1.getStepId(), middleStep1);

    // create the third node
    Step step3 = UnitTestHelper.createNormalStep(user);
    Step middleStep2 = user.createBooleanStep(middleStep1, step3, operator,
        false, booleanFilter);
    strategy.addStep(middleStep1.getStepId(), middleStep2);

    User guest = UnitTestHelper.getGuest();
    Strategy newStrategy = guest.importStrategy(strategy.getChecksum());
    Assert.assertEquals(strategy.getLength(), newStrategy.getLength());
  }

  static void compareStrategy(Strategy expected, Strategy actual)
      throws WdkModelException {
    Assert.assertEquals("strategy id", expected.getStrategyId(),
        actual.getStrategyId());
    Assert.assertEquals("strategy length", expected.getLength(),
        actual.getLength());
    Assert.assertEquals("strategy name", expected.getName(), actual.getName());
    Assert.assertEquals("strategy type", expected.getRecordClass().getFullName(),
        actual.getRecordClass().getFullName());

    // compare the steps
    Assert.assertEquals("steps count", expected.getAllSteps().length,
        actual.getAllSteps().length);
    StepTest.compareStep(expected.getLatestStep(), actual.getLatestStep());
  }
}
