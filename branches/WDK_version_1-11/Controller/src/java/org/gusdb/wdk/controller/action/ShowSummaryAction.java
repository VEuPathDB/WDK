package org.gusdb.wdk.controller.action;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.ApplicationInitListener;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerBean;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.RecordBean;
import org.gusdb.wdk.model.jspwrap.UserAnswerBean;
import org.gusdb.wdk.model.jspwrap.UserBean;

/**
 * This Action is called by the ActionServlet when a WDK question is asked.
 * It 1) reads param values from input form bean,
 *    2) runs the query and saves the answer
 *    3) forwards control to a jsp page that displays a summary
 */

public class ShowSummaryAction extends Action {
    public ActionForward execute(ActionMapping mapping,
				 ActionForm form,
				 HttpServletRequest request,
				 HttpServletResponse response) throws Exception {
	UserBean wdkUser = null;
	AnswerBean wdkAnswer = null;
    
	String ua_id_str = request.getParameter(CConstants.USER_ANSWER_ID);
	if (ua_id_str == null) {
	    ua_id_str = (String)request.getAttribute(CConstants.USER_ANSWER_ID);
	}
	if (ua_id_str != null) {
	    int ua_id = Integer.parseInt(ua_id_str);
 	    wdkUser = (UserBean)request.getSession().getAttribute(CConstants.WDK_USER_KEY);
	    UserAnswerBean userAnswer = wdkUser.getUserAnswerByID(ua_id);
	    wdkAnswer = userAnswer.getAnswer();
	    wdkAnswer = summaryPaging(request, null, null, wdkAnswer);
	    if (userAnswer.isCombinedAnswer()) {
		wdkAnswer.setIsCombinedAnswer(true);
		wdkAnswer.setUserAnswerName(userAnswer.getName());
	    }
	} else {
	    QuestionForm qForm = (QuestionForm)request.getSession().getAttribute(CConstants.QUESTIONFORM_KEY);
	    //System.err.println("DEBUG: qForm retrieved from session " + request.getSession() + " by SSA: " + qForm);

	    //TRICKY: this is for action forward from ProcessQuestionSetsFlatAction
	    qForm.cleanup();

	    //why I am not able to get back my question from the session? use the form for  now
	    QuestionBean wdkQuestion = (QuestionBean)request.getSession().getAttribute(CConstants.WDK_QUESTION_KEY);
	    if(wdkQuestion == null) { wdkQuestion = qForm.getQuestion(); }
            if(wdkQuestion == null) {
  	        throw new RuntimeException("Unexpected error: wdkAnswer is null");
	    }

	    Map params = handleMultiPickParams(new LinkedHashMap(qForm.getMyProps()));
	    wdkAnswer = summaryPaging(request, wdkQuestion, params);

	    request.getSession().setAttribute(CConstants.WDK_QUESTION_PARAMS_KEY, params);

	    //add AnswerBean to User for query history
 	    wdkUser = (UserBean)request.getSession().getAttribute(CConstants.WDK_USER_KEY);
	    if (wdkUser == null) { 
		ShowQuestionSetsAction.sessionStart(request, getServlet());
		wdkUser = (UserBean)request.getSession().getAttribute(CConstants.WDK_USER_KEY);
	    }
	    wdkUser.addAnswerFuzzy(wdkAnswer);

	    ua_id_str = new Integer(wdkUser.getUserAnswerIdByAnswer(wdkAnswer)).toString();
	}

	//clear boolean root from session to prevent interference
	request.getSession().setAttribute(CConstants.CURRENT_BOOLEAN_ROOT_KEY, null);

	request.getSession().setAttribute(CConstants.WDK_ANSWER_KEY, wdkAnswer);

	request.setAttribute(CConstants.USER_ANSWER_ID_KEY, ua_id_str);

        boolean alwaysGoToSummary = false;
        if (CConstants.YES.equalsIgnoreCase((String)getServlet().getServletContext().getAttribute(CConstants.WDK_ALWAYSGOTOSUMMARY_KEY))
            || CConstants.YES.equalsIgnoreCase(request.getParameter(CConstants.ALWAYS_GOTO_SUMMARY_PARAM))) {
            alwaysGoToSummary = true;
        }
        if (CConstants.NO.equalsIgnoreCase(request.getParameter(CConstants.ALWAYS_GOTO_SUMMARY_PARAM))) {
            alwaysGoToSummary = false;
        }
	ActionForward forward = getForward(wdkAnswer, mapping, ua_id_str, alwaysGoToSummary);
	//System.out.println("SSA: control going to " + forward.getPath());
	return forward;
    }

