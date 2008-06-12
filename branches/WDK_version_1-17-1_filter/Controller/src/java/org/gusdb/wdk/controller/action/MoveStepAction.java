package org.gusdb.wdk.controller.action;

import java.net.URLEncoder;

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
import org.gusdb.wdk.model.jspwrap.AnswerBean;
import org.gusdb.wdk.model.jspwrap.HistoryBean;
import org.gusdb.wdk.model.jspwrap.ProtocolBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 *  This Action handles moving a step in a search strategy to a different
 *  position.  It moves the step, updates the relevant filter histories,
 *  and forwards to ShowSummaryAction
 **/

public class MoveStepAction extends Action {
    private static final Logger logger = Logger.getLogger(MoveStepAction.class);

    public ActionForward execture(ActionMapping mapping, ActionForm form,
				  HttpServletRequest request, HttpServletResponse response)
	throws Exception {
	// Make sure protocol, step, and moveto are defined
	String strProtoId = request.getParameter("protocol");
	String strMoveFromIx = request.getParameter("movefrom");
	String op = request.getParameter("op");
	String strMoveToIx = request.getParameter("moveto");

	// Make sure necessary arguments are provided
	if (strProtoId == null || strProtoId.length() == 0) {
	    throw new WdkModelException("No protocol was specified for moving steps!");
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

	if (moveFromIx != moveToIx) {
	    // load model, user
	    WdkModelBean wdkModel = ( WdkModelBean ) servlet.getServletContext().getAttribute(CConstants.WDK_MODEL_KEY );
	    UserBean wdkUser = ( UserBean ) request.getSession().getAttribute(
									      CConstants.WDK_USER_KEY );
	    if ( wdkUser == null ) {
		wdkUser = wdkModel.getUserFactory().getGuestUser();
		request.getSession().setAttribute( CConstants.WDK_USER_KEY, wdkUser );
	    }
	    
	    ProtocolBean protocol = ProtocolBean.getProtocol(strProtoId, wdkUser);
	    StepBean moveStep = protocol.getStep(moveFromIx);
	 
	    String boolExp;
	    int histId = -1;

	    if (moveFromIx < moveToIx) {
		for (int i = moveFromIx; i < moveToIx; ++i) {
		    StepBean step = protocol.getStep(i);
		    if (i == 0) {
			// set histId to the subQueryHistory of the next step...since we're moving
			// step 1, next step 2 is going to become step 1
			histId = step.getNextStep().getSubQueryHistory().getHistoryId();
		    }
		    else {
			// move the step "back"...
			boolExp = step.getNextStep().getFilterHistory().getBooleanExpression();
			if (histId < 0) {
			    histId = step.getPreviousStep().getFilterHistory().getHistoryId();
			}
			boolExp = histId + boolExp.substring(boolExp.indexOf(" "), boolExp.length());
			wdkUser.updateHistory(step.getFilterHistory(), boolExp);
			histId = -1;
		    }
		}
		boolExp = protocol.getStep(moveToIx).getFilterHistory().getBooleanExpression();
		if (moveFromIx == 0) {
		    // need previous step history id + op + first step history id
		    boolExp = boolExp.substring(0, boolExp.indexOf(" ")) + op + " " + moveStep.getFilterHistory().getHistoryId();
		}
		else {
		    // need previous step history id + (op, subquery history id) substring from movestep
		    String moveBoolExp = moveStep.getFilterHistory().getBooleanExpression();
		    boolExp = boolExp.substring(0, boolExp.indexOf(" ")) + moveBoolExp.substring(moveBoolExp.indexOf(" ") + 1, moveBoolExp.length());
		}
		wdkUser.updateHistory(protocol.getStep(moveToIx).getFilterHistory(), boolExp);
	    }
	    else {
		for (int i = moveFromIx; i > moveToIx; --i) {
		    StepBean step = protocol.getStep(i);
		    if (step.getPreviousStep().getIsFirstStep()) {
			//need to build boolExp: moveStep subquery history id  + op + first step filter history id
			boolExp = moveStep.getSubQueryHistory().getHistoryId() + " " + op + " " + step.getPreviousStep().getFilterHistory().getHistoryId();
			wdkUser.updateHistory(step.getFilterHistory(), boolExp);
		    }
		    else {
			boolExp = step.getPreviousStep().getFilterHistory().getBooleanExpression();
			histId = step.getPreviousStep().getFilterHistory().getHistoryId();
			boolExp = histId + boolExp.substring(boolExp.indexOf(" "), boolExp.length());
			wdkUser.updateHistory(step.getFilterHistory(), boolExp);
		    }
		}
	    }
	}
	
	// Forward to ShowSummaryAction
	ActionForward showSummary = mapping.findForward( CConstants.SHOW_SUMMARY_MAPKEY );
	StringBuffer url = new StringBuffer( showSummary.getPath() );
	url.append("?protocol=" + URLEncoder.encode(strProtoId));
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
