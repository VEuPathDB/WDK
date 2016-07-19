package org.gusdb.wdk.model.user.dataset;

import org.gusdb.wdk.model.WdkModelException;

/**
 * A handler for a particular type of dataset.  These are plugged in to the wdk.
 * If a particular type is not plugged in, then datasets of that type are not
 * compatible with this wdk application, for that reason.
 * @author steve
 *
 */
public interface UserDatasetTypeHandler {
  
  /**
   * The type as specified in the Wdk Model Xml, and set during resolveReferences.
   * The handler should validate this, to confirm the model author is using 
   * the right handler
   * @param type
   */
  void setType(String type) throws WdkModelException;
  
  /**
   * The version as specified in the Wdk Model Xml, and set during resolveReferences
   * The handler should validate this, to confirm the model author is using 
   * the right handler
   * @param version
   */
  void setVersion(String version) throws WdkModelException;
  
  /**
   * Check if a dataset is compatible with this application, based on its data dependencies.
   * @param userDataset
   * @return
   */
  UserDatasetCompatibility getCompatibility(UserDataset userDataset);
  
  /**
   * The user dataset type this handler handles.
   * @return
   */
  UserDatasetType getUserDatasetType();

}
