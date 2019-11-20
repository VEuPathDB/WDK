package org.gusdb.wdk.model.ontology;

import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class OntologyNode extends HashMap<String, List<String>> {

  private static final long serialVersionUID = 1L;
  
  public JSONObject toJson() {
    JSONObject propertiesJson = new JSONObject();
    for (String key : keySet()) {
      propertiesJson.put(key, new JSONArray(get(key)));
    }
    return propertiesJson;
  }

  @Override
  public String toString() {
    return toJson().toString();
  }
}
