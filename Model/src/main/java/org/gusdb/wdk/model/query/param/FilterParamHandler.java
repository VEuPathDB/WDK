/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.EnumParamCache;
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author jerric
 * 
 */
public class FilterParamHandler extends AbstractParamHandler {

  public static final String LABELS_SUFFIX = "-labels";
  public static final String TERMS_SUFFIX = "-values";

  public static final String TERMS_KEY = "values";
  public static final String FILTERS_KEY = "filters";

  public FilterParamHandler() {}

  public FilterParamHandler(FilterParamHandler handler, Param param) {
    super(handler, param);
  }

  /**
   * the raw value is a JSON string, and same as the stable value.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandlerPlugin#toStoredValue(org.gusdb .wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toStableValue(User user, Object rawValue, Map<String, String> contextValues) {
    return (String) rawValue;
  }

  /**
   * the raw value is a JSON string, and same as the stable value.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandlerPlugin#toRawValue(org.gusdb .wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public Object toRawValue(User user, String stableValue, Map<String, String> contextValues) {
    return stableValue;
  }

  /**
   * return a string representation of a list of the internals. If noTranslation is true, returns a string
   * representation of a list of terms instead. If quoted is true, each individual value will be quoted
   * properly.
   * 
   * @throws WdkUserException
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandlerPlugin#transform(org.gusdb. wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toInternalValue(User user, String stableValue, Map<String, String> contextValues)
      throws WdkModelException {
    if (stableValue == null || stableValue.length() == 0)
      return stableValue;

    try {
      JSONObject jsValue = new JSONObject(stableValue);
      JSONArray jsTerms = jsValue.getJSONArray(TERMS_KEY);
      String[] terms = new String[jsTerms.length()];
      for (int i = 0; i < terms.length; i++) {
        terms[i] = jsTerms.getString(i);
      }

      AbstractEnumParam enumParam = (AbstractEnumParam) param;
      EnumParamCache cache = enumParam.getValueCache(user, contextValues);

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
    catch (JSONException ex) {
      throw new WdkModelException(ex);
    }
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
    try {
      JSONObject jsValue = new JSONObject(stableValue);
      JSONObject jsNewValue = new JSONObject();
      jsNewValue.put(TERMS_KEY, sort(jsValue.getJSONArray(TERMS_KEY)));
      jsNewValue.put(FILTERS_KEY, sort(jsValue.getJSONArray(FILTERS_KEY)));
      return Utilities.encrypt(jsNewValue.toString());
    }
    catch (JSONException ex) {
      throw new WdkModelException(ex);
    }

  }

  private JSONArray sort(JSONArray jsArray) throws JSONException {
    List<String> values = new ArrayList<>(jsArray.length());
    for (int i = 0; i < jsArray.length(); i++) {
      values.add(jsArray.get(i).toString());
    }
    Collections.sort(values);
    return new JSONArray(values);
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
    if (stableValue == null || stableValue.length() == 0) {
      // use empty value if needed
      if (!param.isAllowEmpty())
        throw new WdkUserException("The input to parameter '" + param.getPrompt() + "' is required.");

      stableValue = param.getDefault();
    }
    return stableValue;
  }

  @Override
  public void prepareDisplay(User user, RequestParams requestParams, Map<String, String> contextValues)
      throws WdkModelException, WdkUserException {
    AbstractEnumParam aeParam = (AbstractEnumParam) param;

    // set labels
    Map<String, String> displayMap = aeParam.getDisplayMap(user, contextValues);
    String[] terms = displayMap.keySet().toArray(new String[0]);
    String[] labels = displayMap.values().toArray(new String[0]);
    requestParams.setArray(param.getName() + LABELS_SUFFIX, labels);
    requestParams.setArray(param.getName() + TERMS_SUFFIX, terms);

    // get the stable value
    String stableValue = requestParams.getParam(param.getName());
    Set<String> values = new HashSet<>();
    if (stableValue == null) { // stable value not set, use default
      stableValue = aeParam.getDefault(user, contextValues);
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
    return new FilterParamHandler(this, param);
  }
}
