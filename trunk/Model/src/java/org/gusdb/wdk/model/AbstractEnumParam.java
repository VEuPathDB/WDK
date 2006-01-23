package org.gusdb.wdk.model;

import java.util.HashMap;
import java.util.Vector;

public abstract class AbstractEnumParam extends Param {
    
    boolean multiPick = false;
    HashMap vocabMap;
    Vector orderedKeySet = new Vector();
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

    public String getInternalValue(String value) throws WdkModelException {
	initVocabMap();
	String internalValue;
	if (multiPick) {
	    StringBuffer buf = new StringBuffer();
	    
	    String [] values = value.split(",");
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
	int keySize = orderedKeySet.size();
	String[] a = new String[keySize];
	for (int i = 0; i < keySize; i++){
	    a[i] = orderedKeySet.elementAt(i).toString();
	}
	return a;
    }

    public String[] getVocabInternal() throws WdkModelException {
	initVocabMap();
	int keySize = orderedKeySet.size();
	String[] a = new String[keySize];
	for (int i = 0; i < keySize; i++){
	    Object nextKey = orderedKeySet.elementAt(i);
	    a[i] = vocabMap.get(nextKey).toString();
	}
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
