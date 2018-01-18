package org.gusdb.wdk.controller.action;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.actionutil.ActionUtility;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.user.StepUtilities;

/**
 * This Action handles moving a step in a search strategy to a different position. It moves the step, updates
 * the subsequent steps, and forwards to ShowSummaryAction
 **/

public class DeleteStepAction extends ProcessFilterAction {

  private static final Logger logger = Logger.getLogger(DeleteStepAction.class);

  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    logger.debug("Entering DeleteStepAction...");

    UserBean wdkUser = ActionUtility.getUser(request);
    WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
    try {
      String state = request.getParameter(CConstants.WDK_STATE_KEY);

      // Make sure a strategy is specified
      String strStratId = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);

      if (strStratId == null || strStratId.length() == 0) {
        throw new WdkModelException("No strategy was specified for deleting a step!");
      }

      // Are we revising or deleting a step?
      String strStepId = request.getParameter("step");
      if (strStepId == null || strStepId.length() == 0) {
        throw new WdkModelException("No step was specified to delete!");
      }
      long stepId = Long.valueOf(strStepId);

      String strategyKey = strStratId;
      if (strStratId.indexOf("_") > 0) {
        strStratId = strStratId.split("_")[0];
      }

      long oldStrategyId = Long.parseLong(strStratId);
      StrategyBean strategy = new StrategyBean(wdkUser, StepUtilities.getStrategy(wdkUser.getUser(), oldStrategyId));
      // verify the checksum
      String checksum = request.getParameter(CConstants.WDK_STRATEGY_CHECKSUM_KEY);
      if (checksum != null && !strategy.getChecksum().equals(checksum)) {
        ShowStrategyAction.outputOutOfSyncJSON(wdkModel, wdkUser, response, state);
        return null;
      }

      // cannot delete step from saved strategy, will need to make a clone first
      if (strategy.getIsSaved()) {
        Map<Long, Long> stepIdMap = new HashMap<>();
        strategy = new StrategyBean(wdkUser, wdkModel.getModel().getStepFactory().copyStrategy(
            strategy.getStrategy(), stepIdMap, strategy.getName()));
        // map the old step id to the new one
        stepId = stepIdMap.get(stepId);
      }

      StepBean step = strategy.getStepById(stepId);
      Map<Long, Long> rootMap = strategy.deleteStep(step);

      if (wdkUser.getViewStrategyId() != null && wdkUser.getViewStrategyId().equals(strategyKey) &&
          wdkUser.getViewStepId() == stepId) {
        // wdkUser.resetViewResults();
        wdkUser.setViewResults(strategyKey, strategy.getLatestStep().getFrontId(),
            wdkUser.getViewPagerOffset());
      }

      // If strategy was marked for deletion as a result of deleting
      // the step, forward to DeleteStrategy
      if (strategy.getIsDeleted()) {
        ActionForward forward = mapping.findForward(CConstants.DELETE_STRATEGY_MAPKEY);
        StringBuffer url = new StringBuffer(forward.getPath());
        url.append("?strategy=" + FormatUtil.urlEncodeUtf8(strStratId));
        forward = new ActionForward(url.toString());
        forward.setRedirect(true);
        return forward;
      }

      try {
        wdkUser.replaceActiveStrategy(oldStrategyId, strategy.getStrategyId(), rootMap);
      }
      catch (WdkUserException ex) {
        // Need to add strategy to active strategies list
        // which will be handled by ShowStrategyAction
      }

      // 5. forward to strategy page
      ActionForward showStrategy = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);
      StringBuffer url = new StringBuffer(showStrategy.getPath());
      url.append("?state=" + FormatUtil.urlEncodeUtf8(state));

      ActionForward forward = new ActionForward(url.toString());
      forward.setRedirect(true);
      return forward;
    }
    catch (Exception ex) {
      logger.error("Error while deleting step", ex);
      ShowStrategyAction.outputErrorJSON(wdkUser, response, ex);
      return null;
    }
  }
}
