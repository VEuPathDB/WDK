package org.gusdb.wdk.model.report;

import java.util.Map;

import org.gusdb.wdk.model.WdkUserException;

public class ReporterConfigException extends WdkUserException {

  public enum ErrorType {
    MISFORMAT,
    DATA_VALIDATION
  }

  private final ErrorType _errorType;

  public ReporterConfigException(String message) {
    this(message, ErrorType.MISFORMAT);
  }

  public ReporterConfigException(String message, ErrorType errorType) {
    super(message);
    _errorType = errorType;
  }

  public ReporterConfigException(String message, Exception cause) {
    this(message, cause, ErrorType.MISFORMAT);
  }

  public ReporterConfigException(String message, Exception cause, ErrorType errorType) {
    super(message, cause);
    _errorType = errorType;
  }

  public ReporterConfigException(String message, Map<String, String> keyedErrors) {
    this(message, keyedErrors, ErrorType.MISFORMAT);
  }

  public ReporterConfigException(String message, Map<String, String> keyedErrors, ErrorType errorType) {
    super(message, keyedErrors);
    _errorType = errorType;
  }

  public ErrorType getErrorType() {
    return _errorType;
  }
}
