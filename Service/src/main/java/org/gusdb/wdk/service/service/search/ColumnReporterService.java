package org.gusdb.wdk.service.service.search;

import com.fasterxml.jackson.databind.JsonNode;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.toolbundle.ColumnReporter;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.request.answer.AnswerSpecServiceFormat;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.service.AbstractWdkService;
import org.gusdb.wdk.service.service.QuestionService;
import org.gusdb.wdk.service.service.RecordService;
import org.json.JSONArray;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.util.function.Supplier;

import static java.lang.String.format;
import static org.gusdb.fgputil.FormatUtil.NL;
import static org.gusdb.fgputil.json.JsonUtil.Jackson;
import static org.gusdb.fgputil.validation.ValidationLevel.RUNNABLE;
import static org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpecBuilder.FillStrategy.FILL_PARAM_IF_MISSING;
import static org.gusdb.wdk.model.user.StepContainer.emptyContainer;

/**
 * Endpoints for getting info about or running column reporters.
 */
@Path(ColumnReporterService.BASE_PATH)
public class ColumnReporterService extends AbstractWdkService {

  /**
   * API Paths
   */
  public static final String
    ID_VAR = "reporter",
    ID_PARAM = "{" + ID_VAR + "}",
    BASE_PATH = SearchColumnService.ID_PATH + "/" + JsonKeys.REPORTS;

  /**
   * Reporter not found message.
   */
  private static final String ERR_404 =
    "Invalid reporter \"%s\" for column " + "\"%s\".";

  /**
   * Stream error message.
   */
  private static final byte[] ERR_STREAM = (
    " ********************************************* " + NL +
    " ********************************************* " + NL +
    " *************** ERROR **************** " + NL +
    "We're sorry, but an error occurred while streaming your result and your request cannot be completed.  " +
    NL + "Please contact us with a description of your download." + NL + NL
  ).getBytes();

  private final Question search;
  private final AttributeField column;

  public ColumnReporterService(
    @PathParam(RecordService.ID_VAR) final String recordType,
    @PathParam(QuestionService.ID_VAR) final String searchType,
    @PathParam(SearchColumnService.ID_VAR) final String columnName,
    @Context ServletContext ctx
  ) {
    setServletContext(ctx);
    this.search = getQuestionOrNotFound(recordType, searchType);
    this.column = requireColumn(this.search, columnName);
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
    return new JSONArray(this.column.getColumnReporterNames());
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
  @Path(ID_PARAM)
  @Produces(MediaType.APPLICATION_JSON)
  public Object getReporterDetails(@PathParam(ID_VAR) final String reporter) {
    var rep = column.getReporter(reporter)
      .orElseThrow(makeNotFound(reporter));

    return Jackson.createObjectNode()
      .put(JsonKeys.NAME, rep.getKey())
      .set("schema", Jackson.createObjectNode()
        .putPOJO("input", rep.inputSpec())
        .putPOJO("output", rep.outputSpec().build()));
  }

  /**
   * Runs the named reporter and streams out the JSON result.
   *
   * @param reporter
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
   */
  @POST
  @Path(ID_PARAM)
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public StreamingOutput runReporter(
    @PathParam(ID_VAR) final String reporter,
    final JsonNode body
  ) throws WdkModelException, WdkUserException {
    return wrapReporter(column.prepareReporter(
      reporter,
      makeAnswer(body.get(JsonKeys.SEARCH_CONFIG)),
      body.get(JsonKeys.REPORT_CONFIG)
    ).orElseThrow(makeNotFound(reporter)));
  }

  // TODO: Unify this with answer service
  private StreamingOutput wrapReporter(final ColumnReporter rep) {
    return stream -> {
      try {
        rep.runner().run(rep.build(stream));
      } catch (final WdkModelException e) {
        stream.write(ERR_STREAM);
        throw new WebApplicationException(e);
      }
    };
  }

  /**
   * Creates an answer value from the given json body.
   *
   * @param body JSON body to parse
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
   */
  private AnswerValue makeAnswer(JsonNode body)
  throws WdkModelException, RequestMisformatException {
    return AnswerValueFactory.makeAnswer(getSessionUser(), makeAnswerSpec(body));
  }

  /**
   * Parses the given json object into a runnable answer spec or throws an
   * exception.
   *
   * @param body
   *   Json body to parse
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
   */
  private RunnableObj<AnswerSpec> makeAnswerSpec(JsonNode body)
  throws WdkModelException, RequestMisformatException {
    return AnswerSpecServiceFormat.parse(
      search,
      JsonUtil.toJSONObject(body)
        .mapError(WdkModelException::new)
        .valueOrElseThrow(),
      getWdkModel()
    )
      .build(getSessionUser(), emptyContainer(), RUNNABLE, FILL_PARAM_IF_MISSING)
      .getRunnable()
      .getOrThrow(ColumnReporterService::specToException);
  }

  /**
   * Returns a supplier for a {@link NotFoundException} configured with the
   * message defined in {@link ColumnReporterService#ERR_404} populated with the
   * given report name and the name of the current column.
   *
   * @param rep
   *   report name
   *
   * @return
   *   a supplier for a configured {@link NotFoundException}.
   */
  private Supplier<NotFoundException> makeNotFound(final String rep) {
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
  private static WdkModelException specToException(final AnswerSpec spec) {
    return new WdkModelException(spec.getValidationBundle().toString(2));
  }
}
