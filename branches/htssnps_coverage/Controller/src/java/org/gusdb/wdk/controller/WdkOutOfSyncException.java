/**
 * 
 */
package org.gusdb.wdk.controller;

import org.gusdb.wdk.model.WdkUserException;

/**
 * @author xingao
 *
 */
public class WdkOutOfSyncException extends WdkUserException {

    /**
     * 
     */
    private static final long serialVersionUID = 3746744460717454633L;

    /**
     * 
     */
    public WdkOutOfSyncException() {
    // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public WdkOutOfSyncException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public WdkOutOfSyncException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param msg
     * @param cause
     */
    public WdkOutOfSyncException(String msg, Throwable cause) {
        super(msg, cause);
        // TODO Auto-generated constructor stub
    }

}
