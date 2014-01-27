/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dataset.DatasetParser;
import org.gusdb.wdk.model.query.param.DatasetParam;

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
   * @see org.gusdb.wdk.model.query.param.DatasetParam#getTypeSubParam()
   */
  public String getTypeSubParam() {
    return datasetParam.getTypeSubParam();
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.query.param.DatasetParam#getFileSubParam()
   */
  public String getFileSubParam() {
    return datasetParam.getFileSubParam();
  }

  public String getDataSubParam() {
    return datasetParam.getDataSubParam();
  }

  public String getStrategySubParam() {
    return datasetParam.getStrategySubParam();
  }
  
  public Collection<DatasetParser> getParsers() {
    return datasetParam.getParsers();
  }
}
