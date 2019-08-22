package org.gusdb.wdk.model.user.dataset.irods.icat.query;

import org.gusdb.fgputil.TraceLog;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.irods.icat.ICatDataObject;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSet;

import static org.gusdb.wdk.model.user.dataset.irods.icat.query.ICat.Column.*;
import static org.gusdb.wdk.model.user.dataset.irods.icat.query.Util.getString;

class AppendObjectMeta extends AbstractQueryRunner {
  private static final TraceLog TRACE = new TraceLog(AppendObjectMeta.class);

  private final ICatDataObject target;

  AppendObjectMeta(
    final ICatDataObject        target,
    final IRODSGenQuery         query,
    final IRODSGenQueryExecutor db
  ) {
    super(db, query);
    this.target = target;
  }

  @Override
  protected void handleResultSet(final IRODSQueryResultSet rs) throws Exception {
    TRACE.start(rs);
    for (final IRODSQueryResultRow row : rs.getResults())
      target.addMetadata(getString(row, DATA_OBJECT_META_KEY),
        getString(row, DATA_OBJECT_META_VALUE));
    TRACE.end();
  }

  static void run(final ICatDataObject obj, final IRODSGenQueryExecutor db)
  throws WdkModelException {
    TRACE.start(obj, db);
    new AppendObjectMeta(obj, Queries.allDataObjectMeta(obj.getPath()), db)
      .run();
    TRACE.end();
  }
}
