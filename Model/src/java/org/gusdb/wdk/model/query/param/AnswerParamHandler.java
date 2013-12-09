/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;

/**
 * @author jerric
 * 
 */
public class AnswerParamHandler extends AbstractParamHandler {

  /**
   * the raw value is the same as the stable value.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandlerPlugin#toStableValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toStableValue(User user, String rawValue,
      Map<String, String> contextValues)  {
    return rawValue;
  }

  /**
   * the raw value is the same as the stable value.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandlerPlugin#toRawValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toRawValue(User user, String refernceValue,
      Map<String, String> contextValues)  {
    return refernceValue;
  }

  /**
   * The internal is an SQL that represent the result of the step. If
   * noTranslation is true, it returns step_id
   * 
   * @throws WdkModelException
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandlerPlugin#toInternalValue(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toInternalValue(User user, String stableValue,
      Map<String, String> contextValues) throws WdkModelException {
    int stepId = Integer.parseInt(stableValue.split(":", 2)[0]);

    if (param.isNoTranslation())
      return Integer.toString(stepId);

    Step step = user.getStep(stepId);
    AnswerValue answerValue = step.getAnswerValue();
    return "(" + answerValue.getIdSql() + ")";
  }

  /**
   * the signature is the checksum of answer, which doesn't have any user
   * related information, to make sure the cache can be shared.
   * @throws WdkModelException 
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toSignature(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toSignature(User user, String stableValue,
      Map<String, String> contextValues) throws WdkModelException  {
    int stepId = Integer.parseInt(stableValue.split(":", 2)[0]);
    Step step = user.getStep(stepId);
    AnswerValue answerValue = step.getAnswerValue();
    return answerValue.getChecksum();
  }

}
