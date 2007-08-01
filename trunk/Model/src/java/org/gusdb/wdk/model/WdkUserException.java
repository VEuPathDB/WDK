package org.gusdb.wdk.model;


/**
 * @author Jerric
 * @modified Jan 6, 2006
 */
public class WdkUserException extends Exception {

    public static String modelName;

    /**
     * 
     */
    private static final long serialVersionUID = 442861349675564533L;

    public WdkUserException(String message) {
        super(message);
    }

    public WdkUserException(Throwable cause) {
        super(cause);
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
                if (!message.trim().startsWith(prefix))
                    buf.append(prefix);
            }

            buf.append(message + newline);
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
