/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.sql.SQLException;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.DatasetParam;

/**
 * @author xingao
 * 
 */
public class DatasetParamBean extends ParamBean {

    private String combinedKey;
    private DatasetParam datasetParam;

    public DatasetParamBean(DatasetParam datasetParam) {
        super(datasetParam);
        this.datasetParam = datasetParam;
    }

    public void setCombinedKey(String combinedKey) {
        this.combinedKey = combinedKey;
    }

    public DatasetBean getDataset() throws WdkModelException, WdkUserException,
            SQLException {
        return new DatasetBean(datasetParam.getDataset(combinedKey));
    }
}
