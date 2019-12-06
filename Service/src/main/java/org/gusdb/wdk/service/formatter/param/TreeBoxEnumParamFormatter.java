package org.gusdb.wdk.service.formatter.param;

import java.util.Arrays;

import org.gusdb.fgputil.validation.ValidObjectFactory.DisplayablyValid;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.AbstractEnumParam;
import org.gusdb.wdk.model.query.param.EnumParamTermNode;
import org.gusdb.wdk.model.query.param.EnumParamVocabInstance;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpec;
import org.json.JSONArray;
import org.json.JSONObject;

public class TreeBoxEnumParamFormatter extends EnumParamFormatter {

  private static final String DUMMY_VALUE = "@@fake@@";

  TreeBoxEnumParamFormatter(AbstractEnumParam param) {
    super(param);
  }

  @Override
  public <S extends ParameterContainerInstanceSpec<S>> JSONObject getJson(DisplayablyValid<S> spec) throws WdkModelException {
    return super.getJson(spec)
        .put(JsonKeys.COUNT_ONLY_LEAVES, _param.getCountOnlyLeaves())
        .put(JsonKeys.DEPTH_EXPANDED, _param.getDepthExpanded());
  }

  @Override
  protected Object getVocabularyObject(EnumParamVocabInstance vocabInstance) {
    EnumParamTermNode[] rootNodes = vocabInstance.getVocabTreeRoots();

    // Use single root node if it has children (the root node is hidden)
    if (rootNodes.length == 1 && rootNodes[0].getChildren().length != 0) {
      return nodeToJson(rootNodes[0]);
    }

    EnumParamTermNode root = new EnumParamTermNode(DUMMY_VALUE);
    root.setDisplay(DUMMY_VALUE);
    for (EnumParamTermNode child: rootNodes) {
      root.addChild(child);
    }
    return nodeToJson(root);
  }

  protected JSONObject nodeToJson(EnumParamTermNode node) {
    return new JSONObject()
        .put(JsonKeys.DATA, new JSONObject().put(JsonKeys.TERM, node.getTerm()).put(JsonKeys.DISPLAY, node.getDisplay()))
        .put(JsonKeys.CHILDREN, new JSONArray(Arrays.stream(node.getChildren()).map(this::nodeToJson).toArray()));
  }
}
