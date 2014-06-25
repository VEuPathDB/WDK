package org.gusdb.wdk.controller.action;

import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.actionutil.ActionUtility;
import org.gusdb.wdk.controller.form.QuestionForm;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * This Action is called by the ActionServlet when a WDK filter is requested. It
 * 1) reads param values from input form bean, 2) runs the step query 3)
 * completes the partial boolean expression that was passed in, if any 4)
 * adds/inserts/edits step in strategy 5) forwards to application page
 */
public class ProcessFilterAction extends ProcessQuestionAction {

  private static final Logger logger = Logger.getLogger(ProcessFilterAction.class);

  private static class RequestParams {
    String strategyKey;     // original strategy value
    int strategyId;         // parsed strategy id
    StrategyBean strategy;  // strategy retrieved with strategy id
    Integer branchId;       // branch id (if boolean other than root?), may be null
    String state;           // ???
    boolean checksumValid;  // whether checksum passed matches strategy
    String op;              // boolean expression to perform ??
    Integer insertStratId;  // strat to insert
    String qFullName;       // full question name
    boolean hasQuestion;    // whether non-empty question name was passed
    QuestionForm qForm;     // question form (null if !hasQuestion)
    String filterName;      // filter name
    boolean hasFilter;      // whether non-empty filter name was passed
    int weight;             // weight to use during filter
    boolean hasWeight;      // whether non-empty weight was passed
    Integer reviseStepId;   // revise step ID
    boolean isRevise;       // whether non-empty revise step ID passed
    Integer insertStepId;   // insert step ID
    boolean isInsert;       // whether non-empty revise step ID passed
    boolean isOrtholog;     // whether ortholog param passed and value eval to true
    
    // additional information gathered during step creation
    boolean isTransform = false;
  }

