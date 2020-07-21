package org.gusdb.wdk.model.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModelException;

public class StepCache {

  private final StepFactory _stepFactory;
  private final Map<Long, Step> _stepCache = new HashMap<>();

  public StepCache(StepFactory stepFactory) {
    _stepFactory = stepFactory;
  }

  public Optional<Step> getStepById(long stepId, ValidationLevel validationLevel) throws WdkModelException {
    Step step = _stepCache.get(stepId);
    if (step == null || !step.getValidationBundle().getLevel().isGreaterThanOrEqualTo(validationLevel)) {
      Optional<Step> stepOpt = _stepFactory.getStepById(stepId, validationLevel);
      if (stepOpt.isPresent()) {
        step = stepOpt.get();
        if (step.getStrategy().isPresent()) {
          for (Step stratStep : step.getStrategy().get().getAllSteps()) {
            _stepCache.put(stratStep.getStepId(), stratStep);
          }
        }
        else {
          _stepCache.put(stepOpt.get().getStepId(), stepOpt.get());
        }
      }
      return stepOpt;
    }
    return Optional.of(step);
  }
}
