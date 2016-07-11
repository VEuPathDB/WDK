package org.gusdb.wdk.model.user.dataset;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.gusdb.wdk.model.WdkModelException;

public interface UserDataset {
  /**
   * Get meta data object, which has the user's way of describing this dataset
   * @return
   */
  UserDatasetMeta getMeta() throws WdkModelException;
  
  /**
   * Update the user's choice of meta info.  User can edit this info, causing
   * a new meta data object to be provided.
   * @param metainfo
   */
  void updateMeta(UserDatasetMeta metainfo) throws WdkModelException;
  
  /**
   * Get the datatype of this dataset.  
   * @return
   */
  String getType() throws WdkModelException;
  
  /**
   * Get the number of datafiles in this dataset
   * @return
   */
  Integer getNumberOfDataFiles() throws WdkModelException;
  
  /**
   * A list of files
   * @return
   */
  List<UserDatasetFile>getFiles() throws WdkModelException;
  
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
  List<UserDatasetShare> getSharedWith() throws WdkModelException;
  
  /**
   * Share this dataset with the specified user
   * @param user
   */
  void share(Integer userId) throws WdkModelException;
  
  /**
   * Unshare this dataset with the specified user
   * @param user
   */
  void unshare(Integer userId) throws WdkModelException;
  
  /**
   * Unshare this dataset with all users it was shared with
   * @param user
   */
  void unshareAllUsers() throws WdkModelException;
  
  /**
   * Get the date this dataset was created, by whatever application created it.
   * Storing this date with the dataset is the responsibility of that program, not the wdk.
   * @return
   */
  Date getCreateDate() throws WdkModelException;
  
  /**
   * The last time it was modified, either meta info or outgoing or incoming sharing.
   * @return
   */
  Date getModifiedDate() throws WdkModelException;
  
  /**
   * Get the set of data dependencies (in the application database) that this dataset has.
   * @return
   */
  Set<UserDatasetDependency> getDependencies() throws WdkModelException;
  
  /**
   * Is this dataset compatible with the WDK's application database (based on its declared
   * dependencies, compared to the content of the database)?
   * @return
   */
  Boolean getIsCompatible() throws WdkModelException;
  
  /**
   * Return an explanation for why this dataset is not compatible, if it is not.
   * @return
   */
  String getIncompatibleReason() throws WdkModelException;
  
  /**
   * Get the size of the datafiles for this dataset.
   * @return
   */
  Integer getSize() throws WdkModelException;
  
  /**
   * Get the percent of quota the user has used up.
   * @return
   */
  Integer getPercentQuota() throws WdkModelException;
}
