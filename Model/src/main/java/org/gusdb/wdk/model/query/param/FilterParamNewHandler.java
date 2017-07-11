package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.gusdb.fgputil.EncryptionUtil;
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
        "max": 2.19
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
  public String toInternalValue(User user, String stableValue, Map<String, String> contextParamValues)
      throws WdkModelException {
    try {
      JSONObject jsValue = new JSONObject(stableValue);
      return toInternalValue(user, jsValue, contextParamValues, (FilterParamNew)this.param);
    }
    catch (JSONException ex) {
      throw new WdkModelException(ex);
    }
  }

  // TODO: add OR clause if unknowns=true
  static String toInternalValue(User user, JSONObject jsValue, Map<String, String> contextParamValues, FilterParamNew param)
      throws WdkModelException {

    try {
      return getFilteredValue(user, jsValue, contextParamValues, param, param.getMetadataQuery());
    }
    catch (JSONException ex) {
      throw new WdkModelException(ex);
    }
  }

  // this is factored out to allow use with an alternative metadata query (eg, the summaryMetadataQuery)
  static String getFilteredValue(User user, JSONObject jsValue, Map<String, String> contextParamValues, FilterParamNew param, Query metadataQuery)
      throws WdkModelException {

    try {
      QueryInstance<?> instance = MetaDataItemFetcher.getQueryInstance(user, contextParamValues, metadataQuery);
      String metadataSql = instance.getSql();
      Map<String, OntologyItem> ontology = param.getOntology(user, contextParamValues);
      JSONArray jsFilters = getFilters(jsValue);
      String metadataTableName = "md";
      String filterSelectSql = "SELECT distinct md.internal FROM (" + metadataSql + ") md";

      if (jsFilters.length() == 0) return filterSelectSql;

      StringBuilder filtersSql = new StringBuilder();
      for (int i = 0; i < jsFilters.length(); i++) {
        if (i > 0) filtersSql.append(" INTERSECT ");
        JSONObject jsFilter = jsFilters.getJSONObject(i);
        filtersSql.append(filterSelectSql);
        filtersSql.append(getFilterAsAndClause(jsFilter, ontology, metadataTableName));
      }
      return filtersSql.toString();
    }
    catch (JSONException | WdkUserException ex) {
      throw new WdkModelException(ex);
    }
  }

  // include in where clause a filter by ontology_id
  private static String getFilterAsAndClause(JSONObject jsFilter, Map<String,
      OntologyItem> ontology, String metadataTableName) throws WdkUserException {

    OntologyItem ontologyItem = ontology.get(jsFilter.getString(FILTERS_FIELD));
    String type = ontologyItem.getType();
    String columnName = FilterParamNew.typeToColumn(type);

    String whereClause = " WHERE " + FilterParamNew.COLUMN_ONTOLOGY_ID + " = '" + ontologyItem.getOntologyId() + "'";

    if (ontologyItem.getIsRange())
      return whereClause + getRangeAndClause(jsFilter, columnName, metadataTableName, type);
    else
      return whereClause + getMembersAndClause(jsFilter, columnName, metadataTableName, type.equals(OntologyItem.TYPE_NUMBER));
  }

  private static String getRangeAndClause(JSONObject jsFilter, String columnName, String metadataTableName, String type) throws WdkUserException {

    if (type.equals(OntologyItem.TYPE_STRING)) throw new WdkUserException("Invalid JSON:  a STRING type cannot be a range");

    // If min or max is null, use an unbounded range
    JSONObject range = jsFilter.getJSONObject(FILTERS_VALUE);
    
    String minStr;
    String maxStr;
    
    if (type.equals(OntologyItem.TYPE_NUMBER)) {
      Double min = range.isNull(FILTERS_MIN) ? null : range.getDouble(FILTERS_MIN);
      Double max = range.isNull(FILTERS_MAX) ? null : range.getDouble(FILTERS_MAX);
      minStr = min.toString();
      maxStr = max.toString();
    }
    
    else if (type.equals(OntologyItem.TYPE_DATE)) {
      minStr = range.isNull(FILTERS_MIN) ? null : "date '" + range.getString(FILTERS_MIN) + "'";
      maxStr = range.isNull(FILTERS_MAX) ? null : "date '" + range.getString(FILTERS_MAX) + "'";
    }  
    
    else throw new WdkUserException("Invalid JSON:  a " + type + " type cannot be a range");
    
    String clauseStart = " AND " + metadataTableName + "." + columnName;
    if (minStr == null) return clauseStart + " <= " + maxStr;
    if (maxStr == null) return clauseStart + " >= " + minStr;
    return clauseStart + " >= " + minStr + " AND " + metadataTableName + "." + columnName + " <= " + maxStr;
  }

  private static String getMembersAndClause(JSONObject jsFilter, String columnName, String metadataTableName, boolean isNumber) {
    JSONArray values = jsFilter.getJSONArray(FILTERS_VALUE);

    if (values.length() == 0) {
      return " AND 1 != 1";
    }

    StringBuilder sb = new StringBuilder();
    for (int j = 0; j < values.length(); j++) {
      String val = (values.get(j) == JSONObject.NULL)? "unknown" : values.getString(j);
      if (!isNumber) val = "'" + val + "'";
      if (j != 0) sb.append(",");
      sb.append(val);
    }
    return " AND " + metadataTableName + "." + columnName + " IN (" + sb + ") ";
  }

  private static String getMetadataQuerySql(User user, Map<String, String> contextParamValues,
      FilterParamNew filterParam) throws WdkModelException, WdkUserException {
    QueryInstance<?> instance = MetaDataItemFetcher.getQueryInstance(
        user, contextParamValues, filterParam.getMetadataQuery());
    return instance.getSql();
  }

  /**
   * the signature is a checksum of sorted stable value.
   *
   * @throws WdkModelException
   * @throws WdkUserException
   *
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toSignature(org.gusdb.wdk.model.user.User,
   *      java.lang.String)
   */
  @Override
  public String toSignature(User user, String stableValue) throws WdkModelException, WdkUserException {
    return EncryptionUtil.encrypt(toSignatureString(stableValue));
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

  // write out an individual filter as a compact string, suitable for use in a signature
  // do not change this method, or risk invalidating existing signatures.
  private String getFilterSignature(JSONObject jsFilter) throws WdkModelException {
    List<String> parts = new ArrayList<String>();
    try {
      //parts.add(jsFilter.getInt(FILTERS_KEY) + ":");

      // don't know if we have an array or object, so try both.
      if (jsFilter.has(FILTERS_VALUE)) {
        try {
          JSONObject value = jsFilter.getJSONObject(FILTERS_VALUE);
          parts.add(FILTERS_MIN + ":" + value.getDouble(FILTERS_MIN));
          parts.add(FILTERS_MAX + ":" + value.getDouble(FILTERS_MIN));
        } catch (JSONException ex) {
          JSONArray value = jsFilter.getJSONArray(FILTERS_VALUE);
          for (int i=0; i < value.length(); i++ ) {
            parts.add(value.getString(i));
          }
        }
      } else jsFilter.getJSONObject(FILTERS_VALUE); // force an exception because this key is absent
      return parts.toString();
    }
    catch (JSONException ex) {
      throw new WdkModelException("Parameter " + param.getPrompt() + " has invalid filter param JSON", ex);
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
    return validateStableValueSyntax(user, requestParams.getParam(param.getName()));
  }

  @Override
  public String validateStableValueSyntax(User user, String inputStableValue) throws WdkUserException, WdkModelException {
    String stableValue = inputStableValue;
    if (stableValue == null || stableValue.length() == 0) {
      // use empty value if needed
      if (!param.isAllowEmpty()) {
        throw new WdkUserException("The input to parameter '" + param.getPrompt() + "' is required.");
      }
      stableValue = param.getDefault();
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
  public String getDisplayValue(User user, String stableValue, Map<String, String> contextParamValues)
      throws WdkModelException {

    JSONObject jsValue = new JSONObject(stableValue);
    JSONArray jsFilters = getFilters(jsValue);

    if (jsFilters.length() == 0)
      return "All " + param.prompt;

    try {
      Map<String, OntologyItem> ontologyMap = ((FilterParamNew) this.param).getOntology(user,
          contextParamValues);

      String display = "";
      for (int i = 0; i < jsFilters.length(); i++) {
        JSONObject jsFilter = jsFilters.getJSONObject(i);
        OntologyItem ontologyItem = ontologyMap.get(jsFilter.getString(FILTERS_FIELD));
        display += ontologyItem.getDisplayName() + " is ";

        if (ontologyItem.getIsRange()) {
          JSONObject range = jsFilter.getJSONObject(FILTERS_VALUE);
          String min = range.getString(FILTERS_MIN);
          String max = range.getString(FILTERS_MAX);
          display += min == null ? "less than " + max
              : max == null ? "greater than " + min : "between " + min + " and " + max;
        }
        else {
          JSONArray values = jsFilter.getJSONArray(FILTERS_VALUE);
          for (int j = 0; j < values.length(); j++) {
            if (values.get(j) == JSONObject.NULL)
              values.put(j, "unknown");
          }
          display += values.join(", ");

        }
        if (i != jsFilters.length())
          display += "\n";
      }
      return display;

    }
    catch (WdkUserException e) {
      throw new WdkModelException(e);
    }
  }

  private static JSONArray getFilters(JSONObject jsValue) {
    return jsValue.has(FILTERS_KEY) ? jsValue.getJSONArray(FILTERS_KEY) : new JSONArray();
  }
}
