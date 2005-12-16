package org.gusdb.wdk.model;



import java.util.logging.Logger;


public class StringParam extends Param {
    
    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.model.StringParam"); 
    
    private String sample;
    private String regex;
    private Boolean substitute = Boolean.FALSE;
    private Integer length;

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

    public void setLength(String length) {
        this.length = new Integer(length);
    }

    public Integer getLength() {
        return length;
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
			    "  length='" + length + "'" + newline +
			    "  substitute='" + substitute + "'"
			    );
       return buf.toString();
    }

    public String validateValue(Object value) throws WdkModelException {
        if (!(value instanceof String)) 
            throw new WdkModelException("Value must be a String " + value) ;

	String svalue = (String)value;
	int len = length.intValue();

	if (svalue == null) return "Value is null";
        if (regex != null && !svalue.matches(regex)) 
            return "Value '" + svalue + "'does not match regex '" + regex +"'";
        if (length != null && svalue.length() > len) 
            return "Value may be no longer than " + len + " characters.  (It is " + svalue.length() + ".)";

        return null;
    }

    /////////////////////////////////////////////////////////////////
    // protected methods
    /////////////////////////////////////////////////////////////////

    protected void resolveReferences(WdkModel model) throws WdkModelException {}

    public String substitute(String value) {
        logger.finest("substitute is called");
        if (!substitute.equals(Boolean.TRUE)) {
            return value;
        }
        StringBuffer ret = new StringBuffer();
        for (int i=0; i < value.length(); i++) {
            if ('*' != value.charAt(i)) {
                ret.append(value.charAt(i));
            } else {
                ret.append('%');
            }
        }
        logger.finest("I've created "+ret.toString());
        return ret.toString();
    }

}
