package org.gusdb.wdk.model.question;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelText;

/**
 * An object representation of the {@code <category>/<question>}. It provides a
 * reference to a {@link Question} in the {@link SearchCategory} object.
 * 
 * @author jerric
 */
public class CategoryQuestionRef extends WdkModelText implements Comparable<CategoryQuestionRef> {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(CategoryQuestionRef.class);

  private String usedBy;
  private Integer sortOrder = null;
  private String questionDisplayName = ""; // default to unknown (but still sortable)

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
  
  public void setQuestionDisplayName(String name) {
    questionDisplayName = name;
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
