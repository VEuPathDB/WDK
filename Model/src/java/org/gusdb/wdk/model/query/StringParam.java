package org.gusdb.gus.wdk.model.query;


public class StringParam extends Param {
    
    String sample;
    String regex;

    public StringParam () {}

    /////////////////////////////////////////////////////////////////////
    /////////////  Public properties ////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    public void setSample(String sample) {
	this.sample = sample;
    }

    public String getSample() {
	return sample;
    }

    public void setRegex(String regex) {
	this.regex = regex;
    }

    public String getRegex() {
	return regex;
    }

    /////////////////////////////////////////////////////////////////////
    /////////////  Protected ////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////

    protected String validateValue(String value) {
	if (regex != null && !value.matches(regex)) 
	    return "Does not match regex '" + regex + "'";
	else return null;
    }


    public String toString() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = 
	   new StringBuffer(super.toString() +
			    "  sample='" + sample + "'" + newline +
			    "  regex='" + regex + "'" + newline
			    );
       return buf.toString();
    }
}
