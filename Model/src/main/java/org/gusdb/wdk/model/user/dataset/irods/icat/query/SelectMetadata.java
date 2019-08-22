package org.gusdb.wdk.model.user.dataset.irods.icat.query;

import org.gusdb.fgputil.TraceLog;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.irods.icat.ICatCollection;
import org.gusdb.wdk.model.user.dataset.irods.icat.ICatDataObject;
import org.gusdb.wdk.model.user.dataset.irods.icat.ICatNode;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;

public class SelectMetadata implements ICatQueryRunner {

  private static final TraceLog TRACE = new TraceLog(SelectMetadata.class);

  private final ICatNode target;

  private final IRODSGenQueryExecutor db;

  public SelectMetadata(ICatNode target, IRODSGenQueryExecutor db) {
    this.target = target;
    this.db     = db;
  }

  @Override
  public ICatQueryRunner run() throws WdkModelException {
    TRACE.start();
    if (target.isObject()) {
      AppendObjectMeta.run((ICatDataObject) target, db);
    } else {
      AppendCollectionMeta.run((ICatCollection) target, db);
    }

    return TRACE.end(this);
  }
}
