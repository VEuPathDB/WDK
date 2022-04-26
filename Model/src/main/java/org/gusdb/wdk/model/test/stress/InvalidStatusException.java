package org.gusdb.wdk.model.test.stress;

/**
 * @author Jerric
 */
public class InvalidStatusException extends Exception {

    private static final long serialVersionUID = 3801781867877203626L;

    public InvalidStatusException() {
        super();
    }

    /**
     * @param msg
     */
    public InvalidStatusException(String msg) {
        super(msg);
    }

    /**
     * @param msg
     * @param internal
     */
    public InvalidStatusException(String msg, Throwable internal) {
        super(msg, internal);
    }

    /**
     * @param internal
     */
    public InvalidStatusException(Throwable internal) {
        super(internal);
    }
}
