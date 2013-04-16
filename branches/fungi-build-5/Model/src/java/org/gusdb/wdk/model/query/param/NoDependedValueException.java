package org.gusdb.wdk.model.query.param;

import org.gusdb.wdk.model.WdkRuntimeException;

/**
 * This exception throws when WDK tries to access the param value list of a
 * dependent param, but the current depended value has not been assigned to that
 * param. A dependent param needs to know the depended value in order to load
 * its available param values.
 * 
 * @author jerric
 * 
 */
public class NoDependedValueException extends WdkRuntimeException {

  private static final long serialVersionUID = 1L;

  public NoDependedValueException(String message) {
    super(message);
  }

  public NoDependedValueException(Exception cause) {
    super(cause);
  }
}
