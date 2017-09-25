package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author jerric
 *
 */
public class FilterParamNewHandler extends AbstractParamHandler {

  public static final String LABELS_SUFFIX = "-labels";
  public static final String TERMS_SUFFIX = "-values";

  public static final String TERMS_KEY = "values";
  public static final String FILTERS_KEY = "filters";
  public static final String FILTERS_FIELD = "field";
  public static final String FILTERS_VALUE = "value";
  public static final String FILTERS_MIN = "min";
  public static final String FILTERS_MAX = "max";
  public static final String FILTERS_INCLUDE_UNKNOWN = "includeUnknown";

  public FilterParamNewHandler() {}

  public FilterParamNewHandler(FilterParamNewHandler handler, Param param) {
    super(handler, param);
  }

  /**
   * the raw value is a JSON string, and same as the stable value.
   *
{
   "filters": [
    {
      "value": {
        "min": 1.82,
        "max": 2.19,
        "includeUnknowns": true    (optional)
      },
      "field": "age"
    },
    {
      "value": [
        "female"
      ],
      "field": "biological sex"
    }
  ]
}
   *
   * @see org.gusdb.wdk.model.query.param.ParamHandlerPlugin#toStoredValue(org.gusdb .wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   *      This method is not relevant to service layer, since it only uses stable values, never raw values.
   */
  @Override
  public String toStableValue(User user, Object rawValue, Map<String, String> contextParamValues) {
    return (String) rawValue;
  }

  /**
   * the raw value is a JSON string, and same as the stable value.
   *
   * @see org.gusdb.wdk.model.query.param.ParamHandlerPlugin#toRawValue(org.gusdb .wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   *      This method is not relevant to service layer, since it only uses stable values, never raw values.
   */
  @Override
  public String toRawValue(User user, String stableValue, Map<String, String> contextParamValues) {
    return stableValue;
  }

