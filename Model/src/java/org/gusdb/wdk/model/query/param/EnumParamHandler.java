/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

  public static final String LABELS_SUFFIX = "-labels";
  public static final String TERMS_SUFFIX = "-values";
  
  public EnumParamHandler(){}
  
  public EnumParamHandler(EnumParamHandler handler, Param param) {
    super(handler, param);
  }

  /**
   * the raw value is a String[] of terms, and stable value is a string representation of term list.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandlerPlugin#toStoredValue(org.gusdb .wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toStableValue(User user, Object rawValue, Map<String, String> contextValues) {
    if (!(rawValue instanceof String[]))
      new Exception().printStackTrace();
    String[] terms = (String[]) rawValue;
    StringBuilder buffer = new StringBuilder();
    for (String term : terms) {
      if (buffer.length() > 0)
        buffer.append(",");
      buffer.append(term);
    }
    return buffer.toString();
  }

  /**
   * the raw value is String[] of terms, and comma separated list of terms in string representation.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandlerPlugin#toRawValue(org.gusdb .wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public Object toRawValue(User user, String stableValue, Map<String, String> contextValues) {
    String[] rawValue = stableValue.split(",");
    for (int i = 0; i < rawValue.length; i++) {
      rawValue[i] = rawValue[i].trim();
    }
    return rawValue;
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
   * the signature is a checksum of sorted stable value.
   * 
   * @throws WdkModelException
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toSignature(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toSignature(User user, String stableValue, Map<String, String> contextValues)
      throws WdkModelException {
    // make sure to get a sorted stable value;
    String[] rawValue = (String[]) toRawValue(user, stableValue, contextValues);
    Arrays.sort(rawValue);
    stableValue = toStableValue(user, rawValue, contextValues);
    return Utilities.encrypt(stableValue);
  }

  /**
   * raw value is a String[] of terms
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#getStableValue(org.gusdb.wdk.model.user.User,
   *      org.gusdb.wdk.model.query.param.RequestParams)
   */
  @Override
  public String getStableValue(User user, RequestParams requestParams) throws WdkUserException,
      WdkModelException {
    String stableValue = requestParams.getParam(param.getName());
    if (stableValue != null)
      return stableValue;

    // stable value not assigned, get raw value first;
    String[] rawValue = requestParams.getArray(param.getName());

    // get the single value, and convert it into array
    if (rawValue == null || rawValue.length == 0) {
      String value = requestParams.getParam(param.getName());
      if (value != null && value.length() > 0)
        rawValue = new String[] { value };
    }

    // use empty value if needed
    if (rawValue == null || rawValue.length == 0) {
      if (!param.isAllowEmpty())
        throw new WdkUserException("The input to parameter '" + param.getPrompt() + "' is required.");
      rawValue = param.getDefault().split(",");
    }

    return param.getStableValue(user, rawValue, new HashMap<String, String>());
  }

  @Override
  public void prepareDisplay(User user, RequestParams requestParams, Map<String, String> contextValues)
      throws WdkModelException, WdkUserException {
    AbstractEnumParam aeParam = (AbstractEnumParam) param;

    // set labels
    Map<String, String> displayMap = aeParam.getDisplayMap(contextValues);
    String[] terms = displayMap.keySet().toArray(new String[0]);
    String[] labels = displayMap.values().toArray(new String[0]);
    requestParams.setArray(param.getName() + LABELS_SUFFIX, labels);
    requestParams.setArray(param.getName() + TERMS_SUFFIX, terms);

    // get the stable value
    String stableValue = requestParams.getParam(param.getName());
    Set<String> values = new HashSet<>();
    if (stableValue == null) { // stable value not set, use default
      stableValue = aeParam.getDefault(contextValues);
      if (stableValue != null) {
        // don't validate default, just use it as is.
        for (String term : stableValue.split(",")) {
          values.add(term.trim());
        }
      }
    }
    else { // stable value set, check if any of them are invalid
      Set<String> invalidValues = new HashSet<>();
      for (String term : stableValue.split(",")) {
        term = term.trim();
        if (displayMap.containsKey(term))
          values.add(term);
        else
          invalidValues.add(term);
      }
      // store the invalid values
      String[] invalids = invalidValues.toArray(new String[0]);
      Arrays.sort(invalids);
      requestParams.setAttribute(param.getName() + Param.INVALID_VALUE_SUFFIX, invalids);
    }

    // set the stable & raw value
    if (stableValue != null)
      requestParams.setParam(param.getName(), stableValue);
    if (values.size() > 0) {
      String[] rawValue = values.toArray(new String[0]);
      requestParams.setArray(param.getName(), rawValue);
      requestParams.setAttribute(param.getName() + Param.RAW_VALUE_SUFFIX, rawValue);
    }
  }

  @Override
  public ParamHandler clone(Param param) {
    return new EnumParamHandler(this, param);
  }
}
