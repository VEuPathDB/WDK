package org.gusdb.wdk.service.formatter;

import java.util.List;

import org.gusdb.fgputil.functional.TreeNode.StructureMapper;
import org.gusdb.wdk.model.ontology.Ontology;
import org.gusdb.wdk.model.ontology.OntologyNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OntologyFormatter {

  public static JSONObject getOntologyJson(Ontology o)
      throws JSONException {
    JSONObject qJson = new JSONObject();
    qJson.put(Keys.NAME, o.getName());
    qJson.put(Keys.TREE, o.mapStructure(new TreeToJsonMapper()));
    return qJson;
  }

  private static class TreeToJsonMapper implements StructureMapper<OntologyNode, JSONObject> {
    @Override
    public JSONObject map(OntologyNode contents, List<JSONObject> mappedChildren) {
      JSONObject nodeJson = new JSONObject();
      nodeJson.put(Keys.PROPERTIES, contents.toJson());
      nodeJson.put(Keys.CHILDREN, mappedChildren);
      return nodeJson;
    }
  }

  public static JSONArray pathsToJson(List<List<OntologyNode>> paths) {
    JSONArray pathsJson = new JSONArray();
    for (List<OntologyNode> path : paths) {
      JSONArray pathJson = new JSONArray();
      for (OntologyNode node : path) {
        pathJson.put(node.toJson());
      }
      pathsJson.put(pathJson);
    }
    return pathsJson;
  }
}
