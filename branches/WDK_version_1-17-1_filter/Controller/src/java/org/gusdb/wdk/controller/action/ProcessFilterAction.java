package org.gusdb.wdk.controller.action;

import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.upload.FormFile;
import org.gusdb.wdk.controller.ApplicationInitListener;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.RecordPageBean;
import org.gusdb.wdk.model.jspwrap.BooleanQuestionNodeBean;
import org.gusdb.wdk.model.jspwrap.DatasetParamBean;
import org.gusdb.wdk.model.jspwrap.DatasetBean;
import org.gusdb.wdk.model.jspwrap.UserAnswerBean;
import org.gusdb.wdk.model.jspwrap.UserStrategyBean;
import org.gusdb.wdk.model.jspwrap.StepBean;
import org.gusdb.wdk.model.jspwrap.HistoryParamBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.EnumParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.RecordBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * This Action is called by the ActionServlet when a WDK filter is requested.  It
 * 1) reads param values from input form bean, 2) runs the filter query 3) completes
 * the partial boolean expression that was passed in 4) forwards control to
 * ProcessBooleanExpressionAction
 */

public class ProcessFilterAction extends ProcessQuestionAction {
    private static final Logger logger = Logger.getLogger(ProcessFilterAction.class);
    
    public ActionForward execute( ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response )
            throws Exception {
        System.out.println("Entering ProcessFilterAction...");


	// Make sure a strategy is specified
	String strProtoId = request.getParameter("strategy");

	System.out.println("Filter strategy: " + strProtoId);
	if (strProtoId == null || strProtoId.length() == 0) {
	    throw new WdkModelException("No strategy was specified for filtering!");
	}

	// load model, user
	WdkModelBean wdkModel = ( WdkModelBean ) servlet.getServletContext().getAttribute(CConstants.WDK_MODEL_KEY );
        UserBean wdkUser = ( UserBean ) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY );
        if ( wdkUser == null ) {
            wdkUser = wdkModel.getUserFactory().getGuestUser();
            request.getSession().setAttribute( CConstants.WDK_USER_KEY, wdkUser );
        }


	UserAnswerBean userAnswer, filterHist;
        RecordPageBean wdkRecordPage;
	UserStrategyBean strategy;
	StepBean step;
	String boolExp;


        // get question
        String qFullName = request.getParameter( CConstants.QUESTION_FULLNAME_PARAM );
        QuestionBean wdkQuestion = getQuestionByFullName( qFullName );
        //FilterForm fForm = prepareFilterForm( wdkQuestion, request,
	//      ( FilterForm ) form );
        QuestionForm fForm = prepareQuestionForm( wdkQuestion, request,
                ( QuestionForm ) form );
        
	// validate & parse params
        Map< String, String > params = prepareParams( wdkUser, request, fForm );

	Map<String, Object> internalParams = fForm.getMyProps();

	// Having booleanExpression present causes question to break (due to unrecognized
	// parameter).  Remove booleanExpression from params, and set using inherited method
	// from BooleanExpressionForm.
	boolExp = internalParams.remove("booleanExpression").toString();

	// Get question name
	String questionName = wdkQuestion.getFullName();
	
	internalParams = handleMultiPickParams(new LinkedHashMap<String, Object>(fForm.getMyProps()));
	handleDatasetParams(wdkUser, wdkQuestion, internalParams);
	
	// How much if this is needed to answer the question?  What is only needed for summary?

	// get sorting key, if have
	String sortingChecksum = request.getParameter(CConstants.WDK_SORTING_KEY);
	Map<String, Boolean> sortingAttributes;
	if (sortingChecksum != null) {
	    sortingAttributes = wdkUser.getSortingAttributesByChecksum(sortingChecksum);
	    request.setAttribute("wdk_sorting_checksum", sortingChecksum);
	} else {
	    sortingAttributes = wdkUser.getSortingAttributes(questionName);
	}
	