    private ActionForward getForward (AnswerBean wdkAnswer, ActionMapping mapping,
                                      String userAnswerIdStr, boolean alwaysGoToSummary) {
	ServletContext svltCtx = getServlet().getServletContext();
	String customViewDir = (String)svltCtx.getAttribute(CConstants.WDK_CUSTOMVIEWDIR_KEY);
	String customViewFile1 = customViewDir + File.separator
	    + wdkAnswer.getQuestion().getFullName() + ".summary.jsp";
	String customViewFile2 = customViewDir + File.separator
	    + wdkAnswer.getRecordClass().getFullName() + ".summary.jsp";
	String customViewFile3 = customViewDir + File.separator
	    + CConstants.WDK_CUSTOM_SUMMARY_PAGE;
	ActionForward forward = null;

	if(wdkAnswer.getResultSize() == 1 && !wdkAnswer.getIsDynamic() && !alwaysGoToSummary) {
	    RecordBean rec = (RecordBean)wdkAnswer.getRecords().next();
	    forward = mapping.findForward(CConstants.SKIPTO_RECORD_MAPKEY);
	    String path = forward.getPath() + "?name=" + rec.getRecordClass().getFullName()
		+ "&primary_key=" + rec.getPrimaryKey().getRecordId();
	    if (rec.getPrimaryKey().getProjectId() != null) {
		path += "&project_id=" + rec.getPrimaryKey().getProjectId();
	    }
	    return new ActionForward(path);
	}

	if (ApplicationInitListener.resourceExists(customViewFile1, svltCtx)) {
	    forward = new ActionForward(customViewFile1);
	} else if (ApplicationInitListener.resourceExists(customViewFile2, svltCtx)) {
	    forward = new ActionForward(customViewFile2);
	} else if (ApplicationInitListener.resourceExists(customViewFile3, svltCtx)) {
	    forward = new ActionForward(customViewFile3);
	} else {
	    forward = mapping.findForward(CConstants.SHOW_SUMMARY_MAPKEY);
	}

	if (userAnswerIdStr == null) { return forward; }

	String path = forward.getPath();
	if(path.indexOf("?") > 0 ) {
	    if(path.indexOf(CConstants.USER_ANSWER_ID) < 0) {
		path += "&" + CConstants.USER_ANSWER_ID + "=" + userAnswerIdStr; 
	    }
	} else {
	    path += "?" + CConstants.USER_ANSWER_ID + "=" + userAnswerIdStr; 
	}
	return new ActionForward(path);
    }

    protected Map handleMultiPickParams (Map params) 
    {
	java.util.Iterator newParamNames = params.keySet().iterator();
	while (newParamNames.hasNext()) {
	    String paramName = (String)newParamNames.next();
	    Object paramVal = params.get(paramName);
	    String paramValStr = null;
	    if (paramVal instanceof String[]) {
		String[] pVals = (String[])paramVal;
		paramValStr = pVals[0];
		for (int i=1; i<pVals.length; i++) { paramValStr += "," + pVals[i]; }
		params.put(paramName, paramValStr);
	    } else {
		paramValStr = (paramVal == null ? null : paramVal.toString());
	    }
	    //System.err.println("*** debug params: (k, v) = " + paramName + ", " + paramValStr);
	}
	return params;
    }
    
    protected AnswerBean booleanAnswerPaging(HttpServletRequest request, Object answerMaker)
	throws WdkModelException, WdkUserException
    {
	return summaryPaging(request, answerMaker, null, null);
    }

    protected AnswerBean summaryPaging (HttpServletRequest request, Object answerMaker, Map params)
	throws WdkModelException, WdkUserException
    {
	return summaryPaging (request, answerMaker, params, null);
    }

    private AnswerBean summaryPaging (HttpServletRequest request, Object answerMaker, Map params, AnswerBean wdkAnswer)
	throws WdkModelException, WdkUserException
    {
	int start = 1;
	if (request.getParameter("pager.offset") != null) {
	    start = Integer.parseInt(request.getParameter("pager.offset"));
	    start++;
	}
	int pageSize = 20;
	if (request.getParameter("pageSize") != null) {
	    pageSize = Integer.parseInt(request.getParameter("pageSize"));
	}

	if (start <1) { start = 1; } 
	int end = start + pageSize-1;	

	if (wdkAnswer != null) {
	    answerMaker = wdkAnswer.getQuestion();
	    params = wdkAnswer.getInternalParams();
	}

	if (answerMaker instanceof org.gusdb.wdk.model.jspwrap.QuestionBean) {
	    wdkAnswer = ((QuestionBean)answerMaker).makeAnswer(params, start, end);
	} else if (answerMaker instanceof org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean) {
	    wdkAnswer = ((BooleanQuestionNodeBean)answerMaker).makeAnswer(start, end);
	} else {
	    throw new RuntimeException("unexpected answerMaker: " + answerMaker);
	}

	int totalSize = wdkAnswer.getResultSize();

	if (end > totalSize) { end = totalSize; }
	
	List editedParamNames = new ArrayList();
	for (Enumeration en = request.getParameterNames(); en.hasMoreElements();) {
	    String key = (String) en.nextElement();
	    if (!"pageSize".equals(key) && !"start".equals(key) &&!"pager.offset".equals(key)) {
		editedParamNames.add(key);
	    }
	}

	String uriString = request.getRequestURI();
	request.setAttribute("wdk_paging_total", new Integer(totalSize));
	request.setAttribute("wdk_paging_pageSize", new Integer(pageSize));
	request.setAttribute("wdk_paging_start", new Integer(start));
	request.setAttribute("wdk_paging_end", new Integer(end));
	request.setAttribute("wdk_paging_url", uriString);
	request.setAttribute("wdk_paging_params", editedParamNames);
	return wdkAnswer;
    }
}
