package org.gusdb.wdk.controller.action;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.ApplicationInitListener;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerFilterInstanceBean;
import org.gusdb.wdk.model.jspwrap.AnswerParamBean;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.json.JSONException;

/**
 * This Action is called by the ActionServlet when a WDK filter is requested. It
 * 1) reads param values from input form bean, 2) runs the step query 3)
 * completes the partial boolean expression that was passed in, if any 4)
 * adds/inserts/edits step in strategy 5) forwards to application page
 */

public class ProcessFilterAction extends ProcessQuestionAction {
    private static final Logger logger = Logger.getLogger(ProcessFilterAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        System.out.println("Entering ProcessFilterAction...");

        // Make sure a strategy is specified
        String strStratId = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);

        if (strStratId == null || strStratId.length() == 0) {
            throw new WdkModelException(
                    "No strategy was specified for processing!");
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

        String strBranchId = null;
        StepBean step;

        QuestionBean wdkQuestion;

        boolean isTransform = false;

        // did we get strategyId_stepId?
        if (strStratId.indexOf("_") > 0) {
            strBranchId = strStratId.split("_")[1];
            strStratId = strStratId.split("_")[0];
        }

        // get strategy
        StrategyBean strategy = wdkUser.getStrategy(Integer.parseInt(strStratId));

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

        String boolExp = request.getParameter("booleanExpression");
        String insertStratIdStr = request.getParameter("insertStrategy");

        String filterName = request.getParameter("filter");

        // are we inserting an existing step?
        if (insertStratIdStr != null && insertStratIdStr.length() != 0) {
            // yes: load step, create a new step w/ same answervalue
            StrategyBean insertStrat = wdkUser.getStrategy(Integer.parseInt(insertStratIdStr));
            step = cloneStrategy(wdkUser, insertStrat.getLatestStep());
            step.setIsCollapsible(true);
            step.setCollapsedName("Copy of " + insertStrat.getName());
            step.update(false);
        } else {
            // no: get question
            String qFullName = request.getParameter(CConstants.QUESTION_FULLNAME_PARAM);
            wdkQuestion = getQuestionByFullName(qFullName);
            QuestionForm fForm = prepareQuestionForm(wdkQuestion, request,
                    (QuestionForm) form);

            // validate & parse params
            Map<String, String> params = prepareParams(wdkUser, request, fForm);

            try {
                step = ShowSummaryAction.summaryPaging(request, wdkQuestion,
                        params, filterName);
            } catch (Exception ex) {
                logger.error(ex);
                ex.printStackTrace();
                return showError(wdkModel, wdkUser, mapping, request, response);
            }

            // We only set isTransform = true if we're running a new query &
            // it's a transform
            // If we're inserting a strategy, it has to be a boolean (given
            // current operations, at least)
            isTransform = step.getIsTransform();
        }

        int stepId = step.getStepId();

        StepBean childStep = step;
        StepBean originalStep;
        if (strBranchId == null) {
            originalStep = strategy.getLatestStep();
        } else {
            originalStep = strategy.getStepById(Integer.parseInt(strBranchId));
	    System.out.println("Original step parent boolean expression: " + originalStep.getParentStep().getBooleanExpression());
        }

        // Are we revising or inserting a step?
        String reviseStep = request.getParameter("revise");
        String insertStep = request.getParameter("insert");
        String op = boolExp;
        if (op.indexOf(" ") >= 0) {
            op = boolExp.substring(boolExp.indexOf(" "), boolExp.length());
        }

        boolExp = null;
        if ((reviseStep == null || reviseStep.length() == 0)
                && (insertStep == null || insertStep.length() == 0)) {
            if (!isTransform) {
                // now create step for operation query, if it's a boolean
                boolExp = originalStep.getStepId() + " " + op + " " + stepId;
                System.out.println("Boolean expression for add: " + boolExp);
                step = wdkUser.combineStep(boolExp, false);
                stepId = step.getStepId();

                step.setChildStep(childStep);
		System.out.println("Created step.");
            }
            // implied: since step is a transform (and we aren't inserting a
            // strategy), we've
            // already run the filter query (b/c the transform is just a query
            // w/ a history param
        } else {
            int stratLen = originalStep.getLength();
            int targetId;
            int targetIx;
            boolean isRevise;

            if (isRevise = (reviseStep != null && reviseStep.length() != 0)) {
                targetId = Integer.parseInt(reviseStep);
                targetIx = originalStep.getIndexFromId(targetId);
            } else {
                targetId = Integer.parseInt(insertStep);
                targetIx = originalStep.getIndexFromId(targetId);
            }

            if (stratLen > 1 || !isRevise) {
                step = originalStep.getStep(targetIx);
                if (step.getIsFirstStep()) {
                    if (isRevise) {
                        // carry over custom name from original query, if any
                        childStep.setCustomName(step.getBaseCustomName());
                        childStep.update(false);
                        if (step.getNextStep().getIsTransform()) {
                            step = updateTransform(request, wdkUser,
                                    step.getNextStep(), stepId);
                        } else {
                            //boolExp = step.getNextStep().getBooleanExpression();
                            //boolExp = stepId
                            //        + boolExp.substring(boolExp.indexOf(" "),
                            //                boolExp.lastIndexOf(" ") + 1)
                            //        + step.getNextStep().getChildStep().getStepId();
                            boolExp = stepId + " "
				+ step.getNextStep().getOperation() + " "
                                    + step.getNextStep().getChildStep().getStepId();
                        }
                        targetIx++;
                    } else {
                        // if inserting before first step, there has to be a
                        // boolean expression
                        // b/c existing first step is a regular non-boolean,
                        // non-transform query
                        boolExp = stepId + " " + op + " " + step.getStepId();
                    }
                    targetIx++;
                } else {
                    if (isRevise) {
                        if (!isTransform) {
                            // check if we've changed the query itself, or just
                            // the operation
                            childStep.setCustomName(step.getChildStep().getBaseCustomName());
                            childStep.update(false);
                            // build standard boolExp for non-first step
                            boolExp = step.getPreviousStep().getStepId() + " "
                                    + op + " " + stepId;
                        }
                        // implied: if we're revising a transform step, we've
                        // already run the revised query,
                        // so we just need to update subsequent steps
                        targetIx++;
                    } else {
                        if (!isTransform) {
                            // need to check if this is boolean or not
                            // the inserted step has to point to the step at
                            // insertIx - 1
                            boolExp = step.getPreviousStep().getStepId() + " "
                                    + op + " " + stepId;
                        }
                        // implied: if we're inserting a transform, the
                        // HistoryParam should already
                        // be pointing to the step at insertIx - 1, so we just
                        // need to update subsequent steps.
                    }
                }

                if (boolExp != null) {
                    System.out.println("Boolean expression for revise/insert: "
                            + boolExp);
                    // now create step for operation query
                    step = wdkUser.combineStep(boolExp, false);
                    stepId = step.getStepId();
                }

                step.setChildStep(childStep);

                System.out.println("Updating subsequent steps.");
                for (int i = targetIx; i < stratLen; ++i) {
                    System.out.println("Updating step " + i);
                    step = originalStep.getStep(i);
                    if (step.getIsTransform()) {
                        step = updateTransform(request, wdkUser, step, stepId);
                    } else {
                        //boolExp = step.getBooleanExpression();
                        boolExp = stepId + " "
			    + step.getOperation() + " "
                                + step.getChildStep().getStepId();
                        step = wdkUser.combineStep(boolExp, false);
                    }
                    stepId = step.getStepId();
                }
            }
        }

        step.setParentStep(originalStep.getParentStep());
        step.setIsCollapsible(originalStep.getIsCollapsible());
        step.setCollapsedName(originalStep.getCollapsedName());
        step.update(false);

        if (strBranchId != null) {
            strBranchId = Integer.toString(step.getStepId());
        }

        // if step has a parent step, need to continue updating the rest of the
        // strategy.
        while (step.getParentStep() != null) {
            // go to parent, update subsequent steps
            StepBean parentStep = step.getParentStep();
            // update parent, then update subsequent
            boolExp = parentStep.getBooleanExpression();
	    System.out.println("Parent boolExp: " + boolExp);
            boolExp = parentStep.getPreviousStep().getStepId() + " " 
                    + parentStep.getOperation() +" " + step.getStepId();
	    System.out.println("Boolean expression for updating parent: " + boolExp);
            step = wdkUser.combineStep(boolExp, false);
            while (parentStep.getNextStep() != null) {
                parentStep = parentStep.getNextStep();
                // need to check if step is a transform (in which case there's
                // no boolean expression; we need to update history param
                if (parentStep.getIsTransform()) {
                    step = updateTransform(request, wdkUser, parentStep,
                            step.getStepId());
                } else {
                    boolExp = parentStep.getBooleanExpression();
                    boolExp = step.getStepId() + " "
			+ parentStep.getOperation() + " "
                            + parentStep.getChildStep().getStepId();
                    step = wdkUser.combineStep(boolExp, false);
                }
            }
	    System.out.println("Updating step with parent info.");
            step.setParentStep(parentStep.getParentStep());
            step.setIsCollapsible(parentStep.getIsCollapsible());
            step.setCollapsedName(parentStep.getCollapsedName());
            step.update(false);
        }

	System.out.println("Adding latest step to strategy.");
        // set latest step
        strategy.setLatestStep(step);

	System.out.println("Updating strategy.");
        // in either case, update and forward to show strategy
        strategy.update(false);

        if (activeStrategies != null && index >= 0) {
            activeStrategies.add(index, new Integer(strategy.getStrategyId()));
        }
        // request.getSession().setAttribute(CConstants.WDK_STRATEGY_COLLECTION_KEY,
        // activeStrategies);
        wdkUser.setActiveStrategies(activeStrategies);

        ActionForward showStrategy = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);
        StringBuffer url = new StringBuffer(showStrategy.getPath());
        url.append("?strategy="
                + URLEncoder.encode(Integer.toString(strategy.getStrategyId()),
                        "UTF-8"));
        if (strBranchId != null) {
            url.append("_" + URLEncoder.encode(strBranchId, "UTF-8"));
        }

