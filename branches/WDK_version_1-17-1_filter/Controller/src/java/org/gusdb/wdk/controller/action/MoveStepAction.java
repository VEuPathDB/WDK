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
import org.gusdb.wdk.model.jspwrap.RecordPageBean;
import org.gusdb.wdk.model.jspwrap.UserAnswerBean;
import org.gusdb.wdk.model.jspwrap.UserStrategyBean;
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
	String strProtoId = request.getParameter("strategy");
	String strMoveFromIx = request.getParameter("movefrom");
	String op = request.getParameter("op");
	String strMoveToIx = request.getParameter("moveto");

	// Make sure necessary arguments are provided
	if (strProtoId == null || strProtoId.length() == 0) {
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
	    
	    UserStrategyBean strategy = wdkUser.getUserStrategy(Integer.parseInt(strProtoId));
	    
	    StepBean moveFromStep = strategy.getStep(moveFromIx);
	    StepBean moveToStep = strategy.getStep(moveToIx);
	    StepBean step, newStep;

	    int stubIx = Math.min(moveFromIx, moveToIx) - 1;
	    int length = strategy.getLength();

	    String boolExp;

	    UserAnswerBean userAnswer;

	    if (stubIx < 0) {
		step = null;
	    }
	    else {
		step = strategy.getStep(stubIx);
	    }

	    for (int i = stubIx + 1; i < length; ++i) {
		if (i == moveToIx) {
		    if (step == null) {
			step = new StepBean(moveFromStep.getChildStepUserAnswer());
		    }
		    else {
			// assuming boolean, will need to add case for non-boolean op
			boolExp = moveFromStep.getFilterUserAnswer().getBooleanExpression();
			boolExp = step.getFilterUserAnswer().getUserAnswerId() + boolExp.substring(boolExp.indexOf(" "), boolExp.length());
			userAnswer = wdkUser.combineUserAnswer(boolExp);
			moveFromStep.setFilterUserAnswer(userAnswer);
			step = moveFromStep;
		    }
		    //again, assuming boolean, will need to add case for non-boolean
		    boolExp = moveToStep.getFilterUserAnswer().getBooleanExpression();
		    boolExp = step.getFilterUserAnswer().getUserAnswerId() + boolExp.substring(boolExp.indexOf(" "), boolExp.length());
		    userAnswer = wdkUser.combineUserAnswer(boolExp);
		    moveToStep.setFilterUserAnswer(userAnswer);
		    step = moveToStep;
		}
		else if (i == moveFromIx) {
		    //do nothing; this step was moved, so we just ignore it.
		}
		else {
		    newStep = strategy.getStep(i);
		    if (step == null) {
			step = new StepBean(newStep.getChildStepUserAnswer());
		    }
		    else {
			//again, assuming boolean, will need to add case for non-boolean
			boolExp = newStep.getFilterUserAnswer().getBooleanExpression();
			boolExp = step.getFilterUserAnswer().getUserAnswerId() + boolExp.substring(boolExp.indexOf(" "), boolExp.length());
			userAnswer = wdkUser.combineUserAnswer(boolExp);
			newStep.setFilterUserAnswer(userAnswer);
			step = moveToStep;
		    }
		}
	    }

	    strategy.setLatestStep(step);
	    strategy.update(false);
			

	    /* Charles:  Commented out on 7/1/08.  Will need to update for new strategy objects
	    String boolExp;
	    int histId = -1;

	    if (moveFromIx < moveToIx) {
		for (int i = moveFromIx; i < moveToIx; ++i) {
		    StepBean step = strategy.getStep(i);
		    if (i == 0) {
			// set histId to the subQueryUserAnswer of the next step...since we're moving
			// step 1, next step 2 is going to become step 1
			histId = step.getNextStep().getSubQueryUserAnswer().getUserAnswerId();
		    }
		    else {
			// move the step "back"...
			boolExp = step.getNextStep().getFilterUserAnswer().getBooleanExpression();
			if (histId < 0) {
			    histId = step.getPreviousStep().getFilterUserAnswer().getUserAnswerId();
			}
			boolExp = histId + boolExp.substring(boolExp.indexOf(" "), boolExp.length());
			wdkUser.updateUserAnswer(step.getFilterUserAnswer(), boolExp);
			histId = -1;
		    }
		}
		boolExp = strategy.getStep(moveToIx).getFilterUserAnswer().getBooleanExpression();
		if (moveFromIx == 0) {
		    // need previous step userAnswer id + op + first step userAnswer id
		    boolExp = boolExp.substring(0, boolExp.indexOf(" ")) + op + " " + moveStep.getFilterUserAnswer().getUserAnswerId();
		}
		else {
		    // need previous step userAnswer id + (op, subquery userAnswer id) substring from movestep
		    String moveBoolExp = moveStep.getFilterUserAnswer().getBooleanExpression();
		    boolExp = boolExp.substring(0, boolExp.indexOf(" ")) + moveBoolExp.substring(moveBoolExp.indexOf(" ") + 1, moveBoolExp.length());
		}
		wdkUser.updateUserAnswer(strategy.getStep(moveToIx).getFilterUserAnswer(), boolExp);
	    }
	    else {
		for (int i = moveFromIx; i > moveToIx; --i) {
		    StepBean step = strategy.getStep(i);
		    if (step.getPreviousStep().getIsFirstStep()) {
			//need to build boolExp: moveStep subquery userAnswer id  + op + first step filter userAnswer id
			boolExp = moveStep.getSubQueryUserAnswer().getUserAnswerId() + " " + op + " " + step.getPreviousStep().getFilterUserAnswer().getUserAnswerId();
			wdkUser.updateUserAnswer(step.getFilterUserAnswer(), boolExp);
		    }
		    else {
			boolExp = step.getPreviousStep().getFilterUserAnswer().getBooleanExpression();
			histId = step.getPreviousStep().getFilterUserAnswer().getUserAnswerId();
			boolExp = histId + boolExp.substring(boolExp.indexOf(" "), boolExp.length());
			wdkUser.updateUserAnswer(step.getFilterUserAnswer(), boolExp);
		    }
		}
	    }
	    */
	}
	
	// Forward to ShowStrategyAction
	ActionForward showSummary = mapping.findForward( CConstants.SHOW_STRATEGY_MAPKEY );
	StringBuffer url = new StringBuffer( showSummary.getPath() );
	url.append("?strategy=" + URLEncoder.encode(strProtoId));
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
