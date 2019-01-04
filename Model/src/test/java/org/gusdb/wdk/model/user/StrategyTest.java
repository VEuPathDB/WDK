package org.gusdb.wdk.model.user;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.UnitTestHelper;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
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
  private StepFactory stepFactory;

  public StrategyTest() throws Exception {
    user = UnitTestHelper.getRegisteredUser();
    stepFactory = user.getWdkModel().getStepFactory();
  }

  @Test
  public void testCreateStrategy() throws Exception {
    int strategyCount = stepFactory.getStrategyCount(user.getUserId());

    Step step = UnitTestHelper.createNormalStep(user);
    Strategy strategy = StepUtilities.createStrategy(step, false);

    Assert.assertEquals("strategy count", strategyCount + 1, stepFactory.getStrategyCount(user.getUserId()));

    Step root = strategy.getLatestStep();
    StepTest.compareStep(step, root);
  }

  @Test
  public void testCreateStrategyWithName() throws Exception {
    String strategyName = "My Strategy";

    Step step = UnitTestHelper.createNormalStep(user);
    Strategy strategy = StepUtilities.createStrategy(step, strategyName, false);

    Assert.assertEquals("strategy name", strategyName, strategy.getName());
  }

  @Test
  public void testGetStrategy() throws Exception {
    Step step = UnitTestHelper.createNormalStep(user);
    Strategy strategy = StepUtilities.createStrategy(step, false);

    Strategy loadedStrategy = StepUtilities.getStrategy(user, strategy.getStrategyId());
    compareStrategy(strategy, loadedStrategy);
  }

  @Test
  public void testGetStrategies() throws Exception {
    Step step = UnitTestHelper.createNormalStep(user);
    Strategy strategy = StepUtilities.createStrategy(step, false);
    Strategy[] strategies = StepUtilities.getStrategies(user);

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
    Strategy strategy = StepUtilities.createStrategy(step, false);

    int count = stepFactory.getStrategyCount(user.getUserId());
    StepUtilities.deleteStrategy(user, strategy.getStrategyId());

    Assert.assertEquals("strategy count", count - 1, stepFactory.getStrategyCount(user.getUserId()));

    // get a delete strategy, should raise a WdkUserException
    try {
      StepUtilities.getStrategy(user, strategy.getStrategyId());
      Assert.assertTrue("strategy not deleted", false);
    }
    catch (WdkModelException ex) {
      // do nothing, expected.
    }

  }

  @Test
  public void testDeleteStrategies() throws Exception {
    Step step = UnitTestHelper.createNormalStep(user);
    StepUtilities.createStrategy(step, false);

    StepUtilities.deleteStrategies(user);

    Assert.assertEquals("strategy count", 0, stepFactory.getStrategyCount(user.getUserId()));
  }

  @Test
  public void testAddStep() throws Exception {
    Step step1 = UnitTestHelper.createNormalStep(user);
    Strategy strategy = StepUtilities.createStrategy(step1, false);

    Step step2 = UnitTestHelper.createNormalStep(user);
    BooleanOperator operator = BooleanOperator.UNION;

    Step booleanStep = StepUtilities.createBooleanStep(user, strategy.getStrategyId(), step1, step2, operator, null);

    strategy.insertStepAfter(booleanStep, step1.getStepId());
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
    AnswerFilterInstance[] filters = recordClass.getFilterInstances();
    if (filters.length == 0)
      return; // no filter exists skip the test

    Strategy strategy = StepUtilities.createStrategy(step1, false);
    Step step2 = UnitTestHelper.createNormalStep(user);
    BooleanOperator operator = BooleanOperator.UNION;
    AnswerFilterInstance filter = recordClass.getDefaultFilter();
    Step oldStep = StepUtilities.createBooleanStep(user, strategy.getStrategyId(), step1, step2, operator, filter);
    strategy.insertStepAfter(oldStep, step1.getStepId());

    AnswerFilterInstance newFilter = null;
    do {
      int index = UnitTestHelper.getRandom().nextInt(filters.length);
      newFilter = filters[index];
    }
    while (filter != null && filter.getName().equals(newFilter.getName()));
    oldStep.setFilterName(newFilter.getName());
    oldStep.saveParamFilters();
    Step newStep = strategy.getStepById(oldStep.getStepId());
    StepTest.compareStep(newStep, strategy.getLatestStep());
  }

  @Test
  public void testChangeFirstStepFilter() throws Exception {
    Step oldStep = UnitTestHelper.createNormalStep(user);

    RecordClass recordClass = oldStep.getQuestion().getRecordClass();
    AnswerFilterInstance[] filters = recordClass.getFilterInstances();
    if (filters.length == 0)
      return; // no filter exists skip the test

    Strategy strategy = StepUtilities.createStrategy(oldStep, false);
    AnswerFilterInstance oldFilter = oldStep.getFilter();
    AnswerFilterInstance newFilter = null;
    do {
      int index = UnitTestHelper.getRandom().nextInt(filters.length);
      newFilter = filters[index];
    }
    while (oldFilter != null && oldFilter.getName().equals(newFilter.getName()));
    oldStep.setFilterName(newFilter.getName());
    oldStep.saveParamFilters();

    Step newStep = strategy.getStepById(oldStep.getStepId());
    StepTest.compareStep(newStep, strategy.getLatestStep());
  }

  @Test
  public void testChangeLeafStepFilter() throws Exception {
    Step step1 = UnitTestHelper.createNormalStep(user);
    Strategy strategy = StepUtilities.createStrategy(step1, false);

    Step step2 = UnitTestHelper.createNormalStep(user);
    RecordClass recordClass = step2.getQuestion().getRecordClass();
    AnswerFilterInstance[] filters = recordClass.getFilterInstances();
    if (filters.length == 0)
      return; // no filter exists skip the test

    AnswerFilterInstance filter = step2.getFilter();
    BooleanOperator operator = BooleanOperator.UNION;
    AnswerFilterInstance booleanFilter = recordClass.getDefaultFilter();
    Step oldBooleanStep = StepUtilities.createBooleanStep(user, strategy.getStrategyId(), step1, step2, operator,
        booleanFilter);
    strategy.insertStepAfter(oldBooleanStep, step1.getStepId());

    AnswerFilterInstance newFilter = null;
    do {
      int index = UnitTestHelper.getRandom().nextInt(filters.length);
      newFilter = filters[index];
    }
    while (filter != null && filter.getName().equals(newFilter.getName()));
    step2.setFilterName(newFilter.getName());
    Step newBooleanStep = strategy.getStepById(oldBooleanStep.getStepId());

    StepTest.compareStep(newBooleanStep, strategy.getLatestStep());
    StepTest.compareStep(step2, strategy.getLatestStep().getChildStep());
  }

  @Test
  public void testChangeMiddleStepFilter() throws Exception {
    Step step1 = UnitTestHelper.createNormalStep(user);
    Strategy strategy = StepUtilities.createStrategy(step1, false);

    Step step2 = UnitTestHelper.createNormalStep(user);
    RecordClass recordClass = step2.getQuestion().getRecordClass();
    AnswerFilterInstance booleanFilter = recordClass.getDefaultFilter();
    AnswerFilterInstance[] filters = recordClass.getFilterInstances();
    if (filters.length == 0)
      return; // no filter exists skip the test

    // create the second node
    BooleanOperator operator = BooleanOperator.UNION;
    Step middleStep1 = StepUtilities.createBooleanStep(user, strategy.getStrategyId(), step1, step2, operator,
        booleanFilter);
    strategy.insertStepAfter(middleStep1, step1.getStepId());

    // create the third node
    Step step3 = UnitTestHelper.createNormalStep(user);
    Step middleStep2 = StepUtilities.createBooleanStep(user, strategy.getStrategyId(), middleStep1, step3, operator,
        booleanFilter);
    strategy.insertStepAfter(middleStep2, middleStep1.getStepId());

    AnswerFilterInstance filter = step2.getFilter();
    AnswerFilterInstance newFilter = null;
    do {
      int index = UnitTestHelper.getRandom().nextInt(filters.length);
      newFilter = filters[index];
    }
    while (filter != null && filter.getName().equals(newFilter.getName()));

    // change the filter of step2
    step2.setFilterName(newFilter.getName());
    Step root = strategy.getLatestStep();
    Step newMiddleStep1 = strategy.getStepById(root.getPreviousStepId());

    StepTest.compareStep(newMiddleStep1, root.getPreviousStep());
    StepTest.compareStep(step3, root.getChildStep());
    StepTest.compareStep(step2, root.getPreviousStep().getChildStep());
    StepTest.compareStep(step1, root.getPreviousStep().getPreviousStep());
  }

  @Test
  public void testLoadSavedStrategy() throws WdkModelException {
    Map<String, List<Strategy>> strategies = StepUtilities.getSavedStrategiesByCategory(user);
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
    Map<String, List<Strategy>> strategies = StepUtilities.getUnsavedStrategiesByCategory(user);
    for (String category : strategies.keySet()) {
      for (Strategy strategy : strategies.get(category)) {
        Assert.assertEquals(category, strategy.getRecordClass().getFullName());
        Assert.assertFalse(strategy.getIsSaved());
        Assert.assertFalse(strategy.isDeleted());
        System.err.println("#" + strategy.getStrategyId() + ": " + strategy.getLastRunTime());
      }
    }
  }

  @Test
  public void testRecentSavedStrategy() throws WdkModelException {
    Calendar calender = Calendar.getInstance();
    calender.add(Calendar.DATE, -1);
    Date threshold = calender.getTime();
    Map<String, List<Strategy>> strategies = StepUtilities.getRecentStrategiesByCategory(user);
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
    Strategy strategy = StepUtilities.createStrategy(step1, false);

    Step step2 = UnitTestHelper.createNormalStep(user);
    RecordClass recordClass = step2.getQuestion().getRecordClass();
    AnswerFilterInstance booleanFilter = recordClass.getDefaultFilter();
    AnswerFilterInstance[] filters = recordClass.getFilterInstances();
    if (filters.length == 0)
      return; // no filter exists skip the test

    // create the second node
    BooleanOperator operator = BooleanOperator.UNION;
    Step middleStep1 = StepUtilities.createBooleanStep(user, strategy.getStrategyId(), step1, step2, operator,
        booleanFilter);
    strategy.insertStepAfter(middleStep1, step1.getStepId());

    // create the third node
    Step step3 = UnitTestHelper.createNormalStep(user);
    Step middleStep2 = StepUtilities.createBooleanStep(user, strategy.getStrategyId(), middleStep1, step3, operator,
        booleanFilter);
    strategy.insertStepAfter(middleStep2, middleStep1.getStepId());

    Step step4 = UnitTestHelper.createNormalStep(user);
    Step middleStep3 = StepUtilities.createBooleanStep(user, strategy.getStrategyId(), middleStep1, step4, operator,
        booleanFilter);
    strategy.insertStepAfter(middleStep3, middleStep1.getStepId());

    Step root = strategy.getLatestStep();
    StepTest.compareStep(step3, root.getChildStep());
    StepTest.compareStep(step4, root.getPreviousStep().getChildStep());
    StepTest.compareStep(step2, root.getPreviousStep().getPreviousStep().getChildStep());
    StepTest.compareStep(step1, root.getPreviousStep().getPreviousStep().getPreviousStep().getPreviousStep());
  }

  @Test
  public void testImportStrategy() throws Exception {
    Step step1 = UnitTestHelper.createNormalStep(user);
    Strategy strategy = StepUtilities.createStrategy(step1, false);

    Step step2 = UnitTestHelper.createNormalStep(user);
    RecordClass recordClass = step2.getQuestion().getRecordClass();
    AnswerFilterInstance booleanFilter = recordClass.getDefaultFilter();
    AnswerFilterInstance[] filters = recordClass.getFilterInstances();
    if (filters.length == 0)
      return; // no filter exists skip the test

    // create the second node
    BooleanOperator operator = BooleanOperator.UNION;
    Step middleStep1 = StepUtilities.createBooleanStep(user, strategy.getStrategyId(), step1, step2, operator,
        booleanFilter);
    strategy.insertStepAfter(middleStep1, step1.getStepId());

    // create the third node
    Step step3 = UnitTestHelper.createNormalStep(user);
    Step middleStep2 = StepUtilities.createBooleanStep(user, strategy.getStrategyId(), middleStep1, step3, operator,
        booleanFilter);
    strategy.insertStepAfter(middleStep2, middleStep1.getStepId());

    User guest = UnitTestHelper.getGuest();
    Strategy newStrategy = StepUtilities.importStrategy(guest, strategy.getChecksum());
    Assert.assertEquals(strategy.getLength(), newStrategy.getLength());
  }

  static void compareStrategy(Strategy expected, Strategy actual) throws WdkModelException, WdkUserException {
    Assert.assertEquals("strategy id", expected.getStrategyId(), actual.getStrategyId());
    Assert.assertEquals("strategy length", expected.getLength(), actual.getLength());
    Assert.assertEquals("strategy name", expected.getName(), actual.getName());
    Assert.assertEquals("strategy type", expected.getRecordClass().getFullName(),
        actual.getRecordClass().getFullName());

    // compare the steps
    Assert.assertEquals("steps count", expected.getMainBranch().size(), actual.getMainBranch().size());
    StepTest.compareStep(expected.getLatestStep(), actual.getLatestStep());
  }
}
