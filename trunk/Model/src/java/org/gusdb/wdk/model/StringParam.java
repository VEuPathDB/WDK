package org.gusdb.gus.wdk.model;


public class StringParam extends Param {
    
    String sample;
    String regex;
    private Boolean substitute = Boolean.FALSE;

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

    public void setSubstitute(String subst) {
        substitute = Boolean.valueOf(subst);
    }
    
    public String toString() {
       String newline = System.getProperty( "line.separator" );
       StringBuffer buf = 
	   new StringBuffer(super.toString() +
			    "  sample='" + sample + "'" + newline +
			    "  regex='" + regex + "'" + newline +
                "  substitute='" + substitute + "'"
			    );
       return buf.toString();
    }

    /////////////////////////////////////////////////////////////////
    // protected methods
    /////////////////////////////////////////////////////////////////

    protected void resolveReferences(WdkModel model) throws WdkModelException {}

    protected String validateValue(String value) {
        if (regex == null) {
            // TODO - Correct? Assuming no regex means we don't care about value
            return null;
        }
        if (substitute.booleanValue() && value != null) {
            value = substitute(value);
        }
        if ( value == null || !value.matches(regex)) {
            return "Value '" + value + "'does not match regex '" + regex + "' or is null";
        }
        return null;
    }

    private String substitute(String value) {
        return value.replaceAll("*","%");
    }

}
