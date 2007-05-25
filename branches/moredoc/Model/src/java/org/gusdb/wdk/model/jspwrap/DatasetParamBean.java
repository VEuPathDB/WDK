/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.DatasetParam;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;


/**
 * @author xingao
 *
 */
public class DatasetParamBean extends ParamBean {
    
    private String combinedId;
    private DatasetParam datasetParam;
    
    public DatasetParamBean(DatasetParam datasetParam) {
        super(datasetParam);
        this.datasetParam = datasetParam;
    }
    
    public void setCombinedId(String combinedId) {
        this.combinedId = combinedId;
    }
    
    public DatasetBean getDataset() throws WdkModelException, WdkUserException {
        return new DatasetBean(datasetParam.getDataset(combinedId));
    }
}
