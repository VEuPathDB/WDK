package org.gusdb.wdk.model;

import java.util.HashMap;

public abstract class AbstractEnumParam extends Param {
    
    boolean multiPick = false;
    HashMap vocabMap;
    boolean quoteInternalValue;

    public AbstractEnumParam () {
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

    public String[] getVocabInternal() throws WdkModelException {
	initVocabMap();
	String[] a = new String[0];
	a = (String[])(vocabMap.values().toArray(a));
	return a;
    }
    /////////////////////////////////////////////////////////////////////
    /////////////  Protected properties ////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    protected abstract void initVocabMap() throws WdkModelException;

    protected String validateSingleValue(Object value) throws WdkModelException {
        initVocabMap();

        if (vocabMap.containsKey(value)) {
            return null;
        }
        return "Invalid value '" + value + "' for parameter '" + name +"'";
    }


}
