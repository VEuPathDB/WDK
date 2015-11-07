/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;

/**
 * @author jerric
 * 
 */
public class EnumParamHandler extends AbstractParamHandler {

  public static final String LABELS_SUFFIX = "-labels";
  public static final String TERMS_SUFFIX = "-values";

  public EnumParamHandler() {}

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
  public String toStableValue(User user, Object rawValue, Map<String, String> contextParamValues) {
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
   * the raw value is String[] of terms, and comma separated list of terms in string representation.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandlerPlugin#toRawValue(org.gusdb .wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String[] toRawValue(User user, String stableValue, Map<String, String> contextParamValues) {
    if (stableValue == null)
      return null;
    String[] rawValue = stableValue.split(",+");
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
   * @throws WdkModelException
   * @throws WdkUserException
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandlerPlugin#transform(org.gusdb. wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toInternalValue(User user, String stableValue, Map<String, String> contextParamValues)
      throws WdkModelException, WdkUserException {
    if (stableValue == null || stableValue.length() == 0)
      return stableValue;

    AbstractEnumParam enumParam = (AbstractEnumParam) param;
    EnumParamVocabInstance cache = enumParam.getVocabInstance(user, contextParamValues);

    String[] terms = enumParam.convertToTerms(stableValue);
    Set<String> internals = new LinkedHashSet<>();
    for (String term : terms) {
      if (!cache.containsTerm(term))
        throw new WdkUserException("The term '" + term + "' is invalid for param " + param.getPrompt());

      String internal = (param.isNoTranslation()) ? term : cache.getInternal(term);

      if (enumParam.getQuote() && !(internal.startsWith("'") && internal.endsWith("'")))
        internal = "'" + internal.replaceAll("'", "''") + "'";
      
      internals.add(internal);
    }
    DBPlatform platform = wdkModel.getAppDb().getPlatform();
    return platform.prepareExpressionList(internals.toArray(new String[0]));
  }

  /**
   * the signature is a checksum of sorted stable value.
   * 
   * @throws WdkModelException
   * @throws WdkUserException 
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toSignature(org.gusdb.wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toSignature(User user, String stableValue, Map<String, String> contextParamValues)
      throws WdkModelException, WdkUserException {
    AbstractEnumParam enumParam = (AbstractEnumParam) param;
    // EnumParamCache cache = enumParam.getValueCache(user, contextParamValues);

    String[] terms = enumParam.convertToTerms(stableValue);
    // jerric - we should use terms to generate signature, not internal value. I don't remember
    // when and why we switched to internal values. I will revert it back to term.
    // Furthermore, I will skip validating the terms here, otherwise, it breaks the deep clone
    // of the steps, which prevents us from revising saved invalid strategies.
 
//    Set<String> internals = new LinkedHashSet<>();
//    for (String term : terms) {
//      if (!cache.containsTerm(term))
//        throw new WdkUserException("The term '" + term + "' is invalid for param " + param.getPrompt());

//     String internal = (param.isNoTranslation()) ? term : cache.getInternal(term);
//      internals.add(internal);
//    }
//    String[] array = internals.toArray(new String[0]);
    String[] array = terms;
    Arrays.sort(array);
    return Utilities.encrypt(Arrays.toString(array));
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
      rawValue = param.getDefault().split(",+");
    }

    return param.getStableValue(user, rawValue, new HashMap<String, String>());
  }

  @Override
  public void prepareDisplay(User user, RequestParams requestParams, Map<String, String> contextParamValues)
      throws WdkModelException, WdkUserException {
    AbstractEnumParam aeParam = (AbstractEnumParam) param;

    // set labels
    Map<String, String> displayMap = aeParam.getDisplayMap(user, contextParamValues);
    String[] terms = displayMap.keySet().toArray(new String[0]);
    String[] labels = displayMap.values().toArray(new String[0]);
    requestParams.setArray(param.getName() + LABELS_SUFFIX, labels);
    requestParams.setArray(param.getName() + TERMS_SUFFIX, terms);

    // get the stable value
    String stableValue = requestParams.getParam(param.getName());
    Set<String> values = new HashSet<>();
    if (stableValue == null) { // stable value not set, use default
      stableValue = aeParam.getDefault(user, contextParamValues);
      if (stableValue != null) {
        // don't validate default, just use it as is.
        for (String term : stableValue.split(",+")) {
          values.add(term.trim());
        }
      }
    }
    else { // stable value set, check if any of them are invalid
      Set<String> invalidValues = new HashSet<>();
      for (String term : stableValue.split(",+")) {
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

  @Override
  public String getDisplayValue(User user, String stableValue, Map<String, String> contextParamValues)
      throws WdkModelException {
    AbstractEnumParam aeParam = (AbstractEnumParam) param;
    Map<String, String> displays = aeParam.getDisplayMap(user, contextParamValues);
    String[] terms = toRawValue(user, stableValue, contextParamValues);
    StringBuilder buffer = new StringBuilder();
    for (String term : terms) {
      if (buffer.length() > 0) buffer.append(", ");
      buffer.append(displays.get(term));
    }
    return buffer.toString();
  }
}
