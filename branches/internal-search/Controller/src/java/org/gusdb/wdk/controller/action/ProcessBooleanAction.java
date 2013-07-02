package org.gusdb.wdk.controller.action;

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
import org.gusdb.wdk.controller.form.WizardForm;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.json.JSONException;

public class ProcessBooleanAction extends Action {

  public static final String PARAM_STRATEGY = "strategy";
  public static final String PARAM_STEP = "step";

  public static final String PARAM_BOOLEAN_OPERATOR = "boolean";
  public static final String PARAM_IMPORT_STEP = "importStep";
  public static final String PARAM_ACTION = "action";
  public static final String PARAM_FILTER = "filter";

  private static final Logger logger = Logger.getLogger(ProcessBooleanAction.class);

  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    logger.debug("Entering ProcessBooleanAction...");

    // get user;
    UserBean user = ActionUtility.getUser(servlet, request);

    String state = request.getParameter(CConstants.WDK_STATE_KEY);
    try {

      // get boolean operator
      String operator = request.getParameter(PARAM_BOOLEAN_OPERATOR);
      if (operator != null && operator.length() == 0)
        operator = null;

      StrategyBean strategy = getStrategy(request, user);
      int oldStrategyId = strategy.getStrategyId();

      // get current step
      StepBean step = null;
      String strStepId = request.getParameter(PARAM_STEP);
      if (strStepId != null && strStepId.length() > 0)
        step = strategy.getStepById(Integer.valueOf(strStepId));

      Map<Integer, Integer> stepIdsMap;
      String action = request.getParameter(PARAM_ACTION);
      if (action.equals(WizardForm.ACTION_REVISE)) {
        // revise a boolean step
        stepIdsMap = reviseBoolean(request, user, strategy, operator, step);
      } else if (action.equals(WizardForm.ACTION_INSERT)) {
        stepIdsMap = insertBoolean(request, user, strategy, operator, step);
      } else { // add a boolean step
        stepIdsMap = addBoolean(request, user, strategy, operator);
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
      System.out.println("Leaving ProcessBooleanAction...");
      return forward;
    } catch (WdkOutOfSyncException ex) {
      logger.error(ex);
      ex.printStackTrace();
      WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
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

  private StepBean getRootStep(HttpServletRequest request, StrategyBean strategy)
      throws WdkUserException, WdkModelException, SQLException, JSONException {
    // get current strategy
    String strategyKey = request.getParameter(PARAM_STRATEGY);
    if (strategyKey == null || strategyKey.length() == 0)
      throw new WdkUserException("No strategy was specified for "
          + "processing!");

    // did we get strategyId_stepId?
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

  private Map<Integer, Integer> reviseBoolean(HttpServletRequest request,
      UserBean user, StrategyBean strategy, String operator, StepBean step)
      throws NumberFormatException, WdkUserException, WdkModelException,
      NoSuchAlgorithmException, SQLException, JSONException {
    logger.debug("Revising boolean...");

    // current step has to exist for revise
    if (step == null)
      throw new WdkUserException("Required param " + PARAM_STEP
          + " is missing.");

    StepBean previousStep = step.getPreviousStep();
    boolean useBooleanFilter = step.isUseBooleanFilter();

    StepBean childStep;
    String filterName;

    // if the operator is not set, use the original one
    if (operator == null) {
      Map<String, String> paramValues = step.getParams();
      String operatorParam = BooleanQuery.OPERATOR_PARAM;
      operator = paramValues.get(operatorParam);
    }

    // check if the import step exists
    String strImport = request.getParameter(PARAM_IMPORT_STEP);
    if (!step.isCombined()) {
      // revise on the first step
      previousStep = user.getStep(Integer.valueOf(strImport));
      step = step.getNextStep();
      childStep = step.getChildStep();
      filterName = step.getFilterName();
    } else if (strImport != null && strImport.length() > 0) {
      // revise with a new child step
      childStep = user.getStep(Integer.valueOf(strImport));
      // don't get the filter from request; it is for child only.
      filterName = step.getFilterName();
    } else {
      // just revise the current boolean with a new filter or new
      // boolean operator
      childStep = step.getChildStep();

      // get filter from request, if having any
      String fName = request.getParameter(PARAM_FILTER);
      filterName = (fName != null && fName.length() > 0) ? fName
          : step.getFilterName();
    }
    logger.debug("previous step: " + previousStep + ", child step: "
        + childStep + ", boolean: " + operator);
    StepBean newStep = user.createBooleanStep(previousStep, childStep,
        operator, useBooleanFilter, filterName);
    // the new step is to replace the current one.
    return strategy.editOrInsertStep(step.getStepId(), newStep);
  }

  private Map<Integer, Integer> insertBoolean(HttpServletRequest request,
      UserBean user, StrategyBean strategy, String operator, StepBean step)
      throws WdkUserException, WdkModelException, NoSuchAlgorithmException,
      SQLException, JSONException {
    logger.debug("Inserting boolean...");

    // current step has to exist for insert
    if (step == null)
      throw new WdkUserException("Required param " + PARAM_STEP
          + " is missing.");

    // the importStep has to exist for insert
    String strImport = request.getParameter(PARAM_IMPORT_STEP);
    if (strImport == null || strImport.length() == 0)
      throw new WdkUserException("The required param " + PARAM_IMPORT_STEP
          + " is missing.");

    if (operator == null)
      throw new WdkUserException("The required param " + PARAM_BOOLEAN_OPERATOR
          + " is missing.");

    StepBean childStep = user.getStep(Integer.valueOf(strImport));
    StepBean previousStep;
    StepBean targetStep;
    if (step.isCombined()) { // not the first step
      previousStep = step.getPreviousStep();
      targetStep = previousStep;
    } else { // the first step is not a combined step
      previousStep = childStep;
      childStep = step;
      targetStep = step;
    }

    // use the default flags to create the new boolean to be inserted
    StepBean newStep = user.createBooleanStep(previousStep, childStep,
        operator, false, null);

    // now set back the next step of the previous step. If we don't do this,
    // the next step will be changed to the new boolean, which is not a part
    // of the strategy yet.
    if (step.isCombined())
      targetStep.setNextStep(step);

    // the new step is to replace the previous step of the current one
    return strategy.editOrInsertStep(targetStep.getStepId(), newStep);
  }

  private Map<Integer, Integer> addBoolean(HttpServletRequest request,
      UserBean user, StrategyBean strategy, String operator)
      throws WdkUserException, NumberFormatException, WdkModelException,
      NoSuchAlgorithmException, SQLException, JSONException {
    logger.debug("Adding boolean...");

    // get root step
    StepBean rootStep = getRootStep(request, strategy);

    // the importStep has to exist for insert
    String strImport = request.getParameter(PARAM_IMPORT_STEP);
    if (strImport == null || strImport.length() == 0)
      throw new WdkUserException("The required param " + PARAM_IMPORT_STEP
          + " is missing.");

    if (operator == null)
      throw new WdkUserException(
          "You must specify how to combine your searches.");
    // + PARAM_BOOLEAN_OPERATOR + " is missing.");

    StepBean previousStep = rootStep;
    StepBean childStep = user.getStep(Integer.valueOf(strImport));

    // use the default flags
    StepBean newStep = user.createBooleanStep(previousStep, childStep,
        operator, false, null);

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
}
