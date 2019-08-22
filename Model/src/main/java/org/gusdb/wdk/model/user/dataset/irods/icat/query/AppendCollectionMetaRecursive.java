package org.gusdb.wdk.model.user.dataset.irods.icat.query;

import org.gusdb.fgputil.TraceLog;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.irods.icat.ICatCollection;
import org.gusdb.wdk.model.user.dataset.irods.icat.ICatNode;
import org.gusdb.wdk.model.user.dataset.irods.icat.query.ICat.Column;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.gusdb.wdk.model.user.dataset.irods.icat.query.Util.getString;

class AppendCollectionMetaRecursive extends RecursiveMetadataInto {
  private static final TraceLog TRACE = new TraceLog(AppendCollectionMetaRecursive.class);

  private AppendCollectionMetaRecursive(
    final ICatCollection        root,
    final IRODSGenQueryExecutor exec,
    final IRODSGenQuery         query
  ) {
    super(root, exec, query);
  }

  @Override
  protected void writeMeta(final ICatNode node, final IRODSQueryResultRow row)
  throws WdkModelException {
    TRACE.start(node, row);
    node.addMetadata(
      getString(row, Column.COLLECTION_META_KEY),
      getString(row, Column.COLLECTION_META_VALUE)
    );
    TRACE.end();
  }

  @Override
  protected Path getFilePath(IRODSQueryResultRow row)
  throws WdkModelException {
    return TRACE.start(row).end(Paths.get(getString(row, Column.COLLECTION_NAME)));
  }

  static void run(
    final ICatCollection        root,
    final Path                  path,
    final IRODSGenQueryExecutor db
  ) throws WdkModelException {
    TRACE.start(root, path, db);
    new AppendCollectionMetaRecursive(root, db,
      Queries.allCollectionMetaRecursive(path)).run();
    TRACE.end();
  }
}
