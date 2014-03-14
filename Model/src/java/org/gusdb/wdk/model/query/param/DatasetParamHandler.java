/**
 * 
 */
package org.gusdb.wdk.model.query.param;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dataset.Dataset;
import org.gusdb.wdk.model.dataset.DatasetFactory;
import org.gusdb.wdk.model.dataset.DatasetParser;
import org.gusdb.wdk.model.dataset.ListDatasetParser;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.user.BasketFactory;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.model.user.User;

/**
 * @author jerric
 * 
 */
public class DatasetParamHandler extends AbstractParamHandler {

  private static final Logger logger = Logger.getLogger(DatasetParamHandler.class);

  public DatasetParamHandler(){}
  
  public DatasetParamHandler(DatasetParamHandler handler, Param param) {
    super(handler, param);
  }
  
  /**
   * The raw value is Dataset object, and stable value is the dataset id.
   * 
   * @throws WdkUserException
   * 
   * @throws WdkModelException
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toStableValue(org.gusdb .wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toStableValue(User user, Object rawValue, Map<String, String> contextValues)
      throws WdkUserException, WdkModelException {
    Dataset dataset = (Dataset) rawValue;
    return Integer.toString(dataset.getDatasetId());
  }

  /**
   * The stable value is dataset id, and raw value is Dataset object.
   * 
   * @throws WdkModelException
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toRawValue(org.gusdb.wdk.model .user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public Object toRawValue(User user, String stableValue, Map<String, String> contextValues)
      throws WdkModelException {
    int datasetId = Integer.valueOf(stableValue);
    return user.getDataset(datasetId);
  }

  /**
   * the internal value is an SQL that queries against the dataset values.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toInternalValue(org.gusdb. wdk.model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toInternalValue(User user, String stableValue, Map<String, String> contextValues) {
    if (param.isNoTranslation())
      return stableValue;

    int datasetId = Integer.valueOf(stableValue);
    DatasetFactory datasetFactory = user.getWdkModel().getDatasetFactory();
    String dvSql = datasetFactory.getDatasetValueSql(datasetId);

    RecordClass recordClass = ((DatasetParam) param).getRecordClass();
    if (recordClass == null)
      return dvSql;

    // use the recordClass primary keys as the column name
    String[] pkColumns = recordClass.getPrimaryKeyAttributeField().getColumnRefs();
    StringBuilder sql = new StringBuilder("SELECT ");
    for (int i = 0; i < pkColumns.length; i++) {
      sql.append("dv.data" + (i + 1) + " AS " + pkColumns[i] + ", ");
    }
    // return the remaining data columns
    sql.append("dv.* FROM (" + dvSql + ") dv");
    return sql.toString();
  }

  /**
   * The signature is the the content check of the dataset.
   * 
   * @throws WdkModelException
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#toSignature(org.gusdb.wdk. model.user.User,
   *      java.lang.String, java.util.Map)
   */
  @Override
  public String toSignature(User user, String stableValue, Map<String, String> contextValues)
      throws WdkModelException {
    int datasetId = Integer.valueOf(stableValue);
    Dataset dataset = user.getDataset(datasetId);
    return dataset.getChecksum();
  }

