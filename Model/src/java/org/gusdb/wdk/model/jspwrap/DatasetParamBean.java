/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.DatasetParam;

/**
 * @author xingao
 * 
 */
public class DatasetParamBean extends ParamBean<DatasetParam> {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DatasetParamBean.class.getName());
	
    public DatasetParamBean(DatasetParam datasetParam) {
        super(datasetParam);
    }

    public DatasetBean getDataset() throws WdkModelException {
        String independentValue = param.dependentValueToIndependentValue(
                user.getUser(), dependentValue);
        DatasetBean dataset = user.getDataset(independentValue);
        dataset.setRecordClass(getRecordClass());
        return dataset;
    }

    /**
     * @param user
     * @param uploadFile
     * @param rawValue
     * @return
     * @see org.gusdb.wdk.model.query.param.DatasetParam#rawValueToDependentValue(org.gusdb.wdk.model.user.User,
     *      java.lang.String, java.lang.String)
     */
    public String rawValueToDependentValue(UserBean user, String uploadFile,
            String rawValue) throws WdkModelException {
        return param.rawValueToDependentValue(user.getUser(), uploadFile,
                rawValue);
    }

    public RecordClassBean getRecordClass() {
        return new RecordClassBean(param.getRecordClass());
    }

    public String getDefaultType() {
        return param.getDefaultType();
    }
}
