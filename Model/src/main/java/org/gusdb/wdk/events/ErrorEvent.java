package org.gusdb.wdk.events;

import org.gusdb.fgputil.events.Event;
import org.gusdb.wdk.errors.ErrorBundle;
import org.gusdb.wdk.errors.ErrorContext;

public class ErrorEvent extends Event {

  private final ErrorBundle _errorBundle;
  private final ErrorContext _errorContext;

  public ErrorEvent(ErrorBundle errorBundle, ErrorContext errorContext) {
    _errorBundle = errorBundle;
    _errorContext = errorContext;
  }

  public ErrorBundle getErrorBundle() {
    return _errorBundle;
  }

  public ErrorContext getErrorContext() {
    return _errorContext;
  }

}
