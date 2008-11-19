package org.gusdb.wdk.model;

import java.util.Map;

import org.gusdb.wdk.model.query.param.Param;

public class WdkModelException extends Exception {

    public static String modelName;

    /**
     * 
     */
    private static final long serialVersionUID = 877548355767390313L;
    Map<Param, String[]> booBoos = null;

    public WdkModelException(Map<Param, String[]> booBoos) {
        super();
        this.booBoos = booBoos;
    }

    public WdkModelException() {
        super();
    }

    public WdkModelException(String msg) {
        super(msg);
    }

    public WdkModelException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public WdkModelException(Throwable cause) {
        super(cause);
    }

    public WdkModelException(String message, Map<Param, String[]> booBoos) {
        super(message);
        this.booBoos = booBoos;
    }

    public WdkModelException(Throwable cause, Map<Param, String[]> booBoos) {
        super(cause);
        this.booBoos = booBoos;
    }

    public WdkModelException(String message, Throwable cause,
            Map<Param, String[]> booBoos) {
        super(message, cause);
        this.booBoos = booBoos;
    }

    /**
     * @return Map where keys are Params and values are an tuple of (value,
     *         errMsg), one for each error param value
     */
    public Map<Param, String[]> getBooBoos() {
        return booBoos;
    }

    /**
     * @return A default formatting of contained errors
     */
    public String formatErrors() {

        String newline = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer(newline);
        String message = super.getMessage();

        if (message != null) {
            // add project header
            if (modelName != null) {
                String prefix = "[" + modelName + "] ";
                if (!message.trim().startsWith(prefix)) buf.append(prefix);
            }

            buf.append(message + newline);
        }
        if (booBoos != null) {
            for (Param param : booBoos.keySet()) {
                String[] details = booBoos.get(param);
                buf.append(param.getName() + " value '" + details[0]
                        + "' has an error: " + details[1] + newline);
            }
        }
        return buf.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Throwable#getMessage()
     */
    @Override
    public String getMessage() {
        return formatErrors();
    }
}
