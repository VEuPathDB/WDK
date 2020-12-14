package org.gusdb.wdk.model.user.dataset.irods.icat.query;

import org.gusdb.fgputil.TraceLog;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.wdk.model.WdkModelException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.query.*;

/**
 * Base implementation of the most common use case for a query runner.
 * <p>
 * Provides a default implementation for iCAT query execution and a simplified
 * extension point for performing specific actions with query results.
 */
abstract class AbstractQueryRunner implements ICatQueryRunner {
  private static final String QUERY_NAME = "iCAT QUERY";

  private static final String
    ERR_CLOSE_FAIL     = "Failed to close iCAT result set for iCAT query %s in "
      + "runner %s",
    ERR_HANDLE_RS_FAIL = "Exception encountered in result set handler for iCAT "
      + "result set #%d for query %s in runner %s",
    ERR_NEW_QUERY_FAIL = "Execution failed for iCAT query %s in runner %s",
    ERR_CON_QUERY_FAIL = "Repeat execution failed for iCAT query %s in runner "
      + "%s";

  private static final TraceLog TRACE = new TraceLog(AbstractQueryRunner.class);

  private final IRODSGenQueryExecutor db;

  private final IRODSGenQuery query;

  private IRODSQueryResultSet currRs;

  private boolean fetchAll = true;

  AbstractQueryRunner(
    final IRODSGenQueryExecutor db,
    final IRODSGenQuery query
  ) {
    this.db    = db;
    this.query = query;
  }

  public boolean getFetchAll() {
    return fetchAll;
  }

  public void setFetchAll(boolean fetchAll) {
    this.fetchAll = fetchAll;
  }

  @Override
  public ICatQueryRunner run() throws WdkModelException {
    TRACE.start();
    final long start = System.currentTimeMillis();
    int i = 1;

    do {
      currRs = currRs == null ? initQuery() : continueQuery();
      try {
        handleResultSet(currRs);
      } catch (final Exception e) {
        throw new WdkModelException(String.format(ERR_HANDLE_RS_FAIL, i, query,
          getClass().getSimpleName()), e);
      }
      i++;
    } while (fetchAll && currRs.isHasMoreRecords());
    close();

    QueryLogger.logEndStatementExecution(QUERY_NAME, getClass().getSimpleName(), start);

    return TRACE.end(this);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{query=" + query + '}';
  }

  /**
   * Performs implementation specific operation with the given result set.
   * <p>
   * This method may be called more than once if the number of rows requested
   * is larger than the result chunk size.
   *
   * @param rs
   *   iRODS iCAT query result set
   *
   * @throws Exception
   *   May be thrown by implementing method.
   */
  protected abstract void handleResultSet(final IRODSQueryResultSet rs)
  throws Exception;

  protected IRODSGenQueryExecutor getDb() {
    return TRACE.getter(db);
  }

  protected AbstractIRODSGenQuery getQuery() {
    return TRACE.getter(query);
  }

  /**
   * Attempts to run the given query, wrapping the iRODS library exception if
   * one is thrown.
   *
   * @return
   *   result set for the given query
   *
   * @throws WdkModelException
   *   if the query execution fails
   */
  private IRODSQueryResultSet initQuery() throws WdkModelException {
    TRACE.start();
    final AbstractIRODSGenQuery que = getQuery();
    try {
      return TRACE.end(getDb().executeIRODSQuery(que, 0));
    } catch (JargonException | JargonQueryException e) {
      throw new WdkModelException(String.format(ERR_NEW_QUERY_FAIL, query,
        getClass().getSimpleName()), e);
    }
  }

  private IRODSQueryResultSet continueQuery() throws WdkModelException {
    TRACE.start();
    try {
      return TRACE.end(getDb().getMoreResults(currRs));
    } catch (JargonException | JargonQueryException e) {
      throw new WdkModelException(String.format(ERR_CON_QUERY_FAIL, query,
        getClass().getSimpleName()), e);
    }
  }

  private void close() throws WdkModelException {
    TRACE.start();
    try {
      getDb().closeResults(currRs);
    } catch (JargonException e) {
      throw new WdkModelException(String.format(ERR_CLOSE_FAIL, query,
        getClass().getSimpleName()), e);
    }
    TRACE.end();
  }
}
