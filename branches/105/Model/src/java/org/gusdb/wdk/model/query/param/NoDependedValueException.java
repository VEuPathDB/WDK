package org.gusdb.wdk.model.query.param;

import org.gusdb.wdk.model.WdkRuntimeException;

public class NoDependedValueException extends WdkRuntimeException {

	private static final long serialVersionUID = 1L;
	
	public NoDependedValueException(String message) {
		super(message);
	}
}
