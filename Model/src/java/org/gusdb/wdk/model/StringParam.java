package org.gusdb.wdk.model;

import org.json.JSONException;
import org.json.JSONObject;

public class StringParam extends Param {

    /**
     * 
     */
    private static final long serialVersionUID = 7561711069245980824L;

    // private static final Logger logger = WdkLogManager.getLogger(
    // "org.gusdb.wdk.model.StringParam" );

    private String regex;
    private Integer length;
    private boolean quote = true;

    public StringParam() {}

    public StringParam(StringParam param) {
        super(param);
        this.regex = param.regex;
        this.length = param.length;
        this.quote = param.quote;
    }

    // ///////////////////////////////////////////////////////////////////
    // /////////// Public properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getRegex() {
        return regex;
    }

    /**
     * @return the length
     */
    public Integer getLength() {
        return length;
    }

    /**
     * @param length
     *            the length to set
     */
    public void setLength(Integer length) {
        this.length = length;
    }

    /**
     * @return the quote
     */
    public boolean isQuote() {
        return quote;
    }

    /**
     * @param quote
     *            the quote to set
     */
    public void setQuote(boolean quote) {
        this.quote = quote;
    }

    public String toString() {
        String newline = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer(super.toString() + "  sample='"
                + sample + "'" + newline + "  regex='" + regex + "'" + newline
                + "  length='" + length + "'");
        return buf.toString();
    }

    public String validateValue(Object value) throws WdkModelException {
        // check if null value is allowed; if so, pass
        if (allowEmpty && value == null) return null;

        if (value == null || value.toString().length() == 0)
            return "Missing the value";

        if (!(value instanceof String))
            return "Value must be a String " + value;

        value = decompressValue((String) value);
        String svalue = (String) value;
        int len = (length == null ? -1 : length.intValue());

        if (svalue == null) return "Missing the value";
        if (regex != null && !svalue.matches(regex))
            return "Value '" + svalue + "'does not match regular expression '"
                    + regex + "'";
        if (length != null && svalue.length() > len)
            return "Value may be no longer than " + len
                    + " characters.  (It is " + svalue.length() + ".)";

        return null;
    }

    // ///////////////////////////////////////////////////////////////
    // protected methods
    // ///////////////////////////////////////////////////////////////

    public void resolveReferences(WdkModel model) throws WdkModelException {}

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#getInternalValue(java.lang.String)
     */
    @Override
    public String getInternalValue(Object value) throws WdkModelException {
        // check if null value is allowed
        if (allowEmpty && value == null) return getEmptyValue();

        String strValue = (value == null) ? "" : value.toString();
        strValue = (String) decompressValue(strValue);
        strValue = strValue.replaceAll("'", "''");
        if (quote) strValue = "'" + strValue + "'";
        return strValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    public Param clone() {
        return new StringParam(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#appendJSONContent(org.json.JSONObject)
     */
    @Override
    protected void appendJSONContent(JSONObject jsParam) throws JSONException {
    // TODO Auto-generated method stub

    }
}
