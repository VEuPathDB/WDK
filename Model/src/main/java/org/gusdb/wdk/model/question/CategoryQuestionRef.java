package org.gusdb.wdk.model.question;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;

/**
 * An object representation of the {@code <category>/<question>}. It provides a
 * reference to a {@link Question} in the {@link SearchCategory} object.
 * 
 * @author jerric
 * 
 */
public class CategoryQuestionRef extends WdkModelText implements Comparable<CategoryQuestionRef>{

  private String usedBy;
  private Integer sortOrder = null;
  private String questionDisplayName;

  public String getUsedBy() {
    return usedBy;
  }

  public void setUsedBy(String usedBy) {
    this.usedBy = usedBy;
  }

  public String getQuestionFullName() {
    return getText().trim();
  }

  public boolean isUsedBy(String usedBy) {
    return isUsedBy(usedBy, false);
  }

  public boolean isUsedBy(String usedBy, boolean strict) {
    if (strict) 
      return (usedBy != null && this.usedBy != null && this.usedBy.equalsIgnoreCase(usedBy));
    return (usedBy == null || this.usedBy == null || this.usedBy.equalsIgnoreCase(usedBy));
  }
  
  public void setSortOrder(int sortOrder) {
    this.sortOrder = new Integer(sortOrder);
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.WdkModelBase#resolveReferences(org.gusdb.wdk.model
   * .WdkModel)
   */
  @Override
  public void resolveReferences(WdkModel wodkModel) throws WdkModelException {
    questionDisplayName = wdkModel.getQuestion(getQuestionFullName()).getDisplayName();
  }
  
  @Override
  public int compareTo(CategoryQuestionRef cqr) {
    if (sortOrder != null) {
      if (cqr.sortOrder != null) return sortOrder - cqr.sortOrder;
      return 1;
    } else {
      if (cqr.sortOrder != null) return -1;
      else {
        return questionDisplayName.compareTo(cqr.questionDisplayName);
      }
    }
  }
}
