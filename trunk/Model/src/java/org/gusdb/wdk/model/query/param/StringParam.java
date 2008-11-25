package org.gusdb.wdk.model.query.param;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author xingao
 * 
 *         The input to a string param is a string, or a compressed version of
 *         it
 * 
 *         The output is the same uncompressed string, quoted as needed.
 */
public class StringParam extends Param {

    /**
     * 
     */
    private static final long serialVersionUID = 7561711069245980824L;

    // private static final Logger logger = WdkLogManager.getLogger(
    // "org.gusdb.wdk.model.StringParam" );

    private String regex;
    private int length = 0;
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
    public int getLength() {
        return length;
    }

    /**
     * @param length
     *            the length to set
     */
    public void setLength(int length) {
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

    public void validateValue(String value) throws WdkModelException {
        // check if null value is allowed; if so, pass
        if (allowEmpty && value == null) return;

        if (value == null || value.length() == 0)
            throw new WdkModelException("StringParam [" + getFullName()
                    + "] Missing the value");

        value = decompressValue(value);

        if (value == null) throw new WdkModelException("Missing the value");

        if (regex != null && !value.matches(regex))
            throw new WdkModelException("Value '" + value
                    + "'does not match regular expression '" + regex + "'");
        if (length != 0 && value.length() > length)
            throw new WdkModelException("Value may be no longer than " + length
                    + " characters.  (It is " + value.length() + ".)");
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
    public String getInternalValue(String value) throws WdkModelException {
        // validate it first
        validateValue(value);

        if (allowEmpty && value == null) value = getEmptyValue();

        value = (String) decompressValue(value);
        value = value.replaceAll("'", "''");
        if (quote) value = "'" + value + "'";
        return value;
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
    // nothing to be added
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.param.Param#getUserIndependentValue(java.lang
     * .String)
     */
    @Override
    protected String getUserIndependentValue(String value)
            throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException, JSONException, SQLException {
        if (value != null && value.length() > Utilities.MAX_PARAM_VALUE_SIZE) {
            value = queryFactory.makeClobChecksum(value);
        }
        return value;
    }
}
