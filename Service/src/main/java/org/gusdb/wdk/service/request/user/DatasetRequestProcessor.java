package org.gusdb.wdk.service.request.user;

import static org.gusdb.fgputil.FormatUtil.join;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.fgputil.json.JsonType;
import org.gusdb.fgputil.json.JsonType.ValueType;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.stream.RecordStream;
import org.gusdb.wdk.model.dataset.AbstractDatasetParser;
import org.gusdb.wdk.model.dataset.Dataset;
import org.gusdb.wdk.model.dataset.DatasetFactory;
import org.gusdb.wdk.model.dataset.DatasetParser;
import org.gusdb.wdk.model.dataset.ListDatasetParser;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.DatasetParamHandler;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.user.BasketFactory;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.StepUtilities;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.service.TemporaryFileService;
import org.json.JSONArray;
import org.json.JSONObject;

public class DatasetRequestProcessor {

  private static final Logger logger = Logger.getLogger(DatasetParamHandler.class);

  public enum DatasetSourceType {

    ID_LIST("idList", "ids", ValueType.ARRAY),
    BASKET("basket", "basketName", ValueType.STRING),
    FILE("file", "temporaryFileId", ValueType.STRING),
    STRATEGY("strategy", JsonKeys.STRATEGY_ID, ValueType.NUMBER);

    private final String _jsonKey;
    private final String _configJsonKey;
    private final ValueType _configValueType;

    private DatasetSourceType(String jsonKey, String configJsonKey, ValueType configValueType) {
      _jsonKey = jsonKey;
      _configJsonKey = configJsonKey;
      _configValueType = configValueType;
    }

    public String getJsonKey() {
      return _jsonKey;
    }

    public String getConfigJsonKey() {
      return _configJsonKey;
    }

    public ValueType getConfigType() {
      return _configValueType;
    }

    public static DatasetSourceType getFromJsonKey(String jsonKey) throws RequestMisformatException {
      return Arrays.stream(values())
        .filter(val -> val._jsonKey.equals(jsonKey))
        .findFirst()
        .orElseThrow(() -> new RequestMisformatException(
            "Invalid source type.  Only [" + FormatUtil.join(values(), ", ") + "] allowed."));
    }
  }

  public static class DatasetRequest {

    private final DatasetSourceType _sourceType;
    private final JsonType _configValue;
    private final Optional<String> _displayName;

    public DatasetRequest(JSONObject input) throws RequestMisformatException {
      _sourceType = DatasetSourceType.getFromJsonKey(input.getString(JsonKeys.SOURCE_TYPE));
      JSONObject sourceContent = input.getJSONObject(JsonKeys.SOURCE_CONTENT);
      _configValue = new JsonType(sourceContent.get(_sourceType.getConfigJsonKey()));
      if (!_configValue.getType().equals(_sourceType.getConfigType())) {
        throw new RequestMisformatException("Value of '" +
            _sourceType.getConfigJsonKey() + "' must be a " + _sourceType.getConfigType());
      }
      _displayName = Optional.ofNullable(JsonUtil.getStringOrDefault(input, JsonKeys.DISPLAY_NAME, null));
    }

    public DatasetSourceType getSourceType() { return _sourceType; }
    public JsonType getConfigValue() { return _configValue; }
    public Optional<String> getDisplayName() { return _displayName; }

  }

  public static Dataset createFromRequest(DatasetRequest request, User user, DatasetFactory factory, HttpSession session)
      throws WdkModelException, DataValidationException {
    JsonType value = request.getConfigValue();
    switch(request.getSourceType()) {
      case ID_LIST:  return createFromIdList(value.getJSONArray(), user, factory);
      case BASKET:   return createFromBasket(value.getString(), user, factory);
      case STRATEGY: return createFromStrategy(getStrategyId(value), user, factory);
      case FILE:     return createFromTemporaryFile(value.getString(), user, factory, session);
      default:
        throw new DataValidationException("Unrecognized " + JsonKeys.SOURCE_TYPE + ": " + sourceType);
    }
  }

  private static long getStrategyId(JsonType value) throws DataValidationException {
    if (value.getNumberSubtype().equals(JsonType.NumberSubtype.LONG)) {
      return value.getLong();
    }
    throw new DataValidationException(value.toString() + " is not a valid strategy ID.  Must be a positive integer.");
  }

