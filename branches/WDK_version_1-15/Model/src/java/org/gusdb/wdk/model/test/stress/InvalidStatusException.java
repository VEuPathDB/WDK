/**
 * 
 */
package org.gusdb.wdk.model.test.stress;

/**
 * @author: Jerric
 * @created: Mar 14, 2006
 * @modified by: Jerric
 * @modified at: Mar 14, 2006
 * 
 */
public class InvalidStatusException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 3801781867877203626L;

    /**
     * 
     */
    public InvalidStatusException() {
        super();
    }

    /**
     * @param arg0
     */
    public InvalidStatusException(String msg) {
        super(msg);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public InvalidStatusException(String msg, Throwable internal) {
        super(msg, internal);
    }

    /**
     * @param arg0
     */
    public InvalidStatusException(Throwable internal) {
        super(internal);
    }
}
