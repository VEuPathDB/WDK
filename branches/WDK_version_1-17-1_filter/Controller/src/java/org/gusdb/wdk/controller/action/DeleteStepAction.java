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

public class DeleteStepAction extends Action {
    private static final Logger logger = Logger.getLogger(DeleteStepAction.class);
    
    public ActionForward execute( ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response )
            throws Exception {
        System.out.println("Entering DeleteStepAction...");


	// Make sure a protocol is specified
	String strProtoId = request.getParameter("protocol");

	System.out.println("Filter protocol: " + strProtoId);
	if (strProtoId == null || strProtoId.length() == 0) {
	    throw new WdkModelException("No protocol was specified for deleting a step!");
	}

	// load model, user
	WdkModelBean wdkModel = ( WdkModelBean ) servlet.getServletContext().getAttribute(CConstants.WDK_MODEL_KEY );
        UserBean wdkUser = ( UserBean ) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY );
        if ( wdkUser == null ) {
            wdkUser = wdkModel.getUserFactory().getGuestUser();
            request.getSession().setAttribute( CConstants.WDK_USER_KEY, wdkUser );
        }

	HistoryBean history, filterHist;
        AnswerBean wdkAnswer;
	ProtocolBean protocol;
	StepBean step;
	String boolExp;

	// Are we revising or deleting a step?
	String deleteStep = request.getParameter("delete");

	if (deleteStep == null || deleteStep.length() == 0) {
	    throw new WdkModelException("No step was specified to delete!");
	}

	protocol = ProtocolBean.getProtocol(strProtoId, wdkUser);
	step = protocol.getStep(Integer.valueOf(deleteStep));
	// are we deleting the first step?
	if (step.getIsFirstStep()) {
	    // if there are two steps, we're just moving to the second step as a one-step protocol
	    if (protocol.getLength() == 2) {
		// will need to change when we have unique ids for protocols
		strProtoId = step.getNextStep().getSubQueryHistory().getHistoryId() + "";
	    }
	    // if there are more than two steps, we need to update the filter history of the third step
	    // so that the boolean expression points to the subquery history of the second step
	    else if (protocol.getLength() > 2) {
		step = step.getNextStep().getNextStep();
		filterHist = step.getFilterHistory();
		boolExp = filterHist.getBooleanExpression();
		boolExp = step.getPreviousStep().getSubQueryHistory().getHistoryId() + boolExp.substring(boolExp.indexOf(" "), boolExp.length());
		wdkUser.updateHistory(filterHist, boolExp);
	    }
	    // not sure what to do here, but something has to happen...
	    else {
		// eventually we'll support deleting protocols...?
		// for now, throw error
		//throw new WdkUserException("Can't delete the only step in a one-step search strategy!");
		ActionForward forward = new ActionForward("");
		forward.setRedirect( true );
		return forward;
	    }
	}
	else {
	    //if this is not the last step, then filter history of the next step needs
	    // to point to filter history of the previous step
	    if (Integer.valueOf(deleteStep) < protocol.getLength() - 1) {
		filterHist = step.getNextStep().getFilterHistory();
		boolExp = filterHist.getBooleanExpression();
		boolExp = step.getPreviousStep().getFilterHistory().getHistoryId() + " " + boolExp.substring(boolExp.indexOf(" "), boolExp.length());
		wdkUser.updateHistory(filterHist, boolExp);
	    }
	    //if this is the last step, we're just moving to the protocol that ends w/ the previous step
	    else {
		strProtoId = step.getPreviousStep().getFilterHistory().getHistoryId() + "";
	    }
	}
	
	// 5. forward to showsummary
	ActionForward showSummary = mapping.findForward( CConstants.SHOW_SUMMARY_MAPKEY );
	StringBuffer url = new StringBuffer( showSummary.getPath() );
	url.append("?protocol=" + URLEncoder.encode(strProtoId));
	String viewStep = request.getParameter("step");
	if (viewStep != null && viewStep.length() != 0) {
	    if (Integer.valueOf(viewStep) > Integer.valueOf(deleteStep)) {
		viewStep = (Integer.valueOf(viewStep) - 1) + "";
		url.append("&step=" + URLEncoder.encode(viewStep));
	    }
	    else if (Integer.valueOf(viewStep) < Integer.valueOf(deleteStep)) {
		url.append("&step=" + URLEncoder.encode(viewStep));
	    }
	}
	String subQuery = request.getParameter("subquery");
	if (subQuery != null && subQuery.length() != 0) {
	    url.append("&subquery=" + URLEncoder.encode(subQuery));
	}
	System.out.println(url.toString());
	ActionForward forward = new ActionForward( url.toString() );
	forward.setRedirect( true );
	return forward;
    }
}
