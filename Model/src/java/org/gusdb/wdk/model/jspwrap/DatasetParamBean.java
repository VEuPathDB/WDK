/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.dataset.DatasetParam;

/**
 * @author xingao
 * 
 */
public class DatasetParamBean extends ParamBean<DatasetParam> {

  @SuppressWarnings("unused")
  private static final Logger logger = Logger.getLogger(DatasetParamBean.class.getName());

  private final DatasetParam datasetParam;

  public DatasetParamBean(DatasetParam datasetParam) {
    super(datasetParam);
    this.datasetParam = datasetParam;
  }

  public DatasetBean getDataset() throws WdkModelException {
    int userDatasetId = Integer.valueOf(stableValue);
    DatasetBean dataset = user.getDataset(userDatasetId);
    return dataset;
  }

  public String getDefaultType() {
    return param.getDefaultType();
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.query.param.dataset.DatasetParam#getTypes()
   */
  public Map<String, String> getTypes() {
    return datasetParam.getTypes();
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.query.param.dataset.DatasetParam#getTypeSubParam()
   */
  public String getTypeSubParam() {
    return datasetParam.getTypeSubParam();
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.query.param.dataset.DatasetParam#getFileSubParam()
   */
  public String getFileSubParam() {
    return datasetParam.getFileSubParam();
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.query.param.dataset.DatasetParam#getMethodSubParam()
   */
  public String getMethodSubParam() {
    return datasetParam.getMethodSubParam();
  }

}
