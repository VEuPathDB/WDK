package org.gusdb.wdk.controller.action;

import java.net.URLEncoder;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * This Action handles moving a step in a search strategy to a different
 * position. It moves the step, updates the subsequent steps, and forwards to
 * ShowSummaryAction
 **/

public class DeleteStepAction extends ProcessFilterAction {
    private static final Logger logger = Logger.getLogger(DeleteStepAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering DeleteStepAction...");

        // Make sure a strategy is specified
        String strStratId = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
        String strBranchId = null;

        System.out.println("Strategy: " + strStratId);
        if (strStratId == null || strStratId.length() == 0) {
            throw new WdkModelException(
                    "No strategy was specified for deleting a step!");
        }

        // load model, user
        WdkModelBean wdkModel = (WdkModelBean) servlet.getServletContext().getAttribute(
                CConstants.WDK_MODEL_KEY);
        UserBean wdkUser = (UserBean) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY);
        if (wdkUser == null) {
            wdkUser = wdkModel.getUserFactory().getGuestUser();
            request.getSession().setAttribute(CConstants.WDK_USER_KEY, wdkUser);
        }

        StrategyBean strategy;
        StepBean targetStep;
        StepBean step, newStep;
        String boolExp;

        // Are we revising or deleting a step?
        String deleteStep = request.getParameter("step");

        if (deleteStep == null || deleteStep.length() == 0) {
            throw new WdkModelException("No step was specified to delete!");
        }

        if (strStratId.indexOf("_") > 0) {
            strBranchId = strStratId.split("_")[1];
            strStratId = strStratId.split("_")[0];
        }

        strategy = wdkUser.getStrategy(Integer.parseInt(strStratId));

        // ArrayList<Integer> activeStrategies =
        // (ArrayList<Integer>)request.getSession().getAttribute(CConstants.WDK_STRATEGY_COLLECTION_KEY);
        ArrayList<Integer> activeStrategies = wdkUser.getActiveStrategies();
        int index = -1;

        if (activeStrategies != null
                && activeStrategies.contains(new Integer(
                        strategy.getStrategyId()))) {
            index = activeStrategies.indexOf(new Integer(
                    strategy.getStrategyId()));
            activeStrategies.remove(index);
        }

        if (strBranchId == null) {
            targetStep = strategy.getLatestStep();
        } else {
            targetStep = strategy.getStepById(Integer.parseInt(strBranchId));
        }

        int stepId = Integer.valueOf(deleteStep);
        int stepIx = targetStep.getIndexFromId(stepId);
        step = targetStep.getStep(stepIx);

        // are we deleting the first step?
        if (step.getIsFirstStep()) {
            // if there are two steps, we're just moving to the second step as a
            // one-step strategy
            if (targetStep.getLength() == 2) {
                // update step so that it is the child step of the second step
                step = step.getNextStep().getChildStep();
            }
            // if there are more than two steps, we need to convert the second
            // step into a first step
            // (i.e., so that it doesn't have an operation) and then update all
            // steps after the second step
            else if (targetStep.getLength() > 2) {
                step = step.getNextStep();
                step = step.getChildStep();
                // we need to update starting w/ step 3
                stepIx += 2;
                for (int i = stepIx; i < targetStep.getLength(); ++i) {
                    newStep = targetStep.getStep(i);
                    if (newStep.getIsTransform()) {
                        step = updateTransform(request, wdkUser, newStep,
                                step.getStepId());
                    } else {
                        //boolExp = newStep.getBooleanExpression();
                        boolExp = step.getStepId() + " " 
			    + newStep.getOperation() + " " 
                                + newStep.getChildStep().getStepId();
                        System.out.println("Delete boolExp " + i + ": "
                                + boolExp);
                        step = wdkUser.combineStep(boolExp, false);
                    }
                }
            } else if (strBranchId != null) {
                // If this is the only step in a branch, unexpand the step &
                // return nothing? ui will have to know to close?
                step.setIsCollapsible(false);
                step.setCollapsedName(null);
                step.update(false);
                return null;
            } else {
                // Delete the strategy if this is the only step in the main
                // strategy;
                ActionForward forward = mapping.findForward(CConstants.DELETE_STRATEGY_MAPKEY);
                StringBuffer url = new StringBuffer(forward.getPath());
                url.append("?strategy="
                        + URLEncoder.encode(strStratId, "utf-8"));
                forward = new ActionForward(url.toString());
                forward.setRedirect(true);
                return forward;
            }
        } else {
            // if this is not the last step, then the next step needs to point
            // to the previous step
            if (stepIx < targetStep.getLength() - 1) {
                step = step.getPreviousStep();
                // need to start by updating the step after the deleted step so
                // that it
                // points to the step before the deleted step, then update
                // subsequent steps
                stepIx++;
                for (int i = stepIx; i < targetStep.getLength(); ++i) {
                    newStep = targetStep.getStep(i);
                    if (newStep.getIsTransform()) {
                        step = updateTransform(request, wdkUser, newStep,
                                step.getStepId());
                    } else {
                        //boolExp = newStep.getBooleanExpression();
                        boolExp = step.getStepId() + " " 
			    + newStep.getOperation() + " " 
                                + newStep.getChildStep().getStepId();
                        System.out.println("Delete boolExp " + i + ": "
                                + boolExp);
                        step = wdkUser.combineStep(boolExp, false);
                    }
                }
            }
            // if this is the last step, then we just set the previous step as
            // the last step of the strategy
            else {
                step = step.getPreviousStep();
            }
        }

