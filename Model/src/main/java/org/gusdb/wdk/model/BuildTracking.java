package org.gusdb.wdk.model;

import org.gusdb.wdk.model.RngAnnotations.RngOptional;

public interface BuildTracking {

  WdkModel getWdkModel();

  String getNewBuild();

  @RngOptional
  void setNewBuild(String newBuild);

  String getReviseBuild();

  @RngOptional
  void setReviseBuild(String reviseBuild);

  /**
   * @return if the object is newly introduced in the current build
   */
  default boolean isNew() {
    return buildMatches(getNewBuild());
  }

  /**
   * @return if the object is revised in the current build
   */
  default boolean isRevised() {
    return buildMatches(getReviseBuild());
  }

  default boolean buildMatches(String trackedBuild) {
    String currentBuild = getWdkModel().getBuildNumber();
    if (currentBuild == null)
      return false; // current release is not set
    else
      return currentBuild.equals(trackedBuild);
    
  }
  
}
