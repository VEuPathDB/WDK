package org.gusdb.wdk.model.user.dataset;

import org.gusdb.wdk.model.WdkModelException;

/**
 * Information the user provides to describe this dataset.  Is immutable.
 * User can provide a new one.
 * @author steve
 *
 */
public interface UserDatasetMeta {
  
  /**
   * Get the name
   * @return
   */
  String getName() throws WdkModelException;
    
  /**
   * Get the summary
   * @return
   */
  String getSummary() throws WdkModelException;
  
  /**
   * get the description
   * @return
   */
  String getDescription() throws WdkModelException;
}