        newStep = null;
        step.setNextStep(newStep);

        // follow any parent pointers, update parent strategies
        step.setParentStep(targetStep.getParentStep());
        step.setIsCollapsible(targetStep.getIsCollapsible());
        step.setCollapsedName(targetStep.getCollapsedName());
        step.update(false);

        if (strBranchId != null) {
            strBranchId = Integer.toString(step.getStepId());
        }

        while (step.getParentStep() != null) {
            // go to parent, update subsequent steps
            StepBean parentStep = step.getParentStep();
            if (parentStep != null) {
                // update parent, then update subsequent
                boolExp = parentStep.getPreviousStep().getStepId() + " "
		    + parentStep.getOperation() + " "
                        + step.getStepId();
                step = wdkUser.combineStep(boolExp, false);
                while (parentStep.getNextStep() != null) {
                    parentStep = parentStep.getNextStep();
                    // need to check if step is a transform (in which case
                    // there's no boolean expression; we need to update history
                    // param
                    if (parentStep.getIsTransform()) {
                        step = updateTransform(request, wdkUser, parentStep,
                                step.getStepId());
                    } else {
                        boolExp = step.getStepId() + " "
			    + parentStep.getOperation() + " "
                                + parentStep.getChildStep().getStepId();
                        step = wdkUser.combineStep(boolExp, false);
                    }
                }
                step.setParentStep(parentStep.getParentStep());
                step.setIsCollapsible(parentStep.getIsCollapsible());
                step.setCollapsedName(parentStep.getCollapsedName());
                step.update(false);
            }
        }

        strategy.setLatestStep(step);
        strategy.update(false);

        if (activeStrategies != null && index >= 0) {
            activeStrategies.add(index, new Integer(strategy.getStrategyId()));
        }
        // request.getSession().setAttribute(CConstants.WDK_STRATEGY_COLLECTION_KEY,
        // activeStrategies);
        wdkUser.setActiveStrategies(activeStrategies);

        // 5. forward to strategy page
        ActionForward showSummary = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);
        StringBuffer url = new StringBuffer(showSummary.getPath());
        url.append("?strategy="
                + URLEncoder.encode(Integer.toString(strategy.getStrategyId()),
                        "UTF-8"));
        if (strBranchId != null) {
            url.append("_" + URLEncoder.encode(strBranchId, "UTF-8"));
        }
        String viewStep = request.getParameter("step");
        if (viewStep != null && viewStep.length() != 0) {
            if (Integer.valueOf(viewStep) > Integer.valueOf(deleteStep)) {
                viewStep = (Integer.valueOf(viewStep) - 1) + "";
                url.append("&step=" + URLEncoder.encode(viewStep, "UTF-8"));
            } else if (Integer.valueOf(viewStep) < Integer.valueOf(deleteStep)) {
                url.append("&step=" + URLEncoder.encode(viewStep, "UTF-8"));
            }
        }
        String subQuery = request.getParameter("subquery");
        if (subQuery != null && subQuery.length() != 0) {
            url.append("&subquery=" + URLEncoder.encode(subQuery, "UTF-8"));
        }
        System.out.println(url.toString());
        ActionForward forward = new ActionForward(url.toString());
        forward.setRedirect(true);
        return forward;
    }
}
