package org.gusdb.wdk.service.formatter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.fgputil.functional.TreeNode.StructureMapper;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.ontology.Ontology;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//import org.apache.log4j.Logger;

public class OntologyFormatter {
  
  //  private static final Logger logger = Logger.getLogger(OntologyFormatter.class);

  public static JSONArray getOntologiesJson(Collection<Ontology> ontologies)
      throws JSONException {
    JSONArray json = new JSONArray();
    for (Ontology o : ontologies) {
        json.put(o.getName());
    }
    return json;
  }

  public static JSONObject getOntologyJson(Ontology o)
      throws JSONException, WdkUserException {
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
  
  private static class treeToJsonMapper implements StructureMapper<Map<String,List<String>>, JSONObject> {
    @Override
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