  /**
   * return SQL that runs the metadataQuery, including its depended params, and applies
   * the filters to it.
   *

    SELECT mf.internal
      FROM (${metadata_qc}) mf
      WHERE mf.ontology_term_id = 'age'
      AND mf.numeric_value      >= 66
      AND mf.numeric_value      <= 80
    INTERSECT
    SELECT mf.internal
      FROM mf.${metadata_qc} mf
      WHERE mf.ontology_term_id = 'mood'
      AND mf.string_value       IN ('confused', 'happy')
    INTERSECT
    SELECT mf.internal
      FROM (${metadata_qc}) mf
      WHERE mf.ontology_term_id = 'size'
      AND mf.string_value       IN ('large')
   *
   * @throws WdkUserException
   *
   * @see org.gusdb.wdk.model.query.param.ParamHandlerPlugin#transform(org.gusdb. wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toInternalValue(User user, String stableValueString, Map<String, String> contextParamValues)
      throws WdkModelException {
    try {
      FilterParamNew fpn = (FilterParamNew) _param;
      FilterParamNewStableValue stableValue = new FilterParamNewStableValue(stableValueString, fpn);
      String err = stableValue.validateSyntaxAndSemantics();
      if (err != null) throw new WdkModelException(err);
      String fv = getFilteredValue(user, stableValue, contextParamValues, fpn, fpn.getMetadataQuery());
      return fpn.getUseIdTransformSqlForInternalValue()? fpn.transformIdSql(fv): fv;
    }
    catch (JSONException ex) {
      throw new WdkModelException(ex);
    }
  }


  // this is factored out to allow use with an alternative metadata query (eg, the summaryMetadataQuery)
  static String getFilteredValue(User user, FilterParamNewStableValue stableValue, Map<String, String> contextParamValues, FilterParamNew param, Query metadataQuery)
      throws WdkModelException {

    try {
      String metadataSql;
      try {
        QueryInstance<?> instance = metadataQuery.makeInstance(user, contextParamValues, true, 0, new HashMap<String, String>());

        metadataSql = instance.getSql();
      } catch (WdkUserException e) {
        throw new WdkModelException(e);
      }
      Map<String, OntologyItem> ontology = param.getOntology(user, contextParamValues);
      List<FilterParamNewStableValue.Filter> filters = stableValue.getFilters();
      String metadataTableName = "md";
      String filterSelectSql = "SELECT distinct md.internal FROM (" + metadataSql + ") md";

      if (filters.size() == 0) return filterSelectSql;

      List<String> filterSqls = new ArrayList<String>();
      for (FilterParamNewStableValue.Filter filter : filters) 
        filterSqls.add(filterSelectSql + filter.getFilterAsAndClause(metadataTableName, ontology));

      return FormatUtil.join(filterSqls, " INTERSECT ");
    }
    catch (JSONException  ex) {
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
  public String toSignature(User user, String stableValue, Map<String, String> contextParamValues) throws WdkModelException, WdkUserException {
    return EncryptionUtil.encrypt(toSignatureString(stableValue) + dependedParamsSignature(user, contextParamValues));
  }

  // convert stable value to a compact string, suitable for use in a signature
  // do not change this method, or risk invalidating existing signatures.
  // also useful to do syntax validation of stableValue JSON
  private String toSignatureString(String stableValue) throws WdkModelException {
    try {
      JSONObject jsValue = new JSONObject(stableValue);
      JSONArray jsFilters = getFilters(jsValue);
      List<String> filterSigsList = new ArrayList<String>();
      for (int i = 0; i < jsFilters.length(); i++) {
        JSONObject jsFilter = jsFilters.getJSONObject(i);
        String filterSig = getFilterSignature(jsFilter);
        filterSigsList.add(filterSig);
      }
      Collections.sort(filterSigsList);
      // wrap with brackets since a signature string cannot be empty (Utilities.encrypt will throw)
      return "[" + filterSigsList.stream().collect(Collectors.joining(",")) + "]";
    }
    catch (JSONException ex) {
      throw new WdkModelException(ex);
    }
  }
  
  private static JSONArray getFilters(JSONObject jsValue) {
    return jsValue.has(FILTERS_KEY) ? jsValue.getJSONArray(FILTERS_KEY) : new JSONArray();
  }

  // write out an individual filter as a compact string, suitable for use in a signature
  // do not change this method, or risk invalidating existing signatures.
  private String getFilterSignature(JSONObject jsFilter) throws WdkModelException {
    List<String> parts = new ArrayList<>();
    try {
      //parts.add(jsFilter.getInt(FILTERS_KEY) + ":");

      // don't know if we have an array or object, so try both.
      if (jsFilter.has(FILTERS_VALUE)) {
        try {
          // this might throw, which probably means we have an array.
          JSONObject value = jsFilter.getJSONObject(FILTERS_VALUE);
          parts.add(FILTERS_MIN + ":" + value.get(FILTERS_MIN));
          parts.add(FILTERS_MAX + ":" + value.get(FILTERS_MIN));
        } catch (JSONException ex) {
          JSONArray value = jsFilter.getJSONArray(FILTERS_VALUE);
          for (int i=0; i < value.length(); i++ ) {
            Object v = value.get(i);
            parts.add(v == null ? null : v.toString());
          }
        }
      }
      return parts.toString();
    }
    catch (JSONException ex) {
      throw new WdkModelException("Parameter " + _param.getPrompt() + " has invalid filter param JSON", ex);
    }
  }
  
  private String dependedParamsSignature(User user, Map<String, String> contextParamValues) throws WdkModelException, WdkUserException {
    FilterParamNew filterParam  = (FilterParamNew)_param;
    if (filterParam.getDependedParams() == null) return "";
    List<Param> dependedParamsList = new ArrayList<Param>(filterParam.getDependedParams());
    java.util.Collections.sort(dependedParamsList);
    StringBuilder sb = new StringBuilder();
    for (Param dependedParam : dependedParamsList)  {
      String stableValue = contextParamValues.get(dependedParam.getName());
      if (stableValue == null) throw new WdkModelException("can't find value for param " + dependedParam.getName());
      sb.append(dependedParam.getParamHandler().toSignature(user, stableValue, contextParamValues));
    }

    return sb.toString();
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
      if (!_param.isAllowEmpty()) {
        throw new WdkUserException("The input to parameter '" + _param.getPrompt() + "' is required.");
      }
      stableValue = _param.getDefault();
    }
    toSignatureString(stableValue);  // this method validates the syntax
    return stableValue;
  }

  @Override
  public void prepareDisplay(User user, RequestParams requestParams, Map<String, String> contextParamValues)
      throws WdkModelException, WdkUserException {
    // do nothing
   }

  @Override
  public ParamHandler clone(Param param) {
    return new FilterParamNewHandler(this, param);
  }

  @Override
  public String getDisplayValue(User user, String stableValueString, Map<String, String> contextParamValues)
      throws WdkModelException {

    FilterParamNew param = (FilterParamNew)_param;
    FilterParamNewStableValue stableValue = new FilterParamNewStableValue(stableValueString, param);
    return stableValue.getDisplayValue(user, contextParamValues);
  } 

}
