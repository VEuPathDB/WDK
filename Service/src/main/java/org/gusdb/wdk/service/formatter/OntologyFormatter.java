package org.gusdb.wdk.service.formatter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.wdk.model.Ontology;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OntologyFormatter {
  
  public static JSONArray getOntologiesJson(Collection<Ontology> ontologies)
      throws JSONException, WdkModelException, WdkUserException {
    JSONArray json = new JSONArray();
    for (Ontology o : ontologies) {
        json.put(o.getName());
    }
    return json;
  }

  public static JSONObject getOntologyJson(Ontology o)
      throws JSONException, WdkModelException, WdkUserException {
    JSONObject qJson = new JSONObject();
    qJson.put("name", o.getName());
    TreeNode<Map<String,List<String>>> tree = o.getTree();
    qJson.put("tree", tree.mapStructure(new treeToJsonMapper()));
    return qJson;
  }
  /*
   { properties : {
      name : blah,
      type : blob
      },
      children : [
        { properties : {},
          children : []
        },
        { properties : {},
          children : []
        },
      ]
    }f
   */
  
  private static class treeToJsonMapper implements org.gusdb.fgputil.functional.TreeNode.StructureMapper<Map<String,List<String>>, JSONObject> {
    public JSONObject map(Map<String,List<String>> contents, List<JSONObject> mappedChildren) {
     JSONObject treeJson = new JSONObject();
     JSONObject propertiesJson = new JSONObject();
     for (String key : contents.keySet()) {
       JSONArray valuesJson = new JSONArray();
       List<String> values = contents.get(key);
       for (String value : values) valuesJson.put(value);
       propertiesJson.put(key, values);
     }
     treeJson.put("properties", propertiesJson);
     treeJson.put("children", mappedChildren);     
     return treeJson;
    }
  }

}
