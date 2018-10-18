package org.gusdb.wdk.service.formatter.param;

import static org.gusdb.wdk.core.api.JsonKeys.MIN_SELECTED_COUNT;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.FilterParamNew;
import org.gusdb.wdk.model.query.param.OntologyItem;
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FilterParamNewFormatter extends ParamFormatter<FilterParamNew> implements DependentParamProvider {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(FilterParamNewFormatter.class);

  protected FilterParamNew filterParam;

  FilterParamNewFormatter(FilterParamNew param) {
    super(param);
    this.filterParam = param;
  }

  @Override
  public JSONObject getJson(User user, Map<String, String> dependedParamValues)
      throws JSONException, WdkModelException, WdkUserException {
    JSONObject pJson = super.getJson();

    pJson.put("filterDataTypeDisplayName", filterParam.getFilterDataTypeDisplayName());
    pJson.put("ontology", getOntologyJson(user, dependedParamValues));
    JSONObject valuesMap = getValuesJson(user, dependedParamValues);
    //TODO: remove the null test when val map query becomes required
    if (valuesMap != null) pJson.put("values", valuesMap);
    pJson.put("hideEmptyOntologyNodes", filterParam.getTrimMetadataTerms());
    pJson.put("countPredictsAnswerCount", filterParam.getCountPredictsAnswerCount());
    pJson.put(MIN_SELECTED_COUNT, filterParam.getMinSelectedCount());
    return pJson;
  }

  public JSONArray getOntologyJson(User user, Map<String, String> dependedParamValues) throws JSONException, WdkModelException {
    Map<String, OntologyItem> ontologyMap = filterParam.getOntology(user, dependedParamValues);
    JSONArray ontologyJson = new JSONArray();
    for (String term : ontologyMap.keySet()) {
      JSONObject itemJson = new JSONObject();
      OntologyItem item = ontologyMap.get(term);
      itemJson.put("term", item.getOntologyId());
      itemJson.put("parent", item.getParentOntologyId());
      itemJson.put("display", item.getDisplayName());
      itemJson.put("description", item.getDescription());
      itemJson.put("type", item.getType().getIdentifier());
      itemJson.put("units", item.getUnits());
      itemJson.put("precision", item.getPrecision());
      itemJson.put("isRange", item.getIsRange());

      ontologyJson.put(itemJson);
    }
    return ontologyJson;
  }

  public JSONObject getValuesJson(User user, Map<String, String> dependedParamValues) throws JSONException, WdkModelException {

    Map<String, Set<String>>  valuesMap = filterParam.getValuesMap(user, dependedParamValues);

    // TODO: remove this when values map is required
    if (valuesMap == null) return null;

    JSONObject valuesMapJson = new JSONObject();
    for (String term : valuesMap.keySet()) {
      JSONArray valuesArrayJson = new JSONArray();
      Set<String> values = valuesMap.get(term);
      for (String value : values) valuesArrayJson.put(value);
      valuesMapJson.put(term, valuesArrayJson);
    }
    return valuesMapJson;
  }

}
