package org.gusdb.wdk.controller.action;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.fgputil.FormatUtil;
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
import org.gusdb.wdk.model.user.StepUtilities;

public class ProcessBooleanAction extends Action {

  public static final String PARAM_STRATEGY = "strategy";
  public static final String PARAM_STEP = "step";

  public static final String PARAM_BOOLEAN_OPERATOR = "boolean";
  public static final String PARAM_IMPORT_STEP = "importStep";
  public static final String PARAM_ACTION = "action";
  public static final String PARAM_FILTER = "filter";

  private static final Logger logger = Logger.getLogger(ProcessBooleanAction.class);

  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    logger.debug("Entering ProcessBooleanAction...");

    // get user;
    UserBean user = ActionUtility.getUser(request);

    String state = request.getParameter(CConstants.WDK_STATE_KEY);
    try {

      // get boolean operator
      String operator = request.getParameter(PARAM_BOOLEAN_OPERATOR);
      if (operator != null && operator.length() == 0)
        operator = null;

      // get current strategy
      String strategyKey = request.getParameter(PARAM_STRATEGY);
      if (strategyKey == null || strategyKey.length() == 0)
        throw new WdkUserException("No strategy was specified for " + "processing!");

      // did we get strategyId_stepId?
      int pos = strategyKey.indexOf("_");
      long branchId = 0;
      if (pos > 0) {
        branchId = Long.valueOf(strategyKey.substring(pos + 1));
        strategyKey = strategyKey.substring(0, pos);
      }
      long oldStrategyId = Long.valueOf(strategyKey); 

      // get strategy, and verify the checksum
      StrategyBean strategy = new StrategyBean(user, StepUtilities.getStrategy(user.getUser(), oldStrategyId));
      String checksum = request.getParameter(CConstants.WDK_STRATEGY_CHECKSUM_KEY);
      if (checksum != null && !strategy.getChecksum().equals(checksum))
        throw new WdkOutOfSyncException("strategy checksum: " + strategy.getChecksum() +
            ", but the input checksum: " + checksum);

      String strStepId = request.getParameter(PARAM_STEP);
      if (strStepId == null || strStepId.isEmpty())
        throw new WdkUserException("The required param '" + PARAM_STEP + "' is missing.");
      long stepId = Long.valueOf(strStepId);
      
      // cannot change step on saved strategy, will need to make a clone first
      if (strategy.getIsSaved()) {
        Map<Long, Long> stepIdMap = new HashMap<>();
        strategy = user.copyStrategy(strategy, stepIdMap, strategy.getName());
        // map the old step id to the new one
        stepId = stepIdMap.get(stepId);
        if (branchId != 0) branchId = stepIdMap.get(branchId);
      }

      // get current step
      StepBean step = strategy.getStepById(stepId);

      Map<Long, Long> rootMap;
      String action = request.getParameter(PARAM_ACTION);
      if (action.equals(WizardForm.ACTION_REVISE)) {
        // revise a boolean step
        reviseBoolean(operator, step);
        rootMap = new HashMap<>();
      }
      else if (action.equals(WizardForm.ACTION_INSERT)) {
        rootMap = insertBoolean(request, user, strategy, operator, step);
      }
      else { // add a boolean step
        rootMap = addBoolean(request, user, strategy, operator, branchId);
      }

      // the strategy id might change due to editting on saved strategies.
      // New unsaved strategy is created.
      user.getUser().getSession().replaceActiveStrategy(oldStrategyId, strategy.getStrategyId(), rootMap);

      ActionForward showStrategy = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);
      StringBuffer url = new StringBuffer(showStrategy.getPath());
      url.append("?state=" + FormatUtil.urlEncodeUtf8(state));

