package org.gusdb.wdk.model.user.dataset.irods.icat;

import org.gusdb.fgputil.TraceLog;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.user.dataset.irods.icat.query.*;
import org.gusdb.wdk.model.user.dataset.irods.session.IrodsSession;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.query.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Wrapper for common uses of the iRODS library to query the iCAT metadata
 * store.
 */
public final class ICatAdaptor {

  private static final String
    ERR_NO_QUERY_EXEC = "Failed to construct an iRODS query executor",
    ERR_QUERY_FAIL    = "Failed to execute iCAT query: %s";

  private static final TraceLog TRACE = new TraceLog(ICatAdaptor.class);

  private final IRODSGenQueryExecutor iCatExec;

  public ICatAdaptor(final IRODSAccount account) {
    TRACE.start(account);
    this.iCatExec = getExecutor(account);
    TRACE.end();
  }

  /*⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺*\
  ▏                                                        ▕
  ▏  Public API                                            ▕
  ▏                                                        ▕
  \*⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽*/

  public Optional<ICatCollection> fetchShallowCollection(final Path path)
  throws WdkModelException {
    return TRACE.start(path)
      .end(new SelectCollectionShallow(path, iCatExec).run().getResult());
  }

  public Optional<ICatNode> fetchNodeAt(final Path path)
  throws WdkModelException {
    TRACE.start(path);

    // Try for a data object at the given path
    final Optional<ICatNode> value = fetchObjectAt(path)
      .map(ICatNode.class::cast);
    if (value.isPresent())
      return TRACE.end(value);

    // Wasn't a data object, try for a collection
    return TRACE.end(fetchCollectionAt(path).map(ICatNode.class::cast));
  }

  public Optional<ICatCollection> fetchCollectionAt(final Path path)
  throws WdkModelException {
    return TRACE.start(path).end(SelectSingleCollection.run(path, iCatExec));
  }

  public Optional<ICatDataObject> fetchObjectAt(final Path path)
  throws WdkModelException {
    return TRACE.start(path).end(SelectSingleObject.run(path, iCatExec));
  }

  /**
   * Constructs a tree representation of all the collections and data objects
   * located at or under the given path.
   *
   * @param path
   *   path to for which all collections and data objects should be retrieved
   *
   * @return a tree representing all the collections and data objects at or
   *   under the given path.
   *
   * @throws WdkModelException
   *   if an error occurs while attempting to fetch or construct the in memory
   *   mirror of the iRODS collection tree.
   */
  public Optional<ICatCollection> fetchFullTreeAt(final Path path)
  throws WdkModelException {
    return TRACE.start(path)
      .end(Optional.of(new SelectPathTree(path, iCatExec).run().getResult())
        .filter(n -> !n.isEmpty()));
  }

  /**
   * Populates the given node's metadata by fetching that metadata from iCAT.
   *
   * @param node
   *   Node for which metadata should be pulled
   */
  public void fetchMetadataInto(final ICatNode node) throws WdkModelException {
    TRACE.start(node);
    new SelectMetadata(node, iCatExec).run();
    TRACE.end();
  }

  /**
   * Populates the given node and all sub nodes' available metadata from iCAT.
   *
   * @param node
   *   starting point for pulling metadata
   */
  public void fetchAllMetadataInto(final ICatCollection node)
  throws WdkModelException {
    TRACE.start(node);
    new SelectAllMetadata(node, node.getPath(), iCatExec).run();
    TRACE.end();
  }

  /**
   * Runs the given query and uses the first column of the result to build the
   * returned list.
   *
   * @param sql
   *   iCAT query to run
   * @param rows
   *   number of rows to retrieve
   *
   * @return a list of the values from the first column of the query result
   *
   * @throws WdkModelException
   *   if an iRODS Jargon exception is thrown while attempting to parse or run
   *   the given query.
   */
  public List<String> singleColumnQuery(final String sql, final int rows)
  throws WdkModelException {
    TRACE.start(sql, rows);
    try {
      return TRACE.end(new SelectStringColumn(iCatExec, IRODSGenQuery.instance(sql, rows))
        .run().getResult());
    } catch(JargonException e) {
      throw new WdkModelException(String.format(ERR_QUERY_FAIL, sql), e);
    }
  }

  /*⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺*\
  ▏                                                        ▕
  ▏  Static Internal API Methods                           ▕
  ▏                                                        ▕
  \*⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽*/

  /**
   * Retrieves an instance of an iRODS query executor for running iCAT queries.
   *
   * @param acc
   *   iRODS account credentials/configuration
   *
   * @return a new iRODS query executor instance.
   *
   * @throws WdkRuntimeException
   *   if an iRODS library error occurs while attempting to instantiate a new
   *   query executor.
   */
  private static IRODSGenQueryExecutor getExecutor(final IRODSAccount acc) {
    TRACE.start(acc);
    try {
      return TRACE.end(IrodsSession.queryExecutor(acc));
    } catch (WdkModelException e) {
      throw new WdkRuntimeException(ERR_NO_QUERY_EXEC, e);
    }
  }
}
