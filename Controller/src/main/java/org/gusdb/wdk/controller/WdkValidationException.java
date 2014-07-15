package org.gusdb.wdk.controller;

import org.gusdb.wdk.controller.actionutil.ParameterValidator;

public class WdkValidationException extends Exception {

	private static final long serialVersionUID = 1L;

	private ParameterValidator _validator;
	
	public WdkValidationException(String message) {
		super(message);
	}

  public WdkValidationException(String message,
      ParameterValidator validator) {
    super(message);
    _validator = validator;
  }

  public ParameterValidator getValidator() {
    return _validator;
  }
	
}
