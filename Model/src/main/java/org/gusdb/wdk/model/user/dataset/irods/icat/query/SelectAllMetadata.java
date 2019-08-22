package org.gusdb.wdk.model.user.dataset.irods.icat.query;

import org.gusdb.fgputil.TraceLog;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.irods.icat.ICatCollection;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;

import java.nio.file.Path;

public class SelectAllMetadata implements ICatQueryRunner {
  private static final TraceLog TRACE = new TraceLog(SelectAllMetadata.class);

  private final ICatCollection root;

  private final Path path;

  private final IRODSGenQueryExecutor db;

  public SelectAllMetadata(
    final ICatCollection        root,
    final Path                  path,
    final IRODSGenQueryExecutor db
  ) {
    this.root = root;
    this.path = path;
    this.db   = db;
  }

  @Override
  public ICatQueryRunner run() throws WdkModelException {
    TRACE.start();
    AppendCollectionMetaRecursive.run(root, path, db);
    AppendObjectMetaRecursive.run(root, path, db);
    return TRACE.end(this);
  }
}
