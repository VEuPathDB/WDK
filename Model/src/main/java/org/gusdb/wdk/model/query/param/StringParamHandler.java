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
   * the raw value is the same as stable value.
   */
  @Override
  public String toRawValue(User user, String stableValue) {
    return stableValue;
  }

  /**
   * the signature is a checksum of the stable value.
   *
   */
  @Override
  public String toSignature(RunnableObj<QueryInstanceSpec> ctxVals) {
    final String stable = ctxVals.getObject().get(_param.getName());
    return stable == null || stable.length() == 0
        ? ""
        : EncryptionUtil.encrypt(stable);
  }

  /**
   * If number is true, the internal is a string representation of a parsed Double; otherwise, quotes are
   * properly applied; If noTranslation is true, the reference value is used without any change.
   */
  @Override
  public String toInternalValue(RunnableObj<QueryInstanceSpec> ctxVals) throws WdkModelException {
    final String stable = ctxVals.getObject().get(_param.getName());

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
  public ParamHandler clone(Param param) {
    return new StringParamHandler(this, param);
  }

  @Override
  public String getDisplayValue(QueryInstanceSpec ctxVals)
      throws WdkModelException {
    return toRawValue(ctxVals.getUser(), ctxVals.get(_param.getName()));
  }
}
