package org.gusdb.wdk.service.service.search;

import static java.lang.String.format;
import static org.gusdb.fgputil.json.JsonUtil.Jackson;
import static org.gusdb.fgputil.validation.ValidationLevel.RUNNABLE;
import static org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpecBuilder.FillStrategy.FILL_PARAM_IF_MISSING;
import static org.gusdb.wdk.model.user.StepContainer.emptyContainer;
import static org.gusdb.wdk.service.service.AnswerService.REPORT_NAME_PATH_PARAM;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.answer.spec.AnswerSpecBuilder;
import org.gusdb.wdk.model.answer.spec.FilterOptionList.FilterOptionListBuilder;
import org.gusdb.wdk.model.columntool.ColumnReporter;
import org.gusdb.wdk.model.columntool.ColumnToolFactory;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.service.request.answer.AnswerSpecServiceFormat;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.service.AnswerService;
import org.gusdb.wdk.service.service.QuestionService;
import org.gusdb.wdk.service.service.RecordService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.util.RawValue;

/**
 * Endpoints for getting info about or running column reporters.
 */
@Path(ColumnReporterService.COLUMN_REPORTS_PATH)
public class ColumnReporterService extends ColumnToolService {

  /**
   * API Paths
   */
  public static final String COLUMN_REPORTS_PATH =
      ColumnService.NAMED_COLUMN_PATH + "/" + AnswerService.REPORTS_URL_SEGMENT;

  /**
   * Reporter not found message.
   */
  private static final String ERR_404 =
    "Invalid reporter \"%s\" for column " + "\"%s\".";

  public ColumnReporterService(
    @PathParam(RecordService.RECORD_TYPE_PATH_PARAM) final String recordType,
    @PathParam(QuestionService.SEARCH_PATH_PARAM) final String searchName,
    @PathParam(ColumnService.COLUMN_PATH_PARAM) final String columnName
  ) {
    super(recordType, searchName, columnName);
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
   * @param toolName
   *   name of the column tool whose reporter's input spec was requested.
   *
   * @return Input specification for what the named reporter expects as config
   * input on run
   * @throws WdkModelException 
   */
  @GET
  @Path(COLUMN_TOOL_PARAM_SEGMENT)
  @Produces(MediaType.APPLICATION_JSON)
  public JsonNode getReporterDetails(@PathParam(COLUMN_TOOL_PATH_PARAM) final String toolName) throws WdkModelException {
    ColumnReporter rep = getColumnReporter(getColumn(), toolName);
    return Jackson.createObjectNode()
      .put(JsonKeys.NAME, toolName)
      .set("schema", Jackson.createObjectNode()
        .putRawValue("input", new RawValue(rep.getInputSchema().build()))
        .putRawValue("output", new RawValue(rep.getOutputSchema().build())));
  }

  /**
   * Runs the named reporter and streams out the JSON result.
   *
   * @param toolName
   *   Name of the reporter to run
   * @param requestBody
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
    final String requestBody
  ) throws WdkModelException, WdkUserException, DataValidationException {
    try {
      JSONObject requestJson = new JSONObject(requestBody);

      // try to find and create a column reporter for this tool
      //   (could validate this after answer spec, etc. but cheap and feel this error should be emitted first)
      ColumnReporter reporter = getColumnReporter(getColumn(), toolName);
  
      // parse any requested view filters
      FilterOptionListBuilder viewFilters = AnswerSpecServiceFormat.parseViewFilters(requestJson);

      // build answer spec for answer that will be fed to the reporter
      RunnableObj<AnswerSpec> answerSpec =  makeAnswerSpec(
          requestJson.getJSONObject(JsonKeys.SEARCH_CONFIG), toolName, viewFilters);

      // build answer value
      AnswerValue answerValue = AnswerValueFactory.makeAnswer(getSessionUser(), answerSpec);

      // finish configuring the reporter
      reporter
        .setAnswerValue(answerValue)
        .configure(requestJson.getJSONObject(JsonKeys.REPORT_CONFIG));
  
      // stream response
      return AnswerService.getAnswerAsStream(reporter);
    }
    catch (JSONException e) {
      throw new RequestMisformatException("Could not parse request body", e);
    }
  }

  /**
   * Checks for the existence of a reporter on the passed column tool for the given column
   * and returns an instance of it if it exists; will throw a NotFoundException if not.
   *
   * @param column requested column
   * @param toolName requested tool/reporter name
   * @return instance of the requested reporter
   * @throws NotFoundException if reporter for that tool does not exist on this column
   * @throws WdkModelException if something goes wrong
   */
  public static ColumnReporter getColumnReporter(AttributeField column, String toolName) throws WdkModelException {
    if (!column.getColumnReporterNames().contains(toolName)) {
      throw new NotFoundException(format(ERR_404, column.getName(), toolName));
    }
    return ColumnToolFactory.getColumnReporterInstance(column, toolName);
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
  private RunnableObj<AnswerSpec> makeAnswerSpec(JSONObject answerSpecJson, String filterToIgnore, FilterOptionListBuilder viewFilters)
      throws WdkModelException, RequestMisformatException, DataValidationException {

    // build a starter answer spec based on the question name, incoming spec JSON, and view filters
    AnswerSpecBuilder specBuilder = AnswerSpecServiceFormat
        .parse(getQuestion(), answerSpecJson, getWdkModel())
        .setViewFilterOptions(viewFilters);

    // remove any existing filters for this column tool so statistics are based on unfiltered answer
    trimColumnFilter(specBuilder, getColumn().getName(), filterToIgnore);

    // build the spec and return
    return specBuilder
        .build(getSessionUser(), emptyContainer(), RUNNABLE, FILL_PARAM_IF_MISSING)
        .getRunnable()
        .getOrThrow(spec -> new DataValidationException(spec.getValidationBundle()));
  }

  /**
   * Trims any existing column filters for the passed column name
   * and filter (tool) name off the passed answer spec builder
   *
   * @param answerSpecBuilder builder to trim
   * @param columnName column name
   * @param filterToIgnore tool/filter name
   */
  public static void trimColumnFilter(AnswerSpecBuilder answerSpecBuilder, String columnName, String filterToIgnore) {
    answerSpecBuilder.getColumnFilters().remove(columnName, filterToIgnore);
  }

}
