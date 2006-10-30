package org.gusdb.wdk.model;

import java.util.Map;
import java.util.Iterator;

/**
 * @author Jerric
 * @modified Jan 6, 2006
 */
public class WdkUserException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 442861349675564533L;
    Map booBoos = null;

    public WdkUserException(Map booBoos) {
        super();
        this.booBoos = booBoos;
    }

    public WdkUserException(String message) {
        super(message);
    }

    public WdkUserException(Throwable cause) {
        super(cause);
    }

    public WdkUserException(String message, Map booBoos) {
        super(message);
        this.booBoos = booBoos;
    }

    public WdkUserException(Throwable cause, Map booBoos) {
        super(cause);
        this.booBoos = booBoos;
    }

    public WdkUserException(String message, Throwable cause, Map booBoos) {
        super(message, cause);
        this.booBoos = booBoos;
    }

    /**
     * @return Map where keys are Params and values are an tuple of (value,
     *         errMsg), one for each error param value
     */
    public Map getBooBoos() {
        return booBoos;
    }

    /**
     * @return A default formatting of contained errors
     */
    public String formatErrors() {

        String newline = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer(newline);
        String message = super.getMessage();
        if (message != null) buf.append(message + newline);
        if (booBoos != null) {
            Iterator keys = booBoos.keySet().iterator();
            while (keys.hasNext()) {
                Param param = (Param) keys.next();
                String[] details = (String[]) booBoos.get(param);
                buf.append(param.getName() + " value '" + details[0]
                        + "' has an error: " + details[1] + newline);
            }
        }
        return buf.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#getMessage()
     */
    @Override
    public String getMessage() {
        return formatErrors();
    }
}
