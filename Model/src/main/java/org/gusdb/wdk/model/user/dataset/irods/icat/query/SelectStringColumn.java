package org.gusdb.wdk.model.user.dataset.irods.icat.query;

import org.gusdb.fgputil.TraceLog;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;

public class SelectStringColumn extends AbstractListQuery<String> {
  private static final TraceLog TRACE = new TraceLog(SelectStringColumn.class);

  public SelectStringColumn(IRODSGenQueryExecutor db, IRODSGenQuery query) {
    super(db, query);
  }

  @Override
  protected String parseRow(IRODSQueryResultRow row) throws Exception {
    return TRACE.start(row).end(Util.getString(row, 0));
  }
}