	// get summary key, if have
	String summaryChecksum = request.getParameter(CConstants.WDK_SUMMARY_KEY);
	String[] summaryAttributes = null;
	if (summaryChecksum != null) {
	    summaryAttributes = wdkUser.getSummaryAttributesByChecksum(summaryChecksum);
	    request.setAttribute("wdk_summary_checksum", sortingChecksum);
	} else {
	    summaryAttributes = wdkUser.getSummaryAttributes(questionName);
	}
	
	try {
	    wdkRecordPage = summaryPaging(request, wdkQuestion, internalParams,
				      sortingAttributes, summaryAttributes);
	} catch (WdkModelException ex) {
	    logger.error(ex);
	    ex.printStackTrace();
	    return showError(wdkModel, wdkUser, mapping, request, response);
	} catch (WdkUserException ex) {
	    logger.error(ex);
	    ex.printStackTrace();
	    return showError(wdkModel, wdkUser, mapping, request, response);
	}

	// get strategy
	strategy = wdkUser.getUserStrategy(Integer.parseInt(strProtoId));
	
	// create userAnswer for filter subquery
	userAnswer = wdkUser.createUserAnswer(wdkRecordPage);
        int userAnswerId = userAnswer.getUserAnswerId();

	StepBean childStep = new StepBean(userAnswer);

	// Are we revising or inserting a step?
	String reviseStep = request.getParameter("revise");
	String insertStep = request.getParameter("insert");
	String op = boolExp;
	if (op.indexOf(" ") >= 0) {
	    op = boolExp.substring(boolExp.indexOf(" "), boolExp.length());
	}
	System.out.println("Op: " + op);
	
	if ((reviseStep == null || reviseStep.length() == 0) &&
	    (insertStep == null || insertStep.length() == 0)) {
	    step = strategy.getLatestStep();
	    boolExp = step.getFilterUserAnswer().getUserAnswerId() + " " + op + " " + userAnswerId;
	    System.out.println("Boolean expression for add: " + boolExp);
	    
	    // now create userAnswer for operation query
	    userAnswer = wdkUser.combineUserAnswer(boolExp);
	    userAnswerId = userAnswer.getUserAnswerId();
	    
	    step = new StepBean(userAnswer);
	    step.setChildStep(childStep);
	    
	    // no insert specified,  so add step at end.
	    strategy.addStep(step);
	}
	else {
	    int stratLen = strategy.getLength();
	    int targetIx;

	    // NOTE:  this is still working off of a simple strategy concept...what about substrategies?
	    // if we are revising/inserting, we need to get subsequent steps and update them as well.
	    if (reviseStep != null && reviseStep.length() != 0) {
		targetIx = Integer.parseInt(reviseStep);
		
		step = strategy.getStep(targetIx);
		if (step.getIsFirstStep()) {
		    // build boolExp for switching to new first query
		    boolExp = step.getNextStep().getFilterUserAnswer().getBooleanExpression();
		    boolExp = userAnswerId + boolExp.substring(boolExp.indexOf(" "), boolExp.length());
		    targetIx++;
		}
		else {
		    // build standard boolExp for non-first step
		    boolExp = step.getPreviousStep().getFilterUserAnswer().getUserAnswerId() + op + " " + userAnswerId;
		}
		targetIx++;
	    }
	    else {
		targetIx = Integer.parseInt(insertStep);

		step = strategy.getStep(targetIx);
		if (step.getIsFirstStep()) {
		    boolExp = userAnswerId +  " " + op + " " + step.getFilterUserAnswer().getUserAnswerId();
		    targetIx++;
		}
		else {
		    // the inserted step has to point to the step at insertIx - 1
		    step = step.getPreviousStep();
		    boolExp = step.getFilterUserAnswer().getUserAnswerId() + " " + op + " " + userAnswerId;
		}
	    }

	    System.out.println("Boolean expression for revise/insert: " + boolExp);
	    // now create userAnswer for operation query
	    userAnswer = wdkUser.combineUserAnswer(boolExp);
	    userAnswerId = userAnswer.getUserAnswerId();
	    
	    step = new StepBean(userAnswer);
	    step.setChildStep(childStep);
	    
	    System.out.println("Updating subsequent steps.");
	    for (int i = targetIx; i < stratLen; ++i) {
		System.out.println("Updating step " + i);
		step = strategy.getStep(i);
		boolExp = step.getFilterUserAnswer().getBooleanExpression();
		boolExp = userAnswerId + boolExp.substring(boolExp.indexOf(" "), boolExp.length());
		userAnswer = wdkUser.combineUserAnswer(boolExp);
		userAnswerId = userAnswer.getUserAnswerId();
		step.setFilterUserAnswer(userAnswer);
	    }
	    // set latest step
	    strategy.setLatestStep(step);
	}

