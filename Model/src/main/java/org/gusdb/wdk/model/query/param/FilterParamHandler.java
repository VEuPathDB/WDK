/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
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
  public String toStableValue(User user, Object rawValue) {
    return (String) rawValue;
  }

  /**
   * the raw value is a JSON string, and same as the stable value.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandlerPlugin#toRawValue(org.gusdb .wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toRawValue(User user, String stableValue) {
    stableValue = normalizeStableValue(stableValue);
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
  public String toInternalValue(User user, String stableValue, ValidatedParamStableValues contextParamValues)
      throws WdkModelException {
    if (stableValue == null || stableValue.length() == 0)
      return stableValue;

    stableValue = normalizeStableValue(stableValue);

    try {
      JSONObject jsValue = new JSONObject(stableValue);
      JSONArray jsTerms = jsValue.getJSONArray(TERMS_KEY);
      String[] terms = new String[jsTerms.length()];
      for (int i = 0; i < terms.length; i++) {
        terms[i] = jsTerms.getString(i);
      }

      AbstractEnumParam enumParam = (AbstractEnumParam) _param;
      EnumParamVocabInstance cache = enumParam.getVocabInstance(user, contextParamValues);

      Set<String> internals = new LinkedHashSet<>();

      for (String term : terms) {
        if (!cache.containsTerm(term))
          continue;

        String internal = _param.isNoTranslation() ? term : cache.getInternal(term);

        if (enumParam.getQuote() && !(internal.startsWith("'") && internal.endsWith("'")))
          internal = "'" + internal.replaceAll("'", "''") + "'";
        internals.add(internal);
      }
      DBPlatform platform = _wdkModel.getAppDb().getPlatform();
      return platform.prepareExpressionList(internals.toArray(new String[0]));
    }
    catch (JSONException ex) {
      throw new WdkModelException(ex);
    }
  }

  /**
   * the signature is a checksum of sorted stable value.
   * 
   * @throws WdkModelException
   * @throws WdkUserException 
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toSignature(org.gusdb.wdk.model.user.User,
   *      java.lang.String, Map)
   */
  @Override
  public String toSignature(User user, String stableValue, ValidatedParamStableValues contextParamValues)
      throws WdkModelException, WdkUserException {
    stableValue = normalizeStableValue(stableValue);
    try {
      JSONObject jsValue = new JSONObject(stableValue);
      JSONArray jsTerms = jsValue.getJSONArray(TERMS_KEY);
      String[] terms = new String[jsTerms.length()];
      for (int i = 0; i < terms.length; i++) {
        terms[i] = jsTerms.getString(i);
      }

      // return stable values, instead of list of terms
      if (_param.isNoTranslation()) {
        return stableValue;
      }

      Arrays.sort(terms);
      return EncryptionUtil.encrypt(Arrays.toString(terms));
    }
    catch (JSONException ex) {
      throw new WdkModelException(ex);
    }

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
    return validateStableValueSyntax(user, requestParams.getParam(_param.getName()));
  }
  
  @Override
  public String validateStableValueSyntax(User user, String inputStableValue) throws WdkUserException, WdkModelException {
    String stableValue = inputStableValue;
    if (stableValue == null || stableValue.length() == 0) {
      // use empty value if needed
      if (!_param.isAllowEmpty())
        throw new WdkUserException("The input to parameter '" + _param.getPrompt() + "' is required.");

      stableValue = _param.getDefault();
    }
    stableValue = normalizeStableValue(stableValue);
    return stableValue;
  }
  
  @Override
  public void prepareDisplay(User user, RequestParams requestParams, ValidatedParamStableValues contextParamValues)
      throws WdkModelException, WdkUserException {
    AbstractEnumParam aeParam = (AbstractEnumParam) _param;

    // set labels
    Map<String, String> displayMap = aeParam.getDisplayMap(user, contextParamValues);
    String[] terms = displayMap.keySet().toArray(new String[0]);
    String[] labels = displayMap.values().toArray(new String[0]);
    requestParams.setArray(_param.getName() + LABELS_SUFFIX, labels);
    requestParams.setArray(_param.getName() + TERMS_SUFFIX, terms);

    // get the stable value
    String stableValue = requestParams.getParam(_param.getName());
    Set<String> values = new HashSet<>();
    if (stableValue == null) { // stable value not set, use default
      stableValue = aeParam.getDefault(user, contextParamValues);
    }
    stableValue = normalizeStableValue(stableValue);
    Set<String> invalidValues = new HashSet<>();
    JSONObject jsValue = new JSONObject(stableValue);
    JSONArray jsTerms = jsValue.getJSONArray(TERMS_KEY);
    for (int i = 0; i < jsTerms.length(); i++) {
      String term = jsTerms.getString(i);
      if (displayMap.containsKey(term))
        values.add(term);
      else
        invalidValues.add(term);
    }
    // store the invalid values
    String[] invalids = invalidValues.toArray(new String[0]);
    Arrays.sort(invalids);
    requestParams.setAttribute(_param.getName() + Param.INVALID_VALUE_SUFFIX, invalids);

    // set the stable & raw value
    if (stableValue != null)
      requestParams.setParam(_param.getName(), stableValue);

    if (values.size() > 0) {
      String[] rawValue = values.toArray(new String[0]);
      requestParams.setArray(_param.getName(), rawValue);
      requestParams.setAttribute(_param.getName() + Param.RAW_VALUE_SUFFIX, rawValue);
    }
  }

  @Override
  public ParamHandler clone(Param param) {
    return new FilterParamHandler(this, param);
  }

  @Override
  public String getDisplayValue(User user, String stableValue, ValidatedParamStableValues contextParamValues)
      throws WdkModelException {
    stableValue = normalizeStableValue(stableValue);
    JSONObject jsValue = new JSONObject(stableValue);
    JSONArray jsFilters = jsValue.getJSONArray(FILTERS_KEY);
    try {
      Map<String, Map<String, String>> metadataSpec = ((FilterParam) this._param).getMetadataSpec(user, contextParamValues);
      if (jsFilters.length() == 0)
        return "All " + _param._prompt;
      else {
        String display = "";
        for (int i = 0; i < jsFilters.length(); i++) {
          JSONObject jsFilter = jsFilters.getJSONObject(i);
          Map<String, String> fieldSpec = metadataSpec.get(jsFilter.getString("field"));
          display += fieldSpec.get("display") + " is ";
          switch(fieldSpec.get("filter")) {
          case "membership":
            JSONArray values = jsFilter.getJSONArray("value");
            for (int j = 0; j < values.length(); j++) {
              if (values.get(j) == JSONObject.NULL) values.put(j, "uknown");
            }
            display += jsFilter.getJSONArray("value").join(", ");
            break;
          case "range":
            JSONObject range = jsFilter.getJSONObject("value");
            Object min = range.get("min");
            Object max = range.get("max");
            display += min == null ? "less than " + max.toString()
                     : max == null ? "greater than " + min.toString()
                     : "between " + min.toString() + " and " + max.toString();
            break;
          }
          if (i != jsFilters.length()) display += "\n";
        }
        return display;
      }
    } catch (WdkUserException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return jsValue.getJSONArray(TERMS_KEY).toString();
    }

  }

  private String normalizeStableValue(String stableValue) {
    JSONObject jsValue;
    if (!stableValue.startsWith("{")) {
      jsValue = new JSONObject();
      jsValue.put(FILTERS_KEY, new JSONArray());

      JSONArray jsTerms = new JSONArray();
      for (String term : stableValue.split(",")) {
        jsTerms.put(term.trim());
      }
      jsValue.put(TERMS_KEY, jsTerms);
    }
    else {
      jsValue = new JSONObject(stableValue);
      if (!jsValue.has(FILTERS_KEY))
        jsValue.put(FILTERS_KEY, new JSONArray());
      if (!jsValue.has(TERMS_KEY))
        jsValue.put(TERMS_KEY, new JSONArray());
    }
    return JsonUtil.serialize(jsValue);
  }
}
