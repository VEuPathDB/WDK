package org.gusdb.wdk.model.query.param;

import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepUtilities;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.model.user.User;

/**
 * @author jerric
 */
public class AnswerParamHandler extends AbstractParamHandler {

  public static final String PARAM_INPUT_STEP = "inputStep";
  public static final String PARAM_INPUT_STRATEGY = "strategy";

  private static final Logger LOG = Logger.getLogger(AnswerParamHandler.class);

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
  public Step toRawValue(User user, String stableValue)
      throws WdkModelException {
    long stepId = Long.valueOf(stableValue);
    return StepUtilities.getStep(user, stepId, validationLevel);
  }

  /**
   * The internal is an SQL that represent the result of the step. If noTranslation is true, it returns
   * step_id
   */
  @Override
  public String toInternalValue(RunnableObj<QueryInstanceSpec> ctxVals)
      throws WdkModelException {
    final QueryInstanceSpec query = ctxVals.getObject();
    final String stable = query.get(_param.getName());
    final int stepId = Integer.parseInt(stable.split(":", 2)[0]);

    if (_param.isNoTranslation())
      return Integer.toString(stepId);

    Step step = StepUtilities.getStep(query.getUser(), stepId, validationLevel);
    AnswerValue answerValue = step.getAnswerValue();
    return answerValue.getIdSql();
  }

  /**
   * the signature is the checksum of answer, which doesn't have any user related information, to make sure
   * the cache can be shared.
   */
  @Override
  public String toSignature(RunnableObj<QueryInstanceSpec> ctxVals)
      throws WdkModelException {
    final QueryInstanceSpec query = ctxVals.getObject();
    final String stable = query.get(_param.getName());
    final long stepId = Long.valueOf(stable);
    final Step step = StepUtilities.getStep(query.getUser(), stepId, validationLevel);
    final String checksum = step.getAnswerValue(false).getChecksum();
    LOG.debug("Signature for step#" + stepId + ": " + checksum);
    return checksum;
  }

  /**
   * The stable value is string representation step id.
   */
  @Override
  public String getStableValue(User user, RequestParams requestParams)
      throws WdkUserException, WdkModelException {
    return validateStableValueSyntax(user, requestParams.getParam(_param.getName()));
  }

  @Override
  public String validateStableValueSyntax(User user, String inputStableValue)
      throws WdkUserException, WdkModelException {
    if (inputStableValue == null || inputStableValue.isEmpty())
      throw new WdkUserException("The input to parameter '" + _param.getPrompt() + "' is required.");
    try {
      Step step = StepUtilities.getStep(user, Long.valueOf(inputStableValue), validationLevel);
      return Long.toString(step.getStepId());
    }
    catch (NumberFormatException ex) {
      throw new WdkUserException("Invalid input to parameter '" + _param.getPrompt() +
          "'; the input must be an integer step id.", ex);
	}
  }

  @Override
  public void prepareDisplay(User user, RequestParams requestParams, Map<String, String> contextParamValues)
      throws WdkModelException, WdkUserException {
    String stableValue = requestParams.getParam(_param.getName());
    if (stableValue == null) { // stable value is not set, choose a default stable value;
      String stepId = (String) requestParams.getAttribute(PARAM_INPUT_STEP);
      if (stepId == null)
        stepId = requestParams.getParam(PARAM_INPUT_STEP);
      if (stepId == null) {
        String strategyKey = requestParams.getParam(PARAM_INPUT_STRATEGY);
        if (strategyKey != null) {
          int pos = strategyKey.indexOf("_");
          if (pos < 0) {
            long strategyId = Long.parseLong(strategyKey);
            Strategy strategy = StepUtilities.getStrategy(user, strategyId, validationLevel);
            stepId = Long.toString(strategy.getRootStepId());
          }
          else {
            stepId = strategyKey.substring(pos + 1);
          }
        }
      }

      // if no step is assigned, use the first step
      stableValue = stepId;
    }

    // if stable value is assigned, also prepare the raw value
    if (stableValue != null) {
      requestParams.setParam(_param.getName(), stableValue);
      Step step = StepUtilities.getStep(user, Long.valueOf(stableValue), validationLevel);
      requestParams.setAttribute(_param.getName() + Param.RAW_VALUE_SUFFIX, step);
    }
  }

  @Override
  public ParamHandler clone(Param param) {
    return new AnswerParamHandler(this, param);
  }

  @Override
  public String getDisplayValue(QueryInstanceSpec ctxVals) throws WdkModelException {
    return toRawValue(ctxVals.getUser(), ctxVals.get(_param.getName())).getCustomName();
  }
}