	// in either case, update and forward to show strategy
	strategy.update(false);

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

    private ActionForward showError(WdkModelBean wdkModel, UserBean wdkUser,
            ActionMapping mapping, HttpServletRequest request,
            HttpServletResponse response) throws WdkModelException,
            WdkUserException {
        // TEST
        logger.info("Show the details of an invalid userAnswer/question");

        String qFullName = request.getParameter(CConstants.QUESTION_FULLNAME_PARAM);
        Map<String, Object> params;
        Map<String, String> paramNames;
        String customName;
        if (qFullName == null || qFullName.length() == 0) {
            String strHistId = request.getParameter(CConstants.WDK_HISTORY_ID_KEY);
            int userAnswerId = Integer.parseInt(strHistId);
            UserAnswerBean userAnswer = wdkUser.getUserAnswer(userAnswerId);
            params = userAnswer.getParams();
            paramNames = userAnswer.getParamNames();
            qFullName = userAnswer.getQuestionName();
            customName = userAnswer.getCustomName();
        } else {
            params = new LinkedHashMap<String, Object>();
            paramNames = new LinkedHashMap<String, String>();
            customName = qFullName;

            // get params from request
            Map parameters = request.getParameterMap();
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
                                if (sb.length() > 0)
                                    sb.append(", ");
                                sb.append(v);
                            }
                            pValue = sb.toString();
                        }
                        pValue = URLDecoder.decode(pValue, "utf-8");
                    }
                    if (pName.startsWith("myProp(")) {
                        pName = pName.substring(7, pName.length() - 1).trim();
                        params.put(pName, pValue);

                        String displayName = wdkModel.getParamDisplayName(pName);
                        if (displayName == null)
                            displayName = pName;
                        paramNames.put(pName, displayName);
                    }
                } catch (UnsupportedEncodingException ex) {
                    throw new WdkModelException(ex);
                }
            }
        }
        String qDisplayName = wdkModel.getQuestionDisplayName(qFullName);
        if (qDisplayName == null)
            qDisplayName = qFullName;

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

    protected RecordPageBean summaryPaging(HttpServletRequest request,
            Object answerMaker, Map<String, Object> params,
            Map<String, Boolean> sortingAttributes, String[] summaryAttributes)
            throws WdkModelException, WdkUserException {
        return summaryPaging(request, answerMaker, params, sortingAttributes,
                summaryAttributes, null);
    }

    private RecordPageBean summaryPaging(HttpServletRequest request,
            Object answerMaker, Map<String, Object> params,
            Map<String, Boolean> sortingAttributes, String[] summaryAttributes,
            RecordPageBean wdkRecordPage) throws WdkModelException, WdkUserException {
        UserBean wdkUser = (UserBean) request.getSession().getAttribute(
                CConstants.WDK_USER_KEY);
        int start = 1;
        if (request.getParameter("pager.offset") != null) {
            start = Integer.parseInt(request.getParameter("pager.offset"));
            start++;
        }
        int pageSize = wdkUser.getItemsPerPage();
        String pageSizeKey = request.getParameter(CConstants.WDK_PAGE_SIZE_KEY);
        if (pageSizeKey != null) {
            pageSize = Integer.parseInt(pageSizeKey);
            wdkUser.setItemsPerPage(pageSize);
        } else {
            String altPageSizeKey = request.getParameter(CConstants.WDK_ALT_PAGE_SIZE_KEY);
            if (altPageSizeKey != null)
                pageSize = Integer.parseInt(altPageSizeKey);
        }

        if (wdkRecordPage != null) {
            answerMaker = wdkRecordPage.getQuestion();
            params = wdkRecordPage.getInternalParams();
        }
        if (start < 1) {
            start = 1;
        }
        int end = start + pageSize - 1;

        logger.info("Make answer with start=" + start + ", end=" + end);

        if (answerMaker instanceof QuestionBean) {
            QuestionBean question = (QuestionBean) answerMaker;
            // check if the question is supposed to make answers containing all
            // records in one page
            if (question.isFullRecordPage()) {
                wdkRecordPage = question.makeRecordPage(params, sortingAttributes);
            } else {
                wdkRecordPage = question.makeRecordPage(params, start, end,
                        sortingAttributes);
            }
            wdkRecordPage.setSumaryAttribute(summaryAttributes);
        } else if (answerMaker instanceof BooleanQuestionNodeBean) {
            wdkRecordPage = ((BooleanQuestionNodeBean) answerMaker).makeRecordPage(
                    start, end);
        } else {
            throw new RuntimeException("unexpected answerMaker: " + answerMaker);
        }

        int totalSize = wdkRecordPage.getResultSize();

        if (end > totalSize) {
            end = totalSize;
        }

        List<String> editedParamNames = new ArrayList<String>();
        for (Enumeration en = request.getParameterNames(); en.hasMoreElements();) {
            String key = (String) en.nextElement();
            if (!key.equals(CConstants.WDK_PAGE_SIZE_KEY)
                    && !key.equals(CConstants.WDK_ALT_PAGE_SIZE_KEY)
                    && !"start".equals(key) && !"pager.offset".equals(key)) {
                editedParamNames.add(key);
            }
        }
        request.setAttribute("wdk_paging_total", new Integer(totalSize));
        request.setAttribute("wdk_paging_pageSize", new Integer(pageSize));
        request.setAttribute("wdk_paging_start", new Integer(start));
        request.setAttribute("wdk_paging_end", new Integer(end));
        request.setAttribute("wdk_paging_url", request.getRequestURI());
        request.setAttribute("wdk_paging_params", editedParamNames);
        return wdkRecordPage;
    }

    protected Map<String, Object> handleMultiPickParams(
            Map<String, Object> params) {
        for (String paramName : params.keySet()) {
            Object paramVal = params.get(paramName);
            String paramValStr = null;
            if (paramVal instanceof String[]) {
                String[] pVals = (String[]) paramVal;
                StringBuffer sb = new StringBuffer();
                for (String pVal : pVals) {
                    if (sb.length() > 0)
                        sb.append(",");
                    sb.append(pVal);
                }
                paramValStr = sb.toString();
            } else {
                paramValStr = (paramVal == null ? null : paramVal.toString());
            }
            params.put(paramName, paramValStr);
            // System.err.println("*** debug params: (k, v) = " + paramName + ",
            // " + paramValStr);
        }
        return params;
    }

    protected void handleDatasetParams(UserBean user, QuestionBean question,
            Map<String, Object> params) {
        Map<String, ParamBean> paramDefinitions = question.getParamsMap();
        for (String paramName : paramDefinitions.keySet()) {
            ParamBean param = paramDefinitions.get(paramName);
            if (param != null && param instanceof DatasetParamBean) {
                String paramValue = user.getSignature() + ":"
                        + (String) params.get(paramName);
                params.put(paramName, paramValue);
            }
        }
    }
}
