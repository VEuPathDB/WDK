package org.gusdb.wdk.controller.action;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.WdkOutOfSyncException;
import org.gusdb.wdk.controller.actionutil.ActionUtility;
import org.gusdb.wdk.controller.form.QuestionForm;
import org.gusdb.wdk.controller.form.WizardForm;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerParamBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * Process regular or transform questions in the context of Wizard. any other types of combined steps won't be
 * using this action.
 * 
 * @author jerric
 *
 */
public class ProcessStepAction extends Action {

  public static final String PARAM_STRATEGY = "strategy";
  public static final String PARAM_STEP = "step";

  public static final String PARAM_QUESTION = "questionFullName";
  public static final String PARAM_ACTION = "action";
  public static final String PARAM_FILTER = "filter";
  public static final String PARAM_CUSTOM_NAME = "customName";

  private static final Logger logger = Logger.getLogger(ProcessStepAction.class);

  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    logger.debug("Entering ProcessStepAction...");

    // get user & model
    UserBean user = ActionUtility.getUser(servlet, request);
    WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);

    String state = request.getParameter(CConstants.WDK_STATE_KEY);
    try {
      // get current strategy
      String strategyKey = request.getParameter(PARAM_STRATEGY);
      if (strategyKey == null || strategyKey.length() == 0)
        throw new WdkUserException("No strategy was specified for " + "processing!");

      // Handle nested strategies, which have an id of the form {id}_{id}
      // did we get strategyId_stepId?
      int pos = strategyKey.indexOf("_");
      int branchId = 0;
      if (pos >= 0) {
        branchId = Integer.valueOf(strategyKey.substring(pos + 1));
        strategyKey = strategyKey.substring(0, pos);
      }
      int oldStrategyId = Integer.valueOf(strategyKey);

      // get strategy, and verify the checksum
      StrategyBean strategy = user.getStrategy(oldStrategyId);
      String checksum = request.getParameter(CConstants.WDK_STRATEGY_CHECKSUM_KEY);
      if (checksum != null && !strategy.getChecksum().equals(checksum))
        throw new WdkOutOfSyncException("strategy checksum: " + strategy.getChecksum() +
            ", but the input checksum: " + checksum);

      int stepId = 0;
      String strStepId = request.getParameter(PARAM_STEP);
      if (strStepId != null && strStepId.length() > 0)
        stepId = Integer.valueOf(strStepId);

      // cannot change step in saved strategy, will need to make a clone first
      Map<Integer, Integer> stepIdMap = new HashMap<>();
      if (strategy.getIsSaved()) {
        strategy = user.copyStrategy(strategy, stepIdMap, strategy.getName());
        // map the old step id to the new one
        stepId = stepIdMap.get(stepId);
      }

      // get current step
      StepBean step = (stepId == 0) ? null : strategy.getStepById(stepId);

      // load custom name
      String customName = request.getParameter(PARAM_CUSTOM_NAME);
      if (customName != null && customName.trim().length() == 0)
        customName = null;
      logger.debug(PARAM_CUSTOM_NAME + "='" + customName + "'");

      QuestionForm questionForm = (QuestionForm) form;

      Map<Integer, Integer> rootMap;
      String action = request.getParameter(PARAM_ACTION);
      if (action.equals(WizardForm.ACTION_REVISE)) {
        // revise the given step with the question & params information.
        reviseStep(request, questionForm, wdkModel, user, strategy, step, customName);
        // FIXME This should hold the current step id
        rootMap = stepIdMap;
      }
      else if (action.equals(WizardForm.ACTION_INSERT)) {
        // insert a step.
        rootMap = insertStep(request, questionForm, wdkModel, user, strategy, step, customName);
      }
      else { // add a boolean step
        rootMap = addStep(request, questionForm, wdkModel, user, strategy, step, customName, branchId);
      }

      // the strategy id might change due to editting on saved strategies.
      // New unsaved strategy is created.
      user.replaceActiveStrategy(oldStrategyId, strategy.getStrategyId(), rootMap);

      ActionForward showStrategy = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);
      StringBuffer url = new StringBuffer(showStrategy.getPath());
      url.append("?state=" + URLEncoder.encode(state, "UTF-8"));

      ActionForward forward = new ActionForward(url.toString());
      forward.setRedirect(true);
      System.out.println("Leaving ProcessStepAction...");
      return forward;
    }
    catch (WdkOutOfSyncException ex) {
      logger.error(ex);
      ex.printStackTrace();
      ShowStrategyAction.outputOutOfSyncJSON(wdkModel, user, response, state);
      return null;
    }
    catch (Exception ex) {
      logger.error(ex);
      ex.printStackTrace();
      ShowStrategyAction.outputErrorJSON(user, response, ex);
      return null;
    }
  }

  private static void reviseStep(HttpServletRequest request, QuestionForm form, WdkModelBean wdkModel,
      UserBean user, StrategyBean strategy, StepBean step, String customName) throws NumberFormatException,
      WdkUserException, WdkModelException {
    logger.debug("Revising step...");

    // current step has to exist for revise
    if (step == null)
      throw new WdkUserException("Required param " + PARAM_STEP + " is missing.");

    // XXX This is a no-op since we clone the strategy above in execute().
    // before changing step, need to check if strategy is saved, if yes, make a copy.
    if (strategy.getIsSaved())
      strategy.update(false);

    // check if the question name exists
    String questionName = request.getParameter(PARAM_QUESTION);
    String filterName = request.getParameter(PARAM_FILTER);
    if (questionName != null && questionName.length() > 0) {
      // revise a step with a new question
      Map<String, String> params = ProcessQuestionAction.prepareParams(user, request, form);
      mapPreviousAndChildStepIds(step, params);
      step.setQuestionName(questionName);
      step.setParamValues(params);
    }
    else if (filterName != null && filterName.length() > 0) {
      // just revise the current step with a new filter
      step.setFilterName(filterName);
    }
    else { // missing required params
      throw new WdkUserException("Required parameter " + PARAM_QUESTION + " or " + PARAM_FILTER +
          " is missing");
    }
    // Update customName
    step.setCustomName(customName);
    step.update(false);
    step.saveParamFilters();
  }

  private static Map<Integer, Integer> insertStep(HttpServletRequest request, QuestionForm form,
      WdkModelBean wdkModel, UserBean user, StrategyBean strategy, StepBean step, String customName)
      throws WdkUserException, WdkModelException {
    logger.debug("Inserting step...");

    // current step has to exist for insert
    if (step == null)
      throw new WdkUserException("Required param " + PARAM_STEP + " is missing.");

    // the question name has to exist
    String questionName = request.getParameter(PARAM_QUESTION);
    if (questionName == null || questionName.length() == 0)
      throw new WdkUserException("Required param " + PARAM_QUESTION + " is missing.");

    StepBean previousStep = step.getPreviousStep();
    QuestionBean question = wdkModel.getQuestion(questionName);
    Map<String, String> newStepParams = ProcessQuestionAction.prepareParams(user, request, form);
    
    // If the step we are inserting before has a previous step, then we need to
    // map the step id in the request to refer to this new step id.
    if (previousStep != null) {
      Map<String, ParamBean<?>> questionParamsMap = question.getParamsMap();
      // Map the first AnswerParam value in requestParams to the id of step. The
      // assumption is that the first AnswerParam is the previous step.
      for (Map.Entry<String, ParamBean<?>> entry : questionParamsMap.entrySet()) {
        if (entry.getValue() instanceof AnswerParamBean) {
          newStepParams.put(entry.getKey(), Integer.toString(previousStep.getStepId()));
          break;
        }
      }
    }



    // get the weight, or use the current step's.
    Integer weight = getWeight(request);
    if (weight == null)
      weight = Utilities.DEFAULT_WEIGHT;

    StepBean newStep = user.createStep(strategy.getStrategyId(), question, newStepParams, null, false, true, weight);
    if (customName != null) {
      newStep.setCustomName(customName);
      newStep.update(false);
    }

    if (previousStep == null) {
      // insert before the first step, need to create a new step to
      // replace the current one.
      Map<String, String> paramValues = step.getParams();
      String previousParam = step.getPreviousStepParam();
      String previousStepId = Integer.toString(newStep.getStepId());
      paramValues.put(previousParam, previousStepId);

      question = step.getQuestion();
      String filterName = step.getFilterName();
      weight = step.getAssignedWeight();

      StepBean newParent = user.createStep(strategy.getStrategyId(), question, newStepParams, filterName, false,
          true, weight);

      // then replace the current step with the newParent
      return strategy.insertStepBefore(newParent, step.getStepId());
    }
    else { // insert on any other steps
      // the new step is to replace the previous step of the current one
      return strategy.insertStepBefore(newStep, step.getStepId());
    }
  }

  private static Map<Integer, Integer> addStep(HttpServletRequest request, QuestionForm form, WdkModelBean wdkModel,
      UserBean user, StrategyBean strategy, StepBean previousStep, String customName, int branchId)
      throws WdkUserException, NumberFormatException, WdkModelException {
    logger.debug("Adding step...");

    // get root step
    if (previousStep == null)
      previousStep = (branchId == 0) ? strategy.getLatestStep() : strategy.getStepById(branchId);

    // the question name has to exist
    String questionName = request.getParameter(PARAM_QUESTION);
    if (questionName == null || questionName.length() == 0)
      throw new WdkUserException("Required param " + PARAM_QUESTION + " is missing.");

    QuestionBean question = wdkModel.getQuestion(questionName);
    Map<String, ParamBean<?>> questionParamsMap = question.getParamsMap();
    Map<String, String> newStepParams = ProcessQuestionAction.prepareParams(user, request, form);

    // Map the first AnswerParam value in newStepParams to the id of step. The
    // assumption is that the first AnswerParam is the previous step.
    for (Map.Entry<String, ParamBean<?>> entry : questionParamsMap.entrySet()) {
      if (entry.getValue() instanceof AnswerParamBean) {
        newStepParams.put(entry.getKey(), Integer.toString(previousStep.getStepId()));
        break;
      }
    }

    // get the weight, or use the current step's.
    Integer weight = getWeight(request);
    if (weight == null)
      weight = Utilities.DEFAULT_WEIGHT;

    StepBean newStep = user.createStep(strategy.getStrategyId(), question, newStepParams, null, false, true, weight);
    if (customName != null) {
      newStep.setCustomName(customName);
      newStep.update(false);
    }

    logger.debug("Insert Afte step: " + previousStep);
    return strategy.insertStepAfter(newStep, previousStep.getStepId());
  }

  private static Integer getWeight(HttpServletRequest request) throws WdkUserException {
    // get the assigned weight
    String strWeight = request.getParameter(CConstants.WDK_ASSIGNED_WEIGHT_KEY);
    boolean hasWeight = (strWeight != null && strWeight.length() > 0);
    Integer weight = null;
    if (hasWeight) {
      if (!strWeight.matches("[\\-\\+]?\\d+"))
        throw new WdkUserException("Invalid weight value: '" + strWeight +
            "'. Only integer numbers are allowed.");
      if (strWeight.length() > 9)
        throw new WdkUserException("Weight number is too big: " + strWeight);
      weight = Integer.parseInt(strWeight);
    }
    return weight;
  }

  // Map old step ids to new step ids in the params map. This is needed in the case of operating
  // on a saved strategy, since we make a deep clone and the params may refer
  // to steps on the saved strategy rather than the new, unsaved strategy.
  private static void mapPreviousAndChildStepIds(StepBean step, Map<String, String> params)
      throws WdkModelException {
    String previousStepParamName = step.getPreviousStepParam();
    String childStepParamName = step.getChildStepParam();
    if (params.containsKey(previousStepParamName)) {
      Integer newStepId = step.getPreviousStep().getStepId();
      logger.debug("updating previous step '" + previousStepParamName
          + "' id: " + newStepId);
      if (newStepId != null) {
        params.put(previousStepParamName, newStepId.toString());
      }
    }
    if (params.containsKey(childStepParamName)) {
      Integer newStepId = step.getChildStep().getStepId();
      logger.debug("updating child step id: " + newStepId);
      if (newStepId != null) {
        params.put(childStepParamName, newStepId.toString());
      }
    }
  }
}
