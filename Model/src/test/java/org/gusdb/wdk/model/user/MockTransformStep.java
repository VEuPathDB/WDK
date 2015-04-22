package org.gusdb.wdk.model.user;

import java.util.Arrays;
import java.util.Collection;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.query.param.MockAnswerParam;

public class MockTransformStep extends MockStep {

  private final AnswerParam previousParam;

  public MockTransformStep(StepFactory stepFactory, User user, Collection<String> inTypes, String outType) throws WdkModelException {
    super(stepFactory, user, outType);
    previousParam = new MockAnswerParam(inTypes);
  }

  public MockTransformStep(StepFactory stepFactory, User user, Step previousStep, String outType)
      throws WdkModelException {
    super(stepFactory, user, outType);
    previousParam = new MockAnswerParam(Arrays.asList(previousStep.getType()));
    setPreviousStep(previousStep);
  }

  @Override
  public boolean isFirstStep() {
    return false;
  }

  @Override
  public boolean isTransform() {
    return true;
  }

  @Override
  public boolean isCombined() {
    return true;
  }

  @Override
  public AnswerParam getPreviousStepParam() throws WdkModelException {
    return previousParam;
  }
}
