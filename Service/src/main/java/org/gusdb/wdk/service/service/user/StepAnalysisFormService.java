package org.gusdb.wdk.service.service.user;

import static org.gusdb.wdk.service.service.user.StepService.STEP_ID_PATH_PARAM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.gusdb.fgputil.db.platform.PostgreSQL;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.service.formatter.StepAnalysisFormatter;
import org.gusdb.wdk.service.request.exception.DataValidationException;

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
  public String getStepAnalysisTypes() throws WdkModelException, DataValidationException {
    RunnableObj<Step> step = getRunnableStepForCurrentUser(_stepId);
    Map<String, StepAnalysis> stepAnalyses = step.get().getAnswerSpec().getQuestion().getStepAnalyses();
    return StepAnalysisFormatter.getStepAnalysisTypesJson(stepAnalyses).toString();
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
  public String getStepAnalysisTypeDataFromName(@PathParam(ANALYSIS_TYPE_PATH_PARAM) String analysisName)
      throws WdkModelException, DataValidationException {

    RunnableObj<Step> step = getRunnableStepForCurrentUser(_stepId);
    StepAnalysis analysis = getStepAnalysisFromQuestion(step.get().getAnswerSpec().getQuestion(), analysisName);
    Map<String, Param> paramMap = analysis.getParamMap();
    Map<String,String> context = new HashMap<>();

    // TODO: this is a hack.  We could fix it by introducing a dedicated
    // param type called something like <stepAnalysisIdSqlParam> that would
    // have no attributes, and be dedicated to this need.
    if (paramMap.containsKey("answerIdSql")) {
      if (context.isEmpty()) {
        context = new HashMap<>();
      }
      context.put("answerIdSql", AnswerValueFactory.makeAnswer(step).getIdSql());
    }

    // TODO: also a hack; PostgreSQL only
    // VALUES list is a SQL construct that creates a temporary table
    // this case, with two fields, one for the param name, one for the param value
    // allowing stepAnalysis parameters to be depended on step parameter values
    if (paramMap.containsKey("stepParamValuesSql")) {
      if (getWdkModel().getAppDb().getPlatform() instanceof PostgreSQL) {
        if (context.isEmpty()) {
          context = new HashMap<>();
        }
        ArrayList<String> values = new ArrayList<String>();
        for (Entry<String, String> param : step.get().getAnswerSpec().getQueryInstanceSpec().entrySet()) {
          String row = "('" + param.getKey() + "', '" + param.getValue() + "')";
          values.add(row);
        }
        context.put("stepParamValuesSql", "SELECT * FROM ( VALUES " + String.join(",", values) + " ) AS p (name, value)");
      } else {
        throw new WdkModelException("Invalid step analysis parameter: stepParamValuesSql only valid for PostgreSQL.");
      }
    }

    /* FIXME: currently broken; need to do some work still to integrate step analysis param containers
    return QuestionFormatter.getParamsJson(
        paramMap.values(),
        true,
        user,
        context).toString();*/

    return "";
  }

}
