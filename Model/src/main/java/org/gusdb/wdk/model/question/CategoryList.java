package org.gusdb.wdk.model.question;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.record.attribute.AttributeCategory;

public class CategoryList extends WdkModelBase {

  private List<String> _expanded;
  private List<String> _collapsed;
  
  public void setExpanded(String list) {
    _expanded = Arrays.asList(list.split(",\\s*"));
    _collapsed = null;
  }
  
  public void setCollapsed(String list) {
    _collapsed = Arrays.asList(list.split(",\\s*"));
    _expanded= null;
  }
  
  /**
   * Returns a list of expanded AttributeCategories.
   *
   * @param attributeCategoryTree
   * @return
   */
  public List<AttributeCategory> getExpanded(Iterable<AttributeCategory> attributeCategoryTree) {
    return filter(attributeCategoryTree, true);
  }
  
  /**
   * Returns a list of collapsed AttributeCategories.
   *
   * @param attributeCategoryTree
   * @return
   */
  public List<AttributeCategory> getCollapsed(Iterable<AttributeCategory> attributeCategoryTree) {
    return filter(attributeCategoryTree, false);
  }
  
  private List<AttributeCategory> filter(Iterable<AttributeCategory> attributeCategoryTree, boolean whereExpanded) {
    List<AttributeCategory> categories = new ArrayList<AttributeCategory>();
    for (AttributeCategory cat : attributeCategoryTree) {
      if (isExpanded(cat) == whereExpanded) categories.add(cat);
    }
    return categories;
  }
  
  private boolean isExpanded (AttributeCategory cat) {
    return _expanded != null  ? _expanded.contains(cat.getName())
         : _collapsed != null ? !_collapsed.contains(cat.getName())
         : true;
  }

}
