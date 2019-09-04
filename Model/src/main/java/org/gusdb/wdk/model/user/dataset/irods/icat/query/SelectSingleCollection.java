package org.gusdb.wdk.model.user.dataset.irods.icat.query;

import org.gusdb.fgputil.TraceLog;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.irods.icat.ICatCollection;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;

import java.nio.file.Path;
import java.util.Optional;

public class SelectSingleCollection
implements ICatResultQueryRunner<Optional<ICatCollection>> {

  private static final TraceLog TRACE = new TraceLog(SelectSingleCollection.class);

  private final ICatCollection root;

  private final Path path;

  private final IRODSGenQueryExecutor db;

  private SelectSingleCollection(
    final Path                  path,
    final IRODSGenQueryExecutor db
  ) {
    this.path = path;
    this.db   = db;
    this.root = new ICatCollection();
  }

  @Override
  public ICatResultQueryRunner<Optional<ICatCollection>> run()
  throws WdkModelException {
    TRACE.start();
    new CollectionIntoQuery(root, db, Queries.specificCollection(path)).run();
    return TRACE.end(this);
  }

  @Override
  public Optional<ICatCollection> getResult() {
    return TRACE.start().end(root.getCollection(path));
  }

  public static Optional<ICatCollection> run(
    final Path                  path,
    final IRODSGenQueryExecutor db
  ) throws WdkModelException {
    TRACE.start(path, db);
    return TRACE.end(new SelectSingleCollection(path, db).run().getResult());
  }
}
