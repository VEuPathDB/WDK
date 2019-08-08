package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.MapBuilder;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.SqlQueryInstance;
import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @author jerric
 *
 */
public class FilterParamNewHandler extends AbstractParamHandler {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(FilterParamNewHandler.class);

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

  /*
   *
   * @see org.gusdb.wdk.model.query.param.ParamHandlerPlugin#toStoredValue(org.gusdb .wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   *      This method is not relevant to service layer, since it only uses stable values, never raw values.
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
   *      This method is not relevant to service layer, since it only uses stable values, never raw values.
   */
  @Override
  public String toRawValue(User user, String stableValue) {
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
      contextParamValues = fpn.ensureRequiredContext(user, contextParamValues);
      FilterParamNewStableValue stableValue = new FilterParamNewStableValue(stableValueString, fpn);
      String fvSql = fpn.getFilteredMetadataSql(user, stableValue, contextParamValues, fpn.getMetadataQuery(), null);
      String cachedSql = getCachedFilteredSql(user, fvSql, _param.getWdkModel());
      return "select " + FilterParamNew.COLUMN_INTERNAL + " from (" + cachedSql + ") fs";
      
    }
    catch (JSONException ex) {
      throw new WdkModelException(ex);
    }
  }
   
  private String getCachedFilteredSql(User user, String filteredSql, WdkModel wdkModel) throws WdkModelException {

     try {
       // get an sqlquery so we can cache this internal value. it is parameterized by the sql itself
       SqlQuery sqlQuery = getSqlQueryForInternalValue(wdkModel);
       Map<String, String> paramValues = new MapBuilder<String, String>("sql", filteredSql).toMap();
       SqlQueryInstance instance = sqlQuery.makeInstance(user, paramValues, false, 0, Collections.emptyMap());
       return  instance.getSqlUnsorted(); // because isCacheable=true, we get the cached sql
     }
     catch (WdkUserException e) {
       throw new WdkModelException(e);
     }

   }

   private SqlQuery getSqlQueryForInternalValue(WdkModel wdkModel) throws WdkModelException {
     SqlQuery sqlQuery = new SqlQuery();
     sqlQuery.setName("InternalValue");
     sqlQuery.setSql("select distinct " + FilterParamNew.COLUMN_INTERNAL + " from ( $$sql$$) sq");  // the sql will be provided by the sql param
     Column column = new Column();
     column.setName(FilterParamNew.COLUMN_INTERNAL);
     sqlQuery.addColumn(column);
     sqlQuery.setDoNotTest(true);
     sqlQuery.setIsCacheable(true);
     StringParam sqlParam = new StringParam();
     sqlParam.setName("sql");
     sqlParam.setNoTranslation(true);  // avoid quotes
     sqlQuery.addParam(sqlParam);
     sqlQuery.resolveReferences(wdkModel);
     QuerySet querySet= new QuerySet();
     querySet.setName("FilterParamNew");
     sqlQuery.setQuerySet(querySet);
     return sqlQuery;
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
  public String toSignature(User user, String stableValueString, Map<String, String> contextParamValues) throws WdkModelException, WdkUserException {
    FilterParamNew param = (FilterParamNew)_param;
    contextParamValues = param.ensureRequiredContext(user, contextParamValues);

    FilterParamNewStableValue stableValue = new FilterParamNewStableValue(stableValueString, param);
    return EncryptionUtil.encrypt(stableValue.toSignatureString() + dependedParamsSignature(user, contextParamValues));
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
    String stableValueString = inputStableValue;
    if (stableValueString == null || stableValueString.length() == 0) {
      // use empty value if needed
      if (!_param.isAllowEmpty()) {
        throw new WdkUserException("The input to parameter '" + _param.getPrompt() + "' is required.");
      }
      stableValueString = _param.getDefault();
    }
    FilterParamNew filterParam = (FilterParamNew) _param;
    FilterParamNewStableValue stableValue = new FilterParamNewStableValue(stableValueString, filterParam);
    String err = stableValue.validateSyntax();
    if (err != null) throw new WdkModelException(err);
    return stableValueString;
  }

  @Override
  public void prepareDisplay(User user, RequestParams requestParams, Map<String, String> contextParamValues)
      throws WdkModelException, WdkUserException {
    String stableValue = requestParams.getParam(_param.getName());

    // If the request doesn't include a stableValue for the param,
    // we don't have to do anything.
    if (stableValue == null) return;

    // Validate stableValue. If there are errors, we will set a request
    // attribute with the error string below.
    FilterParamNewStableValue fpnStableValue = new FilterParamNewStableValue(stableValue, (FilterParamNew) _param);
    String errors = fpnStableValue.validateSyntaxAndSemantics(user, contextParamValues);

    // do nothing
    if (errors == null) return;

    // Since there is an error, set the stable and raw value on the request to
    // an empty filters JSON, and set the errors string on the request.
    String empty = new JSONObject().put("filters", new JSONArray()).toString();
    requestParams.setParam(_param.getName(), empty);
    requestParams.setAttribute(_param.getName(), empty);
    requestParams.setAttribute(_param.getName() + Param.RAW_VALUE_SUFFIX, empty);
    requestParams.setAttribute(_param.getName() + Param.INVALID_VALUE_SUFFIX, errors);

  }

  @Override
  public ParamHandler clone(Param param) {
    return new FilterParamNewHandler(this, param);
  }

  @Override
  public String getDisplayValue(User user, String stableValueString, Map<String, String> contextParamValues)
      throws WdkModelException {

    FilterParamNew param = (FilterParamNew)_param;
    contextParamValues = param.ensureRequiredContext(user, contextParamValues);
    FilterParamNewStableValue stableValue = new FilterParamNewStableValue(stableValueString, param);
    return stableValue.getDisplayValue(user, contextParamValues);
  } 

}

