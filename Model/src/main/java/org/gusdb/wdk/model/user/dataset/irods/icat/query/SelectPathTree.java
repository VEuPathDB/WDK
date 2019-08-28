package org.gusdb.wdk.model.user.dataset.irods.icat.query;

import org.gusdb.fgputil.TraceLog;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.irods.icat.ICatCollection;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;

import java.nio.file.Path;

public class SelectPathTree implements ICatResultQueryRunner<ICatCollection> {

  private static final TraceLog TRACE = new TraceLog(SelectPathTree.class);

  private final IRODSGenQueryExecutor db;

  private final Path path;

  private final ICatCollection root;

  public SelectPathTree(final Path path, final IRODSGenQueryExecutor db) {
    this.path = path;
    this.db   = db;
    this.root = new ICatCollection();
  }

  @Override
  public SelectPathTree run() throws WdkModelException {
    TRACE.start();
    new CollectionIntoQuery(root, db, Queries.allCollectionsRecursive(path)).run();

    new ObjectIntoQuery(root, db, Queries.allObjectsRecursive(path)).run();

    return TRACE.end(this);
  }

  @Override
  public ICatCollection getResult() {
    return TRACE.getter(root);
  }
}
