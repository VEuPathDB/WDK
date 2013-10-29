/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.util.Arrays;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.EnumParamCache;
import org.gusdb.wdk.model.user.User;

/**
 * @author jerric
 * 
 */
public class EnumParamHandler extends AbstractParamHandler {

  /**
   * the raw value is the same as stable value.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandlerPlugin#toStoredValue(org.gusdb
   *      .wdk.model.user.User, java.lang.String, java.util.Map)
   */
  @Override
  public String toStableValue(User user, String rawValue,
      Map<String, String> contextValues) throws WdkUserException {
    return rawValue;
  }

  /**
   * the raw value is the same as stable value.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandlerPlugin#toRawValue(org.gusdb
   *      .wdk.model.user.User, java.lang.String, java.util.Map)
   */
  @Override
  public String toRawValue(User user, String stableValue,
      Map<String, String> contextValues) throws WdkUserException {
    return stableValue;
  }

  /**
   * return a string representation of a list of the internals. If noTranslation
   * is true, returns a string representation of a list of terms instead. If
   * quoted is true, each individual value will be quoted properly.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandlerPlugin#transform(org.gusdb.
   *      wdk.model.user.User, java.lang.String, java.util.Map)
   */
  @Override
  public String toInternalValue(User user, String stableValue,
      Map<String, String> contextValues) throws WdkUserException {
    if (stableValue == null || stableValue.length() == 0)
      return stableValue;

    AbstractEnumParam enumParam = (AbstractEnumParam) param;
    EnumParamCache cache = enumParam.getValueCache(contextValues);

    String[] terms = enumParam.convertToTerms(stableValue);
    StringBuilder buffer = new StringBuilder();
    for (String term : terms) {
      if (!cache.containsTerm(term))
        continue;

      String internal = (param.isNoTranslation()) ? term
          : cache.getInternal(term);

      if (enumParam.getQuote()
          && !(internal.startsWith("'") && internal.endsWith("'")))
        internal = "'" + internal.replaceAll("'", "''") + "'";
      if (buffer.length() != 0)
        buffer.append(", ");
      buffer.append(internal);
    }
    return buffer.toString();
  }

  /**
   * the stable value is a sorted list of terms.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toSignature(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toSignature(User user, String stableValue,
      Map<String, String> contextValues) throws WdkUserException,
      WdkModelException {
    AbstractEnumParam enumParam = (AbstractEnumParam) param;
    String[] terms = enumParam.convertToTerms(stableValue);
    Arrays.sort(terms);
    StringBuilder buffer = new StringBuilder();
    for (String term : terms) {
      if (buffer.length() > 0)
        buffer.append(",");
      buffer.append(term);
    }
    return buffer.toString();
  }
}
