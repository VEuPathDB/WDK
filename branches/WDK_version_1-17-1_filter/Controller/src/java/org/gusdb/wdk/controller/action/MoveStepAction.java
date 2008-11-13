package org.gusdb.wdk.controller.action;

import java.net.URLEncoder;
import java.util.Date;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.upload.FormFile;
import org.gusdb.wdk.controller.ApplicationInitListener;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean;
import org.gusdb.wdk.model.jspwrap.DatasetParamBean;
import org.gusdb.wdk.model.jspwrap.DatasetBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.HistoryParamBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.EnumParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.RecordBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 *  This Action handles moving a step in a search strategy to a different
 *  position.  It moves the step, updates the relevant filter userAnswers,
 *  and forwards to ShowSummaryAction
 **/

public class MoveStepAction extends ProcessFilterAction {
    private static final Logger logger = Logger.getLogger(MoveStepAction.class);

    public ActionForward execture(ActionMapping mapping, ActionForm form,
				  HttpServletRequest request, HttpServletResponse response)
	throws Exception {
	// Make sure strategy, step, and moveto are defined
	String strStratId = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
	String strBranchId = null;
	String strMoveFromIx = request.getParameter("movefrom");
	String op = request.getParameter("op");
	String strMoveToIx = request.getParameter("moveto");

	// Make sure necessary arguments are provided
	if (strStratId == null || strStratId.length() == 0) {
	    throw new WdkModelException("No strategy was specified for moving steps!");
	}
	if (strMoveFromIx == null || strMoveFromIx.length() == 0) {
	    throw new WdkModelException("No step was specified for moving!");
	}
	else if (op == null || op.length() == 0) {
	    throw new WdkModelException("No operation specified for moving first step.");
	}
	if (strMoveToIx == null || strMoveToIx.length() == 0) {
	    throw new WdkModelException("No destination was specified for moving!");
	}

	// load model, user
	WdkModelBean wdkModel = ( WdkModelBean ) servlet.getServletContext().getAttribute(CConstants.WDK_MODEL_KEY );
	UserBean wdkUser = ( UserBean ) request.getSession().getAttribute(CConstants.WDK_USER_KEY );
	if ( wdkUser == null ) {
	    wdkUser = wdkModel.getUserFactory().getGuestUser();
	    request.getSession().setAttribute( CConstants.WDK_USER_KEY, wdkUser );
	}
	if (strStratId.indexOf("_") > 0) {
	    strBranchId = strStratId.split("_")[1];
	    strStratId = strStratId.split("_")[0];
	}
	
	StrategyBean strategy = wdkUser.getStrategy(Integer.parseInt(strStratId));
	
        AnswerValueBean wdkAnswerValue;
	QuestionBean wdkQuestion;
	Map<String, Object> internalParams;
	Map<String, Boolean> sortingAttributes;
	String questionName;
	String[] summaryAttributes = null;

	//ArrayList<Integer> activeStrategies = (ArrayList<Integer>)request.getSession().getAttribute(CConstants.WDK_STRATEGY_COLLECTION_KEY);
	ArrayList<Integer> activeStrategies = wdkUser.getActiveStrategies();
	int index = -1;
	
	if (activeStrategies != null && activeStrategies.contains(new Integer(strategy.getStrategyId()))) {
	    index = activeStrategies.indexOf(new Integer(strategy.getStrategyId()));
	    activeStrategies.remove(index);
	}

	int moveFromIx = Integer.valueOf(strMoveFromIx);
	int moveToIx = Integer.valueOf(strMoveToIx);

	// No need to load anything if there's nothing to move
	if (moveFromIx != moveToIx) {
	    StepBean moveFromStep = strategy.getStep(moveFromIx);
	    StepBean moveToStep = strategy.getStep(moveToIx);
	    StepBean step, newStep, targetStep;

	    if (strBranchId == null) {
		targetStep = strategy.getLatestStep();
	    }
	    else {
		targetStep = strategy.getStepById(Integer.parseInt(strBranchId));
	    }

	    int stubIx = Math.min(moveFromIx, moveToIx) - 1;
	    int length = targetStep.getLength();

	    String boolExp;

	    if (stubIx < 0) {
		step = null;
	    }
	    else {
		step = targetStep.getStep(stubIx);
	    }

	    for (int i = stubIx + 1; i < length; ++i) {
		if (i == moveToIx) {
		    if (step == null) {
			step = moveFromStep.getChildStep();
		    }
		    else {
			// assuming boolean, will need to add case for non-boolean op
			boolExp = moveFromStep.getBooleanExpression();
			boolExp = step.getStepId() + boolExp.substring(boolExp.indexOf(" "), boolExp.length());
			moveFromStep = wdkUser.combineStep(boolExp);
			// may also need clone method here?
			step = moveFromStep;
		    }
		    //again, assuming boolean, will need to add case for non-boolean
		    boolExp = moveToStep.getBooleanExpression();
		    boolExp = step.getStepId() + boolExp.substring(boolExp.indexOf(" "), boolExp.length());
		    moveToStep = wdkUser.combineStep(boolExp);
		    step = moveToStep;
		}
		else if (i == moveFromIx) {
		    //do nothing; this step was moved, so we just ignore it.
		}
		else {
		    newStep = targetStep.getStep(i);
		    if (step == null) {
			step = newStep.getChildStep();
		    }
		    else {
			//again, assuming boolean, will need to add case for non-boolean
			boolExp = newStep.getBooleanExpression();
			boolExp = step.getStepId() + boolExp.substring(boolExp.indexOf(" "), boolExp.length());
			newStep = wdkUser.combineStep(boolExp);
			step = moveToStep;
		    }
		}
	    }

	    // set next step to null so we can set strategy pointer
	    newStep = null;
	    step.setNextStep(newStep);
	    step.setParentStep(targetStep.getParentStep());
	    step.setIsCollapsible(targetStep.getIsCollapsible());
	    step.setCollapsedName(targetStep.getCollapsedName());
	    step.update(false);

	    if (strBranchId != null) {
		strBranchId = Integer.toString(step.getStepId());
	    }

	    while (step.getParentStep() != null) {
		//go to parent, update subsequent steps
		StepBean parentStep = step.getParentStep();
		if (parentStep != null) {
		    //update parent, then update subsequent
		    boolExp = parentStep.getBooleanExpression();
		    boolExp = parentStep.getPreviousStep().getStepId() + boolExp.substring(boolExp.indexOf(" "), boolExp.lastIndexOf(" ") + 1) + step.getStepId();
		    step = wdkUser.combineStep(boolExp);
		    while (parentStep.getNextStep() != null) {
			parentStep = parentStep.getNextStep();
			// need to check if step is a transform (in which case there's no boolean expression; we need to update history param
			if (parentStep.getIsTransform()) {
			    step = updateTransform(request, wdkUser, parentStep, step.getStepId());
			}
			else {
			    boolExp = parentStep.getBooleanExpression();
			    boolExp = step.getStepId() + boolExp.substring(boolExp.indexOf(" "), boolExp.lastIndexOf(" ") + 1) + parentStep.getChildStep().getStepId();
			    step = wdkUser.combineStep(boolExp);
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
	}

	if (activeStrategies != null && index >= 0) {
	    activeStrategies.add(index, new Integer(strategy.getStrategyId()));
	}
	//request.getSession().setAttribute(CConstants.WDK_STRATEGY_COLLECTION_KEY, activeStrategies);
	wdkUser.setActiveStrategies(activeStrategies);
	
	// Forward to ShowStrategyAction
	ActionForward showSummary = mapping.findForward( CConstants.SHOW_STRATEGY_MAPKEY );
	StringBuffer url = new StringBuffer( showSummary.getPath() );
	url.append("?strategy=" + URLEncoder.encode(Integer.toString(strategy.getStrategyId())));
	if (strBranchId != null) {
	    url.append("_" + URLEncoder.encode(strBranchId));
	}
	String viewStep = request.getParameter("step");
	if (viewStep != null && viewStep.length() != 0) {
	    url.append("&step=" + URLEncoder.encode(viewStep));
	}
	String subQuery = request.getParameter("subquery");
	if (subQuery != null && subQuery.length() != 0) {
	    url.append("&subquery=" + URLEncoder.encode(subQuery));
	}
	ActionForward forward = new ActionForward( url.toString() );
	forward.setRedirect( true );
	return forward;
    }
}
