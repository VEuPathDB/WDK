package org.gusdb.wdk.model.user;

import java.util.HashSet;
import java.util.Set;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

public class MockStepFactory extends StepFactory {

  private Set<Long> deletedSteps = new HashSet<>();
  private Set<Long> deletedStrategies = new HashSet<>();
  
  public MockStepFactory(WdkModel wdkModel) {
    super(wdkModel);
  }

  @Override
  protected void initialize() {
    // do nothing
  }

  @Override
  void saveStepParamFilters(Step step) throws WdkModelException {
    // do nothing
  }

  @Override
  int resetStepCounts(Step fromStep) throws WdkModelException {
    int count = 0;
    while (fromStep != null) {
      fromStep.setEstimateSize(-1);
      fromStep = fromStep.getParentOrNextStep();
      count++;
    }
    return count;
  }

  @Override
  void updateStep(User user, Step step, boolean updateTime) throws WdkModelException {
    // do nothing
  }

  @Override
  void updateStrategy(User user, Strategy strategy, boolean overwrite) throws WdkModelException,
      WdkUserException {
    // do nothing
  }
  
  public boolean isStepDeleted(long stepId) {
    return deletedSteps.contains(stepId);
  }

  @Override
  public void deleteStep(long stepId) throws WdkModelException {
    deletedSteps.add(stepId);
  }
  
  public boolean isStrategyDeleted(long strategyId) {
    return deletedStrategies.contains(strategyId);
  }
  
  @Override
  public void deleteStrategy(long strategyId) throws WdkModelException {
    deletedStrategies.add(strategyId);
  }
}
