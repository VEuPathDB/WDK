package org.gusdb.wdk.model.user.dataset.irods.icat.query;

import org.gusdb.fgputil.TraceLog;
import org.gusdb.wdk.model.user.dataset.irods.icat.ICatCollection;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSet;

import java.util.List;

/**
 * Base implementation of an iCAT query runner which parses the query results
 * into a provided ICatCollection representation of a part of the iRODS
 * data store.
 */
abstract class AbstractIntoQuery extends AbstractQueryRunner {

  private static final TraceLog TRACE = new TraceLog(AbstractIntoQuery.class);

  private final ICatCollection _root;

  AbstractIntoQuery(
    final ICatCollection        root,
    final IRODSGenQueryExecutor db,
    final IRODSGenQuery         query
  ) {
    super(db, query);
    _root = root;
  }

  /**
   * Extension point for implementing classes to process individual iCAT query
   * result rows.
   * <p>
   * Each method call will be provided the same root value into which the parsed
   * row data may be appended.
   *
   * @param root
   *   result collection root
   * @param row
   *   current result row
   *
   * @throws Exception
   *   implementing methods may throw an exception for any error that occurs
   *   while attempting to read or process the given input row.
   */
  protected abstract void handleRow(
    final ICatCollection root,
    final IRODSQueryResultRow row
  ) throws Exception;

  @Override
  protected void handleResultSet(final IRODSQueryResultSet rs) throws Exception {
    TRACE.start(rs);
    int i = 0;
    final List<IRODSQueryResultRow> results = rs.getResults();
    for (; i < results.size(); i++)
      handleRow(_root, results.get(i));
    TRACE.end();
  }
}
