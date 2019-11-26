package org.gusdb.wdk.model.query.param;

import static org.gusdb.wdk.model.user.StepContainer.withId;

import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;

/**
 * @author jerric
 */
public class AnswerParamHandler extends AbstractParamHandler {

  public AnswerParamHandler(){}

  public AnswerParamHandler(AnswerParamHandler handler, Param param) {
    super(handler, param);
  }

  /**
   * the stable value is the step id;
   */
  @Override
  public String toStableValue(User user, Object rawValue) {
    Step step = (Step) rawValue;
    return Long.toString(step.getStepId());
  }

  /**
   * The internal is an SQL that represent the result of the step. If noTranslation is true, it returns
   * step_id
   */
  @Override
  public String toInternalValue(RunnableObj<QueryInstanceSpec> runnableSpec)
      throws WdkModelException {
    return _param.isNoTranslation() ?
           Long.toString(getStepIdFromStableValue(runnableSpec)) :
           getAnswerFromStepParam(runnableSpec).getIdSql();
  }

  @Override
  public String toEmptyInternalValue() {
    return _param.isNoTranslation() ? "0" : "SELECT * FROM dual";
  }

  /**
   * the signature is the checksum of answer, which doesn't have any user related information, to make sure
   * the cache can be shared.
   */
  @Override
  public String toSignature(RunnableObj<QueryInstanceSpec> runnableSpec)
      throws WdkModelException {
    return getAnswerFromStepParam(runnableSpec).getChecksum();
  }

  private AnswerValue getAnswerFromStepParam(RunnableObj<QueryInstanceSpec> qiSpec) throws WdkModelException {
    long stepId = getStepIdFromStableValue(qiSpec);
    return AnswerValueFactory.makeAnswer(
      qiSpec
        .get()
        .getStepContainer()
        .findFirstStep(withId(stepId))
        .orElseThrow(() -> new WdkModelException("Cannot find step " + stepId +
            " in step container referenced by query instance spec."))
        .getRunnable()
        .getOrThrow(spec -> new WdkModelException("Answer Spec inside step " +
            stepId + " is not runnable despite being referenced by a runnable " +
            "query instance spec.  Validation bundle: " + spec.getValidationBundle().toString(2))));
  }

  private long getStepIdFromStableValue(RunnableObj<QueryInstanceSpec> qiSpec) {
    String stableValue = qiSpec.get().get(_param.getName());
    // TODO: figure out why this split?  AnswerParam stable values are just step IDs
    return Long.parseLong(stableValue.split(":", 2)[0]);
  }

  @Override
  public ParamHandler clone(Param param) {
    return new AnswerParamHandler(this, param);
  }
}
