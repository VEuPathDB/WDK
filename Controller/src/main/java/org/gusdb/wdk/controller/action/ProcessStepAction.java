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

      StrategyBean strategy = getStrategy(request, user);
      int oldStrategyId = strategy.getStrategyId();

      // get current step
      StepBean step = null;
      String strStepId = request.getParameter(PARAM_STEP);
      if (strStepId != null && strStepId.length() > 0)
        step = strategy.getStepById(Integer.valueOf(strStepId));

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
        rootMap = new HashMap<>();
      }
      else if (action.equals(WizardForm.ACTION_INSERT)) {
        // insert a step.
        rootMap = insertStep(request, questionForm, wdkModel, user, strategy, step, customName);
      }
      else { // add a boolean step
        rootMap = addStep(request, questionForm, wdkModel, user, strategy, step, customName);
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

  private StrategyBean getStrategy(HttpServletRequest request, UserBean user) throws WdkModelException,
      WdkUserException {

    // get current strategy
    String strategyKey = request.getParameter(PARAM_STRATEGY);
    if (strategyKey == null || strategyKey.length() == 0)
      throw new WdkUserException("No strategy was specified for " + "processing!");

    // did we get strategyId_stepId?
    int pos = strategyKey.indexOf("_");
    String strStratId = (pos > 0) ? strategyKey.substring(0, pos) : strategyKey;

    // get strategy, and verify the checksum
    StrategyBean strategy = user.getStrategy(Integer.parseInt(strStratId));
    String checksum = request.getParameter(CConstants.WDK_STRATEGY_CHECKSUM_KEY);
    if (checksum != null && !strategy.getChecksum().equals(checksum))
      throw new WdkOutOfSyncException("strategy checksum: " + strategy.getChecksum() +
          ", but the input checksum: " + checksum);

    return strategy;
  }

  private StepBean getRootStep(HttpServletRequest request, UserBean user, StrategyBean strategy)
      throws WdkUserException, WdkModelException {
    String strategyKey = request.getParameter(PARAM_STRATEGY);
    if (strategyKey == null || strategyKey.length() == 0)
      throw new WdkUserException("No strategy was specified for " + "processing!");
    int pos = strategyKey.indexOf("_");

    // load branch root, if exists
    StepBean rootStep;
    if (pos > 0) {
      int branchRootId = Integer.valueOf(strategyKey.substring(pos + 1));
      rootStep = strategy.getStepById(branchRootId);
    }
    else {
      rootStep = strategy.getLatestStep();
    }

    return rootStep;
  }

  private void reviseStep(HttpServletRequest request, QuestionForm form, WdkModelBean wdkModel,
      UserBean user, StrategyBean strategy, StepBean step, String customName) throws NumberFormatException,
      WdkUserException, WdkModelException {
    logger.debug("Revising step...");

    // current step has to exist for revise
    if (step == null)
      throw new WdkUserException("Required param " + PARAM_STEP + " is missing.");

    // before changing step, need to check if strategy is saved, if yes, make a copy.
      if (strategy.getIsSaved())
        strategy.update(false);

    // check if the question name exists
    String questionName = request.getParameter(PARAM_QUESTION);
    String filterName = request.getParameter(PARAM_FILTER);
    if (questionName != null && questionName.length() > 0) {
      // revise a step with a new question
      Map<String, String> params = ProcessQuestionAction.prepareParams(user, request, form);
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
    step.saveParamFilters();
  }

  private Map<Integer, Integer> insertStep(HttpServletRequest request, QuestionForm form,
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

    QuestionBean question = wdkModel.getQuestion(questionName);
    Map<String, String> params = ProcessQuestionAction.prepareParams(user, request, form);

    // get the weight, or use the current step's.
    Integer weight = getWeight(request);
    if (weight == null)
      weight = Utilities.DEFAULT_WEIGHT;

    StepBean newStep = user.createStep(strategy.getStrategyId(), question, params, null, false, true, weight);
    if (customName != null) {
      newStep.setCustomName(customName);
      newStep.update(false);
    }

    StepBean previousStep = step.getPreviousStep();
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

      StepBean newParent = user.createStep(strategy.getStrategyId(), question, params, filterName, false,
          true, weight);

      // then replace the current step with the newParent
      return strategy.insertStepBefore(newParent, step.getStepId());
    }
    else { // insert on any other steps
      // the new step is to replace the previous step of the current one
      return strategy.insertStepBefore(newStep, step.getStepId());
    }
  }

  private Map<Integer, Integer> addStep(HttpServletRequest request, QuestionForm form, WdkModelBean wdkModel,
      UserBean user, StrategyBean strategy, StepBean step, String customName) throws WdkUserException,
      NumberFormatException, WdkModelException {
    logger.debug("Adding step...");

    // get root step
    if (step == null)
      step = getRootStep(request, user, strategy);

    // the question name has to exist
    String questionName = request.getParameter(PARAM_QUESTION);
    if (questionName == null || questionName.length() == 0)
      throw new WdkUserException("Required param " + PARAM_QUESTION + " is missing.");

    QuestionBean question = wdkModel.getQuestion(questionName);
    Map<String, String> params = ProcessQuestionAction.prepareParams(user, request, form);

    // get the weight, or use the current step's.
    Integer weight = getWeight(request);
    if (weight == null)
      weight = Utilities.DEFAULT_WEIGHT;

    StepBean newStep = user.createStep(strategy.getStrategyId(), question, params, null, false, true, weight);
    if (customName != null) {
      newStep.setCustomName(customName);
      newStep.update(false);
    }

    logger.debug("Insert Afte step: " + step);
    return strategy.insertStepAfter(newStep, step.getStepId());
  }

  private Integer getWeight(HttpServletRequest request) throws WdkUserException {
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
}
