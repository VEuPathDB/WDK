package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QueryInstance;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;


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

  /*
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
      String err = stableValue.validateSyntaxAndSemantics(user, contextParamValues);
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
        filterSqls.add(filterSelectSql + filter.getFilterAsWhereClause(metadataTableName, ontology));

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
  public String toSignature(User user, String stableValueString, Map<String, String> contextParamValues) throws WdkModelException, WdkUserException {
    FilterParamNew param = (FilterParamNew)_param;
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
