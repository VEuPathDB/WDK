package org.gusdb.wdk.service.formatter.param;

import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.FilterParamNew;
import org.gusdb.wdk.model.query.param.OntologyItem;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

public class FilterParamNewFormatter extends ParamFormatter<FilterParamNew> implements DependentParamProvider {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(FilterParamFormatter.class);

  protected FilterParamNew filterParam; 

  FilterParamNewFormatter(FilterParamNew param) {
    super(param);
    this.filterParam = param;
  }

  @Override
  public JSONObject getJson(User user, Map<String, String> dependedParamValues)
      throws JSONException, WdkModelException, WdkUserException {
    JSONObject pJson = super.getJson();
    pJson.put("ontology", getOntologyJson(user, dependedParamValues));
    pJson.put("hideEmptyOntologyNodes", filterParam.getTrimMetadataTerms());
    return pJson;
  }

  public JSONObject getOntologyJson(User user, Map<String, String> dependedParamValues) throws JSONException, WdkModelException, WdkUserException {
    Map<String, OntologyItem> ontologyMap = filterParam.getOntology(user, dependedParamValues);
    JSONObject ontologyJson = new JSONObject();
    for (String term : ontologyMap.keySet()) {
      JSONObject itemJson = new JSONObject();
      OntologyItem item = ontologyMap.get(term);
      itemJson.put("term", item.getOntologyId());
      itemJson.put("parent", item.getParentOntologyId());
      itemJson.put("display", item.getDisplayName());
      itemJson.put("description", item.getDescription());
      itemJson.put("type", item.getType());
      itemJson.put("units", item.getUnits());
      itemJson.put("precision", item.getPrecision());
      itemJson.put("isRange", item.getIsRange());
      
      ontologyJson.put(term, itemJson);
    }
    return ontologyJson; 
  }


}
