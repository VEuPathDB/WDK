package org.gusdb.wdk.controller.action;

import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
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
        logger.debug("Entering ProcessFilterAction...");

        try {
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
                request.getSession().setAttribute(CConstants.WDK_USER_KEY,
                        wdkUser);
            }

            String strBranchId = null;

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

            // Get operation from request params. If it is non-null
            // but contains only whitespace, set it to null.
            String op = request.getParameter("booleanExpression");
            if (op != null) {
                op = op.trim();
                if (op.length() == 0) op = null;
            }

            String insertStratIdStr = request.getParameter("insertStrategy");
            String qFullName = request.getParameter(CConstants.QUESTION_FULLNAME_PARAM);

            String filterName = request.getParameter("filter");
            boolean hasFilter = (filterName != null && filterName.length() > 0);

            // Are we revising or inserting a step?
            // changing filter is considered a revise
            String reviseStep = request.getParameter("revise");
            boolean isRevise = (reviseStep != null && reviseStep.length() != 0);
            String insertStep = request.getParameter("insert");
            boolean isInsert = (insertStep != null && insertStep.length() != 0);

            System.out.println("isRevise: " + isRevise);
            System.out.println("qFullName? "
                    + (qFullName == null || qFullName.trim().length() == 0));
            System.out.println("qFullName: " + qFullName);
            // are we inserting an existing step?
            StepBean newStep;
            if (insertStratIdStr != null && insertStratIdStr.length() != 0) {
                // yes: load step, create a new step w/ same answervalue
                StrategyBean insertStrat = wdkUser.getStrategy(Integer.parseInt(insertStratIdStr));
                newStep = cloneStrategy(wdkUser, insertStrat.getLatestStep());
                newStep.setIsCollapsible(true);
                newStep.setCollapsedName("Copy of " + insertStrat.getName());
                newStep.update(false);
            } else if (isRevise && hasFilter || isRevise
                    && (qFullName == null || qFullName.trim().length() == 0)) {
                logger.debug("change filter: " + filterName);
                // change the filter of an existing step, which can be a child
                // step,
                // or a boolean step
                StepBean oldStep = strategy.getStepById(Integer.parseInt(reviseStep));
                if (hasFilter) newStep = oldStep.createStep(filterName);
                else {
                    newStep = oldStep.getChildStep();
                }
            } else {
                // no: get question
                wdkQuestion = getQuestionByFullName(qFullName);
                // QuestionForm fForm = prepareQuestionForm(wdkQuestion,
                // request, (QuestionForm) form);
                QuestionForm fForm = (QuestionForm) form;

                // validate & parse params
                Map<String, String> params = prepareParams(wdkUser, request,
                        fForm);

                newStep = ShowSummaryAction.summaryPaging(request, wdkQuestion,
                        params, filterName);

                // We only set isTransform = true if we're running a new query &
                // it's a transform
                // If we're inserting a strategy, it has to be a boolean (given
                // current operations, at least)
                isTransform = newStep.getIsTransform();
            }

            int newStepId = newStep.getStepId();
            int baseNewStepId = newStepId;

            Map<Integer, Integer> stepIdsMap;
            int targetStepId;

            // get root step of a strategy or a branch
            StepBean rootStep;
            if (strBranchId == null) {
                rootStep = strategy.getLatestStep();
            } else {
                rootStep = strategy.getStepById(Integer.parseInt(strBranchId));
                System.out.println("Original step parent boolean expression: "
                        + rootStep.getParentStep().getBooleanExpression());
            }

            String boolExp = null;
            if (isRevise && hasFilter) {
                // get the original step
                int originalStepId = Integer.parseInt(reviseStep);
                StepBean targetStep = strategy.getStepById(originalStepId);

                logger.debug("orginal step: " + originalStepId + ", new step: "
                        + newStep.getStepId());

                // check if the step is a simple one or a combined one
                if (targetStep.getParentStep() != null) {
                    // a simple step, and not the only step, then we need to
                    // edit its parent, not itself.
                    StepBean parentStep = targetStep.getParentStep();
                    targetStepId = parentStep.getStepId();

                    logger.debug("target step: " + targetStepId);

                    StepBean previousStep = parentStep.getPreviousStep();
                    StepBean childStep = newStep;
                    String operator = (op == null) ? parentStep.getOperation()
                            : op;
                    boolean useBooleanFilter = parentStep.isUseBooleanFilter();
                    AnswerFilterInstanceBean filter = parentStep.getAnswerValue().getFilter();
                    String bfName = (filter == null) ? null : filter.getName();

                    newStep = wdkUser.createBooleanStep(previousStep,
                            childStep, operator, useBooleanFilter, bfName);
                } else targetStepId = originalStepId;
                stepIdsMap = strategy.editOrInsertStep(targetStepId, newStep);
            } else if (!isRevise && !isInsert) {
                // add new step to the end of a strategy or a branch
                targetStepId = rootStep.getStepId();
                if (!isTransform) {
                    // now create step for operation query, if it's a boolean
                    boolExp = rootStep.getStepId() + " " + op + " " + newStepId;
                    newStep = wdkUser.combineStep(boolExp, false);
                    newStepId = newStep.getStepId();
                }
                // implied: since step is a transform (and we aren't inserting a
                // strategy), we've
                // already run the filter query (b/c the transform is just a
                // query
                // w/ a history param
                stepIdsMap = strategy.addStep(targetStepId, newStep);
            } else { // insert or edit
                int stratLen = rootStep.getLength();

                if (isRevise) { // revise step
                    targetStepId = Integer.parseInt(reviseStep);
                } else { // insert a step
                    targetStepId = Integer.parseInt(insertStep);
                }

                StepBean targetStep = null;
                if (stratLen > 1 || !isRevise) {
                    targetStep = rootStep.getStepByDisplayId(targetStepId);
                    if (targetStep.getIsFirstStep()) {
                        if (isRevise) {
                            // carry over custom name from original query, if
                            // any
                            newStep.setCustomName(targetStep.getBaseCustomName());
                            newStep.update(false);

                            StepBean parent = targetStep.getNextStep();

                            if (parent.getIsTransform()) {
                                targetStep = updateTransform(wdkUser, parent,
                                        newStepId);
                            } else {
                                StepBean previous = newStep;
                                StepBean child = parent.getChildStep();
                                String operator = parent.getOperation();
                                boolean useBooleanFilter = parent.isUseBooleanFilter();
                                AnswerFilterInstanceBean filter = parent.getAnswerValue().getFilter();
                                String bfName = (filter == null) ? null
                                        : filter.getName();
                                newStep = wdkUser.createBooleanStep(previous,
                                        child, operator, useBooleanFilter,
                                        bfName);
                                newStepId = newStep.getStepId();
                            }
                            targetStepId = parent.getStepId();
                        } else {
                            // if inserting before first step, there has to be a
                            // boolean expression
                            // b/c existing first step is a regular non-boolean,
                            // non-transform query
                            boolExp = newStepId + " " + op + " "
                                    + targetStep.getStepId();
                        }
                    } else { // not the first step
                        if (isRevise) {
                            if (!isTransform) {
                                // check if we've changed the query itself, or
                                // just the operation
                                logger.debug("targetStepId: " + targetStepId);
                                logger.debug("newStep: " + newStep.getStepId());
                                logger.debug("newStep: "
                                        + newStep.getCustomName());
                                logger.debug("newStep: "
                                        + newStep.getIsBoolean());
                                newStep.setCustomName(targetStep.getChildStep().getBaseCustomName());
                                newStep.update(false);
                                // build standard boolExp for non-first step
                                StepBean parent = targetStep;
                                StepBean previous = parent.getPreviousStep();
                                StepBean child = newStep;
                                String operator = (op == null)
                                        ? parent.getOperation() : op;
                                boolean useBooleanFilter = parent.isUseBooleanFilter();
                                AnswerFilterInstanceBean filter = parent.getAnswerValue().getFilter();
                                String bfName = (filter == null) ? null
                                        : filter.getName();
                                newStep = wdkUser.createBooleanStep(previous,
                                        child, operator, useBooleanFilter,
                                        bfName);
                                newStepId = newStep.getStepId();
                            }
                            // implied: if we're revising a transform step,
                            // we've already run the revised query,
                            // so we just need to update subsequent steps
                        } else {
                            if (!isTransform) {
                                // the inserted step has to point to the step at
                                // insertIx - 1
                                boolExp = targetStep.getPreviousStep().getStepId()
                                        + " " + op + " " + newStepId;
                            }
                            // implied: if we're inserting a transform, the
                            // HistoryParam should already
                            // be pointing to the step at insertIx - 1, so we
                            // just need to update subsequent steps.

                            // since we want to insert the new step BEFORE
                            // targetStep,
                            // we need to move targetStepId back, so it points
                            // to
                            // targetStep.getPreviousStep
                            targetStepId = targetStep.getPreviousStep().getStepId();
                        }
                    }

                    if (boolExp != null) {
                        // now create step for operation query
                        newStep = wdkUser.combineStep(boolExp, false);
                        newStepId = newStep.getStepId();
                    }
                } else { // branch length = 1 && revise: revise the first step
                    targetStep = strategy.getStepById(targetStepId);
                }
                stepIdsMap = strategy.editOrInsertStep(targetStepId, newStep);
            }

            // If a branch id was specified, look up the new branch id in
            // stepIdsMap
            if (strBranchId != null) {
                strBranchId = stepIdsMap.get(Integer.valueOf(strBranchId)).toString();
            }

            if (activeStrategies != null && index >= 0) {
                activeStrategies.add(index, new Integer(
                        strategy.getStrategyId()));
            }
            // request.getSession().setAttribute(CConstants.WDK_STRATEGY_COLLECTION_KEY,
            // activeStrategies);
            wdkUser.setActiveStrategies(activeStrategies);

            ActionForward showStrategy = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);
            StringBuffer url = new StringBuffer(showStrategy.getPath());
            url.append("?strategy="
                    + URLEncoder.encode(
                            Integer.toString(strategy.getStrategyId()), "UTF-8"));
            if (strBranchId != null) {
                url.append("_" + URLEncoder.encode(strBranchId, "UTF-8"));
            }
            if (isRevise && hasFilter) {
                url.append("&step=" + baseNewStepId);
            }

            ActionForward forward = new ActionForward(url.toString());
            forward.setRedirect(true);
            System.out.println("Leaving ProcessFilterAction...");
            return forward;
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            ShowStrategyAction.outputErrorJSON(ex, response);
            return null;
        }
    }
