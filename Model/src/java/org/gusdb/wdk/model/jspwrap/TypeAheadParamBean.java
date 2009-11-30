/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.TypeAheadParam;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;

/**
 * @author xingao
 * 
 */
public class TypeAheadParamBean extends ParamBean {

    private TypeAheadParam typeAheadParam;

    public TypeAheadParamBean(TypeAheadParam typeAheadParam) {
        super(typeAheadParam);
        this.typeAheadParam = typeAheadParam;
    }

    public boolean getIsDataTypeParam() {
	return typeAheadParam.getIsDataTypeParam();
    }

    public void setIsDataTypeParam(boolean isDataTypeParam) {
	typeAheadParam.setIsDataTypeParam(isDataTypeParam);
    }

    public String getDataTypeRef() {
	return typeAheadParam.getDataTypeRef();
    }

    public void setDataTypeRef(String dataTypeRef) {
	typeAheadParam.setDataTypeRef(dataTypeRef);
    }

    public String getRecordClassRef() {
	return typeAheadParam.getRecordClassRef();
    }

    public void setRecordClassRef(String recordClassRef) {
	typeAheadParam.setRecordClassRef(recordClassRef);
    }

}
