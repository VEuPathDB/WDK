package org.gusdb.gus.wdk.model.implementation;

import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.util.Iterator;

import org.gusdb.gus.wdk.model.QueryI;
import org.gusdb.gus.wdk.model.QueryParamsException;
import org.gusdb.gus.wdk.model.Param;
import org.gusdb.gus.wdk.model.Column;

public class Query implements QueryI {
    
    String name;
    String displayName;
    String help;
    Boolean isCacheable = new Boolean(true);
    HashMap paramsH;
    Vector paramsV;
    HashMap columnsH;
    Vector columnsV;

    public Query () {
	paramsH = new HashMap();
	paramsV = new Vector();
	columnsH = new HashMap();
	columnsV = new Vector();
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
    public void addParam(Param param) {
	paramsV.add(param);
	paramsH.put(param.getName(), param);
    }

    public Param[] getParams() {
	Param[] paramA = new Param[paramsV.size()];
	paramsV.copyInto(paramA);
	return paramA;
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

    public void addColumn(Column column) {
	columnsV.add(column);
	columnsH.put(column.getName(), column);
    }

    public Column[] getColumns() {
	Column[] columnA = new Column[columnsV.size()];
	columnsV.copyInto(columnA);
	return columnA;
    }

    public Column getColumn(String columnName) {
	return (Column)columnsH.get(columnName);
    }

    /////////////////////////////////////////////////////////////////////
    /////////////  Protected ////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

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

	// then check that all params have supplied values
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
	   new StringBuffer("Query: name='" + getName() + "'" + newline +
			    "  displayName='" + getDisplayName() + "'" + newline +
			    "  help='" + getHelp() + "'" + newline 
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
