package org.gusdb.wdk.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Vector;
import java.util.Iterator;

import org.gusdb.wdk.model.ResultFactory;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.Column;
import org.gusdb.wdk.model.QueryInstance;

public abstract class Query {
    
    protected String name;
    protected String fullName;
    protected String displayName;
    protected String description;
    protected String help;
    protected Boolean isCacheable = new Boolean(true);
    protected LinkedHashSet<Reference> paramRefs;
    protected Map<String, Param> paramsH;
    protected Vector<Param> paramsV;
    protected Map<String, Column> columnsH;
    protected Vector<Column> columnsV;
    protected ResultFactory resultFactory;
   
    public Query () {
	paramRefs = new LinkedHashSet<Reference>();
	paramsH = new LinkedHashMap<String, Param>();
	paramsV = new Vector<Param>();
	columnsH = new LinkedHashMap<String, Column>();
	columnsV = new Vector<Column>();
    }

    /////////////////////////////////////////////////////////////////////
    /////////////  Setters for initialization ///////////////////////////
    /////////////////////////////////////////////////////////////////////

    public void setName(String name) {
	this.name = name;
    }

    public void setDisplayName(String displayName) {
	this.displayName = displayName;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public void addParamRef(Reference paramRef) {
	paramRefs.add(paramRef);
    }

    public void setIsCacheable(Boolean isCacheable) {
	this.isCacheable = isCacheable;
    }

    public void setHelp(String help) {
	this.help = help;
    }

    public void addColumn(Column column) {

	column.setQuery(this);
	columnsV.add(column);
	columnsH.put(column.getName(), column);
    }


    /////////////////////////////////////////////////////////////////////////
    //  public getters
    /////////////////////////////////////////////////////////////////////////

    public String getName() {
	return name;
    }

    public String getFullName() {
	return fullName == null? name : fullName;
    }
    
    public String getDisplayName() {
	return (displayName != null)? displayName : name;
    }

    public Param[] getParams() {
	Param[] paramA = new Param[paramsV.size()];
	paramsV.copyInto(paramA);
	return paramA;
    }

    public Boolean getIsCacheable() {
	return isCacheable;
    }

    public String getDescription() {
	return description;
    }

    public String getHelp() {
	return help;
    }

    public Column[] getColumns() {
	Column[] columnA = new Column[columnsV.size()];
	columnsV.copyInto(columnA);
	return columnA;
    }

    public Map<String, Column> getColumnMap() {
	return columnsH;
    }

    public Column getColumn(String columnName) throws WdkModelException {
	if (columnsH.get(columnName) == null)
	    throw new WdkModelException("Query " + name 
				+ " does not have a column '" 
				+ columnName + "'");
	return (Column)columnsH.get(columnName);
    }
    
    public abstract QueryInstance makeInstance();

    /**
     * transform a set of param values to internal param values
     */
    public Map<String, String> getInternalParamValues(Map<String, Object> values) throws WdkModelException {

	Map<String, String> internalValues = new LinkedHashMap<String, String>();
	Iterator<String> paramNames = values.keySet().iterator();
	while (paramNames.hasNext()) {
	    String paramName = paramNames.next();
	    Param param = paramsH.get(paramName);
	    internalValues.put(paramName, 
			       param.getInternalValue(values.get(paramName).toString()));
	}
	return internalValues;
    }

    public String toString() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = formatHeader();

       buf.append( "--- Columns ---" ).append( newline );
       for( int i=0; i<columnsV.size(); i++ ){
	   buf.append( columnsV.elementAt(i) ).append( newline );
       }

       buf.append( "--- Params ---" ).append( newline );
       for( int i=0; i<paramsV.size(); i++ ){
	   buf.append( paramsV.elementAt(i) ).append( newline );
       }

       return buf.toString();
    }


    /////////////////////////////////////////////////////////////////////
    /////////////  Protected methods ////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    public ResultFactory getResultFactory() {
	return resultFactory;
    }

    void setSetName(String querySetName){
	this.fullName = querySetName + "." + name;
    }

    protected void addParam(Param param) {
	paramsV.add(param);
	paramsH.put(param.getName(), param);
    }

    Param getParam(String paramName) {
	return paramsH.get(paramName);
    }

    protected void resolveReferences(WdkModel model) throws WdkModelException {
	Iterator paramRefsIter = paramRefs.iterator();
	while (paramRefsIter.hasNext()) {
	    Reference paramRef = (Reference)paramRefsIter.next();
	    String twoPartName = paramRef.getTwoPartName();
	    Param param = (Param)model.resolveReference(twoPartName, 
							this.name, 
							"Query", 
							"paramRef");
	    addParam(param);
	    param.resolveReferences(model);
	}
    }

    protected void setResources(WdkModel model) throws WdkModelException {

	this.resultFactory = model.getResultFactory();

	Iterator paramIterator = paramsH.values().iterator();
	while (paramIterator.hasNext()) {
	    Param param = (Param)paramIterator.next();
	    param.setResources(model);
	}
    }

    protected void validateParamValues(Map<String, Object> values) throws WdkUserException, WdkModelException {
	LinkedHashMap<Param, String[]> errors = null;
	
	// first confirm that all supplied values have legal names
	Iterator<String> valueNames = values.keySet().iterator();
	while (valueNames.hasNext()) {
	    String valueName = valueNames.next();
	    if (paramsH.get(valueName) == null) {
		throw new WdkUserException("'" + valueName + "' is not a legal parameter name for query '" + getFullName() + "'"  );
	    }
	}

	// then check that all params have supplied values
	int size = paramsV.size();
	for(int i=0; i<size; i++) {
	    Param p = paramsV.elementAt(i);
	    Object value = values.get(p.getName());
	    String errMsg;
	    if (value == null) {
		errMsg = "No value supplied";
	    } else {
		errMsg = p.validateValue(value);
	    }
	    if (errMsg != null) {
		if (errors == null) errors = new LinkedHashMap<Param, String[]>();
		String booBoo[] = {value.toString(), errMsg};
		errors.put(p, booBoo);
	    }
	}
	if (errors != null) {
	    throw new WdkUserException(errors);
	}
    }

    protected void applyDefaults(Map<String, Object> values) {
	int size = paramsV.size();
	for(int i=0; i<size; i++) {
	    Param p = paramsV.elementAt(i);
	    if (values.get(p.getName()) == null && p.getDefault() != null) 
		values.put(p.getName(), p.getDefault());
	}
    }

    protected StringBuffer formatHeader() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = 
	   new StringBuffer("Query: name='" + getName() + "'" + newline +
			    "  displayName='" + getDisplayName() + "'" + newline +
			    "  description='" + getDescription() + "'" + newline +
			    "  help='" + getHelp() + "'" + newline 
			    );
       return buf;
    }

}
