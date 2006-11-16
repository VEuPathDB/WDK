/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import org.gusdb.wdk.model.DatasetParam;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.DatasetParam.InputType;


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

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.DatasetParam#getDataType()
     */
    public String getDataType() {
        return datasetParam.getDataType();
    }

    /* (non-Javadoc)
     * @see org.gusdb.wdk.model.DatasetParam#getInputType(java.lang.String)
     */
    public InputType getInputType(String value) throws WdkModelException {
        return datasetParam.getInputType(value);
    }
}
