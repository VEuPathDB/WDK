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

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.fgputil.json.JsonType;
import org.gusdb.fgputil.json.JsonType.ValueType;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.dataset.AbstractDatasetParser;
import org.gusdb.wdk.model.dataset.Dataset;
import org.gusdb.wdk.model.dataset.DatasetFactory;
import org.gusdb.wdk.model.dataset.DatasetParser;
import org.gusdb.wdk.model.dataset.ListDatasetParser;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.BasketFactory;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.service.TemporaryFileService;
import org.json.JSONArray;
import org.json.JSONObject;

public class DatasetRequestProcessor {

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
    private final Map<String,JsonType> _additionalConfig;

    public DatasetRequest(JSONObject input) throws RequestMisformatException {
      _sourceType = DatasetSourceType.getFromJsonKey(input.getString(JsonKeys.SOURCE_TYPE));
      JSONObject sourceContent = input.getJSONObject(JsonKeys.SOURCE_CONTENT);
      _configValue = new JsonType(sourceContent.get(_sourceType.getConfigJsonKey()));
      if (!_configValue.getType().equals(_sourceType.getConfigType())) {
        throw new RequestMisformatException("Value of '" +
            _sourceType.getConfigJsonKey() + "' must be a " + _sourceType.getConfigType());
      }
      _additionalConfig = Functions.getMapFromKeys(
          JsonUtil.getKeys(sourceContent).stream()
            .filter(key -> !key.equals(_sourceType.getConfigJsonKey()))
            .collect(Collectors.toSet()),
          key -> new JsonType(sourceContent.get(key)));
      _displayName = Optional.ofNullable(JsonUtil.getStringOrDefault(input, JsonKeys.DISPLAY_NAME, null));
    }

