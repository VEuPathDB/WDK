package org.gusdb.gus.wdk.model.query;

import java.util.Hashtable;
import java.util.Enumeration;

public class QueryParamsException extends Exception {

    Hashtable booBoos;

    public QueryParamsException(Hashtable booBoos) {
	super();
	this.booBoos = booBoos;
    }

    public QueryParamsException(String message, Hashtable booBoos) {
	super(message);
	this.booBoos = booBoos;
    }

    public QueryParamsException(Throwable cause, Hashtable booBoos) {
	super(cause);
	this.booBoos = booBoos;
    }

    public QueryParamsException(String message, Throwable cause, Hashtable booBoos) {
	super(message, cause);
	this.booBoos = booBoos;
    }

    /**
     * @return Hashtable where keys are Params and values are an tuple of (value, errMsg), one for each error param value
     */
    public Hashtable getBooBoos() {
	return booBoos;
    }

    /**
     * @return A default formatting of contained errors
     */
    public String formatErrors() {
	Enumeration keys = booBoos.keys();
	String newline = System.getProperty( "line.separator" );
	StringBuffer buf = new StringBuffer(newline);
	while(keys.hasMoreElements()) {
	    Param param = (Param)keys.nextElement();
	    String[] details = (String[])booBoos.get(param);
	    buf.append(param.getName() + " value '" + details[0] + "' has an error: " 
		       + details[1] + newline);
	}
	return buf.toString();
    }

}
