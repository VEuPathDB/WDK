package org.gusdb.wdk.model.query.param;

import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.user.User;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class NumberRangeParamHandler extends AbstractParamHandler {

  public NumberRangeParamHandler(){}

  public NumberRangeParamHandler(NumberRangeParamHandler handler, Param param) {
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
    final String stable = ctxVals.getObject().get(_param.getName());
    return stable == null || stable.length() == 0
        ? ""
        : EncryptionUtil.encrypt(stable);
  }

  /**
   * Formats the stableValue into a value that can be applied to SQL statements.
   */
  @Override
  public String toInternalValue(RunnableObj<QueryInstanceSpec> ctxVals) {
    final String stable = ctxVals.getObject().get(_param.getName());

    /* Something to do with the portal - left this alone */
    if(_param.isNoTranslation())
      return stable;

    // By now stableValue parses properly
    final JSONObject jsonVal = new JSONObject(stable);
    final NumberRangeParam param = (NumberRangeParam) _param;
    final double[] values = {
      jsonVal.getDouble("min"),
      jsonVal.getDouble("max")
    };

    // Modify both ends of the range as needed and reassemble as a JSONObject
    for(int i = 0; i < values.length; i++) {

      // If the number is in exponential form, change to decimal form
      if(stable.matches("^.*[eE].*$")) {
        values[i] = new Double(new BigDecimal(values[i]).toPlainString());
      }

      // If the number is not an integer, round it according to the number of decimal places
      // requested (or the default value).
      if(!param.isInteger()) {
        MathContext mathCtx = new MathContext(param.getNumDecimalPlaces(), RoundingMode.HALF_UP);
        values[i] = (new BigDecimal(values[i], mathCtx)).doubleValue();
      }
    }

    return new JSONObject().put("min", new Double(values[0])).put("max", new Double(values[1])).toString();
  }

  @Override
  public ParamHandler clone(Param param) {
    return new NumberRangeParamHandler(this, param);
  }

  @Override
  public String getDisplayValue(QueryInstanceSpec ctxVals) throws WdkModelException {
    return toRawValue(ctxVals.getUser(), ctxVals.get(_param.getName()))
      .replace("{", "")
      .replace("}", "")
      .replace("\"", "");
  }
}
