package org.gusdb.wdk.model.user.dataset;

import org.gusdb.wdk.model.WdkModelException;

/**
 * Information the user provides to describe this dataset.  Is immutable. User
 * can provide a new one.
 *
 * @author steve
 */
public interface UserDatasetMeta {

  /**
   * Get the name
   */
  String getName() throws WdkModelException;

  /**
   * Get the summary
   */
  String getSummary() throws WdkModelException;

  /**
   * get the description
   */
  String getDescription() throws WdkModelException;
}