      ActionForward forward = new ActionForward(url.toString());
      forward.setRedirect(true);
      System.out.println("Leaving ProcessBooleanAction...");
      return forward;
    }
    catch (WdkOutOfSyncException ex) {
      logger.error(ex);
      ex.printStackTrace();
      WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
      ShowStrategyAction.outputOutOfSyncJSON(wdkModel, user, response, state);
      return null;
    }
    catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      ShowStrategyAction.outputErrorJSON(user, response, ex);
      throw ex;
    }
  }

  private void reviseBoolean(String operator, StepBean step) throws NumberFormatException, WdkUserException, WdkModelException {
    logger.debug("Revising boolean...");

    // current step has to exist for revise
    if (step == null)
      throw new WdkUserException("Required param " + PARAM_STEP + " is missing.");
    if (operator == null)
      throw new WdkUserException("Required param " + PARAM_BOOLEAN_OPERATOR + " is missing.");

    step.setParamValue(BooleanQuery.OPERATOR_PARAM, operator);
    step.saveParamFilters();

    logger.debug("Revise step: " + step.getStepId() + ", boolean: " + operator);
  }

  private Map<Long, Long> insertBoolean(HttpServletRequest request, UserBean user,
      StrategyBean strategy, String operator, StepBean step) throws WdkUserException, WdkModelException {
    logger.debug("Inserting boolean...");

    // current step has to exist for insert
    if (step == null)
      throw new WdkUserException("Required param " + PARAM_STEP + " is missing.");

    // the importStep has to exist for insert
    String strImport = request.getParameter(PARAM_IMPORT_STEP);
    if (strImport == null || strImport.length() == 0)
      throw new WdkUserException("The required param " + PARAM_IMPORT_STEP + " is missing.");

    if (operator == null)
      throw new WdkUserException("The required param " + PARAM_BOOLEAN_OPERATOR + " is missing.");

    Map<Long, Long> rootMap;
    // the new step will be inserted before the given Step
    StepBean newStep = new StepBean(user, StepUtilities.getStepByValidStepId(user.getUser(), Long.valueOf(strImport)));
    if (step.isCombined()) { // there are steps before the given Step, the new step will be combined into
      // a boolean (as the child), then the boolean will be inserted before the given step.
      StepBean booleanStep = new StepBean(user, StepUtilities.createBooleanStep(user.getUser(),
          strategy.getStrategyId(), step.getPreviousStep().getStep(), newStep.getStep(), operator, null));
      logger.debug("new boolean: #" + booleanStep.getStepId() + " (" +
          booleanStep.getPreviousStep().getStepId() + ", " + booleanStep.getChildStep().getStepId() + ")");
      rootMap = strategy.insertStepBefore(booleanStep, step.getStepId());
    }
    else { // the step is the first step, will make the step as child, the new step as previous, then the
      // boolean will be inserted after the given step
      StepBean booleanStep = new StepBean(user, StepUtilities.createBooleanStep(user.getUser(),
          strategy.getStrategyId(), newStep.getStep(), step.getStep(), operator, null));
      rootMap = strategy.insertStepBefore(booleanStep, step.getStepId());
    }
    return rootMap;
  }

  private Map<Long, Long> addBoolean(HttpServletRequest request, UserBean user, StrategyBean strategy,
      String operator, long branchId) throws WdkUserException, NumberFormatException, WdkModelException {
    logger.debug("Adding boolean...");

    // get root step
    StepBean rootStep = (branchId == 0) ? strategy.getLatestStep() : strategy.getStepById(branchId);

    // the importStep has to exist for insert
    String strImport = request.getParameter(PARAM_IMPORT_STEP);
    if (strImport == null || strImport.length() == 0)
      throw new WdkUserException("The required param " + PARAM_IMPORT_STEP + " is missing.");

    if (operator == null)
      throw new WdkUserException("You must specify how to combine your searches.");
    // + PARAM_BOOLEAN_OPERATOR + " is missing.");

    StepBean previousStep = rootStep;
    StepBean childStep = user.getStep(Long.valueOf(strImport));

    // use the default flags
    StepBean newStep = new StepBean(user, StepUtilities.createBooleanStep(user.getUser(),
        strategy.getStrategyId(), previousStep.getStep(), childStep.getStep(), operator, null));
    return strategy.insertStepAfter(newStep, rootStep.getStepId());
  }
}