        ActionForward forward = new ActionForward(url.toString());
        forward.setRedirect(true);
	System.out.println("Leaving ProcessFilterAction...");
        return forward;
    }

    private ActionForward showError(WdkModelBean wdkModel, UserBean wdkUser,
            ActionMapping mapping, HttpServletRequest request,
            HttpServletResponse response) throws WdkModelException,
            WdkUserException, SQLException, JSONException,
            NoSuchAlgorithmException {
        // TEST
        logger.info("Show the details of an invalid step/question");

        String qFullName = request.getParameter(CConstants.QUESTION_FULLNAME_PARAM);
        Map<String, String> params;
        Map<String, String> paramNames;
        String customName;
        if (qFullName == null || qFullName.length() == 0) {
            String strHistId = request.getParameter(CConstants.WDK_HISTORY_ID_KEY);
            int stepId = Integer.parseInt(strHistId);
            StepBean step = wdkUser.getStep(stepId);
            params = step.getParams();
            paramNames = step.getParamNames();
            qFullName = step.getQuestionName();
            customName = step.getCustomName();
        } else {
            params = new LinkedHashMap<String, String>();
            paramNames = new LinkedHashMap<String, String>();
            customName = qFullName;

            // get params from request
            Map<?, ?> parameters = request.getParameterMap();
            for (Object object : parameters.keySet()) {
                try {
                    String pName;
                    pName = URLDecoder.decode((String) object, "utf-8");
                    Object objValue = parameters.get(object);
                    String pValue = null;
                    if (objValue != null) {
                        pValue = objValue.toString();
                        if (objValue instanceof String[]) {
                            StringBuffer sb = new StringBuffer();
                            String[] array = (String[]) objValue;
                            for (String v : array) {
                                if (sb.length() > 0) sb.append(", ");
                                sb.append(v);
                            }
                            pValue = sb.toString();
                        }
                        pValue = URLDecoder.decode(pValue, "utf-8");
                    }
                    if (pName.startsWith("myProp(")) {
                        pName = pName.substring(7, pName.length() - 1).trim();
                        params.put(pName, pValue);

                        String displayName = wdkModel.queryParamDisplayName(pName);
                        if (displayName == null) displayName = pName;
                        paramNames.put(pName, displayName);
                    }
                } catch (UnsupportedEncodingException ex) {
                    throw new WdkModelException(ex);
                }
            }
        }
        String qDisplayName = wdkModel.getQuestionDisplayName(qFullName);
        if (qDisplayName == null) qDisplayName = qFullName;

        request.setAttribute("questionDisplayName", qDisplayName);
        request.setAttribute("customName", customName);
        request.setAttribute("params", params);
        request.setAttribute("paramNames", paramNames);

        ServletContext svltCtx = getServlet().getServletContext();
        String customViewDir = (String) svltCtx.getAttribute(CConstants.WDK_CUSTOMVIEWDIR_KEY);
        String customViewFile = customViewDir + File.separator
                + CConstants.WDK_CUSTOM_SUMMARY_ERROR_PAGE;

        String url;
        if (ApplicationInitListener.resourceExists(customViewFile, svltCtx)) {
            url = customViewFile;
        } else {
            ActionForward forward = mapping.findForward(CConstants.SHOW_ERROR_MAPKEY);
            url = forward.getPath();
        }

        ActionForward forward = new ActionForward(url);
        forward.setRedirect(false);
        return forward;
    }

    protected StepBean updateTransform(HttpServletRequest request,
            UserBean wdkUser, StepBean step, int newStepId)
            throws WdkModelException, WdkUserException,
            NoSuchAlgorithmException, JSONException, SQLException {
        QuestionBean wdkQuestion;
        Map<String, String> internalParams;

        // Get question
        wdkQuestion = step.getAnswerValue().getQuestion();
        ParamBean[] params = wdkQuestion.getParams();
        // Get internal params
        internalParams = step.getAnswerValue().getParams();
        // Change HistoryParam
        AnswerParamBean answerParam = null;
        for (ParamBean param : params) {
            if (param instanceof AnswerParamBean) {
                answerParam = (AnswerParamBean) param;
            }
        }

        internalParams.put(answerParam.getName(), wdkUser.getSignature() + ":"
                + newStepId);

        AnswerFilterInstanceBean filter = step.getAnswerValue().getFilter();
        String filterName = (filter == null) ? null : filter.getName();

        StepBean newStep = ShowSummaryAction.summaryPaging(request,
                wdkQuestion, internalParams, filterName);
        newStep.setCustomName(step.getBaseCustomName());
        newStep.update(false);
        return newStep;
    }

    private StepBean cloneStrategy(UserBean user, StepBean step)
            throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException {
        StepBean cloneStep = null;
        String prevStepId = null;
        for (int i = 0; i < step.getLength(); ++i) {
            cloneStep = step.getStep(i);
            AnswerValueBean answerValue = cloneStep.getAnswerValue();
            String childStepId = null;
            String op = null;
            if (cloneStep.getChildStep() != null) {
                childStepId = cloneStrategy(user, cloneStep.getChildStep()).getStepId()
                        + "";
                op = answerValue.getBooleanOperation();
            }
            String cloneBoolExp = prevStepId + " " + op + " " + childStepId;
            cloneStep = user.combineStep(cloneBoolExp, false);
            prevStepId = cloneStep.getStepId() + "";
        }
        return cloneStep;
    }
}
