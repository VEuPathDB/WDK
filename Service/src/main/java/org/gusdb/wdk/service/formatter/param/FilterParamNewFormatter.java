package org.gusdb.wdk.service.formatter.param;

import static org.gusdb.wdk.core.api.JsonKeys.MIN_SELECTED_COUNT;

import java.util.Map;
import java.util.Set;

import org.gusdb.fgputil.validation.ValidObjectFactory.DisplayablyValid;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.FilterParamNew;
import org.gusdb.wdk.model.query.param.OntologyItem;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FilterParamNewFormatter extends ParamFormatter<FilterParamNew> {

  FilterParamNewFormatter(FilterParamNew param) {
    super(param);
  }

  @Override
  public <S extends ParameterContainerInstanceSpec<S>> JSONObject getJson(DisplayablyValid<S> spec) throws WdkModelException {
    JSONObject pJson = getBaseJson(spec);

    pJson.put("filterDataTypeDisplayName", _param.getFilterDataTypeDisplayName());
    pJson.put("ontology", getOntologyJson(spec));
    JSONObject valuesMap = getValuesJson(spec);
    //TODO: remove the null test when val map query becomes required
    if (valuesMap != null) pJson.put("values", valuesMap);
    pJson.put("hideEmptyOntologyNodes", _param.getTrimMetadataTerms());
    pJson.put("countPredictsAnswerCount", _param.getCountPredictsAnswerCount());
    pJson.put(MIN_SELECTED_COUNT, _param.getMinSelectedCount());
    return pJson;
  }

  public <S extends ParameterContainerInstanceSpec<S>> JSONArray getOntologyJson(DisplayablyValid<S> validSpec) throws JSONException, WdkModelException {
    ParameterContainerInstanceSpec<?> spec = validSpec.get();
    Map<String, OntologyItem> ontologyMap = _param.getOntology(spec.getUser(), spec.toMap());
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

  public <S extends ParameterContainerInstanceSpec<S>> JSONObject getValuesJson(DisplayablyValid<S> validSpec) throws JSONException, WdkModelException {
    ParameterContainerInstanceSpec<?> spec = validSpec.get();
    Map<String, Set<String>> valuesMap = _param.getValuesMap(spec.getUser(), spec.toMap());

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
  public String getParamType() {
    return JsonKeys.FILTER_PARAM_TYPE;
  }

}
