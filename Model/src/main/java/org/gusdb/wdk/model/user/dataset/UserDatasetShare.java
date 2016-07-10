package org.gusdb.wdk.model.user.dataset;

import java.util.Date;

import org.gusdb.wdk.model.user.User;

/**
 * A simple data container with info needed to describe sharing of a user dataset
 * @author steve
 *
 */
public interface UserDatasetShare {
  /**
   * The user the dataset is shared with
   * @return
   */
  User getUser();
  
  /**
   * The time it was shared
   * @return
   */
  Date getTimeShared();
}
