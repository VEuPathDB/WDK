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
public class TypeAheadParam extends StringParam {

    /**
     * 
     */
    private static final long serialVersionUID = 7561711069245980824L;

    // private static final Logger logger = WdkLogManager.getLogger(
    // "org.gusdb.wdk.model.TypeAheadParam" );

    private String recordClassRef;
    private String dataTypeRef;
    private boolean isDataTypeParam;

    public TypeAheadParam() {}

    public TypeAheadParam(TypeAheadParam param) {
        super(param);
	this.recordClassRef = param.recordClassRef;
	this.dataTypeRef = param.dataTypeRef;
	this.isDataTypeParam = param.isDataTypeParam;
    }

    // ///////////////////////////////////////////////////////////////////
    // /////////// Public properties ////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////

    public boolean getIsDataTypeParam() {
	return isDataTypeParam;
    }

    public void setIsDataTypeParam(boolean isDataTypeParam) {
	System.err.println("Set isDataTypeParam in typeAhead param: " + isDataTypeParam);
	this.isDataTypeParam = isDataTypeParam;
    }

    public String getDataTypeRef() {
	return dataTypeRef;
    }

    public void setDataTypeRef(String dataTypeRef) {
	System.err.println("Set dataTypeRef in typeAhead param: " + dataTypeRef);
	this.dataTypeRef = dataTypeRef;
    }

    public String getRecordClassRef() {
	return recordClassRef;
    }

    public void setRecordClassRef(String recordClassRef) {
	System.err.println("Set recordClassRef in typeAhead param: " + recordClassRef);
	this.recordClassRef = recordClassRef;
    }

    public String toString() {
        String newline = System.getProperty("line.separator");
        StringBuffer buf = new StringBuffer(super.toString() + "  recordClassRef='"
                + recordClassRef + "'" + newline + "  dataTypeRef='" + dataTypeRef + "'" + newline
                + "  isDataTypeParam='" + isDataTypeParam + "'");
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
        return new TypeAheadParam(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.Param#appendJSONContent(org.json.JSONObject)
     */
    @Override
    protected void appendJSONContent(JSONObject jsParam, boolean extra)
            throws JSONException {
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
        return super.dependentValueToIndependentValue(user, dependentValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.query.param.Param#independentValueToInternalValue
     * (java.lang.String)
     */
    @Override
    public String dependentValueToInternalValue(User user, String dependentValue)
            throws WdkModelException {
	return super.dependentValueToInternalValue(user, dependentValue);
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
        return super.dependentValueToRawValue(user, dependentValue);
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
        return super.rawOrDependentValueToDependentValue(user, rawValue);
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
	super.validateValue(user, dependentValue);
    }
}
