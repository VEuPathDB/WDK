package org.gusdb.wdk.model.query.spec;

import java.util.Map;
import java.util.Optional;

import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;

public class StepAnalysisFormSpec extends ParameterContainerInstanceSpec<StepAnalysisFormSpec> {

  @SuppressWarnings("unchecked")
  public static StepAnalysisFormSpecBuilder builder() {
    return new StepAnalysisFormSpecBuilder();
  }

  public static StepAnalysisFormSpecBuilder builder(StepAnalysisFormSpec spec) {
    return new StepAnalysisFormSpecBuilder(spec);
  }

  StepAnalysisFormSpec(User user, StepAnalysis stepAnalysis, Map<String, String> paramValues, ValidationBundle validationBundle) {
    super(user, stepAnalysis, paramValues, validationBundle, StepContainer.emptyContainer());
  }

  StepAnalysisFormSpec(Map<String, String> paramValues) {
    super(paramValues);
  }

  public Optional<StepAnalysis> getStepAnalysis() {
    return Optional.ofNullable(
      getParameterContainer()
        .map(container -> (StepAnalysis)container)
        .orElse(null));
  }

}
