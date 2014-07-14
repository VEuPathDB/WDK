package org.gusdb.wdk.model;

public interface WdkResourceChecker {

  /**
   * Looks for a named resource and returns whether it exists or not.
   * 
   * @param name path to the resource
   * @return true if resource exists, otherwise false
   */
  public boolean wdkResourceExists(String name);

}
