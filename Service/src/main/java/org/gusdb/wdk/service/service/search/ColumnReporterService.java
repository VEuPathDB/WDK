package org.gusdb.wdk.service.service.search;

import static java.lang.String.format;
import static org.gusdb.fgputil.json.JsonUtil.Jackson;
import static org.gusdb.fgputil.validation.ValidationLevel.RUNNABLE;
import static org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpecBuilder.FillStrategy.FILL_PARAM_IF_MISSING;
import static org.gusdb.wdk.model.user.StepContainer.emptyContainer;
import static org.gusdb.wdk.service.service.AnswerService.REPORT_NAME_PATH_PARAM;

import java.util.function.Supplier;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.answer.spec.AnswerSpecBuilder;
import org.gusdb.wdk.model.answer.spec.FilterOptionList.FilterOptionListBuilder;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.toolbundle.ColumnReporterInstance;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.request.answer.AnswerSpecServiceFormat;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.service.AbstractWdkService;
import org.gusdb.wdk.service.service.AnswerService;
import org.gusdb.wdk.service.service.QuestionService;
import org.gusdb.wdk.service.service.RecordService;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Endpoints for getting info about or running column reporters.
 */
@Path(ColumnReporterService.NAMED_COLUMN_REPORTS_PATH)
public class ColumnReporterService extends AbstractWdkService {

  /**
   * API Paths
   */
  public static final String NAMED_COLUMN_REPORTS_PATH =
      SearchColumnService.NAMED_COLUMN_PATH + "/" + AnswerService.REPORTS_URL_SEGMENT;

  /**
   * Reporter not found message.
   */
  private static final String ERR_404 =
    "Invalid reporter \"%s\" for column " + "\"%s\".";

  private final String _recordType;
  private final String _searchName;
  private final String _columnName;

  public ColumnReporterService(
    @PathParam(RecordService.RECORD_TYPE_PATH_PARAM) final String recordType,
    @PathParam(QuestionService.SEARCH_PATH_PARAM) final String searchName,
    @PathParam(SearchColumnService.COLUMN_PATH_PARAM) final String columnName
  ) {
    _recordType = recordType;
    _searchName = searchName;
    _columnName = columnName;
  }

  private Question getQuestion() {
    return getQuestionOrNotFound(_recordType, _searchName);
  }

  private AttributeField getColumn() {
    return requireColumn(getQuestion(), _columnName);
  }

  /**
   * Builds a list of available report names for the current column.
   *
   * @return a {@code JSONArray} containing the names of the available reporters
   * for the current column
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray getReporters() {
    return new JSONArray(getColumn().getColumnReporterNames());
  }

  /**
   * Retrieves and returns the input spec for the named reporter on the current
   * column.
   *
   * @param reporter
   *   name of the reporter for which the input spec was requested.
   *
   * @return Input specification for what the named reporter expects as config
   * input on run
   */
  @GET
  @Path(AnswerService.CUSTOM_REPORT_SEGMENT)
  @Produces(MediaType.APPLICATION_JSON)
  public JsonNode getReporterDetails(@PathParam(REPORT_NAME_PATH_PARAM) final String reporter) {
    AttributeField column = getColumn();
    var rep = column.getReporter(reporter)
      .orElseThrow(makeNotFound(column, reporter));

    return Jackson.createObjectNode()
      .put(JsonKeys.NAME, rep.getKey())
      .set("schema", Jackson.createObjectNode()
        .putPOJO("input", rep.getInputSpec(column.getDataType()))
        .putPOJO("output", rep.outputSpec(column.getDataType()).build()));
  }

