package org.gusdb.wdk.model.report;

import java.util.Map;

import org.gusdb.wdk.model.WdkUserException;

public class ReporterConfigException extends WdkUserException {

  private static final long serialVersionUID = 1L;

  public ReporterConfigException(String message) {
    super(message);
  }

  public ReporterConfigException(String message, Exception cause) {
    super(message, cause);
  }

  public ReporterConfigException(String message, Map<String, String> keyedErrors) {
    super(message, keyedErrors);
  }

}
