package org.gusdb.wdk.model.query.param;

import java.security.NoSuchAlgorithmException;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author xingao
 * 
 *         raw data: same as internal data, a raw string;
 * 
 *         user-dependent data: same as user-independent data, can be either a
 *         raw string or a compressed checksum;
 * 
 *         user-independent data: same as user-dependent data;
 * 
 *         internal data: similar to raw data, but the single quotes are
 *         escaped, and the outer quotes are added if necessary;
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

    // ///////////////////////////////////////////////////////////////
    // protected methods
    // ///////////////////////////////////////////////////////////////

    public void resolveReferences(WdkModel model) throws WdkModelException {}

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

    /**
     * the dependent value is the same as the independent value
     * 
     * @see org.gusdb.wdk.model.query.param.Param#dependentValueToIndependentValue(org.gusdb.wdk.model.user.User,
     *      java.lang.String)
     */
    @Override
    public String dependentValueToIndependentValue(User user,
            String dependentValue) {
        return dependentValue;
    }

    /**
     * the dependent value is the same as the independent value
     * 
     * @see org.gusdb.wdk.model.query.param.Param#independentValueToDependentValue(org.gusdb.wdk.model.user.User,
     *      java.lang.String)
     */
    @Override
    public String independentValueToDependentValue(User user,
            String independentValue) {
        return independentValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.param.Param#independentValueToInternalValue
     * (java.lang.String)
     */
    @Override
    public String dependentValueToInternalValue(User user,
            String dependentValue) throws WdkModelException {
        String rawValue = decompressValue(dependentValue);
        if (rawValue == null || rawValue.length() == 0) rawValue = emptyValue;
        rawValue = rawValue.replaceAll("'", "''");
        if (quote) rawValue = "'" + rawValue + "'";
        return rawValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.param.Param#independentValueToRawValue(java
     * .lang.String)
     */
    @Override
    public String dependentValueToRawValue(User user, String dependentValue)
            throws WdkModelException {
        return decompressValue(dependentValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.param.Param#rawValueToIndependentValue(java
     * .lang.String)
     */
    @Override
    public String rawOrDependentValueToDependentValue(User user, String rawValue)
            throws NoSuchAlgorithmException, WdkModelException {
        return compressValue(rawValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.param.Param#validateValue(java.lang.String)
     */
    @Override
    protected void validateValue(User user, String dependentValue)
            throws WdkUserException, WdkModelException {
        String rawValue = decompressValue(dependentValue);
        if (regex != null && !rawValue.matches(regex))
            throw new WdkUserException("Value '" + rawValue
                    + "'does not match regular expression '" + regex + "'");
        if (length != 0 && rawValue.length() > length)
            throw new WdkModelException("Value may be no longer than " + length
                    + " characters.  (It is " + rawValue.length() + ".)");
    }
}
