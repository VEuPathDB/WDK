package org.gusdb.wdk.model.user;

import java.util.Random;

import org.gusdb.wdk.model.MockWdkModel;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

public abstract class StrategyOperationTest {

  protected static final Random random = new Random();

  protected final WdkModel wdkModel = new MockWdkModel();
  protected final MockStepFactory stepFactory = (MockStepFactory)wdkModel.getStepFactory();
  protected final User user = new MockUser(wdkModel);

  protected Strategy createStrategy(Step root) throws WdkModelException {
    Strategy strategy = new Strategy(stepFactory, user, random.nextInt(Integer.MAX_VALUE) + 1);
    strategy.setProjectId(wdkModel.getProjectId());
    strategy.setLatestStep(root);
    return strategy;
  }

}
