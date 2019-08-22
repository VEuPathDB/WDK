package org.gusdb.wdk.model.user.dataset.irods.icat.query;

import org.gusdb.wdk.model.WdkModelException;

/**
 * Wrapper and runner for an iCAT task which may run one or more queries and
 * returns a result of type {@link T}.
 *
 * @param <T>
 *   query result type
 */
public interface ICatResultQueryRunner<T> extends ICatQueryRunner {

  /**
   * Runs the encapsulated query and performs any necessary parse steps to
   * convert the result of that query into the output result type {@link T}.
   *
   * @return this query runner
   *
   * @throws WdkModelException
   *   if an error occurs while executing the wrapped query, or parsing it's
   *   results.
   */
  @Override
  ICatResultQueryRunner<T> run() throws WdkModelException;

  /**
   * Retrieve the result of a run of this query runner.
   *
   * @return the processing result of a run of this query runner.
   */
  T getResult();
}
