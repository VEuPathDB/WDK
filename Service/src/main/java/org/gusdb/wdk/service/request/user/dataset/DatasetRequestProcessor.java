package org.gusdb.wdk.service.request.user.dataset;

import static org.gusdb.fgputil.json.JsonIterators.arrayStream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.client.ClientUtil;
import org.gusdb.fgputil.iterator.IteratorUtil;
import org.gusdb.fgputil.json.JsonType;
import org.gusdb.fgputil.json.JsonType.ValueType;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.cache.TemporaryUserDataStore.TemporaryUserData;
import org.gusdb.wdk.model.dataset.Dataset;
import org.gusdb.wdk.model.dataset.DatasetContents;
import org.gusdb.wdk.model.dataset.DatasetFactory;
import org.gusdb.wdk.model.dataset.DatasetFileContents;
import org.gusdb.wdk.model.dataset.DatasetListContents;
import org.gusdb.wdk.model.dataset.DatasetParser;
import org.gusdb.wdk.model.dataset.DatasetPassThroughParser;
import org.gusdb.wdk.model.dataset.ListDatasetParser;
import org.gusdb.wdk.model.query.param.DatasetParam;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpecBuilder.FillStrategy;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.PrimaryKeyIterator;
import org.gusdb.wdk.model.record.PrimaryKeyValue;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.user.BasketFactory;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.service.TemporaryFileService;
import org.json.JSONArray;

public class DatasetRequestProcessor {

  private static Logger LOG = Logger.getLogger(DatasetRequestProcessor.class);

  public static Dataset createFromRequest(
    DatasetRequest request,
    User user,
    DatasetFactory factory,
    TemporaryUserData tmpUserData
  ) throws WdkModelException, DataValidationException, RequestMisformatException {
    JsonType value = request.getConfigValue();
    switch(request.getSourceType()) {
      case ID_LIST:  return createFromIdList(value.getJSONArray(), user, factory);
      case BASKET:   return createFromBasket(value.getString(), user, factory);
      case STRATEGY: return createFromStrategy(getStrategyId(value), user, factory);
      case FILE:     return createFromTemporaryFile(value.getString(), user, factory, request.getAdditionalConfig(), tmpUserData);
      case URL:      return createFromUrl(value.getString(), user, factory, request.getAdditionalConfig());
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

    RecordClass recordClass = factory.getWdkModel()
      .getRecordClassByUrlSegment(recordClassName)
      .orElseThrow(() -> new DataValidationException(
        "No record class exists with name '" + recordClassName + "'."));

    BasketFactory basketFactory = factory.getWdkModel().getBasketFactory();

    long basketSize = basketFactory.getBasketCounts(user).get(recordClass);
    if (basketSize == 0)
      throw new DataValidationException("Basket '" + recordClassName + "' does "
          + "not contain any records.  No dataset can be made.");

    // write basket records to file (just to parse again :()
    File file = null;
    try (Stream<RecordInstance> basketStream = basketFactory.getBasket(user, recordClass)) {

      Iterator<String[]> recordIterator = basketStream
          .map(RecordInstance::getPrimaryKey)
          .map(PrimaryKeyValue::getValues)
          .map(Map::values)
          .map(c -> c.toArray(new String[0]))
          .iterator();

      file = createTempFile(user, recordClassName);

      writeRecordsToFile(file, recordIterator);

      return createDataset(user, new ListDatasetParser(),
          new DatasetFileContents(null, file, basketSize), factory);
    }
    catch (IOException e) {
      throw new WdkModelException("Could not create dataset from basket", e);
    }
    finally {
      deleteFile(file);
    }
  }

  private static void deleteFile(File file) {
    if (file != null) {
      try {
        Files.delete(file.toPath());
      }
      catch (IOException e) {
        LOG.warn("Unable to delete file after use: " + file.getAbsolutePath());
      }
    }
  }

  private static File createTempFile(User user, String recordClassName) throws IOException {
    return Files.createTempFile(user.getWdkModel().getModelConfig().getWdkTempDir(),
        "dataset-", "-" + user.getStableId() + "-" + recordClassName).toFile();
  }

  private static void writeRecordsToFile(File file, Iterator<String[]> rows) throws IOException {

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

      for (String[] rowArray : IteratorUtil.toIterable(rows)) {
        String row = String.join(ListDatasetParser.DATASET_COLUMN_DIVIDER, rowArray);
        writer.write(row);
        writer.write('\n');
      }

      writer.flush();
    }
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


    long resultSize = answerValue.getResultSizeFactory().getResultSize();
    if (resultSize == 0)
      throw new DataValidationException("Strategy '" + strategyId + "' does "
          + "not contain any records.  No dataset can be made.");

    // write records to file (just to parse again :()
    File file = null;
    try (PrimaryKeyIterator pkIterator = answerValue.getAllIds()) {

      file = createTempFile(user, strategy.get().getRecordClass().get().getUrlSegment());

      writeRecordsToFile(file, pkIterator);

      return createDataset(user, new ListDatasetParser(),
          new DatasetFileContents(null, file, resultSize), factory);
    }
    catch (Exception e) {
      throw new WdkModelException("Could not create dataset from basket", e);
    }
    finally {
      deleteFile(file);
    }
  }

