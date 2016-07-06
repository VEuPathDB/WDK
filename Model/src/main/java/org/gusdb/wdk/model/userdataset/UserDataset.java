package org.gusdb.wdk.model.userdataset;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.gusdb.wdk.model.user.User;

public interface UserDataset {
  /**
   * Get meta data object, which has the user's way of describing this dataset
   * @return
   */
  UserDatasetMeta getMeta();
  
  /**
   * Set the user's choice of meta info.  User is allowed to edit this info
   * @param metainfo
   */
  void setMeta(UserDatasetMeta metainfo);
  
  /**
   * Get the datatype of this dataset.  
   * @return
   */
  String getType();
  
  /**
   * Get the number of datafiles in this dataset
   * @return
   */
  Integer getNumberOfDataFiles();
  
  /**
   * A list of data files
   * @return
   */
  List<Path>getFiles();
  
  /**
   * Get the list of users this dataset is shared with
   * (Should this return a User or a user ID?)
   * @return
   */
  List<UserDatasetShare> getSharedWith();
  
  /**
   * Share this dataset with the specified user
   * @param user
   */
  void share(User user);
  
  /**
   * Unshare this dataset with the specified user
   * @param user
   */
  void unshare(User user);
  
  /**
   * Unshare this dataset with all users it was shared with
   * @param user
   */
  void unshareAllUsers();
  
  /**
   * Get the date this dataset was created, by whatever application created it.
   * Storing this date with the dataset is the responsibility of that program, not the wdk.
   * @return
   */
  Date getCreateDate();
  
  /**
   * The last time it was modified, either meta info or outgoing or incoming sharing.
   * @return
   */
  Date getModifiedDate();
  
  /**
   * Get the set of data dependencies (in the application database) that this dataset has.
   * @return
   */
  Set<UserDatasetDependency> getDependencies();
  
  /**
   * Is this dataset compatible with the WDK's application database (based on its declared
   * dependencies, compared to the content of the database)?
   * @return
   */
  Boolean getIsCompatible();
  
  /**
   * Return an explanation for why this dataset is not compatible, if it is not.
   * @return
   */
  String getIncompatibleReason();
  
  /**
   * Get the size of the datafiles for this dataset.
   * @return
   */
  Integer getSize();
  
  /**
   * Get the percent of quota the user has used up.
   * @return
   */
  Integer getPercentQuota();
}
