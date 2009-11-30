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
	this.isDataTypeParam = isDataTypeParam;
    }

    public String getDataTypeRef() {
	return dataTypeRef;
    }

    public void setDataTypeRef(String dataTypeRef) {
	this.dataTypeRef = dataTypeRef;
    }

    public String getRecordClassRef() {
	return recordClassRef;
    }

    public void setRecordClassRef(String recordClassRef) {
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    public Param clone() {
        return new TypeAheadParam(this);
    }
}