  private static Dataset createFromIdList(JSONArray jsonIds, User user, DatasetFactory factory)
      throws DataValidationException, WdkUserException, WdkModelException {
    if (jsonIds.length() == 0) {
      throw new DataValidationException("At least 1 ID must be submitted");
    }
    final List<String> ids = new ArrayList<>();
    for (int i = 0; i < jsonIds.length(); i++) {
      ids.add(jsonIds.getString(i));
    }
    // FIXME: this is a total hack to comply with the dataset factory API
    //   We are closing over the JSON array we already parsed and will return
    //   a List<String> version of that array
    DatasetParser parser = new AbstractDatasetParser() {
      @Override
      public List<String[]> parse(String content) {
        return Functions.mapToList(ids, str -> new String[]{ str });
      }
      @Override
      public String getName() {
        return "anonymous";
      }
    };
    return factory.createOrGetDataset(user, parser, join(ids.toArray(), " "), "");
  }

  public static Dataset createFromBasket(String recordClassName, User user, DatasetFactory factory)
      throws WdkModelException, WdkUserException {
    WdkModel wdkModel = factory.getWdkModel();
    RecordClass recordClass = wdkModel.getRecordClassByUrlSegment(recordClassName);
    String questionName = BasketFactory.getSnapshotBasketQuestionName(recordClass);
    Question question = wdkModel.getQuestion(questionName).get(); // basket question always present
    DatasetParam param = (DatasetParam) question.getParamMap().get(BasketFactory.getDatasetParamName(recordClass));
    
    String datasetId = handler.getStableValue(user, new MapBasedRequestParams()
        .setParam(param.getTypeSubParam(), DatasetParam.TYPE_BASKET));
    return factory.getDataset(user, Long.parseLong(datasetId));
  }

  private static Dataset createFromStrategy(long strategyId, User user, DatasetFactory factory)
      throws WdkModelException, DataValidationException {
    RunnableObj<Strategy> strategy = factory.getWdkModel().getStepFactory()
        .getStrategyById(strategyId, ValidationLevel.RUNNABLE)
        .orElseThrow(() -> new DataValidationException("Strategy with ID " + strategyId + " not found."))
        .getRunnable()
        .getOrThrow(strat -> new DataValidationException("Strategy with ID " +
            strategyId + " not valid. " + strat.getValidationBundle().toString()));
    AnswerValue answerValue = AnswerValueFactory.makeAnswer(
        Strategy.getRunnableStep(strategy, strategy.getObject().getRootStepId()).get());
    List<String[]> ids = answerValue.getAllIds();
    ListDatasetParser parser = new ListDatasetParser();
    String content = ids.stream()
        .map(idArray -> join(idArray, ListDatasetParser.DATASET_COLUMN_DIVIDER))
        .collect(Collectors.joining("\n"));
    try {
      return factory.createOrGetDataset(user, parser, content, null);
    }
    catch (WdkUserException e) {
      return WdkModelException.unwrap(e);
    }
  }

  private static Dataset createFromTemporaryFile(User user, JSONObject sourceConfig, DatasetFactory factory, HttpSession session) throws WdkUserException, WdkModelException {
    String tempFileId = sourceConfig.getString(JsonKeys.TEMP_FILE_ID);
    String parserName = sourceConfig.getString(JsonKeys.PARSER);
    String questionName = sourceConfig.getString(JsonKeys.QUESTION_NAME);
    String parameterName = sourceConfig.getString(JsonKeys.PARAMETER_NAME);

    Question question = factory.getWdkModel().getQuestion(questionName).orElseThrow(
        () -> new WdkUserException(String.format("Could not find a question with the name `%s`.", questionName)));

    Param param = Optional.of(question.getParamMap().get(parameterName)).orElseThrow(() -> new WdkUserException(
        String.format("Could not find the parameter `%s` with the question `%s`.", parameterName, questionName)));
    
    if (!(param instanceof DatasetParam)) {
      throw new WdkUserException(String.format("Expected % to be a DatasetParam, but instead got a %s.", parameterName,
          param.getClass().getSimpleName()));
    }

    DatasetParser parser = ((DatasetParam) param).getParser(parserName);

    if (parser == null) {
      throw new WdkUserException(String.format("Could not find parser `%s` in parameter `%s` of question `%s`.",
          parserName, parameterName, questionName));
    }

    Path tempFilePath = TemporaryFileService.getTempFileFactory(factory.getWdkModel(), session)
      .apply(tempFileId)
      .orElseThrow(() -> new WdkUserException("TemporaryFile with the name \"" + tempFileId + "\" could not be found for the user."));

    try {
      String contents = new String(Files.readAllBytes(tempFilePath));
      return factory.createOrGetDataset(user, parser, contents, tempFileId);
    } catch (IOException e) {
      throw new RuntimeException("Unable to read Te)mporaryFile with name \"" + tempFileId + "\".", e);
    }
  }

  /**
   * get the step value from the user input, and if empty value is allowed, use empty value as needed.
   */
  private String getStableValue(User user, RequestParams requestParams) throws WdkUserException,
      WdkModelException {


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

  private String validateStableValueSyntax(User user, String inputStableValue) throws WdkModelException {
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
}
