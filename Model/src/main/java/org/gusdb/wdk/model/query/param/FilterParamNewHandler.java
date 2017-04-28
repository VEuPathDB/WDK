/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
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
      Map<String, OntologyItem> ontology = ((FilterParamNew) this.param).getOntology(user, contextParamValues);
      String metadataSql = getMetadataQuerySql(user, stableValue, contextParamValues, (FilterParamNew) this.param);
      JSONObject jsValue = new JSONObject(stableValue);
      JSONArray jsFilters = jsValue.getJSONArray(FILTERS_KEY);
      String metadataTableName = "md";
      String filterSelect = "SELECT md.internal FROM (" + metadataSql + ") md WHERE md.ontology_term_id = ";
      StringBuilder filtersSql = new StringBuilder();
      for (int i = 0; i < jsFilters.length(); i++) {
        if (i > 0) filtersSql.append(" INTERSECT ");
        JSONObject jsFilter = jsFilters.getJSONObject(i);
        filtersSql.append(filterSelect + "'" + jsFilter.getString(FILTERS_FIELD) + "' ");
        filtersSql.append(getFilterAsAndClause(jsFilter, metadataSql, ontology, metadataTableName));
      }
      return filtersSql.toString();
    }
    catch (JSONException | WdkUserException ex) {
      throw new WdkModelException(ex);
    }
  }
  
  public static String toInternalValue(User user, String stableValue, Map<String, String> contextParamValues, FilterParamNew param)
      throws WdkModelException {

    try {
      Map<String, OntologyItem> ontology = param.getOntology(user, contextParamValues);
      String metadataSql = getMetadataQuerySql(user, stableValue, contextParamValues, param);
      JSONObject jsValue = new JSONObject(stableValue);
      JSONArray jsFilters = jsValue.getJSONArray(FILTERS_KEY);
      String metadataTableName = "md";
      String filterSelect = getSqlForDistinctInternals(metadataSql);

      StringBuilder filtersSql = new StringBuilder();
      for (int i = 0; i < jsFilters.length(); i++) {
        if (i > 0) filtersSql.append(" INTERSECT ");
        JSONObject jsFilter = jsFilters.getJSONObject(i);
        filtersSql.append(filterSelect + "'" + jsFilter.getString(FILTERS_FIELD) + "' ");
        filtersSql.append(getFilterAsAndClause(jsFilter, metadataSql, ontology, metadataTableName));
      }
      return filtersSql.toString();
    }
    catch (JSONException | WdkUserException ex) {
      throw new WdkModelException(ex);
    }
  }
  
  // we know that each ontology_term_id has a full set of internals, so we just need to query 
  // one ontology_term_id.
  static String getSqlForDistinctInternals(String metadataSql) {
    return "SELECT distinct md.internal FROM (" + metadataSql + ") md" 
        + " WHERE md.ontology_term_id IN (select ontology_term_id from (" + metadataSql + ") where row_num = 1)";
  }

 
  private static String getFilterAsAndClause(JSONObject jsFilter, String metadataSql, Map<String, OntologyItem> ontology, String metadataTableName) throws WdkModelException, WdkUserException {

    OntologyItem ontologyItem = ontology.get(jsFilter.getString(FILTERS_FIELD));
    String type = ontologyItem.getType();
    String columnName = FilterParamNew.typeToColumn(type);
    
    if (ontologyItem.getIsRange())
      return getRangeAndClause(jsFilter, columnName, metadataTableName);
    else 
      return getMembersAndClause(jsFilter, columnName, metadataTableName, type.equals(OntologyItem.TYPE_NUMBER));
  }
  
  private static String getRangeAndClause(JSONObject jsFilter, String columnName, String metadataTableName) {
    JSONObject range = jsFilter.getJSONObject("value");
    String min = range.getString("min");
    String max = range.getString("max");
    return "AND " + metadataTableName + "." + columnName + " > " + min + "AND " + metadataTableName + ".date_value < " + max; 
  }
  
  private static String getMembersAndClause(JSONObject jsFilter, String columnName, String metadataTableName, boolean isNumber) {
    JSONArray values = jsFilter.getJSONArray(FILTERS_VALUE);
    StringBuilder sb = new StringBuilder();
    for (int j = 0; j < values.length(); j++) {
      String val = (values.get(j) == JSONObject.NULL)? "unknown" : values.getString(j);
      if (!isNumber) val = "'" + val + "'";
      if (j != 0) sb.append(",");
      sb.append(val);
    }
    return "AND " + metadataTableName + "." + columnName + " IN (" + sb + ") ";

  }
  
  private static String getMetadataQuerySql(User user, String stableValue, Map<String, String> contextParamValues, FilterParamNew filterParam) throws WdkModelException, WdkUserException {

    QueryInstance<?> instance = MetaDataItemFetcher.getQueryInstance(user, contextParamValues, filterParam.getMetadataQuery());
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
    return Utilities.encrypt(toSignatureString(stableValue));

  }
  
  // convert stable value to a compact string, suitable for use in a signature
  // do not change this method, or risk invalidating existing signatures.
  // also useful to do syntax validation of stableValue JSON
  private String toSignatureString(String stableValue) throws WdkModelException {
    try {
      JSONObject jsValue = new JSONObject(stableValue);
      JSONArray jsFilters = jsValue.getJSONArray(FILTERS_KEY);
      List<String> filterSigsList = new ArrayList<String>();
      for (int i = 0; i < jsFilters.length(); i++) {
        JSONObject jsFilter = jsFilters.getJSONObject(i);
        String filterSig = getFilterSignature(jsFilter);
        filterSigsList.add(filterSig);
      }
      Collections.sort(filterSigsList);

      return filterSigsList.stream().collect(Collectors.joining(","));
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
      parts.add(jsFilter.getInt(FILTERS_KEY) + ":");
      
      // don't know if we have an array or object, so try both.
      if (jsFilter.has(FILTERS_VALUE)) {
        try { 
          JSONObject value = jsFilter.getJSONObject(FILTERS_VALUE);
          parts.add(FILTERS_MIN + ":" + value.getString(FILTERS_MIN));
          parts.add(FILTERS_MAX + ":" + value.getString(FILTERS_MIN));
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
      if (!param.isAllowEmpty())
        throw new WdkUserException("The input to parameter '" + param.getPrompt() + "' is required.");

      stableValue = param.getDefault();
    }
    toSignatureString(stableValue);  // this method validates the syntax
    return stableValue;
  }
  
  @Override
  public void prepareDisplay(User user, RequestParams requestParams, Map<String, String> contextParamValues)
      throws WdkModelException, WdkUserException {
    throw new UnsupportedOperationException();  // needed for JSPs, so no longer supported.
   }

  @Override
  public ParamHandler clone(Param param) {
    return new FilterParamNewHandler(this, param);
  }

  @Override
  public String getDisplayValue(User user, String stableValue, Map<String, String> contextParamValues)
      throws WdkModelException {

    JSONObject jsValue = new JSONObject(stableValue);
    JSONArray jsFilters = jsValue.getJSONArray(FILTERS_KEY);

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
          JSONObject range = jsFilter.getJSONObject("value");
          String min = range.getString("min");
          String max = range.getString("max");
          display += min == null ? "less than " + max
              : max == null ? "greater than " + min : "between " + min + " and " + max;
        }
        else {
          JSONArray values = jsFilter.getJSONArray("value");
          for (int j = 0; j < values.length(); j++) {
            if (values.get(j) == JSONObject.NULL)
              values.put(j, "uknown");
          }
          display += jsFilter.getJSONArray("value").join(", ");

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

}
