/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.json.JSONException;

/**
 * @author xingao
 * 
 */
public class DatasetParamBean extends ParamBean {

    private DatasetParam datasetParam;

    public DatasetParamBean(DatasetParam datasetParam) {
        super(datasetParam);
        this.datasetParam = datasetParam;
    }

    public DatasetBean getDataset() throws WdkModelException, WdkUserException,
            SQLException, NoSuchAlgorithmException, JSONException {
        String independentValue = param.dependentValueToIndependentValue(
                user.getUser(), dependentValue);
        return user.getDataset(independentValue);
    }

    /**
     * @param user
     * @param uploadFile
     * @param rawValue
     * @return
     * @throws NoSuchAlgorithmException
     * @throws WdkUserException
     * @throws WdkModelException
     * @throws SQLException
     * @see org.gusdb.wdk.model.query.param.DatasetParam#rawValueToDependentValue(org.gusdb.wdk.model.user.User,
     *      java.lang.String, java.lang.String)
     */
    public String rawValueToDependentValue(UserBean user, String uploadFile,
            String rawValue) throws NoSuchAlgorithmException, WdkUserException,
            WdkModelException, SQLException {
        return datasetParam.rawValueToDependentValue(user.getUser(),
                uploadFile, rawValue);
    }

    public RecordClassBean getRecordClass() {
        return new RecordClassBean(datasetParam.getRecordClass());
    }
}