  private static Dataset createDataset(
    final User            user,
    final DatasetParser   parser,
    final DatasetContents content,
    final DatasetFactory  factory
  ) throws WdkModelException, DataValidationException {
    try {
      return factory.createOrGetDataset(user, parser, content);
    }
    catch (WdkUserException e) {
      throw new DataValidationException(e.getMessage());
    }
  }

  private static Dataset createFromTemporaryFile(
    final String                tempFileId,
    final User                  user,
    final DatasetFactory        factory,
    final Map<String, JsonType> additionalConfig,
    final TemporaryUserData     tmpUserData
  ) throws DataValidationException, WdkModelException, RequestMisformatException {

    var model = factory.getWdkModel();
    var parser = getDatasetParser(model, additionalConfig);

    var tempFilePath = TemporaryFileService.getTempFileFactory(factory.getWdkModel(), tmpUserData)
      .apply(tempFileId)
      .orElseThrow(() -> new DataValidationException(
          "Temporary file with the ID '" + tempFileId + "' could not be found in this session."));

    var contents = new DatasetFileContents(tempFileId, tempFilePath.toFile());
    return createDataset(user, parser, contents, factory);
  }

  private static Dataset createFromUrl(
    final String url,
    final User user,
    final DatasetFactory factory,
    final Map<String, JsonType> additionalConfig
  ) throws DataValidationException, RequestMisformatException, WdkModelException {

    var model = factory.getWdkModel();
    var parser = getDatasetParser(model, additionalConfig);

    try (InputStream fileStream = ClientUtil
        .makeAsyncGetRequest(url, MediaType.WILDCARD)
        .getEither()
        .leftOrElseThrowWithRight(error -> new DataValidationException(
            "Unable to retrieve file at url " + url + ".  GET request returned " +
            error.getStatusType().getStatusCode() + ": " + error.getResponseBody()))) {

      Path filePath = TemporaryFileService.writeTemporaryFile(factory.getWdkModel(), fileStream);
      LOG.debug("Wrote content retrieved from URL [" + url + "] to file " + filePath.toAbsolutePath());

      // delete this temporary file on JVM exit
      filePath.toFile().deleteOnExit();

      var contents = new DatasetFileContents(url, filePath.toFile());
      return createDataset(user, parser, contents, factory);
    }
    catch (Exception e) {
      throw new DataValidationException("Unable to retrieve file", e);
    }
  }

  private static DatasetParser getDatasetParser(
      WdkModel wdkModel,
      Map<String, JsonType> additionalConfig
  ) throws RequestMisformatException, WdkModelException, DataValidationException {
    var parserName = getStringOrFail(additionalConfig, JsonKeys.PARSER);
    return parserName.isPresent()
      ? findDatasetParser(parserName.get(), additionalConfig, wdkModel)
      : new ListDatasetParser();
  }

  private static DatasetParser findDatasetParser(
    final String parserName,
    final Map<String, JsonType> additionalConfig,
    final WdkModel model
  ) throws DataValidationException, RequestMisformatException, WdkModelException {

    var questionName  = getStringOrFail(additionalConfig, JsonKeys.SEARCH_NAME);
    var parameterName = getStringOrFail(additionalConfig, JsonKeys.PARAMETER_NAME);

    if (questionName.isEmpty() || parameterName.isEmpty()) {
      throw new DataValidationException("If '" + JsonKeys.PARSER + "' property "
        + "is specified, '" + JsonKeys.SEARCH_NAME + "' and '"
        + JsonKeys.PARAMETER_NAME + "' must also be specified.");
    }

    Question question = model.getQuestionByName(questionName.get())
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
