package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.stream.RecordStream;
import org.gusdb.wdk.model.dataset.Dataset;
import org.gusdb.wdk.model.dataset.DatasetFactory;
import org.gusdb.wdk.model.dataset.DatasetParser;
import org.gusdb.wdk.model.dataset.ListDatasetParser;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.user.BasketFactory;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepUtilities;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.model.user.User;

/**
 * @author jerric
 */
public class DatasetParamHandler extends AbstractParamHandler {

  private static final Logger logger = Logger.getLogger(DatasetParamHandler.class);

  public DatasetParamHandler() {}

  public DatasetParamHandler(DatasetParamHandler handler, Param param) {
    super(handler, param);
  }

  /**
   * The raw value is Dataset object, and stable value is the dataset id.
   *
   */
  @Override
  public String toStableValue(User user, Object rawValue) {
    Dataset dataset = (Dataset) rawValue;
    return Long.toString(dataset.getDatasetId());
  }

  /**
   * The stable value is dataset id, and raw value is Dataset object.
   */
  @Override
  public Dataset toRawValue(User user, String stableValue) throws WdkModelException {
    long datasetId = Long.valueOf(stableValue);
    return user.getWdkModel().getDatasetFactory().getDataset(user, datasetId);
  }

  /**
   * the internal value is an SQL that queries against the dataset values.
   */
  @Override
  public String toInternalValue(RunnableObj<QueryInstanceSpec> ctxParamVals) {
    final String name  = _param.getName();
    final String value = ctxParamVals.getObject().get(name);

    if (_param.isNoTranslation())
      return value;

    long datasetId = Long.valueOf(value);
    DatasetFactory datasetFactory = ctxParamVals.getObject().getUser()
        .getWdkModel()
        .getDatasetFactory();
    String dvSql = datasetFactory.getDatasetValueSql(datasetId);

    RecordClass recordClass = ((DatasetParam) _param).getRecordClass();
    if (recordClass == null)
      return dvSql;

    // use the recordClass primary keys as the column name
    String[] pkColumns = recordClass.getPrimaryKeyDefinition().getColumnRefs();
    StringBuilder sql = new StringBuilder("SELECT ");
    for (int i = 0; i < pkColumns.length; i++) {
      sql.append("dv.data")
          .append(i + 1)
          .append(" AS ")
          .append(pkColumns[i])
          .append(", ");
    }
    // return the remaining data columns
    sql.append("dv.* FROM (").append(dvSql).append(") dv");
    return sql.toString();
  }

  /**
   * The signature is the the content check of the dataset.
   */
  @Override
  public String toSignature(RunnableObj<QueryInstanceSpec> ctxParamVals)
      throws WdkModelException {
    final QueryInstanceSpec spec = ctxParamVals.getObject();
    final String value = ctxParamVals.getObject().get(spec.get(_param.getName()));
    long datasetId = Long.valueOf(value);
    Dataset dataset = spec.getUser().getWdkModel()
        .getDatasetFactory()
        .getDataset(spec.getUser(), datasetId);
    return dataset.getChecksum();
  }

  /**
   * get the step value from the user input, and if empty value is allowed, use empty value as needed.
   */
  @Override
  public String getStableValue(User user, RequestParams requestParams) throws WdkUserException,
      WdkModelException {

    // check if stable value is assigned
    String datasetId = requestParams.getParam(_param.getName());
    if (datasetId != null) {
      return validateStableValueSyntax(user, datasetId);
    }

    // dataset id not assigned, create one.
    DatasetParam datasetParam = (DatasetParam) _param;
    String type = requestParams.getParam(datasetParam.getTypeSubParam());
    if (type == null) // use data as default input type
      type = DatasetParam.TYPE_DATA;

    String data = null;
    String uploadFile = "";
    RecordClass recordClass = datasetParam.getRecordClass();
    String parserName = requestParams.getParam(datasetParam.getParserSubParam());
    if (parserName == null) // list parser is the default parser.
      parserName = ListDatasetParser.NAME;

    // retrieve data by type.
    if (type.equalsIgnoreCase(DatasetParam.TYPE_DATA)) {
      data = requestParams.getParam(datasetParam.getDataSubParam());
      if (data == null || data.length() == 0)
        throw new WdkUserException("Please input data for parameter '" + _param.getPrompt() + "'.");
    }
    else if (type.equalsIgnoreCase(DatasetParam.TYPE_FILE)) {
      String fileParam = datasetParam.getFileSubParam();
      uploadFile = requestParams.getParam(fileParam);
      if (uploadFile == null || uploadFile.length() == 0)
        throw new WdkUserException("Please select a file to upload for parameter '" + _param.getPrompt() +
            "'.");
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
        long strategyId = Long.valueOf(strId);
        Strategy strategy = StepUtilities.getStrategy(user, strategyId, ValidationLevel.RUNNABLE);
        Step step = strategy.getRootStep();
        List<RecordInstance> list = new ArrayList<>();
        try (RecordStream fullAnswer = step.getAnswerValue().getFullAnswer()) {
          for (RecordInstance record : fullAnswer) {
            list.add(record);
          }
        }
        records = list.toArray(new RecordInstance[0]);
      }
      if (records != null)
        data = toString(records);
    }

    logger.debug("DATASET.geStableValue: dataset parser: " + parserName + ", data: '" + data + "'");
    if (data == null) {
      if (!_param.isAllowEmpty())
        throw new WdkUserException("The dataset param '" + _param.getPrompt() + "' does't allow empty value.");
      data = _param.getEmptyValue();
    }

    if (data != null) {
      data = data.trim();
      // get parser and parse the content
      DatasetParser parser = datasetParam.getParser(parserName);
      DatasetFactory datasetFactory = user.getWdkModel().getDatasetFactory();
      Dataset dataset = datasetFactory.createOrGetDataset(user, parser, data, uploadFile);
      logger.info("User #" + user.getUserId() + " - dataset created: #" + dataset.getDatasetId());
      return Long.toString(dataset.getDatasetId());
    }
    else
      return null;
  }

  @Override
  public String validateStableValueSyntax(User user, String inputStableValue) throws WdkModelException {
    DatasetFactory datasetFactory = user.getWdkModel().getDatasetFactory();
    Dataset dataset = datasetFactory.getDataset(user, Long.valueOf(inputStableValue));
    return Long.toString(dataset.getDatasetId());
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
          buffer.append(ListDatasetParser.DATASET_COLUMN_DIVIDER);
        buffer.append(value);
      }
      buffer.append("\n");
    }
    return buffer.toString();
  }

  @Override
  public void prepareDisplay(User user, RequestParams requestParams, Map<String, String> contextParamValues)
      throws WdkModelException {
    DatasetParam datasetParam = (DatasetParam) _param;
    // check if the stable value is available
    String stableValue = requestParams.getParam(_param.getName());

    // get dataset if possible
    Dataset dataset = null;
    if (stableValue != null) {
      DatasetFactory datasetFactory = user.getWdkModel().getDatasetFactory();
      long sv = Long.parseLong(stableValue);
      logger.debug("User: " + user + ", sv: " + sv + "stable: " + stableValue);
      dataset = datasetFactory.getDataset(user, sv);
      requestParams.setAttribute(_param.getName() + Param.RAW_VALUE_SUFFIX, dataset);
    }

    // get data
    String data = (dataset != null) ? dataset.getContent() : _param.getXmlDefault();
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

  @Override
  public String getDisplayValue(QueryInstanceSpec ctxParamVals)
      throws WdkModelException {
    return toRawValue(ctxParamVals.getUser(), ctxParamVals.get(_param.getName()))
        .getContent();
  }
}
