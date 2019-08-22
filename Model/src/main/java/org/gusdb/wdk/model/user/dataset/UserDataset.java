package org.gusdb.wdk.model.user.dataset;

import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.WdkModelException;

public interface UserDataset {

  /**
   * The permanent ID of this dataset
   */
  Long getUserDatasetId();

  /**
   * Get the user ID of the owner of this dataset
   */
  Long getOwnerId();

  /**
   * Get meta data object, which has the user's way of describing this dataset
   */
  UserDatasetMeta getMeta() throws WdkModelException;

  /**
   * Get the datatype of this dataset.
   */
  UserDatasetType getType() throws WdkModelException;

  /**
   * Get the number of datafiles in this dataset
   */
  Integer getNumberOfDataFiles() throws WdkModelException;

  /**
   * A list of files
   */
  Map<String, UserDatasetFile>getFiles() throws WdkModelException;

  /**
   * Get a file by name.  We don't need more than the basename, because, within
   * a dataset, it is just a flat set of files.
   */
  UserDatasetFile getFile(UserDatasetSession dsSession, String name) throws WdkModelException;

  /**
   * Get the date this dataset was created, by whatever application created it.
   * Storing this date with the dataset is the responsibility of that program,
   * not the wdk. Milliseconds since epoch.
   */
  Long getCreatedDate() throws WdkModelException;

  /**
   * Get the set of data dependencies (in the application database) that this
   * dataset has.
   */
  Set<UserDatasetDependency> getDependencies() throws WdkModelException;

  /**
   * Get the set of projects this dataset applies to.  Null if all
   */
  Set<String> getProjects() throws WdkModelException;

  /**
   * Get the size of the datafiles for this dataset.
   */
  Integer getSize() throws WdkModelException;

  /**
   * Get the percent of quota the user has used up.
   */
  Integer getPercentQuota(int quota) throws WdkModelException;
}
