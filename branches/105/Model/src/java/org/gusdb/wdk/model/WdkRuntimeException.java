package org.gusdb.wdk.model;

public class WdkRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public WdkRuntimeException(String message) {
		super(message);
	}

	public WdkRuntimeException(Exception cause) {
		super(cause);
	}
	
	public WdkRuntimeException(String message, Exception cause) {
		super(message, cause);
	}
	
}
