package org.gusdb.wdk.errors;

import java.util.List;

public interface ErrorBundle {

  /* Methods common to all ErrorBundles */
  public boolean hasErrors();
  public String getDetailedDescription();

  /* Java-specific information; default to empty value */
  public default Exception getException() { return null; }

  /* Struts-specific information; default to empty values */
  public default List<String> getActionErrors() { return null; }
  public default String getActionErrorsAsHtml() { return ""; }
  public default String getActionErrorsAsText() { return ""; }

}
