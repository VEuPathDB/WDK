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
import org.gusdb.wdk.controller.actionutil.ActionUtility;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * This Action handles expanding a step in a search strategy (i.e., turning the step into a substrategy) by
 * setting the isCollapsible and collapsedName (if not set already) and returning the expanded step to
 * strategy.jsp.
 *
 * This action is used in 3 user scenarios:
 * 1- open an existing nested strategy     (step has IS_COLLAPSIBLE = 1 / step.getIsCollapsible() == true)  -- class= expand_step_link => ExpandStep   (uncollapse=false) 
 * 2- create new nested strategy           (step has IS_COLLAPSIBLE = 0 / step.getIsCollapsible() == false) -- class= expand_step_link => ExpandStep   (uncollapse=false) 
 * 3- unnest a single step nested strategy (step has IS_COLLAPSIBLE = 1 / step.getIsCollapsible() == true)  -- class= collpase_step_link => ExpandStep (uncollapse=true)
 *
 * If this is a saved strategy:
 * - case 1: no need to generate unsaved strategy, just add nested strategy in activestrategy set
 * - in cases 2 and 3 we need to
 *     ---  make a copy of the strategy (a new unsaved strategy)
 *     ---  replace the strategy in the activestrategy set
 *     ---  make the changes in this new strategy
 **/
public class ExpandStepAction extends Action {

  private static final String PARAM_UNCOLLAPSE = "uncollapse";

  private static final Logger logger = Logger.getLogger(ExpandStepAction.class);

  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    logger.debug("Entering ExpandStepAction...");

    UserBean wdkUser = ActionUtility.getUser(servlet, request);
    WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
    try {
      String state = request.getParameter(CConstants.WDK_STATE_KEY); //state

      String strStratId = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY); //strategy
      String strStepId = request.getParameter(CConstants.WDK_STEP_ID_KEY); //step

      if (strStratId == null || strStratId.length() == 0) {
        throw new WdkModelException("No strategy was specified for expanding a step!");
      }
      if (strStepId == null || strStepId.length() == 0) {
        throw new WdkModelException("No step specified to expand!");
      }
      int stepId = Integer.valueOf(strStepId);

      if (strStratId.indexOf("_") > 0)
        strStratId = strStratId.split("_")[0];
      int oldStrategyId = Integer.valueOf(strStratId);

      StrategyBean strategy = wdkUser.getStrategy(oldStrategyId);
      // verify the checksum
      String checksum = request.getParameter(CConstants.WDK_STRATEGY_CHECKSUM_KEY);
      if (checksum != null && !strategy.getChecksum().equals(checksum)) {
        ShowStrategyAction.outputOutOfSyncJSON(wdkModel, wdkUser, response, state);
        return null;
      }
 
      StepBean step = strategy.getStepById(stepId);
      if (step.getParentStep() == null) {
        throw new WdkModelException("Only top-row steps can be expanded!");
      }

      String strUncollapse = request.getParameter(PARAM_UNCOLLAPSE);
      boolean uncollapse = false;
      if (strUncollapse != null && strUncollapse.equalsIgnoreCase("true"))
        uncollapse = true;

      // cannot change step (unnest or make a new nested) on saved strategy, will need to make a clone first
      if ( strategy.getIsSaved() && ( uncollapse == true || !step.getIsCollapsible() ) ) {
        Map<Integer, Integer> stepIdMap = new HashMap<>();
        strategy = wdkUser.copyStrategy(strategy, stepIdMap, strategy.getName());
        // map the old step id to the new one
        stepId = stepIdMap.get(stepId);
        step = strategy.getStepById(stepId);
        try {
          wdkUser.replaceActiveStrategy(oldStrategyId, strategy.getStrategyId(), stepIdMap);
        }
        catch (WdkUserException ex) {
          // Need to add strategy to active strategies list
          // which will be handled by ShowStrategyAction
        }
      }

      if (uncollapse && step.isUncollapsible()) {
        // uncollapse a single-step nested strategy
        step.setCollapsedName(null);
        step.setIsCollapsible(false);
        step.update(false);
      }
      else if (!step.getIsCollapsible()) {
        // make a new nested strategy:   collapse a step into a single-step nested strategy
        String branch = request.getParameter("collapsedName");
        if (branch == null || branch.length() == 0) {
          throw new WdkModelException("No collapsed name given for newly expanded step!");
        }
        step.setIsCollapsible(true);
        step.setCollapsedName(branch);
        step.update(false);
      }
      String strategyKey = strategy.getStrategyId() + "_" + step.getStepId();
      wdkUser.addActiveStrategy(strategyKey);

      // Add branch (Step object) to request as strategy
      request.setAttribute(CConstants.WDK_STEP_KEY, step);
      request.setAttribute(CConstants.WDK_STRATEGY_KEY, strategy);

      // forward to strategyPage.jsp
      ActionForward showSummary = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);
      StringBuffer url = new StringBuffer(showSummary.getPath());
      url.append("?state=" + URLEncoder.encode(state, "UTF-8"));
      ActionForward forward = new ActionForward(url.toString());
      forward.setRedirect(false);
      return forward;
    }
    catch (Exception ex) {
      logger.error(ex);
      ex.printStackTrace();
      ShowStrategyAction.outputErrorJSON(wdkUser, response, ex);
      return null;
    }
  }
}
