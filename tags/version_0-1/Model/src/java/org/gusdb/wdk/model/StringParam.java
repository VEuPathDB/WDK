package org.gusdb.wdk.model;



import java.util.logging.Logger;


public class StringParam extends Param {
    
    private static final Logger logger = WdkLogManager.getLogger("org.gusdb.wdk.model.StringParam"); 
    
    private String sample;
    private String regex;
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

    protected String validateValue(Object value) throws WdkModelException {
        if (!(value instanceof String)) {
            throw new WdkModelException("Value must be a String " + value) ;
        }
	    String svalue = (String)value;
        if (regex == null) {
            // TODO - Correct? Assuming no regex means we don't care about value
            return null;
        }
        if ( svalue == null || !svalue.matches(regex)) {
            return "Value '" + svalue + "'does not match regex '" + regex + "' or is null";
        }
        return null;
    }

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
