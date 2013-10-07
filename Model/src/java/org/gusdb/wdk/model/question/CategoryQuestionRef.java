package org.gusdb.wdk.model.question;

import org.gusdb.wdk.model.WdkModelText;

/**
 * An object representation of the {@code <category>/<question>}. It provides a
 * reference to a {@link Question} in the {@link SearchCategory} object.
 * 
 * @author jerric
 * 
 */
public class CategoryQuestionRef extends WdkModelText {

  private String usedBy;

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
}
