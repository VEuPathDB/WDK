/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;

/**
 * @author xingao
 * 
 */
public class WdkEmptyEnumListException extends WdkModelException {

    /**
     * 
     */
    private static final long serialVersionUID = -4587879598508535198L;

    /**
     * @param paramErrors
     */
    public WdkEmptyEnumListException(Map<String, String> paramErrors) {
        super(paramErrors);
        // TODO Auto-generated constructor stub
    }

    /**
     * 
     */
    public WdkEmptyEnumListException() {}

    /**
     * @param msg
     */
    public WdkEmptyEnumListException(String msg) {
        super(msg);
    }

    /**
     * @param msg
     * @param cause
     */
    public WdkEmptyEnumListException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * @param cause
     */
    public WdkEmptyEnumListException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param paramErrors
     */
    public WdkEmptyEnumListException(String message,
            Map<String, String> paramErrors) {
        super(message, paramErrors);
    }

    /**
     * @param cause
     * @param paramErrors
     */
    public WdkEmptyEnumListException(Throwable cause,
            Map<String, String> paramErrors) {
        super(cause, paramErrors);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     * @param paramErrors
     */
    public WdkEmptyEnumListException(String message, Throwable cause,
            Map<String, String> paramErrors) {
        super(message, cause, paramErrors);
    }

}
