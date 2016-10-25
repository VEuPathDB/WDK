package org.gusdb.wdk.service.factory;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.request.answer.AnswerSpec;
import org.gusdb.wdk.service.request.strategy.StepRequest;

public class WdkStepFactory {

  public static Step createStep(StepRequest stepRequest, User user, StepFactory stepFactory) throws WdkModelException {
    try {
      // new step must be created from raw spec
      AnswerSpec answerSpec = stepRequest.getAnswerSpec();
      Step step = stepFactory.createStep(user, null, answerSpec.getQuestion(),
          AnswerValueFactory.convertParams(answerSpec.getParamValues()),
          answerSpec.getLegacyFilter(), 1, -1, false, true, answerSpec.getWeight(),
          answerSpec.getFilterValues());
      step.setViewFilterOptions(answerSpec.getViewFilterValues());
      step.saveParamFilters();

      // once created, additional user-provided fields can be applied
      step.setCustomName(stepRequest.getCustomName());
      step.setCollapsible(stepRequest.isCollapsible());
      step.setCollapsedName(stepRequest.getCollapsedName());
      step.update(true);
      return step;
    }
    catch (WdkUserException e) {
      throw new WdkModelException("Unable to create step", e);
    }
  }
}
