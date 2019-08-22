package org.gusdb.wdk.model.user.dataset.irods.icat.query;

import org.gusdb.fgputil.TraceLog;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.irods.icat.ICatCollection;
import org.gusdb.wdk.model.user.dataset.irods.icat.ICatDataObject;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;

import java.nio.file.Path;
import java.util.Optional;

public class SelectSingleObject
implements ICatResultQueryRunner<Optional<ICatDataObject>> {

  private static final TraceLog TRACE = new TraceLog(SelectSingleObject.class);

  private final ICatCollection root;

  private final Path path;

  private final IRODSGenQueryExecutor db;

  private SelectSingleObject(
    final Path                  path,
    final IRODSGenQueryExecutor db
  ) {
    this.path = path;
    this.db   = db;
    this.root = new ICatCollection();
  }

  @Override
  public ICatResultQueryRunner<Optional<ICatDataObject>> run()
  throws WdkModelException {
    TRACE.start();
    new ObjectIntoQuery(root, db, Queries.specificObject(path)).run();
    return TRACE.end(this);
  }

  @Override
  public Optional<ICatDataObject> getResult() {
    return TRACE.start().end(root.getObject(path));
  }

  public static Optional<ICatDataObject> run(
    final Path                  path,
    final IRODSGenQueryExecutor db
  ) throws WdkModelException {
    return TRACE.start(path, db)
      .end(new SelectSingleObject(path, db).run().getResult());
  }
}
