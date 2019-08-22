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

/**
 * Fetches all the data object specific metadata for all data objects in a given
 * path an all of it's child paths then appends that metadata to the matching
 * data object in the given input {@link ICatCollection}
 */
class AppendObjectMetaRecursive extends RecursiveMetadataInto {
  private static final TraceLog TRACE = new TraceLog(AppendObjectMetaRecursive.class);

  private AppendObjectMetaRecursive(
    final ICatCollection        root,
    final IRODSGenQuery         query,
    final IRODSGenQueryExecutor db
  ) {
    super(root, db, query);
  }

  @Override
  protected void writeMeta(final ICatNode node, final IRODSQueryResultRow row)
  throws WdkModelException {
    TRACE.start(node, row);
    node.addMetadata(
      getString(row, Column.DATA_OBJECT_META_KEY),
      getString(row, Column.DATA_OBJECT_META_VALUE)
    );
    TRACE.end();
  }

  @Override
  protected Path getFilePath(final IRODSQueryResultRow row)
  throws WdkModelException {
    return TRACE.start(row).end(Paths.get(
      getString(row, Column.COLLECTION_NAME),
      getString(row, Column.DATA_OBJECT_NAME)
    ));
  }

  public static void run(
    final ICatCollection        collection,
    final Path                  path,
    final IRODSGenQueryExecutor executor
  ) throws WdkModelException {
    TRACE.start(collection, path, executor);
    new AppendObjectMetaRecursive(
      collection,
      Queries.allDataObjectMetaRecursive(path),
      executor
    ).run();
    TRACE.end();
  }
}
