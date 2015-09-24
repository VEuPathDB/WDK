package org.gusdb.wdk.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Function;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Predicate;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Reducer;

public class FieldTree {

  public static final IsSelectedPredicate IS_SELECTED = new IsSelectedPredicate(true);
  public static final IsSelectedPredicate IS_UNSELECTED = new IsSelectedPredicate(false);
  public static final IsDefaultPredicate IS_DEFAULT = new IsDefaultPredicate(true);

  public static final SetSelectedFunction SET_SELECTED = new SetSelectedFunction(true);
  public static final SetSelectedFunction SET_UNSELECTED = new SetSelectedFunction(false);
  public static final SetAsDefaultFunction SET_AS_DEFAULT = new SetAsDefaultFunction(true);

  public static final Function<SelectableItem,String> NAME_MAPPER = new Function<SelectableItem,String>() {
    @Override public String apply(SelectableItem obj) { return obj.getName(); }};

  public static final Function<SelectableItem,String> QUOTED_NAME_MAPPER = new Function<SelectableItem,String>() {
    @Override public String apply(SelectableItem obj) { return "'" + obj.getName() + "'"; }};

  private static final Reducer<SelectableItem,Boolean> ALL_SELECTED = new Reducer<SelectableItem,Boolean>() {
    @Override
    public Boolean reduce(SelectableItem obj) {
      return obj.isSelected();
    }
    @Override
    public Boolean reduce(SelectableItem obj, Boolean incomingValue) {
      return incomingValue && obj.isSelected();
    }
  };

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
    _root.apply(new Function<SelectableItem,SelectableItem>() {
      @Override public SelectableItem apply(SelectableItem obj) {
        return obj.setSelected(selectedList.contains(obj.getName()));
      }
    }, _root.LEAF_PREDICATE);
  }

  public void addSelectedLeaves(final List<String> selectedList) {
    _root.apply(_root.LEAF_PREDICATE, new NameMatchPredicate(selectedList), SET_SELECTED);
  }

  public void setAllLeavesSelected() {
    _root.apply(SET_SELECTED, _root.LEAF_PREDICATE);
  }

  public void addDefaultLeaves(String... names) {
    addDefaultLeaves(Arrays.asList(names));
  }

  public void addDefaultLeaves(final List<String> defaultList) {
    _root.apply(_root.LEAF_PREDICATE, new NameMatchPredicate(defaultList), SET_AS_DEFAULT);
  }

  public void setAllLeavesAsDefault() {
    _root.apply(SET_AS_DEFAULT, _root.LEAF_PREDICATE);
  }

  public String getSelectedAsList() {
    return FormatUtil.join(_root.findAndMap(_root.LEAF_PREDICATE, IS_SELECTED, QUOTED_NAME_MAPPER).toArray(), ",");
  }
  
  public List<TreeNode<SelectableItem>> getSelectedLeaves() {
    return _root.findAll(_root.LEAF_PREDICATE, IS_SELECTED);
  }

  public String getDefaultAsList() {
    return FormatUtil.join(_root.findAndMap(_root.LEAF_PREDICATE, IS_DEFAULT, QUOTED_NAME_MAPPER).toArray(), ",");
  }

  public boolean isAllSelected() {
    return _root.reduce(ALL_SELECTED);
  }

  public boolean isAllLeavesSelected() {
    return _root.reduce(_root.LEAF_PREDICATE, ALL_SELECTED);
  }

  public static class NameMatchPredicate implements Predicate<SelectableItem> {
    private final List<String> _searchNames = new ArrayList<>();
    public NameMatchPredicate(String searchName) { _searchNames.add(searchName); }
    public NameMatchPredicate(List<String> searchNames) { _searchNames.addAll(searchNames); }
    @Override public boolean test(SelectableItem obj) { return (_searchNames.contains(obj.getName())); }
  }

  private static class IsSelectedPredicate implements Predicate<SelectableItem> {
    private final boolean _isSelected;
    public IsSelectedPredicate(boolean isSelected) { _isSelected = isSelected; }
    @Override public boolean test(SelectableItem obj) { return (_isSelected == obj.isSelected()); }
  }

  private static class IsDefaultPredicate implements Predicate<SelectableItem> {
    private final boolean _isDefault;
    public IsDefaultPredicate(boolean isDefault) { _isDefault = isDefault; }
    @Override public boolean test(SelectableItem obj) { return (_isDefault == obj.isDefault()); }
  }

  private static class SetSelectedFunction implements Function<SelectableItem,SelectableItem> {
    private final boolean _isSelected;
    public SetSelectedFunction(boolean isSelected) { _isSelected = isSelected; }
    @Override public SelectableItem apply(SelectableItem obj) { return obj.setSelected(_isSelected); }
  }

  private static class SetAsDefaultFunction implements Function<SelectableItem,SelectableItem> {
    private final boolean _isDefault;
    public SetAsDefaultFunction(boolean isDefault) { _isDefault = isDefault; }
    @Override public SelectableItem apply(SelectableItem obj) { return obj.setDefault(_isDefault); }
  }
}