    public DatasetSourceType getSourceType() { return _sourceType; }
    public JsonType getConfigValue() { return _configValue; }
    public Optional<String> getDisplayName() { return _displayName; }
    public Map<String,JsonType> getAdditionalConfig() { return _additionalConfig; }

  }

  public static Dataset createFromRequest(DatasetRequest request, User user, DatasetFactory factory, HttpSession session)
      throws WdkModelException, DataValidationException, RequestMisformatException {
    JsonType value = request.getConfigValue();
    switch(request.getSourceType()) {
      case ID_LIST:  return createFromIdList(value.getJSONArray(), user, factory);
      case BASKET:   return createFromBasket(value.getString(), user, factory);
      case STRATEGY: return createFromStrategy(getStrategyId(value), user, factory);
      case FILE:     return createFromTemporaryFile(value.getString(), user, factory, request.getAdditionalConfig(), session);
      default:
        throw new DataValidationException("Unrecognized " + JsonKeys.SOURCE_TYPE + ": " + request.getSourceType());
    }
  }

  private static long getStrategyId(JsonType value) throws DataValidationException {
    if (value.getNumberSubtype().equals(JsonType.NumberSubtype.LONG)) {
      return value.getLong();
    }
    throw new DataValidationException(value.toString() + " is not a valid strategy ID.  Must be a positive integer.");
  }

  private static Dataset createFromIdList(JSONArray jsonIds, User user, DatasetFactory factory)
      throws DataValidationException, WdkModelException {
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
    return createDataset(user, parser, join(ids.toArray(), " "), null, factory);
  }

  private static Dataset createFromBasket(String recordClassName, User user, DatasetFactory factory)
      throws WdkModelException, DataValidationException {
    WdkModel wdkModel = factory.getWdkModel();
    RecordClass recordClass = wdkModel.getRecordClassByUrlSegment(recordClassName)
        .orElseThrow(() -> new DataValidationException(
            "No record class exists with name '" + recordClassName + "'."));

    BasketFactory basketFactory = factory.getWdkModel().getBasketFactory();
    List<String[]> ids = basketFactory.getBasket(user, recordClass).stream()
        .map(ri -> ri.getPrimaryKey().getValues().values().toArray(new String[0]))
        .collect(Collectors.toList());

    return createDataset(user, new ListDatasetParser(), joinIds(ids), null, factory);
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
        Strategy.getRunnableStep(strategy, strategy.get().getRootStepId()).get());
    List<String[]> ids = answerValue.getAllIds();
    return createDataset(user, new ListDatasetParser(), joinIds(ids), null, factory);
  }

  private static String joinIds(List<String[]> ids) {
    return ids.stream()
        .map(idArray -> join(idArray, ListDatasetParser.DATASET_COLUMN_DIVIDER))
        .collect(Collectors.joining("\n"));
  }

  private static Dataset createDataset(User user, DatasetParser parser,
      String content, String uploadFileName, DatasetFactory factory)
          throws WdkModelException, DataValidationException {
    try {
      return factory.createOrGetDataset(user, parser, content, uploadFileName);
    }
    catch (WdkUserException e) {
      throw new DataValidationException(e.getMessage());
    }
  }

  private static Dataset createFromTemporaryFile(String tempFileId, User user, DatasetFactory factory,
      Map<String, JsonType> additionalConfig, HttpSession session)
          throws DataValidationException, WdkModelException, RequestMisformatException {
    Optional<String> parserName = getStringOrFail(additionalConfig, JsonKeys.PARSER);
    DatasetParser parser = parserName.isPresent() ?
        findDatasetParser(parserName.get(), additionalConfig, factory.getWdkModel()) :
        new ListDatasetParser();

    Path tempFilePath = TemporaryFileService.getTempFileFactory(factory.getWdkModel(), session)
      .apply(tempFileId)
      .orElseThrow(() -> new DataValidationException("Temporary file with the ID '" + tempFileId + "' could not be found in this session."));

    try {
      String contents = new String(Files.readAllBytes(tempFilePath));
      return createDataset(user, parser, contents, tempFileId, factory);
    }
    catch (IOException e) {
      throw new WdkModelException("Unable to read temporary file with ID '" + tempFileId + "'.", e);
    }
  }

  private static DatasetParser findDatasetParser(String parserName,
      Map<String, JsonType> additionalConfig, WdkModel model)
          throws DataValidationException, RequestMisformatException, WdkModelException {

    Optional<String> questionName = getStringOrFail(additionalConfig, JsonKeys.SEARCH_NAME);
    Optional<String> parameterName = getStringOrFail(additionalConfig, JsonKeys.PARAMETER_NAME);

    if (!questionName.isPresent() || !parameterName.isPresent()) {
      throw new DataValidationException("If '" + JsonKeys.PARSER + "' property is specified, '" +
          JsonKeys.SEARCH_NAME + "' and '" + JsonKeys.PARAMETER_NAME + "' must also be specified.");
    }

    Question question = model.getQuestionByName(questionName.get()).orElseThrow(
        () -> new DataValidationException(String.format(
            "Could not find question with name '%s'.", questionName.get())));

    Param param = Optional.ofNullable(question.getParamMap().get(parameterName.get())).orElseThrow(
        () -> new DataValidationException(String.format(
            "Could not find parameter '%s' in question '%s'.", parameterName.get(), questionName.get())));
    
    if (!(param instanceof DatasetParam)) {
      throw new DataValidationException(String.format(
          "Parameter '%s' must be a DatasetParam, is a %s.", parameterName.get(), param.getClass().getSimpleName()));
    }

    DatasetParser parser = ((DatasetParam) param).getParser(parserName);

    if (parser == null) {
      throw new DataValidationException(String.format(
          "Could not find parser '%s' in parameter '%s' of question '%s'.", parserName, parameterName.get(), questionName.get()));
    }

    return parser;

  }

  private static Optional<String> getStringOrFail(Map<String, JsonType> map, String key) throws RequestMisformatException {
    if (map.containsKey(key)) {
      JsonType value = map.get(key);
      if (value.getType().equals(ValueType.STRING)) {
        return Optional.of(value.getString());
      }
      throw new RequestMisformatException("Property '" + key + "' must be a string.");
    }
    return Optional.empty();
  }
}
