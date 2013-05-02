package org.gusdb.wdk.controller.action;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
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
import org.json.JSONException;

public class ProcessStepAction extends Action {

  public static final String PARAM_STRATEGY = "strategy";
  public static final String PARAM_STEP = "step";

  public static final String PARAM_QUESTION = "questionFullName";
  public static final String PARAM_ACTION = "action";
  public static final String PARAM_FILTER = "filter";
  public static final String PARAM_CUSTOM_NAME = "customName";

  private static final Logger logger = Logger.getLogger(ProcessStepAction.class);

  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws Exception {
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

      Map<Integer, Integer> stepIdsMap;
      String action = request.getParameter(PARAM_ACTION);
      if (action.equals(WizardForm.ACTION_REVISE)) {
        // revise a boolean step
        stepIdsMap = reviseStep(request, questionForm, wdkModel, user,
            strategy, step, customName);
      } else if (action.equals(WizardForm.ACTION_INSERT)) {
        stepIdsMap = insertStep(request, questionForm, wdkModel, user,
            strategy, step, customName);
      } else { // add a boolean step
        stepIdsMap = addStep(request, questionForm, wdkModel, user, strategy,
            customName);
      }

      // the strategy id might change due to editting on saved strategies.
      // New unsaved strategy is created.
      user.replaceActiveStrategy(oldStrategyId, strategy.getStrategyId(),
          stepIdsMap);

      ActionForward showStrategy = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);
      StringBuffer url = new StringBuffer(showStrategy.getPath());
      url.append("?state=" + URLEncoder.encode(state, "UTF-8"));

