package org.gusdb.wdk.model.user.dataset.irods.icat.query;

import org.gusdb.fgputil.TraceLog;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.irods.icat.ICatCollection;
import org.gusdb.wdk.model.user.dataset.irods.icat.ICatDataObject;
import org.gusdb.wdk.model.user.dataset.irods.icat.query.ICat.Column;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;

import java.nio.file.Paths;

import static org.gusdb.wdk.model.user.dataset.irods.icat.query.Util.getLong;
import static org.gusdb.wdk.model.user.dataset.irods.icat.query.Util.getString;

class ObjectIntoQuery extends AbstractIntoQuery {

  private static final TraceLog TRACE = new TraceLog(ObjectIntoQuery.class);

  ObjectIntoQuery(
    final ICatCollection root,
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

  private ICatDataObject parse(final IRODSQueryResultRow row)
  throws WdkModelException {
    return TRACE.start(row).end(new ICatDataObject(
      Paths.get(getString(row, Column.COLLECTION_NAME)),
      getString(row, Column.DATA_OBJECT_NAME),
      getLong(row, Column.DATA_OBJECT_SIZE),
      getLong(row, Column.DATA_OBJECT_LAST_MODIFIED)
    ));
  }
}
