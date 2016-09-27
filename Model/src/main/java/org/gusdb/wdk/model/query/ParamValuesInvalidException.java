package org.gusdb.wdk.model.query;

import java.util.Map;

import org.gusdb.wdk.model.WdkUserException;

public class ParamValuesInvalidException extends WdkUserException {

  private static final long serialVersionUID = 1L;

  public ParamValuesInvalidException(String message, Map<String, String> errors) {
    super(message, errors);
  }

}
