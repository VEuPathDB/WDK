package org.gusdb.wdk.controller.action;

import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerParamBean;
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

        logger.debug("strategy: " + request.getParameter("strategy") + ", step: " + request.getParameter("step"));

        WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
        UserBean wdkUser = ActionUtility.getUser(servlet, request);
        try {
            String state = request.getParameter(CConstants.WDK_STATE_KEY);

            // Make sure a strategy is specified
            String strStratId = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);

            if (strStratId == null || strStratId.length() == 0) {
                throw new WdkModelException(
                        "No strategy was specified for processing!");
            }
            String strBranchId = null;

            QuestionBean wdkQuestion = null;

            boolean isTransform = false;

            // did we get strategyId_stepId?
            String strategyKey = strStratId;
            if (strStratId.indexOf("_") > 0) {
                strBranchId = strStratId.split("_")[1];
                strStratId = strStratId.split("_")[0];
            }

            // get strategy
            StrategyBean strategy = wdkUser.getStrategy(Integer.parseInt(strStratId));

            // verify the checksum
            String checksum = request.getParameter(CConstants.WDK_STRATEGY_CHECKSUM_KEY);
            if (checksum != null && !strategy.getChecksum().equals(checksum)) {
                logger.error("strategy checksum: " + strategy.getChecksum()
                        + ", but the input checksum: " + checksum);
                ShowStrategyAction.outputOutOfSyncJSON(wdkModel, wdkUser, response, state);
                return null;
            }

            int oldStrategyId = strategy.getStrategyId();

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

            // get the assigned weight
            String strWeight = request.getParameter(CConstants.WDK_ASSIGNED_WEIGHT_KEY);
            boolean hasWeight = (strWeight != null && strWeight.length() > 0);
            int weight = Utilities.DEFAULT_WEIGHT;
            if (hasWeight) {
                if (!strWeight.matches("[\\-\\+]?\\d+"))
                    throw new WdkUserException("Invalid weight value: '"
                            + strWeight
                            + "'. Only integer numbers are allowed.");
                if (strWeight.length() > 9)
                    throw new WdkUserException("Weight number is too big: "
                            + strWeight);
                weight = Integer.parseInt(strWeight);
            }

            // Are we revising or inserting a step?
            // changing filter is considered a revise
            String reviseStep = request.getParameter("revise");
            boolean isRevise = (reviseStep != null && reviseStep.length() != 0);
            String insertStep = request.getParameter("insert");
            boolean isInsert = (insertStep != null && insertStep.length() != 0);
            boolean isOrtholog = Boolean.valueOf(request.getParameter("ortholog"));
            boolean hasQuestion = (qFullName != null && qFullName.trim().length() > 0);

            logger.debug("isRevise: " + isRevise + "; isInsert: " + isInsert);
            logger.debug("has question? " + hasQuestion + "; qFullName: "
                    + qFullName);
            logger.debug("has filter? " + hasFilter + "; filter: " + filterName);
            logger.debug("has weight? " + hasWeight + "; weight: " + weight);
            // are we inserting an existing step?
            StepBean newStep;
            if (insertStratIdStr != null && insertStratIdStr.length() != 0) {
                // yes: load step, create a new step w/ same answervalue
                StrategyBean insertStrat = wdkUser.getStrategy(Integer.parseInt(insertStratIdStr));
                // deep clone the root step of the input strategy
                newStep = insertStrat.getLatestStep().deepClone();
                newStep.setIsCollapsible(true);
                newStep.setCollapsedName("Copy of " + insertStrat.getName());
                newStep.update(false);
            } else if (hasQuestion) { // no: get question
                // QuestionForm fForm = prepareQuestionForm(wdkQuestion,
                // request, (QuestionForm) form);
                QuestionForm fForm = (QuestionForm) form;

                // validate & parse params
                Map<String, String> params = prepareParams(wdkUser, request,
                        fForm);

                if (isRevise) { // TODO need investigation of this code
                    /* StepBean oldStep = */ 
                    strategy.getStepById(Integer.parseInt(reviseStep));
                }
                if (wdkQuestion == null) {
                    if (!hasQuestion)
                        throw new WdkUserException(
                                "The required question name is not provided, cannot process operation.");
                    wdkQuestion = wdkModel.getQuestion(qFullName);
                }
                newStep = ShowSummaryAction.summaryPaging(request, wdkQuestion,
                        params, filterName, false, weight);

                // We only set isTransform = true if we're running a new query &
                // it's a transform If we're inserting a strategy, it has to be
                // a boolean (given current operations, at least)
                isTransform = newStep.getIsTransform() || (newStep.isCombined() && !newStep.getIsBoolean());
            } else { // revise, but just change filter or weight.
                logger.debug("change filter: " + filterName);
                // change the filter of an existing step, which can be a child
                // step, or a boolean step
                StepBean oldStep = strategy.getStepById(Integer.parseInt(reviseStep));
                if (hasFilter) {
                    newStep = oldStep.createStep(filterName,
                            oldStep.getAssignedWeight());
                } else if (hasWeight) {
                    newStep = oldStep.createStep(oldStep.getFilterName(),
                            weight);
                } else {
                    newStep = oldStep.getChildStep();
                }
                // reset pager info in session
                wdkUser.setViewResults(wdkUser.getViewStrategyId(),
                        wdkUser.getViewStepId(), 0);
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
            if (isRevise && (hasFilter || (hasWeight && !hasQuestion))) {
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
                    String bfName = parentStep.getFilterName();

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
                // strategy), we've already run the filter query (b/c the
                // transform is just a query w/ a history param
                stepIdsMap = strategy.addStep(targetStepId, newStep);
                // set the view step to the one just added
                wdkUser.setViewResults(strategyKey, newStepId, 0);
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
                    if (isOrtholog && targetStep.getParentStep() != null) {
                        // if this action was called from an ortholog link, and
                        // we're operating on a substrat, add the substrat to
                        // the active strategies list.
                        wdkUser.addActiveStrategy(strategy.getStrategyId()
                                + "_" + targetStep.getStepId());
                        if (!targetStep.getIsCollapsible()) {
                            // if the target step has a parent but hasn't been
                            // converted to a substrat, need to convert it
                            // before adding the ortholog step
                            targetStep.setIsCollapsible(true);
                            targetStep.setCollapsedName(targetStep.getCustomName());
                        }
                    }
                    if (targetStep.getIsFirstStep()) {
                        if (isRevise) {
                            // carry over custom name from original query, if
                            // any
                            newStep.setCustomName(targetStep.getBaseCustomName());
                            newStep.update(false);

                            StepBean parent = targetStep.getNextStep();

                            if (parent.getIsTransform()) {
                                newStep = updateTransform(wdkUser, parent,
                                        newStepId, weight);
                            } else {
                                StepBean previous = newStep;
                                StepBean child = parent.getChildStep();
                                String operator = parent.getOperation();
                                boolean useBooleanFilter = parent.isUseBooleanFilter();
                                String bfName = parent.getFilterName();
                                newStep = wdkUser.createBooleanStep(previous,
                                        child, operator, useBooleanFilter,
                                        bfName);
                                newStepId = newStep.getStepId();
                            }
                            targetStepId = parent.getStepId();
                        } else {
                            if (stratLen == 1 && strBranchId != null) {
                                // if this is the only step in a substrat,
                                // make an uncollapsed copy before inserting
                                targetStep = targetStep.deepClone();
                                targetStep.setIsCollapsible(false);
                                targetStep.setCollapsedName(null);
                                targetStep.setParentStep(null);
                                targetStep.update(false);
                            }
                            if (!isOrtholog) {
                                // if inserting before first step, there has to
                                // be a boolean expression b/c existing first
                                // step is a regular non-boolean, non-transform
                                // query
                                boolExp = newStepId + " " + op + " "
                                        + targetStep.getStepId();
                            }
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
                                // save the old child id into revise
                                reviseStep = Integer.toString(parent.getChildStep().getStepId());
                                StepBean child = newStep;
                                String operator = (op == null) ? parent.getOperation()
                                        : op;
                                boolean useBooleanFilter = parent.isUseBooleanFilter();
                                String bfName = parent.getFilterName();
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
                            if (!isOrtholog) {
                                // implied: if we're inserting a transform, the
                                // HistoryParam should already be pointing to
                                // the step at insertIx - 1, so we just need to
                                // update subsequent steps.

                                // since we want to insert the new step BEFORE
                                // targetStep, we need to move targetStepId
                                // back, so it points to
                                // targetStep.getPreviousStep
                                targetStepId = targetStep.getPreviousStep().getStepId();
                            }
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
            if (isRevise) {
                int reviseId = Integer.parseInt(reviseStep);
                if (!stepIdsMap.containsKey(reviseId))
                    stepIdsMap.put(reviseId, baseNewStepId);
            }
            logger.debug("revise " + isRevise + ", " + reviseStep + "===>"
                    + baseNewStepId);

            // If a branch id was specified, look up the new branch id in
            // stepIdsMap
            if (strBranchId != null) {
                strBranchId = stepIdsMap.get(Integer.valueOf(strBranchId)).toString();
            }

            try {
                wdkUser.replaceActiveStrategy(oldStrategyId,
                        strategy.getStrategyId(), stepIdsMap);
            } catch (WdkUserException ex) {
                // Replace failed, need to add strategy to active list
                // which is handled by ShowStrategyAction
            }

            // set the view, if it's not set yet
            logger.debug("old view: strategy=" + wdkUser.getViewStrategyId() + ", step=" + wdkUser.getViewStepId());
            String viewStrategyKey = wdkUser.getViewStrategyId();
            if (viewStrategyKey == null) viewStrategyKey = strategyKey;
            if (strategyKey.equals(viewStrategyKey)) {
                int viewStepId = wdkUser.getViewStepId();
                if (0 == viewStepId || strategy.getStepById(viewStepId) == null) {
                    // the view is not set
                    wdkUser.setViewResults(viewStrategyKey, newStepId, 0);
                    logger.debug("new view: strategy=" + viewStrategyKey + ", step=" + newStepId);
                }
            }

            ActionForward showStrategy = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);
            StringBuffer url = new StringBuffer(showStrategy.getPath());
            url.append("?state=" + URLEncoder.encode(state, "UTF-8"));

            ActionForward forward = new ActionForward(url.toString());
            forward.setRedirect(true);
            logger.debug("Leaving ProcessFilterAction...");
            return forward;
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            ShowStrategyAction.outputErrorJSON(wdkUser, response, ex);
            return null;
        }
    }

    protected StepBean updateTransform(UserBean wdkUser, StepBean step,
            int newStepId, int assignedWeight) throws WdkModelException,
            WdkUserException, NoSuchAlgorithmException, SQLException,
            JSONException {
        // Get question
        QuestionBean wdkQuestion = step.getQuestion();
        // Get internal params
        Map<String, String> paramValues = step.getParams();
        // Change HistoryParam
        wdkQuestion.setInputType(wdkQuestion.getRecordClass().getFullName());
        List<AnswerParamBean> answerParams = wdkQuestion.getTransformParams();
        for (AnswerParamBean p : answerParams) {
            paramValues.put(p.getName(), Integer.toString(newStepId));
        }
        String filterName = step.getFilterName();
        StepBean newStep = wdkUser.createStep(wdkQuestion, paramValues,
                filterName, step.getIsDeleted(), false, assignedWeight);
        newStep.setCustomName(step.getBaseCustomName());
        newStep.update(false);
        return newStep;
    }
}
