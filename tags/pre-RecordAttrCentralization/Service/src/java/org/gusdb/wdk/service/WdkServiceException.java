package org.gusdb.wdk.service;
/**
 * 
 */

/**
 * @author  Jerric
 * @created Nov 2, 2005
 */
public class WdkServiceException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 6228934705413257448L;

    /**
     * 
     */
    public WdkServiceException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public WdkServiceException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public WdkServiceException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public WdkServiceException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }
}