  /**
   * Runs the named reporter and streams out the JSON result.
   *
   * @param toolName
   *   Name of the reporter to run
   * @param body
   *   Reporter configuration
   *
   * @return A stream provider that will write the reporter's result as JSON to
   * the given output stream.
   *
   * @throws WdkModelException
   *   if an internal error occurs while attempting to build an answer value,
   *   answer spec, or reporter.
   * @throws WdkUserException
   *   if the given reporter configuration is invalid.
   * @throws DataValidationException if answer spec is invalid
   */
  @POST
  @Path(AnswerService.CUSTOM_REPORT_SEGMENT)
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public StreamingOutput runReporter(
    @PathParam(REPORT_NAME_PATH_PARAM) final String toolName,
    final JsonNode body
  ) throws WdkModelException, WdkUserException, DataValidationException {
    AttributeField column = getColumn();
    FilterOptionListBuilder viewFilters = AnswerSpecServiceFormat
        .parseViewFilters(JsonUtil.toJSONObject(body)
            .valueOrElseThrow(() -> new RequestMisformatException("Passed body is not a JSON object.")));
    ColumnReporterInstance reporter = column.makeReporterInstance(
      toolName,
      makeAnswer(body.get(JsonKeys.SEARCH_CONFIG), toolName, viewFilters),
      body.get(JsonKeys.REPORT_CONFIG)
    ).orElseThrow(makeNotFound(column, toolName));
    return AnswerService.getAnswerAsStream(reporter);
  }

  /**
   * Creates an answer value from the given json body.
   *
   * @param body JSON body to parse
   * @param filterToIgnore name of filter to ignore
   * @param viewFilters 
   *
   * @return an answer value from the given answer spec json
   *
   * @throws WdkModelException
   *   See <ul>
   *   <li>{@link AnswerValueFactory#makeAnswer(User, RunnableObj)}
   *   <li>{@link #makeAnswerSpec(JsonNode)}
   *   </ul>
   * @throws RequestMisformatException
   *   See {@link #makeAnswerSpec(JsonNode)}
   * @throws DataValidationException if answer spec is invalid
   */
  private AnswerValue makeAnswer(JsonNode body, String filterToIgnore, FilterOptionListBuilder viewFilters)
  throws WdkModelException, RequestMisformatException, DataValidationException {
    return AnswerValueFactory.makeAnswer(getSessionUser(), makeAnswerSpec(body, filterToIgnore, viewFilters));
  }

  /**
   * Parses the given json object into a runnable answer spec or throws an
   * exception.
   *
   * @param body
   *   Json body to parse
   * @param viewFilters 
   *
   * @return
   *   A runnable answer spec
   *
   * @throws WdkModelException
   *   if the json body was not an object or if the spec could not pass
   *   validation as runnable
   * @throws RequestMisformatException
   *   if the json body was semantically invalid and could not be parsed into
   *   an answer spec.
   * @throws DataValidationException if answer spec is invalid
   */
  private RunnableObj<AnswerSpec> makeAnswerSpec(JsonNode body, String filterToIgnore, FilterOptionListBuilder viewFilters)
  throws WdkModelException, RequestMisformatException, DataValidationException {
    Question search = getQuestion();
    AttributeField column = getColumn();
    JSONObject parentJson = JsonUtil.toJSONObject(body)
        .mapError(WdkModelException::new)
        .valueOrElseThrow();
    return trimColumnFilter(AnswerSpecServiceFormat.parse(
      search,
      parentJson,
      getWdkModel()
    )
    .setViewFilterOptions(viewFilters),
    column.getName(), filterToIgnore)
      .build(getSessionUser(), emptyContainer(), RUNNABLE, FILL_PARAM_IF_MISSING)
      .getRunnable()
      .getOrThrow(ColumnReporterService::specToException);
  }

  public static AnswerSpecBuilder trimColumnFilter(AnswerSpecBuilder specBuilder,
      String columnOfFilterToIgnore, String filterToIgnore) {
    specBuilder.getColumnFilters().remove(columnOfFilterToIgnore, filterToIgnore);
    return specBuilder;
  }

  /**
   * Returns a supplier for a {@link NotFoundException} configured with the
   * message defined in {@link ColumnReporterService#ERR_404} populated with the
   * given report name and the name of the passed column.
   *
   * @param column
   *   selected attribute field
   * @param rep
   *   report name
   *
   * @return
   *   a supplier for a configured {@link NotFoundException}.
   */
  public static Supplier<NotFoundException> makeNotFound(AttributeField column, final String rep) {
    return () -> new NotFoundException(format(ERR_404, rep, column.getName()));
  }

  /**
   * Converts the validation bundle string from an AnswerSpec into an exception.
   *
   * @param spec
   *   answer spec from which to pull the validation bundle.
   * @return
   *   an exception containing the string form of the validation errors from the
   *   given answer spec.
   */
  private static DataValidationException specToException(final AnswerSpec spec) {
    return new DataValidationException(spec.getValidationBundle());
  }
}
