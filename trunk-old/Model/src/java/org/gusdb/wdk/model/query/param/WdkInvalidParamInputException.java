/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import org.gusdb.wdk.model.WdkUserException;

/**
 * @author xingao
 *
 */
public class WdkInvalidParamInputException extends WdkUserException {

    /**
     * 
     */
    private static final long serialVersionUID = 1727774057465109560L;

    /**
     * 
     */
    public WdkInvalidParamInputException() {
    }

    /**
     * @param message
     */
    public WdkInvalidParamInputException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public WdkInvalidParamInputException(Throwable cause) {
        super(cause);
    }

    /**
     * @param msg
     * @param cause
     */
    public WdkInvalidParamInputException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
