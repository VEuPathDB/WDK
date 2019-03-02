package org.gusdb.wdk.model.query.param;

import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.user.User;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class NumberParamHandler extends AbstractParamHandler {

  public NumberParamHandler(){}

  public NumberParamHandler(NumberParamHandler handler, Param param) {
    super(handler, param);
  }

  /**
   * the raw value is the same as stable value.
   */
  @Override
  public String toStableValue(User user, Object rawValue) {
    return (String) rawValue;
  }

  /**
   * the raw value is the same as stable value.
   */
  @Override
  public String toRawValue(User user, String stableValue) {
    return stableValue;
  }

  /**
   * the signature is a checksum of the stable value.
   */
  @Override
  public String toSignature(RunnableObj<QueryInstanceSpec> ctxVals) {
    final String stable = ctxVals.get().get(_param.getName());
    return stable == null || stable.length() == 0
        ? ""
        : EncryptionUtil.encrypt(stable);
  }

  /**
   * Formats the stableValue into a value that can be applied to SQL statements.
   */
  @Override
  public String toInternalValue(RunnableObj<QueryInstanceSpec> ctxVals) {
    final String stable = ctxVals.get().get(_param.getName());

    // Something to do with the portal - left this alone
    if(_param.isNoTranslation())
      return stable;

    final NumberParam param = (NumberParam) _param;
    // Set internalValue to stableValue and determine whether additional
    // modifications are needed.
    final String internal = stable.matches("^.*[eE].*$")
      ? new BigDecimal(stable).toPlainString()
      : stable;

    // If the number is not an integer, round it according to the number of decimal places
    // requested (or the default value).
    if(!param.isInteger()) {
      MathContext mathContext = new MathContext(param.getNumDecimalPlaces(), RoundingMode.HALF_UP);
      return new BigDecimal(internal, mathContext).toPlainString();
    }

    return internal;
  }

  @Override
  public ParamHandler clone(Param param) {
    return new NumberParamHandler(this, param);
  }

  @Override
  public String getDisplayValue(QueryInstanceSpec ctxVals)
      throws WdkModelException {
    return toRawValue(ctxVals.getUser(), ctxVals.get(_param.getName()));
  }
}
