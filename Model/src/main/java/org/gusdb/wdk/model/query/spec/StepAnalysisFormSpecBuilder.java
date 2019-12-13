package org.gusdb.wdk.model.query.spec;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.validation.ValidObjectFactory;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidationBundle.ValidationBundleBuilder;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.analysis.StepAnalysisSupplementalParams;

public class StepAnalysisFormSpecBuilder extends ParameterContainerInstanceSpecBuilder<StepAnalysisFormSpecBuilder> {

  StepAnalysisFormSpecBuilder() {
    super();
  }

  StepAnalysisFormSpecBuilder(ParameterContainerInstanceSpec<?> spec) {
    super(spec);
  }

  /**
   * Builds a "known invalid" spec- basically a container for params when an answer spec's question is
   * unknown or invalid.
   * 
   * @return invalid query instance spec (no validation performed)
   */
  
  public StepAnalysisFormSpec buildInvalid() {
    return new StepAnalysisFormSpec(toMap());
  }

  /**
   * Convenience method to create a runnable step analysis instance spec
   * 
   * @param step step from which dynamic params will be gleaned
   * @param stepAnalysis stepAnalysis for this instance spec
   * @return
   * @throws WdkModelException 
   */
  public RunnableObj<StepAnalysisFormSpec> buildRunnable(RunnableObj<Step> step, StepAnalysis stepAnalysis)
      throws WdkModelException {
    return ValidObjectFactory.getRunnable(buildValidated(
        step, stepAnalysis, ValidationLevel.RUNNABLE, FillStrategy.NO_FILL));
  }

  /**
   * Fills any missing parameters in the passed builder and builds a StepAnalysisFormSpec using the passed
   * validation level.
   * 
   * @param step step from which dynamic params will be gleaned
   * @param stepAnalysis stepAnalysis for this instance spec
   * @param validationLevel a level to validate the spec against
   * @param fillStrategy whether to fill in missing param values with defaults
   * @return a built spec
   * @throws WdkModelException if unable to validate (e.g. DB query fails or other runtime exception)
   */
  public StepAnalysisFormSpec buildValidated(RunnableObj<Step> step, StepAnalysis stepAnalysis,
      ValidationLevel validationLevel, FillStrategy fillStrategy) throws WdkModelException {
    User user = step.get().getUser();
    standardizeStableValues(stepAnalysis);
    putAll(StepAnalysisSupplementalParams.getValues(stepAnalysis, step.get().getUser(), Step.getRunnableAnswerSpec(step)));
    TwoTuple<PartiallyValidatedStableValues, ValidationBundleBuilder> paramValidation =
        validateParams(user, stepAnalysis, StepContainer.emptyContainer(), validationLevel, fillStrategy);
    return new StepAnalysisFormSpec(user, stepAnalysis, paramValidation.getFirst(),
        paramValidation.getSecond().build());
  }
}
