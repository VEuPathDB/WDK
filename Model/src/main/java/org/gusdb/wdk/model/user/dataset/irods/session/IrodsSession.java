package org.gusdb.wdk.model.user.dataset.irods.session;

import org.gusdb.fgputil.TraceLog;
import org.gusdb.wdk.model.WdkModelException;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;

public final class IrodsSession {

  private static final TraceLog TRACE = new TraceLog(IrodsSession.class);

  private static IRODSFileSystem system;
  private static IRODSAccessObjectFactory factory;

  public static IRODSAccessObjectFactory objectFactory()
  throws WdkModelException {
    TRACE.start();

    if (system == null) {
      TRACE.log("new IRODSFileSystem instance");
      try { system = IRODSFileSystem.instance(); }
      catch (JargonException e) { throw new WdkModelException(e); }
    }

    if (factory == null) {
      TRACE.log("new IRODSAccessObjectFactory instance");
      try {
        factory = system.getIRODSAccessObjectFactory();
      } catch (JargonException e) {
        throw new WdkModelException(e);
      }
    }

    return TRACE.end(factory);
  }

  private static IRODSGenQueryExecutor queryExec;
  public static IRODSGenQueryExecutor queryExecutor(final IRODSAccount acc)
  throws WdkModelException {
    TRACE.start(acc);
    if (queryExec == null) {
      TRACE.log("new IRODSGenQueryExecutor instance");
      try {
        queryExec = objectFactory().getIRODSGenQueryExecutor(acc);
      } catch (JargonException e) {
        throw new WdkModelException(e);
      }
    }

    return TRACE.end(queryExec);
  }

  static void close() {
    TRACE.start();
    system.closeAndEatExceptions();
    TRACE.end();
  }
}
