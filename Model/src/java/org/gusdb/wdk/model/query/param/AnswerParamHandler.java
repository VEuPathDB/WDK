/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;

/**
 * @author jerric
 * 
 */
public class AnswerParamHandler extends AbstractParamHandler {

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
   * The raw value is a Step object.
   * 
   * @throws WdkUserException
   * @throws WdkModelException
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#getRawValue(org.gusdb.wdk.model.user.User,
   *      org.gusdb.wdk.model.query.param.RequestParams)
   */
  @Override
  public Object getRawValue(User user, RequestParams requestParams) throws WdkUserException,
      WdkModelException {
    String stepId = requestParams.getParam(param.getName());
    if (stepId == null || stepId.length() == 0)
      throw new WdkUserException("The input to parameter '" + param.getPrompt() + "' is required.");
    try {
      return user.getStep(Integer.valueOf(stepId));
    }
    catch (NumberFormatException ex) {
      throw new WdkUserException("Invalid input to parameter '" + param.getPrompt() +
          "'; the input must be a step id.", ex);
    }
  }

}
