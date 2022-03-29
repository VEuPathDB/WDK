package org.gusdb.wdk.service.service.user;

import static org.gusdb.fgputil.functional.Functions.mapToList;
import static org.gusdb.wdk.service.service.QuestionService.FILTER_PARAM_NAME_PATH_PARAM;
import static org.gusdb.wdk.service.service.QuestionService.ONTOLOGY_TERM_SUMMARY_EXTENSION;
import static org.gusdb.wdk.service.service.QuestionService.REFRESHED_DEPENDENT_PARAMS_EXTENSION;
import static org.gusdb.wdk.service.service.QuestionService.SUMMARY_COUNTS_EXTENSION;
import static org.gusdb.wdk.service.service.QuestionService.getFilterParam;
import static org.gusdb.wdk.service.service.user.StepService.STEP_ID_PATH_PARAM;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.gusdb.fgputil.MapBuilder;
import org.gusdb.fgputil.Named.NamedObject;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.fgputil.validation.ValidObjectFactory.DisplayablyValid;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.ValidObjectFactory.SemanticallyValid;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.query.param.FilterParamNew;
import org.gusdb.wdk.model.query.param.FilterParamNew.FilterParamSummaryCounts;
import org.gusdb.wdk.model.query.param.OntologyItem;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpecBuilder.FillStrategy;
import org.gusdb.wdk.model.query.spec.StepAnalysisFormSpec;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.service.formatter.StepAnalysisFormatter;
import org.gusdb.wdk.service.formatter.param.ParamContainerFormatter;
import org.gusdb.wdk.service.request.ParamValueSetRequest;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Provides endpoints related to step analysis forms.  All endpoints are
 * relative to /users/{id}/steps/{id} (i.e. all pertain to a particular step).
 * These endpoints each have a parallel endpoint in the question service and
 * their APIs are identical to promote code sharing in clients. They are:
 * 
 * GET    /analysis-types         Returns basic info about analysis types that can be run against this step
 * GET    /analysis-types/{name}  Returns displayable form data (defaults and vocabs) for a new analysis of this type
 * POST   /analysis-types/{name}  Returns displayable form data (defaults and vocabs) based on a set of existing param values
 * POST   /analysis-types/{name}/refreshed-dependent-params
 *                                Returns displayable form data (defaults and vocabs) based on a depended param value change
 * POST   /analysis-types/{name}/{filterParamName}/ontology-term-summary
 *                                Returns a filter param's ontology term summary for this result
 * POST   /analysis-types/{name}/{filterParamName}/summary-counts
 *                                Returns a filter param's summary counts for this result
 * 
 * See also: StepAnalysisService
 * 
 * @author rdoherty
 */
public class StepAnalysisFormService extends UserService implements StepAnalysisLookupMixin {

  // endpoints to handle analysis types for a given step
  private static final String ANALYSIS_TYPES_PATH = StepService.NAMED_STEP_PATH + "/analysis-types";
  private static final String ANALYSIS_TYPE_PATH_PARAM = "analysisTypeName";
  private static final String NAMED_ANALYSIS_TYPE_PATH = ANALYSIS_TYPES_PATH + "/{" + ANALYSIS_TYPE_PATH_PARAM + "}";

  private final long _stepId;

  protected StepAnalysisFormService(
      @PathParam(USER_ID_PATH_PARAM) String uid,
      @PathParam(STEP_ID_PATH_PARAM) long stepId) {
    super(uid);
    _stepId = stepId;
  }

  @Override
  public long getStepId() {
    return _stepId;
  }

