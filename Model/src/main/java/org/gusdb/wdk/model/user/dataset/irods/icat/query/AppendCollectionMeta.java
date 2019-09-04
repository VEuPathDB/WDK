package org.gusdb.wdk.model.user.dataset.irods.icat.query;

import org.gusdb.fgputil.TraceLog;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.irods.icat.ICatCollection;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSet;

import static org.gusdb.wdk.model.user.dataset.irods.icat.query.ICat.Column.COLLECTION_META_KEY;
import static org.gusdb.wdk.model.user.dataset.irods.icat.query.ICat.Column.COLLECTION_META_VALUE;
import static org.gusdb.wdk.model.user.dataset.irods.icat.query.Util.getString;

/**
 * Retrieves collection specific metadata from iCAT for the given collection and
 * appends that metadata to the collection instance.
 */
class AppendCollectionMeta extends AbstractQueryRunner {
  private static final TraceLog TRACE = new TraceLog(AppendCollectionMeta.class);

  private final ICatCollection target;

  AppendCollectionMeta(
    final ICatCollection        target,
    final IRODSGenQuery query,
    final IRODSGenQueryExecutor db
  ) {
    super(db, query);
    this.target = target;
  }

  @Override
  protected void handleResultSet(final IRODSQueryResultSet rs) throws Exception {
    TRACE.start();
    for (final IRODSQueryResultRow row : rs.getResults())
      target.addMetadata(getString(row, COLLECTION_META_KEY),
        getString(row, COLLECTION_META_VALUE));
    TRACE.end();
  }

  static void run(final ICatCollection col, final IRODSGenQueryExecutor db)
  throws WdkModelException {
    TRACE.start(col, db);
    new AppendCollectionMeta(col, Queries.allCollectionMeta(col.getPath()), db)
      .run();
    TRACE.end();
  }
}
