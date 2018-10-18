package org.gusdb.wdk.service.formatter.param;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.EnumParamTermNode;
import org.gusdb.wdk.model.query.param.EnumParamVocabInstance;
import org.gusdb.wdk.model.user.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractEnumParamFormatter extends ParamFormatter<AbstractEnumParam> {

  AbstractEnumParamFormatter(AbstractEnumParam param) {
    super(param);
  }

  @Override
  public JSONObject getJson() throws JSONException, WdkModelException, WdkUserException {
    return super.getJson()
        .put(JsonKeys.COUNT_ONLY_LEAVES, _param.getCountOnlyLeaves())
        .put(JsonKeys.MAX_SELECTED_COUNT, _param.getMaxSelectedCount())
        .put(JsonKeys.MIN_SELECTED_COUNT, _param.getMinSelectedCount())
        .put(JsonKeys.IS_MULTIPICK, _param.getMultiPick())
        .put(JsonKeys.DISPLAY_TYPE, _param.getDisplayType())
        .put(JsonKeys.DEPTH_EXPANDED, _param.getDepthExpanded());
  }

  /**
   * Override so we don't unnecessarily calculate context-free default
   */
  @Override
  protected String getDefault() {
    return null;
  }

  protected EnumParamVocabInstance getVocabInstance(User user, Map<String,String> dependedParamValues) {
    return _param.getVocabInstance(user, dependedParamValues);
  }

  protected JSONArray getVocabArrayJson(EnumParamVocabInstance vocabInstance) throws WdkModelException {
    List<List<String>> vocabRows = vocabInstance.getFullVocab();
    JSONArray jsonRows = new JSONArray();
    for (List<String> row : vocabRows) {
      if (row.size() != 3) throw new WdkModelException("Enum vocab includes a row that does not contain 3 columns");
      JSONArray jsonRow = new JSONArray();
      jsonRow.put(row.get(0));
      jsonRow.put(row.get(1));
      jsonRow.put(row.get(2));
      jsonRows.put(jsonRow);
    }
    return jsonRows;
  }

  protected JSONObject getVocabTreeJson(EnumParamVocabInstance vocabInstance) {
    EnumParamTermNode[] rootNodes = vocabInstance.getVocabTreeRoots();

    // Use single root node if it has children (the root node is hidden)
    if (rootNodes.length == 1 && rootNodes[0].getChildren().length != 0) {
      return nodeToJson(rootNodes[0]);
    }

    EnumParamTermNode root = new EnumParamTermNode("@@fake@@");
    root.setDisplay("@@fake@@");
    for (EnumParamTermNode child: rootNodes) {
      root.addChild(child);
    }
    return nodeToJson(root);
  }

  protected JSONObject nodeToJson(EnumParamTermNode node) {
    return new JSONObject()
        .put("data", new JSONObject().put("term", node.getTerm()).put("display", node.getDisplay()))
        .put("children", new JSONArray(Arrays.stream(node.getChildren()).map(this::nodeToJson).toArray()));
  }

}
