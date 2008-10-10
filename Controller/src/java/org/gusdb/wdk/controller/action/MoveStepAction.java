package org.gusdb.wdk.controller.action;

import java.net.URLEncoder;
import java.util.Date;

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
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.StrategyBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 *  This Action handles moving a step in a search strategy to a different
 *  position.  It moves the step, updates the relevant filter userAnswers,
 *  and forwards to ShowSummaryAction
 **/

public class MoveStepAction extends Action {
    private static final Logger logger = Logger.getLogger(MoveStepAction.class);

    public ActionForward execture(ActionMapping mapping, ActionForm form,
				  HttpServletRequest request, HttpServletResponse response)
	throws Exception {
	// Make sure strategy, step, and moveto are defined
	String strStratId = request.getParameter(CConstants.WDK_STRATEGY_ID_KEY);
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

	int moveFromIx = Integer.valueOf(strMoveFromIx);
	int moveToIx = Integer.valueOf(strMoveToIx);

	// No need to load anything if there's nothing to move
	if (moveFromIx != moveToIx) {
	    // load model, user
	    WdkModelBean wdkModel = ( WdkModelBean ) servlet.getServletContext().getAttribute(CConstants.WDK_MODEL_KEY );
	    UserBean wdkUser = ( UserBean ) request.getSession().getAttribute(
									      CConstants.WDK_USER_KEY );
	    if ( wdkUser == null ) {
		wdkUser = wdkModel.getUserFactory().getGuestUser();
		request.getSession().setAttribute( CConstants.WDK_USER_KEY, wdkUser );
	    }
	    
	    StrategyBean strategy = wdkUser.getStrategy(Integer.parseInt(strStratId));
	    
	    StepBean moveFromStep = strategy.getStep(moveFromIx);
	    StepBean moveToStep = strategy.getStep(moveToIx);
	    StepBean step, newStep;

	    int stubIx = Math.min(moveFromIx, moveToIx) - 1;
	    int length = strategy.getLength();

	    String boolExp;

	    if (stubIx < 0) {
		step = null;
	    }
	    else {
		step = strategy.getStep(stubIx);
	    }

	    for (int i = stubIx + 1; i < length; ++i) {
		if (i == moveToIx) {
		    if (step == null) {
			// used to be step = new StepBean(moveFromStep.getChildStepUserAnswer());
			// may need a clone method so that step is a separate object from moveFromStep
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
		    newStep = strategy.getStep(i);
		    if (step == null) {
			// need clone method?
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
	    strategy.setLatestStep(step);
	    strategy.update(false);
	}
	
	// Forward to ShowStrategyAction
	ActionForward showSummary = mapping.findForward( CConstants.SHOW_STRATEGY_MAPKEY );
	StringBuffer url = new StringBuffer( showSummary.getPath() );
	url.append("?strategy=" + URLEncoder.encode(strStratId));
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
