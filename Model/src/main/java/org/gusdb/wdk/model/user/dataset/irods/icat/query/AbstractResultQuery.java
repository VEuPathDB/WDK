package org.gusdb.wdk.model.user.dataset.irods.icat.query;

import org.gusdb.fgputil.TraceLog;
import org.gusdb.wdk.model.WdkModelException;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultSet;

/**
 * Provides a base implementation for iCAT query runners that will return a
 * result.
 * <p>
 * Contains a default implementation for all interface and parent class method
 * stubs and provides the following abstract extension points:
 * <ul>
 * <li>Value constructor: Implementing classes must provide some value
 *   constructor which instantiates a new value of type {@link T} that will be
 *   passed to the parse extension point.
 * <li>Result parser: Implementing classes will be given one or more result sets
 *   via this method alongside the constructed value of type {@link T} into
 *   which the result sets can be parsed.
 * </ul>
 *
 * @param <T>
 *   result type
 */
abstract class AbstractResultQuery <T>
extends AbstractQueryRunner
implements ICatResultQueryRunner<T> {

  private static final TraceLog TRACE = new TraceLog(AbstractResultQuery.class);

  private T value;

  AbstractResultQuery(
    final IRODSGenQueryExecutor db,
    final IRODSGenQuery         query
  ) {
    super(db, query);
  }

  /**
   * Extension point for implementing classes to instantiate a new value into
   * which the result set can be parsed.
   * <p>
   * This is done rather than having the parse method instantiate the value due
   * to the potential for a single iCAT query to return multiple result sets
   * which may all be necessary to construct a complete result.
   *
   * @return newly constructed result value.
   */
  protected abstract T newValue();

  /**
   * Extension point for implementing classes to parse a query result set and
   * append the values into the provided output value.
   *
   * @param value
   *   output value into which the parsed data should be populated
   * @param rs
   *   result set from which data should be parsed
   *
   * @throws Exception
   *   may be thrown by implementing classes for any error that is encountered
   *   while attempting to parse or process the given result set.
   */
  protected abstract void parseResultSet(
    final T                   value,
    final IRODSQueryResultSet rs
  ) throws Exception;

  @Override
  public T getResult() {
    return value;
  }

  @Override
  protected void handleResultSet(final IRODSQueryResultSet rs) throws Exception {
    TRACE.start(rs);
    if (value == null) {
      value = newValue();
    }

    parseResultSet(value, rs);
    TRACE.end();
  }

  @Override
  @SuppressWarnings("unchecked")
  public ICatResultQueryRunner<T> run() throws WdkModelException {
    TRACE.start();
    return TRACE.end((ICatResultQueryRunner<T>) super.run());
  }
}