      ActionForward forward = new ActionForward(url.toString());
      forward.setRedirect(true);
      System.out.println("Leaving ProcessStepAction...");
      return forward;
    } catch (WdkOutOfSyncException ex) {
      logger.error(ex);
      ex.printStackTrace();
      ShowStrategyAction.outputOutOfSyncJSON(wdkModel, user, response, state);
      return null;
    } catch (Exception ex) {
      logger.error(ex);
      ex.printStackTrace();
      ShowStrategyAction.outputErrorJSON(user, response, ex);
      return null;
    }
  }

  private StrategyBean getStrategy(HttpServletRequest request, UserBean user)
      throws NoSuchAlgorithmException, WdkModelException, WdkUserException,
      JSONException, SQLException {

    // get current strategy
    String strategyKey = request.getParameter(PARAM_STRATEGY);
    if (strategyKey == null || strategyKey.length() == 0)
      throw new WdkUserException("No strategy was specified for "
          + "processing!");

    // did we get strategyId_stepId?
    int pos = strategyKey.indexOf("_");
    String strStratId = (pos > 0) ? strategyKey.substring(0, pos) : strategyKey;

    // get strategy, and verify the checksum
    StrategyBean strategy = user.getStrategy(Integer.parseInt(strStratId));
    String checksum = request.getParameter(CConstants.WDK_STRATEGY_CHECKSUM_KEY);
    if (checksum != null && !strategy.getChecksum().equals(checksum))
      throw new WdkOutOfSyncException("strategy checksum: "
          + strategy.getChecksum() + ", but the input checksum: " + checksum);

    return strategy;
  }

  private StepBean getRootStep(HttpServletRequest request, UserBean user,
      StrategyBean strategy) throws WdkUserException, WdkModelException,
      SQLException, JSONException {
    String strategyKey = request.getParameter(PARAM_STRATEGY);
    if (strategyKey == null || strategyKey.length() == 0)
      throw new WdkUserException("No strategy was specified for "
          + "processing!");
    int pos = strategyKey.indexOf("_");

    // load branch root, if exists
    StepBean rootStep;
    if (pos > 0) {
      int branchRootId = Integer.valueOf(strategyKey.substring(pos + 1));
      rootStep = strategy.getStepById(branchRootId);
    } else {
      rootStep = strategy.getLatestStep();
    }

    return rootStep;
  }

  private Map<Integer, Integer> reviseStep(HttpServletRequest request,
      QuestionForm form, WdkModelBean wdkModel, UserBean user,
      StrategyBean strategy, StepBean step, String customName)
      throws NumberFormatException, WdkUserException, WdkModelException,
      NoSuchAlgorithmException, SQLException, JSONException,
      FileNotFoundException, IOException {
    logger.debug("Revising step...");

    // current step has to exist for revise
    if (step == null)
      throw new WdkUserException("Required param " + PARAM_STEP
          + " is missing.");

    StepBean newStep;

    // get the weight, or use the current step's.
    Integer weight = getWeight(request);
    if (weight == null)
      weight = step.getAssignedWeight();

    // check if the question name exists
    String questionName = request.getParameter(PARAM_QUESTION);
    if (questionName != null && questionName.length() > 0) {
      // revise a step with a new question
      QuestionBean question = wdkModel.getQuestion(questionName);
      Map<String, String> params = ProcessQuestionAction.prepareParams(user,
          request, form);
      // reuse the filter of the current step
      // but only if it's valid for the revised recordclass
      String filterName = step.getFilterName();
      if (!question.getRecordClass().getFilterMap().containsKey(filterName)) {
        filterName = null;
      }

      newStep = user.createStep(question, params, filterName, false, true,
          weight);
    } else {
      // just revise the current step with a new filter or new weight

      // get filter from request, if having any
      String filterName = request.getParameter(PARAM_FILTER);
      if (filterName == null || filterName.length() == 0)
        filterName = step.getFilterName();

      newStep = step.createStep(filterName, weight);
    }

    // set custom name to the new step
    if (customName != null) {
      newStep.setCustomName(customName);
      newStep.update(false);
    }

    // the new step is to replace the current one.
    Map<Integer, Integer> changeMap = strategy.editOrInsertStep(
        step.getStepId(), newStep);
    return changeMap;
  }

  private Map<Integer, Integer> insertStep(HttpServletRequest request,
      QuestionForm form, WdkModelBean wdkModel, UserBean user,
      StrategyBean strategy, StepBean step, String customName)
      throws WdkUserException, WdkModelException, NoSuchAlgorithmException,
      SQLException, JSONException, FileNotFoundException, IOException {
    logger.debug("Inserting step...");

    // current step has to exist for insert
    if (step == null)
      throw new WdkUserException("Required param " + PARAM_STEP
          + " is missing.");

    // the question name has to exist
    String questionName = request.getParameter(PARAM_QUESTION);
    if (questionName == null || questionName.length() == 0)
      throw new WdkUserException("Required param " + PARAM_QUESTION
          + " is missing.");

    QuestionBean question = wdkModel.getQuestion(questionName);
    Map<String, String> params = ProcessQuestionAction.prepareParams(user,
        request, form);

    // get the weight, or use the current step's.
    Integer weight = getWeight(request);
    if (weight == null)
      weight = Utilities.DEFAULT_WEIGHT;

    StepBean newStep = user.createStep(question, params, null, false, true,
        weight);
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

      StepBean newParent = user.createStep(question, params, filterName, false,
          true, weight);

      // then replace the current step with the newParent
      return strategy.editOrInsertStep(step.getStepId(), newParent);
    } else {
      // the new step is to replace the previous step of the current one

      // need to recover the link to the original current step
      previousStep.setNextStep(step);

      return strategy.editOrInsertStep(previousStep.getStepId(), newStep);
    }
  }

  private Map<Integer, Integer> addStep(HttpServletRequest request,
      QuestionForm form, WdkModelBean wdkModel, UserBean user,
      StrategyBean strategy, String customName) throws WdkUserException,
      NumberFormatException, WdkModelException, NoSuchAlgorithmException,
      SQLException, JSONException, FileNotFoundException, IOException {
    logger.debug("Adding step...");

    // get root step
    StepBean rootStep = getRootStep(request, user, strategy);

    // the question name has to exist
    String questionName = request.getParameter(PARAM_QUESTION);
    if (questionName == null || questionName.length() == 0)
      throw new WdkUserException("Required param " + PARAM_QUESTION
          + " is missing.");

    QuestionBean question = wdkModel.getQuestion(questionName);
    Map<String, String> params = ProcessQuestionAction.prepareParams(user,
        request, form);

    // get the weight, or use the current step's.
    Integer weight = getWeight(request);
    if (weight == null)
      weight = Utilities.DEFAULT_WEIGHT;

    StepBean newStep = user.createStep(question, params, null, false, true,
        weight);
    if (customName != null) {
      newStep.setCustomName(customName);
      newStep.update(false);
    }

    logger.debug("root step: " + rootStep);
    if (rootStep.getStepId() != strategy.getLatestStepId()) {
      // add on a branch, it is equivalent to a insert
      newStep.setIsCollapsible(true);
      newStep.setCollapsedName(rootStep.getCollapsedName());

      // the new Step is to replace the current branch root
      return strategy.editOrInsertStep(rootStep.getStepId(), newStep);
    } else {
      // add on top level, append the step to the end of the strategy.
      return strategy.addStep(rootStep.getStepId(), newStep);
    }
  }

  private Integer getWeight(HttpServletRequest request) throws WdkUserException {
    // get the assigned weight
    String strWeight = request.getParameter(CConstants.WDK_ASSIGNED_WEIGHT_KEY);
    boolean hasWeight = (strWeight != null && strWeight.length() > 0);
    Integer weight = null;
    if (hasWeight) {
      if (!strWeight.matches("[\\-\\+]?\\d+"))
        throw new WdkUserException("Invalid weight value: '" + strWeight
            + "'. Only integer numbers are allowed.");
      if (strWeight.length() > 9)
        throw new WdkUserException("Weight number is too big: " + strWeight);
      weight = Integer.parseInt(strWeight);
    }
    return weight;
  }
}
