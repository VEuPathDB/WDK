package org.gusdb.wdk.model;

public class WdkRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public WdkRuntimeException(String message) {
		super(message);
	}

	public WdkRuntimeException(Throwable cause) {
		super(cause);
	}
	
	public WdkRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
