package org.gusdb.wdk.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

public abstract class AbstractEnumParam extends Param {
    
    protected boolean multiPick = false;
    protected Map vocabMap;
    protected Vector orderedKeySet = new Vector();
    protected boolean quoteInternalValue;

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

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.Param#getInternalValue(java.lang.String)
     */
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

    public Map getVocabMap() throws WdkModelException {
	initVocabMap();
	return vocabMap;
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
        if (value == null || value.toString().trim().length()== 0)
            return " - Please choose value(s) for parameter '" + name +"'";
        else 
            return " - Invalid value '" + value + "' for parameter '" + name +"'";
    }

    protected void clone(AbstractEnumParam param) {
        super.clone(param);
        param.multiPick = multiPick;
        if (vocabMap != null) {
            if (param.vocabMap == null) param.vocabMap = new LinkedHashMap();
            param.vocabMap.putAll(vocabMap);
        }
        param.orderedKeySet.addAll(orderedKeySet);
        param.quoteInternalValue = quoteInternalValue;
    }
}
