package org.gusdb.wdk.model.user.dataset.irods.icat.query;

import org.gusdb.fgputil.TraceLog;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.irods.icat.ICatCollection;
import org.gusdb.wdk.model.user.dataset.irods.icat.query.ICat.Column;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;

import java.nio.file.Paths;

import static org.gusdb.wdk.model.user.dataset.irods.icat.query.Util.getLong;
import static org.gusdb.wdk.model.user.dataset.irods.icat.query.Util.getString;

class CollectionIntoQuery extends AbstractIntoQuery {

  private static final TraceLog TRACE = new TraceLog(CollectionIntoQuery.class);

  CollectionIntoQuery(
    final ICatCollection        root,
    final IRODSGenQueryExecutor db,
    final IRODSGenQuery query
  ) {
    super(root, db, query);
  }

  @Override
  protected void handleRow(ICatCollection root, IRODSQueryResultRow row)
  throws WdkModelException {
    TRACE.start(root, row);
    root.push(parse(row));
    TRACE.end();
  }

  private ICatCollection parse(final IRODSQueryResultRow row)
  throws WdkModelException {
    return TRACE.start(row).end(new ICatCollection(
      Paths.get(getString(row, Column.COLLECTION_NAME)),
      getLong(row, Column.COLLECTION_LAST_MODIFIED)
    ));
  }
}
