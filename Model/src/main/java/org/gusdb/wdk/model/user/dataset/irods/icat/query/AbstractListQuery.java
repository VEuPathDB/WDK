package org.gusdb.wdk.model.user.dataset.irods.icat.query;

import org.gusdb.fgputil.TraceLog;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a base implementation for iCAT query runners that will return a list
 * of results.
 * <p>
 * Contains default implementations for all interface and parent class method
 * stubs and provides an extension point for extending classes to implement
 * parsing an individual row from the result set into an object of type {@link
 * T} to be appended to the output result list.
 *
 * @param <T>
 *   result list item type
 */
abstract class AbstractListQuery <T> extends AbstractResultQuery<List<T>> {
  private static final TraceLog TRACE = new TraceLog(AbstractListQuery.class);

  AbstractListQuery(final IRODSGenQueryExecutor db, final IRODSGenQuery query) {
    super(db, query);
  }

  /**
   * Extension point for implementing classes to parse an iCAT result row into
   * an instance of {@link T}
   *
   * @param row
   *   row to parse
   *
   * @return an instance of {@link T} parsed from the input row
   *
   * @throws Exception
   *   may be thrown by implementing classes for any error that occurs while
   *   attempting to parse an iCAT result row into an instance of {@link T}.
   */
  protected abstract T parseRow(final IRODSQueryResultRow row) throws Exception;

  /**
   * Constructor for the output result list.
   * <p>
   * Extending classes may override this to provide a different list
   * implementation that better suits their needs.
   *
   * @return a new list into which the parsed results will be collected.
   */
  protected List<T> newList() {
    return new ArrayList<>();
  }

  @Override
  protected List<T> newValue() {
    return newList();
  }

  @Override
  protected void parseResultSet(
    final List<T>             value,
    final IRODSQueryResultSet rs
  ) throws Exception {
    TRACE.start("List(size="+value.size()+')', rs);
    for (final IRODSQueryResultRow row : rs.getResults())
      value.add(parseRow(row));
    TRACE.end();
  }
}
