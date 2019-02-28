package org.gusdb.wdk.model.query.param;

import static org.gusdb.wdk.model.user.StepContainer.withId;

import java.util.Optional;

import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepUtilities;
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
   * the raw value is a step object.
   */
  @Override
  public Optional<Step> toRawValue(User user, String stableValue) throws WdkModelException {
    try {
      return AnswerParam.NULL_VALUE.equals(stableValue)
          ? Optional.empty()
          : Optional.of(StepUtilities.getStep(user, Long.valueOf(stableValue), ValidationLevel.NONE));
    }
    catch (WdkUserException e) {
      return WdkModelException.unwrap(e);
    }
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
    return AnswerValueFactory.makeAnswer(
      qiSpec
        .getObject()
        .getStepContainer()
        .findFirstStep(withId(getStepIdFromStableValue(qiSpec)))
        .orElseThrow(WdkModelException::new)
        .getRunnable()
        .getLeft());
  }

  private long getStepIdFromStableValue(RunnableObj<QueryInstanceSpec> qiSpec) {
    String stableValue = qiSpec.getObject().get(_param.getName());
    // TODO: figure out why this split?  AnswerParam stable values are just step IDs
    return Long.parseLong(stableValue.split(":", 2)[0]);
  }

  @Override
  public ParamHandler clone(Param param) {
    return new AnswerParamHandler(this, param);
  }

  @Override
  public String getDisplayValue(QueryInstanceSpec ctxVals) throws WdkModelException {
    return toRawValue(ctxVals.getUser(), ctxVals.get(_param.getName()))
      .map(Step::getCustomName)
      .orElse("unknown");
  }
}