  /**
   * get the step value from the user input, and if empty value is allowed, use empty value as needed.
   * 
   * @see org.gusdb.wdk.model.query.param.ParamHandler#getStableValue(org.gusdb.wdk.model.user.User,
   *      org.gusdb.wdk.model.query.param.RequestParams)
   */
  @Override
  public String getStableValue(User user, RequestParams requestParams) throws WdkUserException,
      WdkModelException {
    DatasetFactory datasetFactory = user.getWdkModel().getDatasetFactory();

    // check if stable value is assigned
    String datasetId = requestParams.getParam(param.getName());
    if (datasetId != null) {
      Dataset dataset = datasetFactory.getDataset(user, Integer.valueOf(datasetId));
      return Integer.toString(dataset.getDatasetId());
    }

    // dataset id not assigned, create one.
    DatasetParam datasetParam = (DatasetParam) param;
    String type = requestParams.getParam(datasetParam.getTypeSubParam());
    if (type == null)
      throw new WdkUserException("Please choose the type of the input for paramter '" + param.getPrompt() +
          "'.");

    String data = null;
    String uploadFile = "";
    RecordClass recordClass = datasetParam.getRecordClass();
    String parserName = requestParams.getParam(datasetParam.getParserSubParam());
    if (type.equalsIgnoreCase(DatasetParam.TYPE_DATA)) {
      data = requestParams.getParam(datasetParam.getDataSubParam());
    }
    else if (type.equalsIgnoreCase(DatasetParam.TYPE_FILE)) {
      String fileParam = datasetParam.getFileSubParam();
      uploadFile = requestParams.getParam(fileParam);
      logger.debug("upload file: " + uploadFile);
      data = requestParams.getUploadFileContent(fileParam);
    }
    else if (recordClass != null) {
      RecordInstance[] records = null;
      if (type.equalsIgnoreCase(DatasetParam.TYPE_BASKET)) {
        BasketFactory basketFactory = user.getWdkModel().getBasketFactory();
        List<RecordInstance> list = basketFactory.getBasket(user, recordClass);
        records = list.toArray(new RecordInstance[0]);
      }
      else if (type.equals("strategy")) {
        String strId = requestParams.getParam(datasetParam.getStrategySubParam());
        int strategyId = Integer.valueOf(strId);
        Strategy strategy = user.getStrategy(strategyId);
        Step step = strategy.getLatestStep();
        records = step.getAnswerValue().getRecordInstances();
      }
      if (records != null)
        data = toString(records);
      parserName = ListDatasetParser.NAME; // use the list parser.
    }

    logger.debug("dataset data: '" + data + "'");
    if (data == null) {
      if (!param.isAllowEmpty())
        throw new WdkUserException("The dataset param '" + param.getPrompt() + "' does't allow empty value.");
      data = param.getEmptyValue();
    }

    if (data != null) {
      data = data.trim();
      // get parser and parse the content
      DatasetParser parser = datasetParam.getParser(parserName);
      Dataset dataset = datasetFactory.createOrGetDataset(user, parser, data, uploadFile);
      logger.debug("User #" + user.getUserId() + " - dataset created: #" + dataset.getDatasetId());
      return Integer.toString(dataset.getDatasetId());
    }
    else
      return null;
  }

  private String toString(RecordInstance[] records) {
    StringBuilder buffer = new StringBuilder();
    for (RecordInstance record : records) {
      Map<String, String> primaryKey = record.getPrimaryKey().getValues();
      boolean first = true;
      for (String value : primaryKey.values()) {
        if (first)
          first = false;
        else
          buffer.append("|");
        buffer.append(value);
      }
      buffer.append("\n");
    }
    return buffer.toString();
  }

  @Override
  public void prepareDisplay(User user, RequestParams requestParams, Map<String, String> contextValues)
      throws WdkModelException, WdkUserException {
    DatasetParam datasetParam = (DatasetParam) param;
    // check if the stable value is available
    String stableValue = requestParams.getParam(param.getName());

    // get dataset if possible
    Dataset dataset = null;
    if (stableValue != null) {
      DatasetFactory datasetFactory = user.getWdkModel().getDatasetFactory();
      Integer sv = Integer.getInteger(stableValue);
      dataset = datasetFactory.getDataset(user, sv);
      requestParams.setAttribute(param.getName() + Param.RAW_VALUE_SUFFIX, dataset);
    }

    // get data
    String data = (dataset != null) ? dataset.getContent() : param.getDefault();
    requestParams.setParam(datasetParam.getDataSubParam(), data);

    if (dataset != null) {
      String fileName = dataset.getUploadFile();
      if (fileName != null)
        requestParams.setParam(datasetParam.getFileSubParam(), fileName);
    }
  }

  @Override
  public ParamHandler clone(Param param) {
    return new DatasetParamHandler(this, param);
  }
}
