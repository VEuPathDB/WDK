package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Reducer;
import org.gusdb.fgputil.functional.TreeNode.StructureMapper;
import org.json.JSONArray;
import org.json.JSONObject;

public class FieldTree {

  public static final Function<SelectableItem,String> NAME_MAPPER = SelectableItem::getName;

  public static final Function<SelectableItem,String> QUOTED_NAME_MAPPER = obj -> "'" + obj.getName() + "'";

  private static final Reducer<SelectableItem,Boolean> ALL_SELECTED =
      (incomingValue, obj) -> incomingValue && obj.isSelected();

  private TreeNode<SelectableItem> _root;

  public FieldTree(SelectableItem treeField) {
    _root = new TreeNode<SelectableItem>(treeField);
  }

  public void setRoot(TreeNode<SelectableItem> root) {
    _root = root;
  }

  public TreeNode<SelectableItem> getRoot() {
    return _root;
  }

  public void setSelectedLeaves(String... names) {
    setSelectedLeaves(Arrays.asList(names));
  }

  public void setSelectedLeaves(final List<String> selectedList) {
    _root.apply(
      obj -> obj.setSelected(selectedList.contains(obj.getName())),
      _root.LEAF_PREDICATE);
  }

  public void addSelectedLeaves(final List<String> selectedList) {
    _root.apply(_root.LEAF_PREDICATE, new NameMatchPredicate(selectedList), obj -> obj.setSelected(true));
  }

  public void setAllLeavesSelected() {
    _root.apply(obj -> obj.setSelected(true), _root.LEAF_PREDICATE);
  }

  public void addDefaultLeaves(String... names) {
    addDefaultLeaves(Arrays.asList(names));
  }

  public void addDefaultLeaves(final List<String> defaultList) {
    _root.apply(_root.LEAF_PREDICATE, new NameMatchPredicate(defaultList), obj -> obj.setDefault(true));
  }

  public void setAllLeavesAsDefault() {
    _root.apply(obj -> obj.setDefault(true), _root.LEAF_PREDICATE);
  }

  public String getSelectedAsList() {
    return FormatUtil.join(_root.findAndMap(_root.LEAF_PREDICATE, SelectableItem::isSelected, QUOTED_NAME_MAPPER).toArray(), ",");
  }
  
  public List<TreeNode<SelectableItem>> getSelectedLeaves() {
    return _root.findAll(_root.LEAF_PREDICATE, SelectableItem::isSelected);
  }

  public String getDefaultAsList() {
    return FormatUtil.join(_root.findAndMap(_root.LEAF_PREDICATE, SelectableItem::isDefault, QUOTED_NAME_MAPPER).toArray(), ",");
  }

  public boolean isAllSelected() {
    return _root.reduce(ALL_SELECTED);
  }

  public boolean isAllLeavesSelected() {
    return _root.reduce(_root.LEAF_PREDICATE, ALL_SELECTED, true);
  }

  public JSONObject toJson() {
    return toJson(_root);
  }

  public JSONObject toJson(TreeNode<SelectableItem> node) {
    return node.mapStructure(new StructureMapper<SelectableItem, JSONObject>() {
      @Override
      public JSONObject apply(SelectableItem item, List<JSONObject> mappedChildren) {
        JSONObject json = new JSONObject();
        json.put("name", item.getName());
        json.put("displayName", item.getDisplayName());
        json.put("isDefault", item.isDefault());
        json.put("isSelected", item.isSelected());
        json.put("isOpenByDefault", item.isOpenByDefault());
        if (!mappedChildren.isEmpty()) {
          JSONArray children = new JSONArray();
          for (JSONObject mappedChild : mappedChildren) {
            children.put(mappedChild);
          }
          json.put("children", children);
        }
        return json;
      }
    });
  }

  public static class NameMatchPredicate implements Predicate<SelectableItem> {

    private final List<String> _searchNames = new ArrayList<>();

    public NameMatchPredicate(String searchName) {
      _searchNames.add(searchName);
    }

    public NameMatchPredicate(List<String> searchNames) {
      _searchNames.addAll(searchNames);
    }

    @Override
    public boolean test(SelectableItem obj) {
      return (_searchNames.contains(obj.getName()));
    }
  }
}
