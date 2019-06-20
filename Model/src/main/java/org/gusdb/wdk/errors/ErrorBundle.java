package org.gusdb.wdk.errors;

public interface ErrorBundle {

  /* Methods common to all ErrorBundles */
  public boolean hasErrors();
  public String getDetailedDescription();

  /* Java-specific information; default to empty value */
  public default Exception getException() { return null; }

}
