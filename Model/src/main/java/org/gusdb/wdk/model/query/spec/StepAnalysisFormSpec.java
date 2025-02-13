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

  StepAnalysisFormSpec(User requestingUser, StepAnalysis stepAnalysis, Map<String, String> paramValues, ValidationBundle validationBundle) {
    super(requestingUser, stepAnalysis, paramValues, validationBundle, StepContainer.emptyContainer());
  }

  StepAnalysisFormSpec(User requestingUser, Map<String, String> paramValues) {
    super(requestingUser, paramValues);
  }

  public Optional<StepAnalysis> getStepAnalysis() {
    return getParameterContainer().map(sa -> (StepAnalysis)sa);
  }
}
