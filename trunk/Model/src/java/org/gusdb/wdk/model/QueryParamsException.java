package org.gusdb.gus.wdk.model;

import java.util.Map;
import java.util.Iterator;

public class QueryParamsException extends Exception {

    Map booBoos;

    public QueryParamsException(Map booBoos) {
	super();
	this.booBoos = booBoos;
    }

    public QueryParamsException(String message, Map booBoos) {
	super(message);
	this.booBoos = booBoos;
    }

    public QueryParamsException(Throwable cause, Map booBoos) {
	super(cause);
	this.booBoos = booBoos;
    }

    public QueryParamsException(String message, Throwable cause, Map booBoos) {
	super(message, cause);
	this.booBoos = booBoos;
    }

    /**
     * @return Map where keys are Params and values are an tuple of (value, errMsg), one for each error param value
     */
    public Map getBooBoos() {
	return booBoos;
    }

    /**
     * @return A default formatting of contained errors
     */
    public String formatErrors() {
	Iterator keys = booBoos.keySet().iterator();
	String newline = System.getProperty( "line.separator" );
	StringBuffer buf = new StringBuffer(newline);
	while(keys.hasNext()) {
	    Param param = (Param)keys.next();
	    String[] details = (String[])booBoos.get(param);
	    buf.append(param.getName() + " value '" + details[0] + "' has an error: " 
		       + details[1] + newline);
	}
	return buf.toString();
    }

}
