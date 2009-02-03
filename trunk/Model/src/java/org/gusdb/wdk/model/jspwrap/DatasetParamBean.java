/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.DatasetParam;

/**
 * @author xingao
 * 
 */
public class DatasetParamBean extends ParamBean {

    private DatasetParam datasetParam;
    private String checksum;

    public DatasetParamBean(DatasetParam datasetParam) {
        super(datasetParam);
        this.datasetParam = datasetParam;
    }

    /**
     * @param checksum
     */
    public void setCombinedKey(String checksum) {
        this.checksum = checksum;
    }

    public DatasetBean getDataset() throws WdkModelException, WdkUserException,
            SQLException {
        return user.getDataset(checksum);
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

}
