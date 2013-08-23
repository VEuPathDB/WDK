/**
 * 
 */
package org.gusdb.wdk.controller.action;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.actionutil.ActionUtility;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * @author ctreatma
 * 
 */
public class ProcessRenameStrategyAction extends Action {

    private static Logger logger = Logger.getLogger(ProcessRenameStrategyAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering Rename Strategy...");

        UserBean wdkUser = ActionUtility.getUser(servlet, request);
        WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);

        try {
            String state = request.getParameter(CConstants.WDK_STATE_KEY);

            String strStratId = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
            String customName = request.getParameter("name");
            String description = request.getParameter("description");
            boolean save = Boolean.valueOf(request.getParameter("save")).booleanValue();
            boolean checkName = Boolean.valueOf(
                    request.getParameter("checkName")).booleanValue();
            // TEST
            if (customName == null || customName.length() == 0) {
                throw new Exception("No name was given for saving Strategy.");
            }
            if (strStratId == null || strStratId.length() == 0) {
                throw new Exception("No Strategy was given for saving");
            }

            int stratId = Integer.parseInt(strStratId);
            StrategyBean strategy = wdkUser.getStrategy(stratId);
            boolean opened = (wdkUser.getStrategyOrder(strStratId) > 0);

            // verify the checksum
            String checksum = request.getParameter(CConstants.WDK_STRATEGY_CHECKSUM_KEY);
            if (checksum != null && !strategy.getChecksum().equals(checksum)) {
                ShowStrategyAction.outputOutOfSyncJSON(wdkModel, wdkUser, response, state);
                return null;
            }

            // if we haven't been asked to check the user-specified name, or a
            // strategy with that name does not already exist, do the rename
            if (!checkName
                    || !wdkUser.checkNameExists(strategy, customName, save)) {
                logger.debug("failed check.  either not checking name, or strategy doesn't already exist.");
                int oldStrategyId = strategy.getStrategyId();

                if (save) {
                    if (wdkUser.isGuest()) {
                        throw new Exception(
                                "You must be logged in to save a strategy!");
                    }
                    // if we're saving, and the strat is already saved (which
                    // means
                    // savedName is not null),
                    // and the new name to save with is different from the
                    // savedName
                    // (which means we're
                    // doing a "save as"), then make a new copy of this
                    // strategy.
                    if (strategy.getIsSaved()
                            && !customName.equals(strategy.getSavedName())) {
                        // clone the last step
                        StepBean step = strategy.getLatestStep().deepClone();
                        strategy = wdkUser.createStrategy(step, false);
                    }

                    // mark the strategy as saved, set saved name
                    strategy.setIsSaved(true);
                }

                // whether its a save or rename, set new name specified by user.
                strategy.setName(customName);
                strategy.setSavedName(customName);
                strategy.setDescription(description);
                strategy.update(save || strategy.getIsSaved());

                try {
                    wdkUser.replaceActiveStrategy(
                            oldStrategyId,
                            strategy.getStrategyId(), null);
                } catch (WdkUserException ex) {
                    // Adding active strat will be handled by ShowStrategyAction
                }

		// If a front end action is specified in the url, set it in the current user
		String frontAction = request.getParameter("action");
		Integer frontStrategy = null;
		try {
		    frontStrategy = Integer.valueOf(request.getParameter("actionStrat"));
		}
		catch (Exception ex) {
		}
		Integer frontStep = null;
		try {
		    frontStep = Integer.valueOf(request.getParameter("actionStep"));
		}
		catch (Exception ex) {
		}

		if (frontStrategy != null && frontStrategy.intValue() == oldStrategyId) {
		    frontStrategy = strategy.getStrategyId();
		}
		System.out.println("front strategy: " + frontStrategy);
		System.out.println("front step: " + frontStep);

		wdkUser.setFrontAction(frontAction);
		if (frontStrategy != null) {
		    wdkUser.setFrontStrategy(frontStrategy);
		}
		if (frontStep != null) {
		    wdkUser.setFrontStep(frontStep);
		}

                request.setAttribute(CConstants.WDK_STEP_KEY,
                        strategy.getLatestStep());
                request.setAttribute(CConstants.WDK_STRATEGY_KEY, strategy);
            } else {    // name already exists
                ShowStrategyAction.outputDuplcicateNameJSON(wdkModel, wdkUser, response, state);
                return null;
            }

            // forward to strategyPage.jsp
            ActionForward showStrategy = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);
            StringBuffer url = new StringBuffer(showStrategy.getPath());
            url.append("?state=" + URLEncoder.encode(state, "UTF-8"));
            if (!opened)
                url.append("&").append(CConstants.WDK_OPEN_KEY).append("=false");
            url.append("&").append(CConstants.WDK_STRATEGY_ID_KEY).append("=")
                .append(strategy.getStrategyId());

            ActionForward forward = new ActionForward(url.toString(), true);
            return forward;
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            ShowStrategyAction.outputErrorJSON(wdkUser, response, ex);
            return null;
        }
    }

}
