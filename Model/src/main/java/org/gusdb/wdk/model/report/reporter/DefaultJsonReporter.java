package org.gusdb.wdk.model.report.reporter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.SortDirectionSpec;
import org.gusdb.fgputil.json.JsonWriter;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.stream.RecordStream;
import org.gusdb.wdk.model.answer.stream.RecordStreamFactory;
import org.gusdb.wdk.model.filter.FilterOptionList;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.report.AbstractReporter;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.model.report.ReporterConfigException;
import org.gusdb.wdk.model.report.ReporterRef;
import org.gusdb.wdk.model.report.config.AnswerDetails;
import org.gusdb.wdk.model.report.config.AnswerDetailsFactory;
import org.gusdb.wdk.model.report.util.RecordFormatter;
import org.gusdb.wdk.model.user.UserPreferences;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Formats WDK answer service responses.  JSON will have the following form:
 * {
 *   meta: {
 *     class: String,
 *     totalCount: Number,
 *     responseCount: Number,
 *     attributes: [ String ],
 *     tables: [ String ]
 *   },
 *   records: [ see RecordFormatter ]
 * }
 */
public class DefaultJsonReporter extends AbstractReporter {

  public static final String WDK_SERVICE_JSON_REPORTER_RESERVED_NAME = "wdk-service-json";

  private Map<String,AttributeField> _attributes;
  private Map<String,TableField> _tables;
  private ContentDisposition _contentDisposition;
  
  public DefaultJsonReporter(AnswerValue answerValue) {
    super(answerValue);
  }

  @Override
  public Reporter configure(Map<String, String> config) {
    throw new UnsupportedOperationException("Map configuration not supported by this reporter.");
  }

  @Override
  public Reporter configure(JSONObject config) throws ReporterConfigException, WdkModelException {
    return configure(AnswerDetailsFactory.createFromJson(config, _baseAnswer.getQuestion()));
  }

  private DefaultJsonReporter configure(AnswerDetails config) throws WdkModelException {
    _baseAnswer = getConfiguredAnswer(_baseAnswer, config);
    _attributes = config.getAttributes();
    _tables = config.getTables();
    _contentDisposition = config.getContentDisposition();
    return this;
  }

  private static AnswerValue getConfiguredAnswer(AnswerValue answerValue, AnswerDetails config) throws WdkModelException {
    int startIndex = config.getOffset() + 1;
    int endIndex = startIndex + config.getNumRecords() - 1;
    AnswerValue configuredAnswer = answerValue.cloneWithNewPaging(startIndex, endIndex);
    Map<String, Boolean> sorting = SortDirectionSpec.convertSorting(
        config.getSorting(), UserPreferences.MAX_NUM_SORTING_COLUMNS);
    configuredAnswer.setSortingMap(sorting);
    return configuredAnswer;
  }

  @Override
  public String getHttpContentType() {
    return MediaType.APPLICATION_JSON;
  }

  @Override
  public String getDownloadFileName() {
    return _baseAnswer.getQuestion().getName() + "_std.json";
  }

  @Override
  public ContentDisposition getContentDisposition() {
    return _contentDisposition;
  }

  @Override
  protected void write(OutputStream out) throws WdkModelException {

    // create output writer and initialize record stream
    try (JsonWriter writer = new JsonWriter(new BufferedWriter(new OutputStreamWriter(out)));
         RecordStream recordStream = RecordStreamFactory.getRecordStream(
            _baseAnswer, _attributes.values(), _tables.values())) {

      // start parent object and records array
      writer.object().key(JsonKeys.RECORDS).array();

      // write records
      int numRecordsReturned = 0;
      for (RecordInstance record : recordStream) {
        writer.value(RecordFormatter.getRecordJson(record, _attributes.keySet(), _tables.keySet()).getFirst());
        numRecordsReturned++;
      }

      // get metadata object
      JSONObject metadata = getMetaData(_baseAnswer, _attributes.keySet(), _tables.keySet(), numRecordsReturned);

      // end records array, write meta property, and close object
      writer.endArray().key(JsonKeys.META).value(metadata);
      
      // allow subclasses an opportunity to extend the JSON
      writeAdditionalJson(writer).endObject();
    }
    catch (WdkUserException e) {
      // should already have validated any user input
      throw new WdkModelException("Internal validation failure", e);
    }
    catch (IOException e) {
      throw new WdkModelException("Unable to write reporter result to output stream", e);
    }
  }
  