  @GET
  @Path(ANALYSIS_TYPES_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray getStepAnalysisTypes() throws WdkModelException, DataValidationException {
    RunnableObj<Step> step = getRunnableStepForCurrentUser(_stepId);
    Collection<StepAnalysis> stepAnalyses = step.get().getAnswerSpec().getQuestion().getStepAnalyses().values();
    return StepAnalysisFormatter.getStepAnalysisTypesJsonWithoutParams(stepAnalyses);
  }

  /**
   * Get Step Analysis form building data
   *
   * @param analysisName name of the step analysis for which the form data
   *                     should be retrieved
   * @return Ok response containing JSON provided by the Analyzer instance.
   */
  @GET
  @Path(NAMED_ANALYSIS_TYPE_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONObject getStepAnalysisTypeDataFromName(
      @PathParam(ANALYSIS_TYPE_PATH_PARAM) String analysisName)
      throws WdkModelException, DataValidationException {

    // get step and analysis plugin specified by caller
    RunnableObj<Step> step = getRunnableStepForCurrentUser(_stepId);
    StepAnalysis stepAnalysis = getStepAnalysisFromQuestion(step.get().getAnswerSpec().getQuestion(), analysisName);

    // make sure the chosen analysis is compatible with this step
    validStepForAnalysisOrThrow(step, stepAnalysis);

    DisplayablyValid<StepAnalysisFormSpec> formSpec = StepAnalysisFormSpec
        .builder()
        .buildValidated(step, stepAnalysis, ValidationLevel.DISPLAYABLE, FillStrategy.FILL_PARAM_IF_MISSING)
        .getDisplayablyValid()
        .getOrThrow(spec -> new WdkModelException("Default values for step analysis type '" +
            analysisName + "' on step " + _stepId + " are not displayable. Validation " +
            "details: " + spec.getValidationBundle().toString(2)));
    return StepAnalysisFormatter.getStepAnalysisTypeJsonWithParams(formSpec, formSpec.get().getValidationBundle());
  }

  /**
   * Returns form information about a single analysis type, given a set of parameter
   * values.  Any missing or invalid parameters are replaced with valid values
   * and the associated vocabularies.  Response includes parameter information,
   * including vocabularies and metadata based on the incoming values, and error
   * messages for any parameter values that were invalid. This endpoint is
   * typically used to render a revise form.  Input JSON should have
   * the following form:
   * <pre>
   * {
   *   "contextParamValues": {
   *     "<each-param-name>": String (stable value for param)
   *   }
   * }
   * </pre>
   *
   * @param analysisName name of the analysis type
   * @param body body of request (see JSON above)
   * @return response json
   * @throws WdkModelException
   *   if unable to generate param information
   */
  @POST
  @Path(NAMED_ANALYSIS_TYPE_PATH)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONObject getQuestionRevise(
      @PathParam(ANALYSIS_TYPE_PATH_PARAM) String analysisName,
      String body) throws WdkModelException, RequestMisformatException, DataValidationException {
    RunnableObj<Step> step = getRunnableStepForCurrentUser(_stepId);
    StepAnalysis stepAnalysis = getStepAnalysisFromQuestion(step.get().getAnswerSpec().getQuestion(), analysisName);
    validateAnswerValueForAnalysis(step, stepAnalysis);
    ParamValueSetRequest request = ParamValueSetRequest.parse(body, stepAnalysis);
    StepAnalysisFormSpec inputSpec = StepAnalysisFormSpec
        .builder()
        .putAll(request.getContextParamValues())
        .buildValidated(
            step,
            stepAnalysis,
            ValidationLevel.SEMANTIC,
            FillStrategy.NO_FILL);
    // save off the validation (including errors) of the spec representing the passed-in values
    ValidationBundle validation = inputSpec.getValidationBundle();
    DisplayablyValid<StepAnalysisFormSpec> displayableSpec =
        inputSpec.isValid() ?
        // input spec was already valid; populate vocabularies and return
        inputSpec.getDisplayablyValid().getLeft() :
        // need to generate a new, displayable answer spec so revise form can be shown
        StepAnalysisFormSpec.builder()
            .putAll(request.getContextParamValues())
            .buildValidated(
                step,
                stepAnalysis,
                ValidationLevel.DISPLAYABLE,
                FillStrategy.FILL_PARAM_IF_MISSING_OR_INVALID)
            .getDisplayablyValid()
            .getOrThrow(spec -> new WdkModelException("Default values for step analysis type '" +
                analysisName + "' on step " + _stepId + " are not displayable. Validation " +
                "details: " + spec.getValidationBundle().toString(2)));
    return StepAnalysisFormatter.getStepAnalysisTypeJsonWithParams(displayableSpec, validation);
  }

  /**
   * Get an updated set of vocabularies (and meta data info) for the parameters
   * that depend on the specified changed parameter. (Also validate the changed
   * parameter.)
   * <p>
   * Request must provide the parameter values of any other parameters that
   * those vocabularies depend on, as well as the changed parameter. (This
   * endpoint is typically used when a user changes a depended param.)
   * <p>
   * Sample request body:
   * <pre>
   * {
   *   "changedParam" : { "name": "height", "value": "12" },
   *   "contextParamValues" : [see /{questionUrlSegment} endpoint]
   * }
   * </pre>
   */
  @POST
  @Path(NAMED_ANALYSIS_TYPE_PATH + REFRESHED_DEPENDENT_PARAMS_EXTENSION)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray getQuestionChange(
      @PathParam(ANALYSIS_TYPE_PATH_PARAM) String analysisName,
      String body) throws WdkUserException, WdkModelException, DataValidationException {

    RunnableObj<Step> step = getRunnableStepForCurrentUser(_stepId);
    StepAnalysis stepAnalysis = getStepAnalysisFromQuestion(step.get().getAnswerSpec().getQuestion(), analysisName);
    validateAnswerValueForAnalysis(step, stepAnalysis);
    ParamValueSetRequest request = ParamValueSetRequest.parse(body, stepAnalysis);

    // find the param object for the changed param
    Entry<String,String> changedParamEntry = request.getChangedParam()
        .orElseThrow(() -> new RequestMisformatException("'changedParam' property is required at this endpoint"));
    Param changedParam = stepAnalysis.getParamMap().get(changedParamEntry.getKey());

    // set incoming values to reflect changed value
    Map<String,String> contextParams = new MapBuilder<>(request.getContextParamValues()).put(changedParamEntry).toMap();

    // Build a form spec with the passed values but replace missing/invalid
    // values with defaults.  Will remove unaffected params below.
    DisplayablyValid<StepAnalysisFormSpec> formSpec = StepAnalysisFormSpec
        .builder()
        .putAll(contextParams)
        .buildValidated(
            step,
            stepAnalysis,
            ValidationLevel.DISPLAYABLE,
            FillStrategy.FILL_PARAM_IF_MISSING_OR_INVALID)
        .getDisplayablyValid()
        .getOrThrow(spec -> new WdkModelException("Unable to produce a valid spec from incoming param values"));

    // see if changed param value changed during build; if so, then it was invalid
    if (!formSpec.get().get(changedParam.getName()).equals(changedParamEntry.getValue())) {
      // means the build process determined the incoming changed param value to
      // be invalid and changed it to the default; this is disallowed, so throw
      // TODO: figure out an elegant way to tell the user WHY the value they entered is invalid
      throw new DataValidationException("The passed changed param value '" +
          changedParamEntry.getValue() + "' is invalid.");
    }

    // get stale params of the changed value
    Set<Param> staleDependentParams = changedParam.getStaleDependentParams();

    // output JSON but tell formatter to skip non-stale params; their values
    // may have inadvertently changed (if incoming values were invalid) but the
    // client is only interested in params that depend on the changed value
    List<String> paramsToOutput = mapToList(staleDependentParams, NamedObject::getName);
    return ParamContainerFormatter.getParamsJson(formSpec,
        param -> paramsToOutput.contains(param.getName()));
  }

  /**
   * Exclusive to FilterParams.  Get a summary of filtered and unfiltered counts
   * for a specified ontology term.
   * <p>
   * Sample request body:
   * <pre>
   * {
   *   "ontologyId" : string
   *   "filters" : [ see raw value for FilterParamHandler ]
   *   "contextParamValues" : [see ParamValueSetRequest]
   * }
   * </pre>
   */
  @POST
  @Path(NAMED_ANALYSIS_TYPE_PATH + ONTOLOGY_TERM_SUMMARY_EXTENSION)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONObject getFilterParamOntologyTermSummary(
      @PathParam(ANALYSIS_TYPE_PATH_PARAM) String analysisName,
      @PathParam(FILTER_PARAM_NAME_PATH_PARAM) String paramName,
      String body)
          throws WdkModelException, DataValidationException, RequestMisformatException {

    // parse elements of the request
    RunnableObj<Step> step = getRunnableStepForCurrentUser(_stepId);
    StepAnalysis stepAnalysis = getStepAnalysisFromQuestion(step.get().getAnswerSpec().getQuestion(), analysisName);
    FilterParamNew filterParam = getFilterParam(stepAnalysis, paramName);
    Map<String, String> contextParamValues = ParamValueSetRequest.parse(body, stepAnalysis).getContextParamValues();
    JSONObject jsonBody = new JSONObject(body);
    String ontologyId = jsonBody.getString("ontologyId");

    // build a query instance spec from passed values
    SemanticallyValid<StepAnalysisFormSpec> validSpec = StepAnalysisFormSpec
        .builder()
        .putAll(contextParamValues)
        .buildValidated(
            step,
            stepAnalysis,
            ValidationLevel.SEMANTIC,
            FillStrategy.NO_FILL)
        .getSemanticallyValid()
        .getOrThrow(spec -> new DataValidationException(spec.getValidationBundle().toString()));

    // try to look up ontology term with this ID
    StepAnalysisFormSpec spec = validSpec.get();
    OntologyItem ontologyItem = filterParam.getOntology(spec.getUser(), spec.toMap()).get(ontologyId);
    if (ontologyItem == null) {
      throw new DataValidationException("Requested ontology item '" + ontologyId + "' does not exist for this parameter (" + paramName + ").");
    }

    // get term summary and format
    return ParamContainerFormatter.getOntologyTermSummaryJson(
        Functions.f0Swallow(() -> filterParam.getOntologyTermSummary(validSpec, ontologyItem,
            jsonBody, ontologyItem.getType().getJavaClass())));
  }

  /**
   * Exclusive to FilterParams.  Get a summary of filtered and unfiltered
   * counts.
   * <p>
   * Sample request body:
   * <pre>
   * {
   *   "filters" : [ see raw value for FilterParamHandler ]
   *   "contextParamValues" : [see ParamValueSetRequest]
   * }
   * </pre>
   */
  @POST
  @Path(NAMED_ANALYSIS_TYPE_PATH + SUMMARY_COUNTS_EXTENSION)
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public JSONObject getFilterParamSummaryCounts(
      @PathParam(ANALYSIS_TYPE_PATH_PARAM) String analysisName,
      @PathParam(FILTER_PARAM_NAME_PATH_PARAM) String paramName,
      String body)
          throws WdkModelException, RequestMisformatException, DataValidationException {

    // parse elements of the request
    RunnableObj<Step> step = getRunnableStepForCurrentUser(_stepId);
    StepAnalysis stepAnalysis = getStepAnalysisFromQuestion(step.get().getAnswerSpec().getQuestion(), analysisName);
    FilterParamNew filterParam = getFilterParam(stepAnalysis, paramName);
    Map<String, String> contextParamValues = ParamValueSetRequest.parse(body, stepAnalysis).getContextParamValues();
    JSONObject jsonBody = new JSONObject(body);

    // build a query instance spec from passed values
    SemanticallyValid<StepAnalysisFormSpec> validSpec = StepAnalysisFormSpec
        .builder()
        .putAll(contextParamValues)
        .buildValidated(
            step,
            stepAnalysis,
            ValidationLevel.SEMANTIC,
            FillStrategy.NO_FILL)
        .getSemanticallyValid()
        .getOrThrow(spec -> new DataValidationException(spec.getValidationBundle().toString()));

    FilterParamSummaryCounts counts = filterParam.getTotalsSummary(validSpec, jsonBody);
    return ParamContainerFormatter.getFilterParamSummaryJson(counts);
  }

  private void validateAnswerValueForAnalysis(RunnableObj<Step> step, StepAnalysis stepAnalysis)
      throws WdkModelException, DataValidationException {
    ValidationBundle stepValidation = getWdkModel().getStepAnalysisFactory().validateStep(step, stepAnalysis);
    if (!stepValidation.getStatus().isValid()) {
      throw new DataValidationException(stepValidation.toString());
    }
  }
}
