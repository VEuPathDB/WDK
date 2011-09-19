package org.gusdb.wdk.model;

public class WdkException extends Exception {

    public static String modelName;

    /**
     * 
     */
    private static final long serialVersionUID = 877548355767390313L;

    public WdkException() {
        super();
    }

    public WdkException(String msg) {
        super(msg);
    }

    public WdkException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public WdkException(Throwable cause) {
        super(cause.getMessage(), cause);
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
        return buf.toString();
    }
}
