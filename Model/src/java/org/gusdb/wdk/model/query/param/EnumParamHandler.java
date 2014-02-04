/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.util.Arrays;
import java.util.Map;

import org.gusdb.wdk.model.Utilities;
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
   * the raw value is a String[] of terms, and stable value is a string representation of ordered term list.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandlerPlugin#toStoredValue(org.gusdb .wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toStableValue(User user, Object rawValue, Map<String, String> contextValues)
      throws WdkUserException {
    if (!(rawValue instanceof String[]))
      new Exception().printStackTrace();
    String[] terms = (String[]) rawValue;
    Arrays.sort(terms);
    StringBuilder buffer = new StringBuilder();
    for (String term : terms) {
      if (buffer.length() > 0)
        buffer.append(",");
      buffer.append(term);
    }
    return buffer.toString();
  }

  /**
   * the raw value is String[] of terms, and stable value is a sorted, and comma separated list of terms in
   * string representation.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandlerPlugin#toRawValue(org.gusdb .wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public Object toRawValue(User user, String stableValue, Map<String, String> contextValues) {
    return stableValue.split(",");
  }

  /**
   * return a string representation of a list of the internals. If noTranslation is true, returns a string
   * representation of a list of terms instead. If quoted is true, each individual value will be quoted
   * properly.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandlerPlugin#transform(org.gusdb. wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toInternalValue(User user, String stableValue, Map<String, String> contextValues) {
    if (stableValue == null || stableValue.length() == 0)
      return stableValue;

    AbstractEnumParam enumParam = (AbstractEnumParam) param;
    EnumParamCache cache = enumParam.getValueCache(contextValues);

    String[] terms = enumParam.convertToTerms(stableValue);
    StringBuilder buffer = new StringBuilder();
    for (String term : terms) {
      if (!cache.containsTerm(term))
        continue;

      String internal = (param.isNoTranslation()) ? term : cache.getInternal(term);

      if (enumParam.getQuote() && !(internal.startsWith("'") && internal.endsWith("'")))
        internal = "'" + internal.replaceAll("'", "''") + "'";
      if (buffer.length() != 0)
        buffer.append(", ");
      buffer.append(internal);
    }
    return buffer.toString();
  }

  /**
   * the stable value is a checksum of stable value.
   * 
   * @throws WdkModelException
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toSignature(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toSignature(User user, String stableValue, Map<String, String> contextValues)
      throws WdkModelException {
    return Utilities.encrypt(stableValue);
  }

  /**
   * raw value is a String[] of terms
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#getRawValue(org.gusdb.wdk.model.user.User,
   *      org.gusdb.wdk.model.query.param.RequestParams)
   */
  @Override
  public Object getRawValue(User user, RequestParams requestParams) throws WdkUserException,
      WdkModelException {
    String[] rawValue = requestParams.getArray(param.getName());

    // get the single value, and convert it into array
    if (rawValue == null || rawValue.length == 0) {
	String value = requestParams.getParam(param.getName());
        if (value != null && value.length() > 0)
          rawValue = new String[]{ value };
    }

    // use empty value if needed
    if (rawValue == null || rawValue.length == 0) {
      if (!param.isAllowEmpty())
        throw new WdkUserException("The input to parameter '" + param.getPrompt() + "' is required.");
      rawValue = param.getDefault().split(",");
    }

    return rawValue;
  }
}
