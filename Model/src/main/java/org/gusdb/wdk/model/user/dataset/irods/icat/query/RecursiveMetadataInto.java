package org.gusdb.wdk.model.user.dataset.irods.icat.query;

import org.gusdb.fgputil.TraceLog;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.irods.icat.ICatCollection;
import org.gusdb.wdk.model.user.dataset.irods.icat.ICatNode;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

abstract class RecursiveMetadataInto extends AbstractIntoQuery {

  private static final TraceLog TRACE = new TraceLog(RecursiveMetadataInto.class);

  private final HashSet<Path> hits;

  private final HashMap<Path, ICatNode> cache;

  protected RecursiveMetadataInto(
    final ICatCollection        root,
    final IRODSGenQueryExecutor db,
    final IRODSGenQuery         query
  ) {
    super(root, db, query);
    hits  = new HashSet<>();
    cache = new HashMap<>();
  }

  protected abstract void writeMeta(ICatNode node, IRODSQueryResultRow row)
  throws WdkModelException;

  @Override
  protected void handleRow(
    final ICatCollection root,
    final IRODSQueryResultRow row
  ) throws WdkModelException {
    TRACE.start(root, row);
    final Path path = getFilePath(row);

    if (hits.contains(path)) {
      final ICatNode tmp = cache.get(path);
      if (tmp != null)
        writeMeta(tmp, row);
    } else {
      final Optional<ICatNode> node = root.get(path);
      if (node.isPresent()) {
        hits.add(path);
        cache.put(path, node.get());
        node.get().clearMetadata();
        writeMeta(node.get(), row);
      } else {
        hits.add(path);
      }
    }
    TRACE.end();
  }

  protected abstract Path getFilePath(final IRODSQueryResultRow row)
  throws WdkModelException;
}
