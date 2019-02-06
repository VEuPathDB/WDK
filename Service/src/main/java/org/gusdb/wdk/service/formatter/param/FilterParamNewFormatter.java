package org.gusdb.wdk.service.formatter.param;

import static org.gusdb.wdk.core.api.JsonKeys.MIN_SELECTED_COUNT;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.validation.ValidObjectFactory.SemanticallyValid;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.FilterParamNew;
import org.gusdb.wdk.model.query.param.OntologyItem;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FilterParamNewFormatter extends ParamFormatter<FilterParamNew> {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(FilterParamNewFormatter.class);

  protected FilterParamNew _filterParam; 

  FilterParamNewFormatter(FilterParamNew param) {
    super(param);
    _filterParam = param;
  }

  @Override
  public JSONObject getJson(SemanticallyValid<QueryInstanceSpec> spec) throws WdkModelException {
    JSONObject pJson = getBaseJson(spec);

    pJson.put("filterDataTypeDisplayName", _filterParam.getFilterDataTypeDisplayName());
    pJson.put("ontology", getOntologyJson(spec));
    JSONObject valuesMap = getValuesJson(spec);
    //TODO: remove the null test when val map query becomes required
    if (valuesMap != null) pJson.put("values", valuesMap);
    pJson.put("hideEmptyOntologyNodes", _filterParam.getTrimMetadataTerms());
    pJson.put("countPredictsAnswerCount", _filterParam.getCountPredictsAnswerCount());
    pJson.put(MIN_SELECTED_COUNT, _filterParam.getMinSelectedCount());
    return pJson;
  }

  public JSONArray getOntologyJson(SemanticallyValid<QueryInstanceSpec> validSpec) throws JSONException, WdkModelException {
    QueryInstanceSpec spec = validSpec.getObject();
    Map<String, OntologyItem> ontologyMap = _filterParam.getOntology(spec.getUser(), spec.toMap());
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

  public JSONObject getValuesJson(SemanticallyValid<QueryInstanceSpec> validSpec) throws JSONException, WdkModelException {

    QueryInstanceSpec spec = validSpec.getObject();
    Map<String, Set<String>> valuesMap = _filterParam.getValuesMap(spec.getUser(), spec.toMap());

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
  
  @Override
  public String getTypeDisplayName() {
    return JsonKeys.FILTER_PARAM_TYPE;   
  }

}
