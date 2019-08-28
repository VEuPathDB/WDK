package org.gusdb.wdk.model.user.dataset.irods.icat.query;

import org.gusdb.fgputil.TraceLog;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.irods.icat.ICatCollection;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;

import java.nio.file.Path;
import java.util.Optional;

public class SelectCollectionShallow implements ICatResultQueryRunner<Optional<ICatCollection>> {
  private static final TraceLog TRACE = new TraceLog(SelectCollectionShallow.class);

  private final ICatCollection col;

  private final IRODSGenQueryExecutor db;

  private final Path path;

  public SelectCollectionShallow(final Path path, final IRODSGenQueryExecutor db) {
    this.col  = new ICatCollection();
    this.path = path;
    this.db   = db;
  }

  @Override
  public ICatResultQueryRunner<Optional<ICatCollection>> run()
  throws WdkModelException {
    TRACE.start();
    new ObjectIntoQuery(col, db, Queries.directChildObjects(path)).run();
    new CollectionIntoQuery(col, db, Queries.directChildCollections(path)).run();
    return TRACE.end(this);
  }

  @Override
  public Optional<ICatCollection> getResult() {
    return TRACE.start().end(col.getCollection(path).filter(c -> !c.isEmpty()));
  }
}
