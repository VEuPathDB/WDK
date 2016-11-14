package org.gusdb.wdk.model.user.dataset;

import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.WdkModelException;

public interface UserDataset {

  /**
   * The permanent ID of this dataset  
   * @return
   */
  Integer getUserDatasetId();
  
  /**
   * Get the user ID of the owner of this dataset
   * @return
   */
  Integer getOwnerId();
  
  /**
   * Get meta data object, which has the user's way of describing this dataset
   * @return
   */
  UserDatasetMeta getMeta() throws WdkModelException;
      
  /**
   * Get the datatype of this dataset.  
   * @return
   */
  UserDatasetType getType() throws WdkModelException;
  
  /**
   * Get the number of datafiles in this dataset
   * @return
   */
  Integer getNumberOfDataFiles() throws WdkModelException;
  
  /**
   * A list of files
   * @return
   */
  Map<String, UserDatasetFile>getFiles() throws WdkModelException;
  
  /**
   * Get a file by name.  We don't need more than the basename, because, within
   * a dataset, it is just a flat set of files.
   */
  UserDatasetFile getFile(String name) throws WdkModelException;
  
  /**
   * Get the list of users this dataset is shared with
   * (Should this return a User or a user ID?)
   * @return
   */
  Set<UserDatasetShare> getSharedWith() throws WdkModelException;
    
  /**
   * Get the date this dataset was created, by whatever application created it.
   * Storing this date with the dataset is the responsibility of that program, not the wdk.
   * Milliseconds since epoch.
   * @return
   */
  Long getCreatedDate() throws WdkModelException;
  
  /**
   * The last time it was modified, either meta info or outgoing or incoming sharing.
   * Milliseconds since epoch.
   * @return
   */
  Long getModifiedDate() throws WdkModelException;
  
  /**
   * The time this dataset was uploaded to the UserDatasetStore
   * Milliseconds since epoch.
   * @return
   */
  Long getUploadedDate() throws WdkModelException;
  
  /**
   * Get the set of data dependencies (in the application database) that this dataset has.
   * @return
   */
  Set<UserDatasetDependency> getDependencies() throws WdkModelException;
    
  /**
   * Get the size of the datafiles for this dataset.
   * @return
   */
  Integer getSize() throws WdkModelException;
  
  /**
   * Get the percent of quota the user has used up.
   * @return
   */
  Integer getPercentQuota(int quota) throws WdkModelException;
}
