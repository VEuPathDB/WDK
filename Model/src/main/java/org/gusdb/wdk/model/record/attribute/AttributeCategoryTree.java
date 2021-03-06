package org.gusdb.wdk.model.record.attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.FieldTree;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.model.record.RecordClass;

/**
 * Represent the tree structure of the attributes in a {@link RecordClass}. you can
 * access the top level categories from this class.
 *
 * @author jerric
 *
 */
public class AttributeCategoryTree extends WdkModelBase implements Iterable<AttributeCategory> {

  private final List<AttributeCategory> topLevelCategories = new ArrayList<>();

  private final List<AttributeField> topLevelAttributes = new ArrayList<>();

  // map from category name to Category (for easy lookup when placing
  // attributes)
  private final Map<String, AttributeCategory> categoryMap = new HashMap<>();

  public void addAttributeCategory(AttributeCategory category) {
    topLevelCategories.add(category);
  }

  public void prependAttributeCategory(AttributeCategory dynamic) {
    topLevelCategories.add(0, dynamic);
  }

  public void addAttributeToCategories(AttributeField attribute)
      throws WdkModelException {
    if (attribute.getAttributeCategory() == null) {
      // add no-category attribute to default list
      topLevelAttributes.add(attribute);
      return;
    }
    // split attributeCategory into individual names
    String[] categories = attribute.getAttributeCategory().split(",");
    for (String category : categories) {
      category = category.trim();
      if (!categoryMap.containsKey(category)) {
        throw new WdkModelException("Attribute field " + attribute.getName()
            + " is assigned to a category '" + category
            + "' that does not exist.");
      }
      categoryMap.get(category).addField(attribute);
    }
  }

  /**
   * Creates and returns a copy of this tree with only attributes valid under
   * the given scope. Also recursively trims categories that do not have
   * attributes.
   *
   * @param scope
   *          scope in which attributes must be valid
   * @return copy of tree, trimmed for scope and empty categories
   */
  public AttributeCategoryTree getTrimmedCopy(FieldScope scope) {
    AttributeCategoryTree copy = new AttributeCategoryTree();
    for (AttributeCategory cat : topLevelCategories) {
      AttributeCategory copyCat = cat.getTrimmedCopy(scope);
      if (!(copyCat.getFields().isEmpty() && copyCat.getSubCategories().isEmpty())) {
        copy.topLevelCategories.add(copyCat);
      }
    }
    for (AttributeField field : topLevelAttributes) {
      if (scope.isFieldInScope(field)) {
        copy.topLevelAttributes.add(field);
      }
    }
    try {
      copy.resolveReferences(getWdkModel());
    } catch (WdkModelException e) {
      throw new IllegalStateException(
          "Existing category tree has been corrupted.", e);
    }
    return copy;
  }

  public List<AttributeCategory> getTopLevelCategories() {
    return topLevelCategories;
  }

  /**
   * Builds out a map from category name to category
   */
  @Override
  public void resolveReferences(WdkModel model) throws WdkModelException {
    super.resolveReferences(model);
    for (AttributeCategory cat : topLevelCategories) {
      addCategoryToMap(cat);
    }
  }

  /**
   * Recursively adds the passed category and all subcategories to the category
   * map
   *
   * @param cat
   *          category to add
   * @throws WdkModelException if any categories' names are duplicated
   */
  private void addCategoryToMap(AttributeCategory cat) throws WdkModelException {
    if (categoryMap.containsKey(cat.getName())) {
      throw new WdkModelException("Attribute categories must be unique. "
          + cat.getName() + " is duplicated.");
    }
    categoryMap.put(cat.getName(), cat);
    for (AttributeCategory subcat : cat.getSubCategories()) {
      addCategoryToMap(subcat);
    }
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder().append("uncategorized (").append(
        topLevelAttributes.size()).append(")").append(FormatUtil.NL);
    for (AttributeCategory cat : topLevelCategories) {
      cat.appendToStringBuffer("", str);
    }
    return str.toString();
  }

  public FieldTree toFieldTree(String rootName, String rootDisplayName) {
    return AttributeCategory.toFieldTree(rootName, rootDisplayName, topLevelCategories, topLevelAttributes);
  }

  @Override
  public Iterator<AttributeCategory> iterator() {
    return new AttributeCategoryTreeIterator(this);
  }


  private static class AttributeCategoryTreeIterator implements Iterator<AttributeCategory> {

    private final LinkedList<AttributeCategory> items = new LinkedList<>();

    public AttributeCategoryTreeIterator(AttributeCategoryTree attributeCategoryTree) {
      items.addAll(attributeCategoryTree.getTopLevelCategories());
    }

    @Override
    public boolean hasNext() {
      return !items.isEmpty();
    }

    @Override
    public AttributeCategory next() {
      if (!hasNext()) throw new NoSuchElementException();

      // get our next item, which we will return
      AttributeCategory item = items.pop();

      // check if there are any children, and if so, push that to the iterator stack
      List<AttributeCategory> children = item.getSubCategories();
      if (children != null) {
        items.addAll(0, children);
      }

      return item;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

}
