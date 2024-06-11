package org.gusdb.wdk.errors;

import static org.gusdb.fgputil.FormatUtil.getStackTrace;

import org.apache.log4j.Logger;

public class ServerErrorBundle implements ErrorBundle {

  private static final Logger LOG = Logger.getLogger(ServerErrorBundle.class);

  private Exception _cause;

  public ServerErrorBundle(Exception cause) {
      _cause = cause;
      LOG.debug("Created bundle with exception: ", cause);
  }

  @Override
  public boolean hasErrors() {
    return _cause != null;
  }

  @Override
  public Exception getException() {
    return _cause;
  }

  @Override
  public String getDetailedDescription() {
    return getStackTrace(_cause);
  }

}
