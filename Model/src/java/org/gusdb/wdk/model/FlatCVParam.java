package org.gusdb.gus.wdk.model;

import java.util.HashMap;


public class FlatCVParam extends Param {
    
    boolean multiPick = false;
    Query query;
    HashMap vocabMap;
    boolean quoteInternalValue;

    public FlatCVParam () {
	super();
    }

    /////////////////////////////////////////////////////////////////////
    /////////////  Public properties ////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    public void setMultiPick(Boolean multiPick) {
	this.multiPick = multiPick.booleanValue();
    }

    public Boolean getMultiPick() {
	return new Boolean(multiPick);
    }

    public void setQuery(Query query) {
	this.query = query;
	// here must check columns
    }

    public Query getQuery() {
	return query;
    }

    public void setQuoteInternalValue(Boolean quote) {
	this.quoteInternalValue = quote.booleanValue();
    }

    public Boolean getQuoteInternalValue() {
	return new Boolean(quoteInternalValue);
    }

    public String validateValue(Object value) throws WdkModelException {
	String err = null;
	if (multiPick) {
	    String sval = (String)value;
	    String [] values = sval.split(",");
	    for (int i=0; i<values.length; i++) {
		err = validateSingleValue(values[i]);
		if (err != null) break;
	    }
	    
	} else err = validateSingleValue(value);	
	
	return err;
    }

    public Object getInternalValue(Object value) throws WdkModelException {
	initVocabMap();
	String internalValue;
	if (multiPick) {
	    StringBuffer buf = new StringBuffer();
	    
	    String [] values = value.toString().split(",");
	    for (int i=0; i<values.length; i++) {
		String v = vocabMap.get(values[i]).toString();
		if (i > 0) buf.append(",");
		if (quoteInternalValue) buf.append("'" + v + "'");
		else buf.append( v);
	    }
	    internalValue = buf.toString();
	} else {
	    internalValue = vocabMap.get(value).toString();
	    if (quoteInternalValue) internalValue = "'" + internalValue + "'";
	}
	return internalValue;
    }

    public String[] getVocab() throws WdkModelException {
	initVocabMap();
	String[] a = new String[0];
	a = (String[])(vocabMap.keySet().toArray(a));
	return a;
    }

    /////////////////////////////////////////////////////////////////////
    /////////////  Protected properties ////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    protected void resolveReferences(WdkModel model) throws WdkModelException {
	query.resolveReferences(model);
    }

    public void setResources(WdkModel model)throws WdkModelException {
	query.setResources(model);
    }
    
    protected void initVocabMap() throws WdkModelException {
	if (vocabMap == null) {
	    vocabMap = new HashMap();
	    QueryInstance instance = query.makeInstance();
	    ResultList result = instance.getResult();
	    while (result.next()) {
		vocabMap.put(result.getValue("term"),
			     result.getValue("internal"));
	    }
	}
    }

    protected String validateSingleValue(Object value) throws WdkModelException {
	initVocabMap();

	if (vocabMap.containsKey(value)) 
	    return null;
	else 
	    return "Invalid value '" + value + "' for parameter '" + name +"'";
    }


}
