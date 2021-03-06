package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.List;

import org.gusdb.fgputil.ListBuilder;
import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.wdk.model.FieldTree;
import org.gusdb.wdk.model.SelectableItem;

/**
 * This class represents a tree node in the tree display of a enum/flatVocab
 * param.
 * 
 * @author xingao
 * 
 */
public class EnumParamTermNode {

  private String term;
  private String display;
  private List<EnumParamTermNode> children;

  /**
     * 
     */
  public EnumParamTermNode(String term) {
    this.term = term;
    children = new ArrayList<EnumParamTermNode>();
  }

  /**
   * @return
   */
  public String getTerm() {
    return term;
  }

  /**
   * @param child
   */
  public void addChild(EnumParamTermNode child) {
    children.add(child);
  }

  /**
   * @return
   */
  public EnumParamTermNode[] getChildren() {
    EnumParamTermNode[] array = new EnumParamTermNode[children.size()];
    children.toArray(array);
    return array;
  }

  List<EnumParamTermNode> getChildrenList() {
    return children;
  }

  /**
   * @return the display
   */
  public String getDisplay() {
    return display;
  }

  /**
   * @param display
   *          the display to set
   */
  public void setDisplay(String display) {
    this.display = display;
  }

  /**
   * @return this term node as a tree node for rendering as a checkboxTree
   */
  public FieldTree toFieldTree() {
    FieldTree tree = new FieldTree(new SelectableItem(getTerm(), getDisplay()));
    TreeNode<SelectableItem> root = tree.getRoot();
    for (EnumParamTermNode paramNode : getChildrenList()) {
      if (paramNode.getChildren().length == 0) {
        root.addChild(new SelectableItem(paramNode.getTerm(),
            paramNode.getDisplay(), paramNode.getDisplay()));
      }
      else {
        root.addChildNode(paramNode.toFieldTree().getRoot());
      }
    }
    return tree;
  }

  public List<String> getLeafTerms() {
    if (children.isEmpty()) {
      return ListBuilder.asList(term);
    }
    else {
      List<String> list = new ArrayList<>();
      for (EnumParamTermNode child : children) {
        list.addAll(child.getLeafTerms());
      }
      return list;
    }
  }
}