  /**
   * Adds supplemental JSON to the top-level response object. Default implementation adds nothing, but can
   * be overridden by subclasses if additional data must be delivered
   * 
   * @param writer response stream writer
   * @return the passed writer
   * @throws WdkModelException if unable to generate or write supplemental data
   */
  public JsonWriter writeAdditionalJson(JsonWriter writer) throws WdkModelException {
    return writer;
  }

  private static JSONObject getMetaData(AnswerValue answerValue,
      Set<String> includedAttributes, Set<String> includedTables, int numRecordsReturned)
      throws WdkModelException, WdkUserException {
    AnswerValue answerValueWithoutViewFilters = getAnswerValueWithoutViewFilters(answerValue);
    JSONObject meta = new JSONObject();
    meta.put(JsonKeys.RECORD_CLASS_NAME, answerValue.getQuestion().getRecordClass().getFullName());
    meta.put(JsonKeys.TOTAL_COUNT, answerValueWithoutViewFilters.getResultSizeFactory().getResultSize());
    meta.put(JsonKeys.DISPLAY_TOTAL_COUNT, answerValueWithoutViewFilters.getResultSizeFactory().getDisplayResultSize());
    meta.put(JsonKeys.VIEW_TOTAL_COUNT, answerValue.getResultSizeFactory().getResultSize());
    meta.put(JsonKeys.DISPLAY_VIEW_TOTAL_COUNT, answerValue.getResultSizeFactory().getDisplayResultSize());
    meta.put(JsonKeys.RESPONSE_COUNT, numRecordsReturned);
    meta.put(JsonKeys.PAGINATION, new JSONObject()
        .put(JsonKeys.OFFSET, answerValue.getStartIndex() - 1)
        .put(JsonKeys.NUM_RECORDS, answerValue.getEndIndex() - (answerValue.getStartIndex() - 1)));
    meta.put(JsonKeys.ATTRIBUTES, FormatUtil.stringCollectionToJsonArray(includedAttributes));
    meta.put(JsonKeys.TABLES, FormatUtil.stringCollectionToJsonArray(includedTables));
    meta.put(JsonKeys.SORTING, formatSorting(answerValue.getSortingMap(), answerValue.getQuestion().getAttributeFieldMap()));
    return meta;
  }

  private static AnswerValue getAnswerValueWithoutViewFilters(AnswerValue answerValue) {
    if (answerValue.getViewFilterOptions() == null || answerValue.getViewFilterOptions().getSize() == 0) return answerValue;
    AnswerValue answerValueWithoutViewFilters = new AnswerValue(answerValue);
    answerValueWithoutViewFilters.setViewFilterOptions(null);
    return answerValueWithoutViewFilters;
  }

  public static JSONArray formatSorting(Map<String, Boolean> sortingAttributeMap, Map<String, AttributeField> allowedValues) {
    return new JSONArray(
        SortDirectionSpec.convertSorting(sortingAttributeMap, allowedValues)
            .stream()
            .map(spec -> new JSONObject()
                .put("attributeName", spec.getItemName())
                .put("direction", spec.getDirection()))
            .collect(Collectors.toList()));
  }

  public static ReporterRef createReference() {
    ReporterRef ref = new ReporterRef();
    ref.setName(WDK_SERVICE_JSON_REPORTER_RESERVED_NAME);
    ref.setDisplayName("Standard JSON");
    ref.setDescription(new WdkModelText(null, "Converts your result to the standard JSON used by the web service."));
    ref.setImplementation(DefaultJsonReporter.class.getName());
    return ref;
  }
}
