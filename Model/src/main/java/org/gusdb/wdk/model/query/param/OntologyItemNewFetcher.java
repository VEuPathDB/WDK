package org.gusdb.wdk.model.query.param;

import static org.gusdb.fgputil.functional.Functions.mapToList;
import static org.gusdb.fgputil.functional.Functions.transformValues;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.cache.ValueFactory;
import org.gusdb.fgputil.cache.ValueProductionException;
import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.json.JSONException;
import org.json.JSONObject;

public class OntologyItemNewFetcher implements ValueFactory<String, Map<String, OntologyItem>> {

  private static final String QUERY_NAME_KEY = "queryName";
  private static final String DEPENDED_PARAM_VALUES_KEY = "dependedParamValues";

  private final RunnableObj<QueryInstanceSpec> _queryInstanceSpec;

  public OntologyItemNewFetcher(RunnableObj<QueryInstanceSpec> queryInstanceSpec) {
    _queryInstanceSpec = queryInstanceSpec;
  }

  @Override
  public Map<String, OntologyItem> getNewValue(String cacheKey) throws ValueProductionException {
    try {
      Map<String, OntologyItem> ontologyItemMap = new LinkedHashMap<>();
      try (ResultList resultList = Query.makeQueryInstance(_queryInstanceSpec).getResults()) {
        while (resultList.next()) {
          OntologyItem oItem = new OntologyItem();
          oItem.setOntologyId((String) resultList.get(FilterParamNew.COLUMN_ONTOLOGY_ID));
          oItem.setParentOntologyId((String) resultList.get(FilterParamNew.COLUMN_PARENT_ONTOLOGY_ID));
          oItem.setDisplayName((String) resultList.get(FilterParamNew.COLUMN_DISPLAY_NAME));
          oItem.setDescription((String) resultList.get(FilterParamNew.COLUMN_DESCRIPTION));
          oItem.setType(OntologyItemType.getType((String)resultList.get(FilterParamNew.COLUMN_TYPE)));
          oItem.setUnits((String) resultList.get(FilterParamNew.COLUMN_UNITS));
          try { oItem.setVariableName((String) resultList.get(FilterParamNew.COLUMN_VARIABLE_NAME)); }
          catch (WdkModelException e) {}

          BigDecimal precision = (BigDecimal)resultList.get(FilterParamNew.COLUMN_PRECISION);
          if (precision != null) oItem.setPrecision(precision.toBigInteger().longValue() );

          BigDecimal isRange = (BigDecimal)resultList.get(FilterParamNew.COLUMN_IS_RANGE);
          if (isRange != null) oItem.setIsRange(isRange.toBigInteger().intValue() != 0);

          if (ontologyItemMap.containsKey(oItem.getOntologyId()))
            throw new WdkModelException("FilterParamNew Ontology Query " +
                _queryInstanceSpec.get().getQuery().get().getFullName() +
                " has duplicate " + FilterParamNew.COLUMN_ONTOLOGY_ID + ": " +
                oItem.getOntologyId());
 
          ontologyItemMap.put(oItem.getOntologyId(), oItem);
        }
      }

      // secondary validation: make sure node types are compatible with placement in the graph
      validateOntologyItems(ontologyItemMap);

      return ontologyItemMap;
    }
    catch (WdkModelException ex) {
      throw new ValueProductionException(ex);
    }
  }

  private static void validateOntologyItems(Map<String, OntologyItem> ontologyItemMap) throws WdkModelException {

    // allow empty ontology
    if (ontologyItemMap.size() == 0) return;

    // first, build a new map from name to TreeNode from the map
    Map<String, TreeNode<OntologyItem>> nodeMap = transformValues(ontologyItemMap, item -> new TreeNode<OntologyItem>(item));

    // build the tree by attaching children
    TreeNode<OntologyItem> root = new TreeNode<>(new OntologyItem().setType(OntologyItemType.BRANCH));
    for (TreeNode<OntologyItem> node : nodeMap.values()) {
      String parentId = node.getContents().getParentOntologyId();
      if (parentId == null) {
        // no parent ID means this is a root node (i.e. child of "master" root)
        root.addChildNode(node);
      }
      else {
        TreeNode<OntologyItem> parent = nodeMap.get(parentId);
        // if parent not present then child's parent ontology ID is invalid; throw error
        if (parent == null) {
          throw new WdkModelException("Parent ontology ID '" + parentId + "' for ontology item '" +
              node.getContents().getOntologyId() + "' cannot be found.");
        }
        // parent found; add as child
        parent.addChildNode(node);
      }
    }

    // tree populated; try to find illegal types
    List<TreeNode<OntologyItem>> badLeafNodes = root.findAll(
        root.LEAF_PREDICATE, item -> item.getType().equals(OntologyItemType.BRANCH));
    if (!badLeafNodes.isEmpty()) {
      String message = "The following ontology items have no children (i.e. is are leaf nodes) but have a null item type: " +
          FormatUtil.join(mapToList(badLeafNodes, node -> node.getContents().getOntologyId()),", ");
      throw new WdkModelException(message);
    }

    // XXX Commenting out the block below as we now allow branch node to also be filters (e.g., have a type) - dmf.
    // List<TreeNode<OntologyItem>> badBranchNodes = root.findAll(
    //     root.NONLEAF_PREDICATE, item -> !item.getType().equals(OntologyItemType.BRANCH));
    // if (!badBranchNodes.isEmpty()) {
    //   for (TreeNode<OntologyItem> branchWithNonNullType : badBranchNodes) {
    //     // FIXME: for now, warn and correct branch node types if non-null; should probably fix data in DB
    //     branchWithNonNullType.getContents().setType(OntologyItemType.BRANCH);
    //   }
    // }
  }

  public String getCacheKey() throws JSONException {
    QueryInstanceSpec spec = _queryInstanceSpec.get();
    Query query = spec.getQuery().get();
    JSONObject cacheKeyJson = new JSONObject();
    cacheKeyJson.put(QUERY_NAME_KEY, query.getName());
    JSONObject paramValuesJson = new JSONObject();
    for (String paramName : spec.keySet())
      if (query.getParamMap() != null && query.getParamMap().containsKey(paramName))
        paramValuesJson.put(paramName, spec.get(paramName));
    cacheKeyJson.put(DEPENDED_PARAM_VALUES_KEY, paramValuesJson);
    return JsonUtil.serialize(cacheKeyJson);
  }

}