//
//    private ActionForward showError(WdkModelBean wdkModel, UserBean wdkUser,
//            ActionMapping mapping, HttpServletRequest request,
//            HttpServletResponse response) throws WdkModelException,
//            WdkUserException, SQLException, JSONException,
//            NoSuchAlgorithmException {
//        // TEST
//        logger.info("Show the details of an invalid step/question");
//
//        String qFullName = request.getParameter(CConstants.QUESTION_FULLNAME_PARAM);
//        Map<String, String> params;
//        Map<String, String> paramNames;
//        String customName;
//        if (qFullName == null || qFullName.length() == 0) {
//            String strHistId = request.getParameter(CConstants.WDK_HISTORY_ID_KEY);
//            int stepId = Integer.parseInt(strHistId);
//            StepBean step = wdkUser.getStep(stepId);
//            params = step.getParams();
//            paramNames = step.getParamNames();
//            qFullName = step.getQuestionName();
//            customName = step.getCustomName();
//        } else {
//            params = new LinkedHashMap<String, String>();
//            paramNames = new LinkedHashMap<String, String>();
//            customName = qFullName;
//
//            // get params from request
//            Map<?, ?> parameters = request.getParameterMap();
//            for (Object object : parameters.keySet()) {
//                try {
//                    String pName;
//                    pName = URLDecoder.decode((String) object, "utf-8");
//                    Object objValue = parameters.get(object);
//                    String pValue = null;
//                    if (objValue != null) {
//                        pValue = objValue.toString();
//                        if (objValue instanceof String[]) {
//                            StringBuffer sb = new StringBuffer();
//                            String[] array = (String[]) objValue;
//                            for (String v : array) {
//                                if (sb.length() > 0) sb.append(", ");
//                                sb.append(v);
//                            }
//                            pValue = sb.toString();
//                        }
//                        pValue = URLDecoder.decode(pValue, "utf-8");
//                    }
//                    if (pName.startsWith("myProp(")) {
//                        pName = pName.substring(7, pName.length() - 1).trim();
//                        params.put(pName, pValue);
//
//                        String displayName = wdkModel.queryParamDisplayName(pName);
//                        if (displayName == null) displayName = pName;
//                        paramNames.put(pName, displayName);
//                    }
//                } catch (UnsupportedEncodingException ex) {
//                    throw new WdkModelException(ex);
//                }
//            }
//        }
//        String qDisplayName = wdkModel.getQuestionDisplayName(qFullName);
//        if (qDisplayName == null) qDisplayName = qFullName;
//
//        request.setAttribute("questionDisplayName", qDisplayName);
//        request.setAttribute("customName", customName);
//        request.setAttribute("params", params);
//        request.setAttribute("paramNames", paramNames);
//
//        ServletContext svltCtx = getServlet().getServletContext();
//        String customViewDir = (String) svltCtx.getAttribute(CConstants.WDK_CUSTOMVIEWDIR_KEY);
//        String customViewFile = customViewDir + File.separator
//                + CConstants.WDK_CUSTOM_SUMMARY_ERROR_PAGE;
//
//        String url;
//        if (ApplicationInitListener.resourceExists(customViewFile, svltCtx)) {
//            url = customViewFile;
//        } else {
//            ActionForward forward = mapping.findForward(CConstants.SHOW_ERROR_MAPKEY);
//            url = forward.getPath();
//        }
//
//        ActionForward forward = new ActionForward(url);
//        forward.setRedirect(false);
//        return forward;
//    }

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
                String cloneBoolExp = prevStepId + " " + op + " " + childStepId;
                cloneStep = user.combineStep(cloneBoolExp, false);
            } else if (cloneStep.getIsTransform()) {
                cloneStep = updateTransform(user, cloneStep,
                        Integer.parseInt(prevStepId));
            }
            // Carry custom name, collapsible flag, and collapsed name from
            // cloned step
            cloneStep.setIsCollapsible(step.getStep(i).getIsCollapsible());
            cloneStep.setCollapsedName(step.getStep(i).getCollapsedName());
            cloneStep.setCustomName(step.getStep(i).getBaseCustomName());
            cloneStep.update(false);
            prevStepId = Integer.toString(cloneStep.getStepId());
        }
        return cloneStep;
    }

    protected StepBean updateTransform(UserBean wdkUser, StepBean step,
            int newStepId) throws WdkModelException, WdkUserException,
            NoSuchAlgorithmException, SQLException, JSONException {
        // Get question
        QuestionBean wdkQuestion = step.getAnswerValue().getQuestion();
        ParamBean[] params = wdkQuestion.getParams();
        // Get internal params
        Map<String, String> internalParams = step.getAnswerValue().getInternalParams();
        // Change HistoryParam
        wdkQuestion.setInputType(step.getAnswerValue().getRecordClass().getFullName());
        List<AnswerParamBean> answerParams = wdkQuestion.getTransformParams();
        for (AnswerParamBean p : answerParams) {
            internalParams.put(p.getName(), Integer.toString(newStepId));
        }
        AnswerFilterInstanceBean filter = step.getAnswerValue().getFilter();
        String filterName = (filter == null) ? null : filter.getName();

        StepBean newStep = wdkUser.createStep(wdkQuestion, internalParams,
                filterName);
        newStep.setCustomName(step.getBaseCustomName());
        newStep.update(false);
        return newStep;
    }
}
