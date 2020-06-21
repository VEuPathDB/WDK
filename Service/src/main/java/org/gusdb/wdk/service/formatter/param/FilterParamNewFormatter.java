package org.gusdb.wdk.service.formatter.param;

import org.gusdb.fgputil.validation.ValidObjectFactory.DisplayablyValid;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.FilterParamNew;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static org.gusdb.wdk.core.api.JsonKeys.MIN_SELECTED_COUNT;

public class FilterParamNewFormatter extends ParamFormatter<FilterParamNew> {

  FilterParamNewFormatter(FilterParamNew param) {
    super(param);
  }

  @Override
  public <S extends ParameterContainerInstanceSpec<S>> JSONObject getJson(DisplayablyValid<S> spec) throws WdkModelException {
    return getBaseJson(spec)
      .put("filterDataTypeDisplayName", _param.getFilterDataTypeDisplayName())
      .put("ontology", getOntologyJson(spec))
      .put("values", getValuesJson(spec))
      .put("hideEmptyOntologyNodes", _param.getTrimMetadataTerms())
      .put("sortLeavesBeforeBranches", _param.getSortLeavesBeforeBranches())
      .put("countPredictsAnswerCount", _param.getCountPredictsAnswerCount())
      .put(MIN_SELECTED_COUNT, _param.getMinSelectedCount());
  }

  public <S extends ParameterContainerInstanceSpec<S>> JSONArray getOntologyJson(DisplayablyValid<S> validSpec) throws JSONException, WdkModelException {
    var spec = validSpec.get();
    var ontologyMap = _param.getOntology(spec.getUser(), spec.toMap());
    var ontologyJson = new JSONArray();
    for (var item : ontologyMap.values()) {
      ontologyJson.put(new JSONObject()
        .put("term", item.getOntologyId())
        .put("parent", item.getParentOntologyId())
        .put("display", item.getDisplayName())
        .put("description", item.getDescription())
        .put("type", item.getType().getIdentifier())
        .put("units", item.getUnits())
        .put("precision", item.getPrecision())
        .put("isRange", item.getIsRange())
        .put("variableName", item.getVariableName()));
    }
    return ontologyJson;
  }

  public <S extends ParameterContainerInstanceSpec<S>> JSONObject getValuesJson(DisplayablyValid<S> validSpec) throws JSONException, WdkModelException {
    var spec = validSpec.get();
    var valuesMap = _param.getValuesMap(spec.getUser(), spec.toMap());

    // TODO: remove this when values map is required
    if (valuesMap == null) return null;

    return new JSONObject(valuesMap);
  }

  @Override
  public String getParamType() {
    return JsonKeys.FILTER_PARAM_TYPE;
  }

}
