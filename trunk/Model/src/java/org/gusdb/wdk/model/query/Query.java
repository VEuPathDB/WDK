package org.gusdb.gus.wdk.model.query;

import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.util.Iterator;

public abstract class Query {
    
    String name;
    String displayName;
    String help;
    Boolean isCacheable = new Boolean(true);
    HashMap paramsH;
    Vector paramsV;

    public Query () {
	paramsH = new HashMap();
	paramsV = new Vector();
    }

    /////////////////////////////////////////////////////////////////////
    /////////////  Public properties ////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    public void setName(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }

    public void setDisplayName(String displayName) {
	this.displayName = displayName;
    }

    public String getDisplayName() {
	return displayName;
    }

    public void setIsCacheable(Boolean isCacheable) {
	this.isCacheable = isCacheable;
    }

    public Boolean getIsCacheable() {
	return isCacheable;
    }

    public void setHelp(String help) {
	this.help = help;
    }

    public String getHelp() {
	return help;
    }

    public void addParam(Param param) {
	paramsV.add(param);
	paramsH.put(param.getName(), param);
    }

    public Param[] getParams() {
	Param[] paramA = new Param[paramsV.size()];
	paramsV.copyInto(paramA);
	return paramA;
    }


    /////////////////////////////////////////////////////////////////////
    /////////////  Protected ////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    protected QueryInstance makeInstance() {
	return makeInstance(true);
    }

    /**
     * @param useCache True to allow this instance to insert into and/or 
     * retrieve from the cache (if permitted by the Query).
     */
    protected abstract QueryInstance makeInstance(boolean useCache);

    protected void validateParamValues(Map values) throws QueryParamsException {
	HashMap errors = null;

	// first confirm that all supplied values have legal names
	Iterator valueNames = values.keySet().iterator();
	while (valueNames.hasNext()) {
	    String valueName = (String)valueNames.next();
	    if (paramsH.get(valueName) == null) {
		throw new IllegalArgumentException("'" + valueName + "' is not a legal parameter name for query '" + name + "'"  );
	    }
	}

	int size = paramsV.size();
	for(int i=0; i<size; i++) {
	    Param p = (Param)paramsV.elementAt(i);
	    String value = (String)values.get(p.getName());
	    String errMsg = p.validateValue(value);
	    if (errMsg != null) {
		if (errors == null) errors = new HashMap();
		String booBoo[] = {value, errMsg};
		errors.put(p, booBoo);
	    }
	}
	if (errors != null) {
	    throw new QueryParamsException(errors);
	}
    }

    protected StringBuffer formatHeader() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = 
	   new StringBuffer("Query: name='" + name + "'" + newline +
			    "  displayName='" + displayName + "'" + newline +
			    "  help='" + help + "'" + newline 
			    );
       return buf;
    }

    public String toString() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = formatHeader();

       buf.append( "--- Params ---" ).append( newline );
       for( int i=0; i<paramsV.size(); i++ ){
	   buf.append( paramsV.elementAt(i) ).append( newline );
       }

       return buf.toString();
    }

	
}
