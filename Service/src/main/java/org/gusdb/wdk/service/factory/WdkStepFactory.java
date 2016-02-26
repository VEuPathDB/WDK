package org.gusdb.wdk.service.factory;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.request.answer.AnswerRequest;

public class WdkStepFactory {

  public static Step createStep(AnswerRequest answerSpec, User user, StepFactory stepFactory) throws WdkModelException {
    try {
      Step step = stepFactory.createStep(user, null, answerSpec.getQuestion(), WdkAnswerFactory.convertParams(answerSpec.getParamValues()),
          answerSpec.getLegacyFilter(), 0, -1, false, true, answerSpec.getWeight(), answerSpec.getFilterValues());
      step.setViewFilterOptions(answerSpec.getViewFilterValues());
      step.saveParamFilters();
      return step;
    }
    catch (WdkUserException e) {
      throw new WdkModelException("Unable to create step", e);
    }
  }
}
