package org.gusdb.wdk.service.request.user;

import static org.gusdb.fgputil.FormatUtil.join;
import static org.gusdb.fgputil.json.JsonIterators.arrayStream;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.fgputil.json.JsonType;
import org.gusdb.fgputil.json.JsonType.ValueType;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.fgputil.web.SessionProxy;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.dataset.*;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpecBuilder.FillStrategy;
import org.gusdb.wdk.model.question.Question;
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

    DatasetSourceType(String jsonKey, String configJsonKey, ValueType configValueType) {
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

  public static Dataset createFromRequest(
    DatasetRequest request,
    User user,
    DatasetFactory factory,
    SessionProxy session
  ) throws WdkModelException, DataValidationException, RequestMisformatException {
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

  private static Dataset createFromIdList(
    final JSONArray jsonIds,
    final User user,
    final DatasetFactory factory
  ) throws DataValidationException, WdkModelException {

    if (jsonIds.length() == 0)
      throw new DataValidationException("At least 1 ID must be submitted");

    final var ids = arrayStream(jsonIds)
      .map(JsonType::getString)
      .collect(Collectors.toList());

    DatasetParser parser = new DatasetPassThroughParser(ids);
    return createDataset(user, parser, new DatasetListContents(ids), factory);
  }

  private static Dataset createFromBasket(
    final String recordClassName,
    final User user,
    final DatasetFactory factory
  ) throws WdkModelException, DataValidationException {
    var recordClass = factory.getWdkModel()
      .getRecordClassByUrlSegment(recordClassName)
      .orElseThrow(() -> new DataValidationException(
        "No record class exists with name '" + recordClassName + "'."));

    var basketFactory = factory.getWdkModel().getBasketFactory();
    List<String[]> ids = basketFactory.getBasket(user, recordClass).stream()
        .map(ri -> ri.getPrimaryKey().getValues().values().toArray(new String[0]))
        .collect(Collectors.toList());

    if (ids.isEmpty())
      throw new DataValidationException("Basket '" + recordClassName + "' does "
        + "not contain any records.  No dataset can be made.");

    return createDataset(user, new ListDatasetParser(),
      new DatasetListContents(joinIds(ids)), factory);
  }

  private static Dataset createFromStrategy(
    final long strategyId,
    final User user,
    final DatasetFactory factory
  ) throws WdkModelException, DataValidationException {

    RunnableObj<Strategy> strategy = factory.getWdkModel().getStepFactory()
      .getStrategyById(strategyId, ValidationLevel.RUNNABLE, FillStrategy.FILL_PARAM_IF_MISSING)
      .orElseThrow(() -> new DataValidationException("Strategy with ID " + strategyId + " not found."))
      .getRunnable()
      .getOrThrow(strat -> new DataValidationException("Strategy with ID " +
        strategyId + " not valid. " + strat.getValidationBundle().toString()));

    AnswerValue answerValue = AnswerValueFactory.makeAnswer(
        Strategy.getRunnableStep(strategy, strategy.get().getRootStepId()).get());

    List<String[]> ids = answerValue.getAllIds();

    if (ids.isEmpty())
      throw new DataValidationException("Strategy '" + strategyId + "' does not"
        + " contain any records.  No dataset can be made.");

    return createDataset(user, new ListDatasetParser(),
      new DatasetListContents(joinIds(ids)), factory);
  }

  private static List<String> joinIds(List<String[]> ids) {
    return ids.stream()
        .map(idArray -> join(idArray, ListDatasetParser.DATASET_COLUMN_DIVIDER))
        .collect(Collectors.toList());
  }

  private static Dataset createDataset(
    final User            user,
    final DatasetParser   parser,
    final DatasetContents content,
    final DatasetFactory  factory
  ) throws WdkModelException, DataValidationException {
    try {
      return factory.createOrGetDataset(user, parser, content, uploadFileName);
    } catch (WdkUserException e) {
      throw new DataValidationException(e.getMessage());
    }
  }

  private static Dataset createFromTemporaryFile(
    final String                tempFileId,
    final User                  user,
    final DatasetFactory        factory,
    final Map<String, JsonType> additionalConfig,
    final SessionProxy          session
  ) throws DataValidationException, WdkModelException, RequestMisformatException {
    var parserName = getStringOrFail(additionalConfig, JsonKeys.PARSER);

    Path tempFilePath = TemporaryFileService.getTempFileFactory(factory.getWdkModel(), session)
      .apply(tempFileId)
      .orElseThrow(() -> new DataValidationException("Temporary file with the ID '" + tempFileId + "' could not be found in this session."));

    var contents = new DatasetFileContents(tempFileId, tempFilePath.toFile());
    var parser   = parserName.isPresent()
      ? findDatasetParser(parserName.get(), additionalConfig, factory.getWdkModel())
      : new ListDatasetParser(contents);

    try {
//      if (contents.isEmpty()) {
//        throw new DataValidationException("The file submitted is empty.  No dataset can be made.");
//      }
      return createDataset(user, parser, contents, tempFileId, factory);
    }
    catch (IOException e) {
      throw new WdkModelException("Unable to read temporary file with ID '" + tempFileId + "'.", e);
    }
  }

  private static DatasetParser findDatasetParser(
    final String parserName,
    final Map<String, JsonType> additionalConfig,
    final WdkModel model
  ) throws DataValidationException, RequestMisformatException, WdkModelException {

    Optional<String> questionName  = getStringOrFail(additionalConfig, JsonKeys.SEARCH_NAME);
    Optional<String> parameterName = getStringOrFail(additionalConfig, JsonKeys.PARAMETER_NAME);

    if (questionName.isEmpty() || parameterName.isEmpty()) {
      throw new DataValidationException("If '" + JsonKeys.PARSER + "' property "
        + "is specified, '" + JsonKeys.SEARCH_NAME + "' and '"
        + JsonKeys.PARAMETER_NAME + "' must also be specified.");
    }

    Question question = model.getQuestionByFullName(questionName.get())
      .orElseThrow(
        () -> new DataValidationException(String.format(
          "Could not find question with name '%s'.", questionName.get()
        )));

    Param param = Optional.ofNullable(question.getParamMap().get(parameterName.get()))
      .orElseThrow(
        () -> new DataValidationException(String.format(
          "Could not find parameter '%s' in question '%s'.",
          parameterName.get(),
          questionName.get()
        )));

    if (!(param instanceof DatasetParam)) {
      throw new DataValidationException(String.format(
        "Parameter '%s' must be a DatasetParam, is a %s.",
        parameterName.get(),
        param.getClass().getSimpleName()
      ));
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
