package org.gusdb.wdk.model.question;

import org.gusdb.wdk.model.WdkModelText;

/**
 * An object representation of the {@code <category>/<question>}. It provides a
 * reference to a {@link Question} in the {@link SearchCategory} object.
 * 
 * @author jerric
 */
public class CategoryQuestionRef extends WdkModelText implements Comparable<CategoryQuestionRef> {

  private String _usedBy;
  private Integer _sortOrder = null;
  private String _questionDisplayName = ""; // default to unknown (but still sortable)

  public String getUsedBy() {
    return _usedBy;
  }

  public void setUsedBy(String usedBy) {
    this._usedBy = usedBy;
  }

  public String getQuestionFullName() {
    return getText().trim();
  }

  public boolean isUsedBy(String usedBy) {
    return isUsedBy(usedBy, false);
  }

  public boolean isUsedBy(String usedBy, boolean strict) {
    if (strict) 
      return (usedBy != null && this._usedBy != null && this._usedBy.equalsIgnoreCase(usedBy));
    return (usedBy == null || this._usedBy == null || this._usedBy.equalsIgnoreCase(usedBy));
  }

  public void setSortOrder(int sortOrder) {
    _sortOrder = Integer.valueOf(sortOrder);
  }

  public void setQuestionDisplayName(String name) {
    _questionDisplayName = name;
  }

  @Override
  public int compareTo(CategoryQuestionRef cqr) {
    if (_sortOrder != null) {
      if (cqr._sortOrder != null) return _sortOrder - cqr._sortOrder;
      return 1;
    } else {
      if (cqr._sortOrder != null) return -1;
      else {
        return _questionDisplayName.compareTo(cqr._questionDisplayName);
      }
    }
  }
}
