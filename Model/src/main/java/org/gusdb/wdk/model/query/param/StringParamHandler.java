package org.gusdb.wdk.model.query.param;

import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.user.User;

/**
 * @author jerric
 */
public class StringParamHandler extends AbstractParamHandler {

  public StringParamHandler(){}

  public StringParamHandler(StringParamHandler handler, Param param) {
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
   * the signature is a checksum of the stable value.
   *
   */
  @Override
  public String toSignature(RunnableObj<QueryInstanceSpec> ctxVals) {
    final String stable = ctxVals.get().get(_param.getName());
    return stable == null || stable.isEmpty()
        ? ""
        : EncryptionUtil.encrypt(stable);
  }

  /**
   * If number is true, the internal is a string representation of a parsed Double; otherwise, quotes are
   * properly applied; If noTranslation is true, the reference value is used without any change.
   */
  @Override
  public String toInternalValue(RunnableObj<QueryInstanceSpec> ctxVals) throws WdkModelException {
    final String stable = ctxVals.get().get(_param.getName());

    if (_param.isNoTranslation())
      return stable;

    StringParam stringParam = (StringParam) _param;

    if (stringParam.isNumber()) {
      return stable.replaceAll(",", "");
    } else if (stringParam.getIsSql()) {
      return stable;
    } else {
      return "'" + stable.replaceAll("'", "''") + "'";
    }
  }

  @Override
  public String toEmptyInternalValue() {
    return "?";
  }

  @Override
  public ParamHandler clone(Param param) {
    return new StringParamHandler(this, param);
  }
}