  @Override
  public ActionForward execute(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    logger.debug("Entering ProcessFilterAction...");
    WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
    UserBean wdkUser = ActionUtility.getUser(servlet, request);
    
    try {
      RequestParams requestParams = getValidatedParams(request, wdkUser, form);

      // verify the checksum and return error if mismatch
      if (!requestParams.checksumValid) {
        ShowStrategyAction.outputOutOfSyncJSON(wdkModel, wdkUser, response, requestParams.state);
        return null;
      }

      StrategyBean strategy = requestParams.strategy;
      int oldStrategyId = requestParams.strategy.getStrategyId();

      // are we inserting an existing step?
      StepBean newStep = createNewFilterStep(wdkModel, wdkUser, request, requestParams);
      int newStepId = newStep.getStepId();
      int baseNewStepId = newStepId;

      Map<Integer, Integer> stepIdsMap;
      int targetStepId;

      // get root step of a strategy or a branch
      StepBean rootStep = determineRootStep(requestParams.branchId, requestParams.strategy);

      String boolExp = null;
      if (requestParams.isRevise && (requestParams.hasFilter || (requestParams.hasWeight && !requestParams.hasQuestion))) {
        // get the original step
        int originalStepId = requestParams.reviseStepId;
        StepBean targetStep = strategy.getStepById(originalStepId);

        logger.debug("orginal step: " + originalStepId + ", new step: " + newStep.getStepId());

        // check if the step is a simple one or a combined one
        if (targetStep.getParentStep() != null) {
          // a simple step, and not the only step, then we need to
          // edit its parent, not itself.
          StepBean parentStep = targetStep.getParentStep();
          targetStepId = parentStep.getStepId();

          logger.debug("target step: " + targetStepId);

          StepBean previousStep = parentStep.getPreviousStep();
          StepBean childStep = newStep;
          String operator = (requestParams.op == null) ? parentStep.getOperation() : requestParams.op;
          boolean useBooleanFilter = parentStep.isUseBooleanFilter();
          String bfName = parentStep.getFilterName();

          newStep = wdkUser.createBooleanStep(previousStep,
              childStep, operator, useBooleanFilter, bfName);
        }
        else {
          targetStepId = originalStepId;
        }
        
        stepIdsMap = strategy.editOrInsertStep(targetStepId, newStep);
      }
      else if (!requestParams.isRevise && !requestParams.isInsert) {
        // add new step to the end of a strategy or a branch
        targetStepId = rootStep.getStepId();
        if (!requestParams.isTransform) {
          // now create step for operation query, if it's a boolean
          boolExp = rootStep.getStepId() + " " + requestParams.op + " " + newStepId;
          newStep = wdkUser.combineStep(boolExp, false);
          newStepId = newStep.getStepId();
        }
        // implied: since step is a transform (and we aren't inserting a
        // strategy), we've already run the filter query (b/c the
        // transform is just a query w/ a history param
        stepIdsMap = strategy.addStep(targetStepId, newStep);
        // set the view step to the one just added
        wdkUser.setViewResults(requestParams.strategyKey, newStepId, 0);
      }
      else {
        // insert or edit
        int stratLen = rootStep.getLength();
        // determine whether to revise or insert a step
        targetStepId = (requestParams.isRevise ? requestParams.reviseStepId : requestParams.insertStepId);

        StepBean targetStep = null;
        if (stratLen > 1 || !requestParams.isRevise) {
          targetStep = rootStep.getStepByDisplayId(targetStepId);
          if (requestParams.isOrtholog && targetStep.getParentStep() != null) {
            // if this action was called from an ortholog link, and
            // we're operating on a substrat, add the substrat to
            // the active strategies list.
            wdkUser.addActiveStrategy(strategy.getStrategyId() + "_" + targetStep.getStepId());
            if (!targetStep.getIsCollapsible()) {
              // if the target step has a parent but hasn't been
              // converted to a substrat, need to convert it
              // before adding the ortholog step
              targetStep.setIsCollapsible(true);
              targetStep.setCollapsedName(targetStep.getCustomName());
            }
          }
          if (targetStep.getIsFirstStep()) {
            if (requestParams.isRevise) {
              // carry over custom name from original query, if any
              newStep.setCustomName(targetStep.getBaseCustomName());
              newStep.update(false);

              StepBean parent = targetStep.getNextStep();
              if (parent.getIsTransform()) {
                newStep = updateTransform(wdkUser, parent, newStepId, requestParams.weight);
              }
              else {
                StepBean previous = newStep;
                StepBean child = parent.getChildStep();
                String operator = parent.getOperation();
                boolean useBooleanFilter = parent.isUseBooleanFilter();
                String bfName = parent.getFilterName();
                newStep = wdkUser.createBooleanStep(previous, child, operator, useBooleanFilter, bfName);
                newStepId = newStep.getStepId();
              }
              targetStepId = parent.getStepId();
            }
            else {
              if (stratLen == 1 && requestParams.branchId != null) {
                // if this is the only step in a substrat,
                // make an uncollapsed copy before inserting
                targetStep = targetStep.deepClone();
                targetStep.setIsCollapsible(false);
                targetStep.setCollapsedName(null);
                targetStep.setParentStep(null);
                targetStep.update(false);
              }
              if (!requestParams.isOrtholog) {
                // if inserting before first step, there has to
                // be a boolean expression b/c existing first
                // step is a regular non-boolean, non-transform
                // query
                boolExp = newStepId + " " + requestParams.op + " " + targetStep.getStepId();
              }
            }
          }
          else {
            // not the first step
            if (requestParams.isRevise) {
              if (!requestParams.isTransform) {
                // check if we've changed the query itself, or
                // just the operation
                logger.debug("targetStepId: " + targetStepId);
                logger.debug("newStep: " + newStep.getStepId());
                logger.debug("newStep: " + newStep.getCustomName());
                logger.debug("newStep: " + newStep.getIsBoolean());
                newStep.setCustomName(targetStep.getChildStep().getBaseCustomName());
                newStep.update(false);
                // build standard boolExp for non-first step
                StepBean parent = targetStep;
                StepBean previous = parent.getPreviousStep();
                // save the old child id into revise
                requestParams.reviseStepId = parent.getChildStep().getStepId();
                StepBean child = newStep;
                String operator = (requestParams.op == null) ? parent.getOperation() : requestParams.op;
                boolean useBooleanFilter = parent.isUseBooleanFilter();
                String bfName = parent.getFilterName();
                newStep = wdkUser.createBooleanStep(previous, child, operator, useBooleanFilter, bfName);
                newStepId = newStep.getStepId();
              }
              // implied: if we're revising a transform step,
              // we've already run the revised query,
              // so we just need to update subsequent steps
            }
            else {
              if (!requestParams.isTransform) {
                // the inserted step has to point to the step at insertIx - 1
                boolExp = targetStep.getPreviousStep().getStepId()
                    + " " + requestParams.op + " " + newStepId;
              }
              if (!requestParams.isOrtholog) {
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
        }
        else {
          // branch length = 1 && revise: revise the first step
          targetStep = strategy.getStepById(targetStepId);
        }
        stepIdsMap = strategy.editOrInsertStep(targetStepId, newStep);
      }
      
      if (requestParams.isRevise) {
        if (!stepIdsMap.containsKey(requestParams.reviseStepId)) {
          stepIdsMap.put(requestParams.reviseStepId, baseNewStepId);
        }
      }
      logger.debug("revise " + requestParams.isRevise + ", " + requestParams.reviseStepId + "===>" + baseNewStepId);

      try {
        wdkUser.replaceActiveStrategy(oldStrategyId, strategy.getStrategyId(), stepIdsMap);
      }
      catch (WdkUserException ex) {
        // Replace failed, need to add strategy to active list
        // which is handled by ShowStrategyAction
      }

      // set the view, if it's not set yet
      logger.debug("old view: strategy=" + wdkUser.getViewStrategyId() + ", step=" + wdkUser.getViewStepId());
      String viewStrategyKey = wdkUser.getViewStrategyId();
      if (viewStrategyKey == null) {
        viewStrategyKey = requestParams.strategyKey;
      }
      if (requestParams.strategyKey.equals(viewStrategyKey)) {
        int viewStepId = wdkUser.getViewStepId();
        if (0 == viewStepId || strategy.getStepById(viewStepId) == null) {
          // the view is not set
          wdkUser.setViewResults(viewStrategyKey, newStepId, 0);
          logger.debug("new view: strategy=" + viewStrategyKey + ", step=" + newStepId);
        }
      }

      ActionForward showStrategy = mapping.findForward(CConstants.SHOW_STRATEGY_MAPKEY);
      StringBuffer url = new StringBuffer(showStrategy.getPath());
      url.append("?state=" + URLEncoder.encode(requestParams.state, "UTF-8"));

      ActionForward forward = new ActionForward(url.toString());
      forward.setRedirect(true);
      logger.debug("\nLeaving ProcessFilterAction...\n");
      return forward;
    }
    catch (Exception ex) {
      logger.error("Error while processing filter.", ex);
      ShowStrategyAction.outputErrorJSON(wdkUser, response, ex);
      return null;
    }
  }

  private static StepBean determineRootStep(Integer branchId, StrategyBean strategy) throws WdkModelException {
    if (branchId == null) {
      return strategy.getLatestStep();
    }
    else {
      StepBean rootStep = strategy.getStepById(branchId);
      logger.debug("Original step parent boolean expression: " +
          rootStep.getParentStep().getBooleanExpression());
      return rootStep;
    }
  }

  private static StepBean createNewFilterStep(WdkModelBean wdkModel, UserBean wdkUser,
      HttpServletRequest request, RequestParams requestParams) throws WdkUserException, WdkModelException {
    StepBean newStep;
    if (requestParams.insertStratId != null) {
      // yes: load step, create a new step w/ same answervalue
      StrategyBean insertStrat = wdkUser.getStrategy(requestParams.insertStratId);
      // deep clone the root step of the input strategy
      newStep = insertStrat.getLatestStep().deepClone();
      newStep.setIsCollapsible(true);
      newStep.setCollapsedName("Copy of " + insertStrat.getName());
      newStep.update(false);
    }
    else if (requestParams.hasQuestion) { // no: get question

      // validate & parse params
      Map<String, String> params = prepareParams(wdkUser, request, requestParams.qForm);

      if (requestParams.isRevise) { // TODO need investigation of this code
        /* StepBean oldStep = */ 
        requestParams.strategy.getStepById(requestParams.reviseStepId);
      }

      if (!requestParams.hasQuestion) throw new WdkUserException(
          "The required question name is not provided, cannot process operation.");
      
      QuestionBean wdkQuestion = wdkModel.getQuestion(requestParams.qFullName);
      newStep = ShowSummaryAction.summaryPaging(request, wdkQuestion,
          params, requestParams.filterName, false, requestParams.weight);

      // We only set isTransform = true if we're running a new query &
      // it's a transform If we're inserting a strategy, it has to be
      // a boolean (given current operations, at least)
      requestParams.isTransform = newStep.getIsTransform() || (newStep.isCombined() && !newStep.getIsBoolean());
    }
    else {
      // revise, but just change filter or weight.
      logger.debug("change filter: " + requestParams.filterName);
      // change the filter of an existing step, which can be a child
      // step, or a boolean step
      StepBean oldStep = requestParams.strategy.getStepById(requestParams.reviseStepId);
      if (requestParams.hasFilter) {
        newStep = oldStep.createStep(requestParams.filterName, oldStep.getAssignedWeight());
      }
      else if (requestParams.hasWeight) {
        newStep = oldStep.createStep(oldStep.getFilterName(), requestParams.weight);
      }
      else {
        newStep = oldStep.getChildStep();
      }
      
      // reset pager info in session
      wdkUser.setViewResults(wdkUser.getViewStrategyId(), wdkUser.getViewStepId(), 0);
    }
    return newStep;
  }

  private static RequestParams getValidatedParams(HttpServletRequest request,
      UserBean wdkUser, ActionForm form) throws WdkUserException, WdkModelException {
    try {
      RequestParams params = new RequestParams();
      params.strategyKey = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
      logger.debug("strategy: " + params.strategyKey);
      // make sure a strategy is specified
      if (params.strategyKey == null || params.strategyKey.isEmpty()) {
        throw new WdkUserException("No strategy was specified for processing!");
      }
      // did we get strategyId_stepId?
      if (params.strategyKey.indexOf("_") > 0) {
        params.strategyId = Integer.parseInt(params.strategyKey.split("_")[0]);
        params.branchId = Integer.parseInt(params.strategyKey.split("_")[1]);
      }
      else {
        params.strategyId = Integer.parseInt(params.strategyKey);
      }
      params.state = request.getParameter(CConstants.WDK_STATE_KEY);
      params.strategy = wdkUser.getStrategy(params.strategyId);
      String checksum = request.getParameter(CConstants.WDK_STRATEGY_CHECKSUM_KEY);
      params.checksumValid = (checksum == null ? false :
          params.strategy.getChecksum().equals(checksum));
      // log checksum conflict
      if (!params.checksumValid) {
        logger.error("strategy checksum: " + params.strategy.getChecksum() +
            ", but the input checksum: " + checksum);
      }
      // Get operation from request params. If it is non-null
      // but contains only whitespace, set it to null.
      params.op = request.getParameter("booleanExpression");
      params.op = (params.op == null || params.op.trim().isEmpty()) ? null : params.op.trim();
      String insertStratIdStr = request.getParameter("insertStrategy");
      params.insertStratId = (insertStratIdStr == null || insertStratIdStr.isEmpty()) ?
          null : Integer.parseInt(insertStratIdStr);
      
      params.qFullName = request.getParameter(CConstants.QUESTION_FULLNAME_PARAM);
      params.hasQuestion = (params.qFullName != null && !params.qFullName.trim().isEmpty());
      if (params.hasQuestion) params.qForm = (QuestionForm)form;
      params.filterName = request.getParameter("filter");
      params.hasFilter = (params.filterName != null && !params.filterName.isEmpty());
      
      // get the assigned weight
      String strWeight = request.getParameter(CConstants.WDK_ASSIGNED_WEIGHT_KEY);
      params.hasWeight = (strWeight != null && !strWeight.isEmpty());
      params.weight = Utilities.DEFAULT_WEIGHT;
      if (params.hasWeight) {
        if (!strWeight.matches("[\\-\\+]?\\d+"))
          throw new WdkUserException("Invalid weight value: '" + strWeight + "'. Only integer numbers are allowed.");
        if (strWeight.length() > 9)
          throw new WdkUserException("Weight number is too big: " + strWeight);
        params.weight = Integer.parseInt(strWeight);
      }
      
      // Are we revising or inserting a step?  Changing filter is considered a revise
      String reviseStepStr = request.getParameter("revise");
      params.isRevise = (reviseStepStr != null && !reviseStepStr.isEmpty());
      if (params.isRevise) params.reviseStepId = Integer.parseInt(reviseStepStr);
      String insertStepStr = request.getParameter("insert");
      params.isInsert = (insertStepStr != null && !insertStepStr.isEmpty());
      if (params.isInsert) params.insertStepId = Integer.parseInt(insertStepStr);
      params.isOrtholog = Boolean.valueOf(request.getParameter("ortholog"));

      logger.debug("isRevise: " + params.isRevise + "; isInsert: " + params.isInsert);
      logger.debug("has question? " + params.hasQuestion + "; qFullName: " + params.qFullName);
      logger.debug("has filter? " + params.hasFilter + "; filter: " + params.filterName);
      logger.debug("has weight? " + params.hasWeight + "; weight: " + params.weight);
      
      return params;
    }
    catch (NumberFormatException e) {
      throw new WdkUserException("Misformatted parameter.", e);
    }
  }

  private static StepBean updateTransform(UserBean wdkUser, StepBean step,
      int newStepId, int assignedWeight) throws WdkModelException, WdkUserException {
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
