package org.gusdb.wdk.model.user.dataset.irods.icat.query;

import org.gusdb.wdk.model.WdkModelException;

/**
 * Individual iCAT query handler.
 * <p>
 * Implementing classes should wrap an iCAT query task consisting of one or more
 * queries and the processing necessary to handle the results of those queries.
 */
public interface ICatQueryRunner {

  /**
   * Main execution point for the current query runner.
   * <p>
   * This method should cause any necessary queries and/or processing actions to
   * be run against the results of those queries.
   *
   * @return the current instance of the query runner for convenience when
   *   performing inline execution of the query runner.
   *
   * @throws WdkModelException
   *   may be thrown by the underlying implementation for any error that occur
   *   while executing the steps contained in the implementing query runner.
   */
  ICatQueryRunner run() throws WdkModelException;
}
