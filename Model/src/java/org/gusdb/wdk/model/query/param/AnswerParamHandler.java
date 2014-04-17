/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.model.user.User;

/**
 * @author jerric
 * 
 */
public class AnswerParamHandler extends AbstractParamHandler {

  public static final String PARAM_INPUT_STEP = "inputStep";
  public static final String PARAM_INPUT_STRATEGY = "strategy";
  
  public AnswerParamHandler(){}
  
  public AnswerParamHandler(AnswerParamHandler handler, Param param) {
    super(handler, param);
  }

  /**
   * the stable value is the step id;
   * 
   * @throws WdkModelException
   * @see org.gusdb.wdk.model.query.param.ParamHandlerPlugin#toStableValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toStableValue(User user, Object rawValue, Map<String, String> contextValues)
      throws WdkModelException {
    Step step = (Step) rawValue;
    return Integer.toString(step.getStepId());
  }

  /**
   * the raw value is a step object.
   * 
   * @throws WdkModelException
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandlerPlugin#toRawValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public Step toRawValue(User user, String stableValue, Map<String, String> contextValues)
      throws WdkModelException {
    int stepId = Integer.valueOf(stableValue);
    return user.getStep(stepId);
  }

  /**
   * The internal is an SQL that represent the result of the step. If noTranslation is true, it returns
   * step_id
   * 
   * @throws WdkModelException
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandlerPlugin#toInternalValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toInternalValue(User user, String stableValue, Map<String, String> contextValues)
      throws WdkModelException {
    int stepId = Integer.parseInt(stableValue.split(":", 2)[0]);

    if (param.isNoTranslation())
      return Integer.toString(stepId);

    Step step = user.getStep(stepId);
    AnswerValue answerValue = step.getAnswerValue();
    return "(" + answerValue.getIdSql() + ")";
  }

  /**
   * the signature is the checksum of answer, which doesn't have any user related information, to make sure
   * the cache can be shared.
   * 
   * @throws WdkModelException
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toSignature(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toSignature(User user, String stableValue, Map<String, String> contextValues)
      throws WdkModelException {
    int stepId = Integer.valueOf(stableValue);
    Step step = user.getStep(stepId);
    AnswerValue answerValue = step.getAnswerValue();
    return answerValue.getChecksum();
  }

  /**
   * The stable value is string representation step id.
   * 
   * @throws WdkUserException
   * @throws WdkModelException
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#getStableValue(org.gusdb.wdk.model.user.User,
   *      org.gusdb.wdk.model.query.param.RequestParams)
   */
  @Override
  public String getStableValue(User user, RequestParams requestParams) throws WdkUserException,
      WdkModelException {
    String stepId = requestParams.getParam(param.getName());
    if (stepId == null || stepId.length() == 0)
      throw new WdkUserException("The input to parameter '" + param.getPrompt() + "' is required.");
    try {
      Step step = user.getStep(Integer.valueOf(stepId));
      return Integer.toString(step.getStepId());
    }
    catch (NumberFormatException ex) {
      throw new WdkUserException("Invalid input to parameter '" + param.getPrompt() +
          "'; the input must be a step id.", ex);
    }
  }

  @Override
  public void prepareDisplay(User user, RequestParams requestParams, Map<String, String> contextValues)
      throws WdkModelException, WdkUserException {
    String stableValue = requestParams.getParam(param.getName());
    if (stableValue == null) { // stable value is not set, choose a default stable value;
      String stepId = (String) requestParams.getAttribute(PARAM_INPUT_STEP);
      if (stepId == null)
        stepId = requestParams.getParam(PARAM_INPUT_STEP);
      if (stepId == null) {
        String strategyKey = requestParams.getParam(PARAM_INPUT_STRATEGY);
        if (strategyKey != null) {
          int pos = strategyKey.indexOf("_");
          if (pos < 0) {
            int strategyId = Integer.parseInt(strategyKey);
            Strategy strategy = user.getStrategy(strategyId);
            stepId = Integer.toString(strategy.getLatestStepId());
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
      requestParams.setParam(param.getName(), stableValue);
      Step step = user.getStep(Integer.valueOf(stableValue));
      requestParams.setAttribute(param.getName() + Param.RAW_VALUE_SUFFIX, step);
    }
  }

  @Override
  public ParamHandler clone(Param param) {
    return new AnswerParamHandler(this, param);
  }
}
